// path: app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildHandoffOrchestrator.kt
package com.isardomains.sameview.ui.wackelbild

import com.isardomains.sameview.image.readExifOrientedDimensions
import com.isardomains.sameview.image.wackelbild.WackelbildDateOverlay
import com.isardomains.sameview.image.wackelbild.WackelbildPrintPair
import com.isardomains.sameview.image.wackelbild.WackelbildPrintResult
import com.isardomains.sameview.image.wackelbild.WackelbildPrintTarget
import com.isardomains.sameview.net.deinwackelbild.CreateHandoffRequest
import com.isardomains.sameview.net.deinwackelbild.CreateHandoffResponse
import com.isardomains.sameview.net.deinwackelbild.DeinWackelbildApiClient
import com.isardomains.sameview.net.deinwackelbild.DeinWackelbildApiError
import com.isardomains.sameview.net.deinwackelbild.DeinWackelbildErrorClassification
import com.isardomains.sameview.net.deinwackelbild.DeinWackelbildResult
import com.isardomains.sameview.net.deinwackelbild.DeinWackelbildSlot
import com.isardomains.sameview.net.deinwackelbild.UploadResponse
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val MAX_ATTEMPTS_PER_STAGE = 3
private val RETRY_DELAYS_MS = listOf(1000L, 2000L) // before attempt 2, before attempt 3
private const val MAX_HANDOFF_GENERATIONS = 3 // the original + at most 2 restarts

private val RETRYABLE_CLASSIFICATIONS = setOf(
    DeinWackelbildErrorClassification.RETRYABLE_NETWORK,
    DeinWackelbildErrorClassification.RETRYABLE_SERVER,
    DeinWackelbildErrorClassification.RATE_LIMITED
)

/** 403 and 410 both map to `EXPIRED_HANDOFF` (already collapsed by Block 7's classification); 409
 * maps to `INCOMPLETE_HANDOFF`. All three trigger a full handoff restart -- see
 * `DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md` for why 409 is treated the same as 403/410. */
private val RESTART_CLASSIFICATIONS = setOf(
    DeinWackelbildErrorClassification.EXPIRED_HANDOFF,
    DeinWackelbildErrorClassification.INCOMPLETE_HANDOFF
)

// Partner create-request configuration values (spec §27/§32). The `format` slug is never chosen
// here -- it comes from the already-selected WackelbildPrintTarget.
private const val ORIENTATION_PORTRAIT = "portrait"
private const val ORIENTATION_LANDSCAPE = "landscape"
private const val DIRECTION_HORIZONTAL = "horizontal"

/**
 * Composes Block 5 (rendering), Block 6 (temp-file lifecycle), and Block 7 (raw API client) into
 * one finite, deterministic, cancellation-safe DeinWackelbild handoff operation.
 *
 * Stateless across calls -- every dependency and all operation-local state (idempotency key,
 * handoff response data, retry/generation counters) live inside [execute]'s own local variables,
 * never as fields on this class. [execute] receives its dependencies as parameters rather than
 * capturing them at construction time, so a caller's dependency-injection test seam (see
 * [WackelbildViewModel]'s internal constructor) always resolves to whatever the caller currently
 * holds, not whatever existed when this orchestrator itself was constructed.
 *
 * [readImageDimensions] reads a rendered JPEG's pixel dimensions; production uses the existing
 * `readExifOrientedDimensions` helper, tests inject deterministic values.
 */
class WackelbildHandoffOrchestrator(
    private val idempotencyKeyFactory: () -> String = { UUID.randomUUID().toString() },
    private val readImageDimensions: (File) -> Pair<Int, Int>? = ::readExifOrientedDimensions
) {

    /**
     * Runs one complete handoff operation end-to-end. Returns only on a genuine terminal outcome
     * ([WackelbildOperationState.Ready] or [WackelbildOperationState.Failed]) -- cancellation is
     * never converted into a return value; it propagates as
     * [kotlinx.coroutines.CancellationException] through this function exactly like any other
     * suspension point, and the `finally` block below still runs cleanup regardless.
     *
     * @param awaitFallbackConfirmation Required, no default. Called only when the renderer used
     *   the lower-quality fallback source. Must not return until genuine explicit user consent has
     *   been obtained -- there is no implicit-approval path. If the caller cancels while this is
     *   suspended, the cancellation propagates normally and no network call is ever made.
     * @param printTarget The print target the preview already showed, or null for the full session
     *   frame. The exact same object is handed to [renderPrintPair] and drives the request's
     *   `format`; it is never re-selected from the rendered JPEG dimensions.
     */
    suspend fun execute(
        sessionDir: File,
        tempFileManager: WackelbildTempFileManager,
        apiClient: DeinWackelbildApiClient,
        dateOverlay: WackelbildDateOverlay?,
        renderPrintPair: suspend (
            sessionDir: File,
            outputDir: File,
            dateOverlay: WackelbildDateOverlay?,
            printTarget: WackelbildPrintTarget?
        ) -> WackelbildPrintResult,
        awaitFallbackConfirmation: suspend () -> Unit,
        onPhaseChange: (WackelbildOperationState) -> Unit,
        printTarget: WackelbildPrintTarget? = null
    ): WackelbildOperationState {
        val operationDir = tempFileManager.createOperationDir()

        suspend fun <T> runStageWithRetry(call: suspend () -> DeinWackelbildResult<T>): StageOutcome<T> {
            lateinit var lastFailure: DeinWackelbildApiError
            for (attempt in 1..MAX_ATTEMPTS_PER_STAGE) {
                if (attempt > 1) delay(RETRY_DELAYS_MS[attempt - 2])
                when (val result = call()) {
                    is DeinWackelbildResult.Success -> return StageOutcome.Success(result.value)
                    is DeinWackelbildResult.Failure -> {
                        lastFailure = result.error
                        val classification = result.error.classification
                        if (classification in RESTART_CLASSIFICATIONS) {
                            return StageOutcome.Restart(classification)
                        }
                        if (classification !in RETRYABLE_CLASSIFICATIONS) {
                            return StageOutcome.Terminal(mapToOperationFailure(classification))
                        }
                        // else: retryable classification, loop continues to the next attempt
                    }
                }
            }
            return StageOutcome.Terminal(mapToOperationFailure(lastFailure.classification))
        }

        suspend fun runOneGeneration(
            idempotencyKey: String,
            pair: WackelbildPrintPair,
            usedFallback: Boolean,
            createRequest: CreateHandoffRequest
        ): GenerationOutcome {
            onPhaseChange(WackelbildOperationState.CreatingHandoff)
            val createOutcome = runStageWithRetry { apiClient.createHandoff(createRequest, idempotencyKey) }
            val createResponse = when (createOutcome) {
                is StageOutcome.Restart -> return GenerationOutcome.RestartNeeded(createOutcome.classification)
                is StageOutcome.Terminal -> return GenerationOutcome.Done(WackelbildOperationState.Failed(createOutcome.failure))
                is StageOutcome.Success -> createOutcome.value
            }
            if (!isValidCreateResponse(createResponse)) {
                return GenerationOutcome.Done(WackelbildOperationState.Failed(handoffFailed()))
            }

            onPhaseChange(WackelbildOperationState.UploadingSlot(DeinWackelbildSlot.ONE))
            val uploadOneOutcome = runStageWithRetry {
                apiClient.uploadImage(createResponse.uploadUrl, createResponse.handoffToken, DeinWackelbildSlot.ONE, pair.referenceFile)
            }
            val uploadOneResponse = when (uploadOneOutcome) {
                is StageOutcome.Restart -> return GenerationOutcome.RestartNeeded(uploadOneOutcome.classification)
                is StageOutcome.Terminal -> return GenerationOutcome.Done(WackelbildOperationState.Failed(uploadOneOutcome.failure))
                is StageOutcome.Success -> uploadOneOutcome.value
            }
            if (!isValidUploadOneResponse(uploadOneResponse)) {
                return GenerationOutcome.Done(WackelbildOperationState.Failed(handoffFailed()))
            }

            onPhaseChange(WackelbildOperationState.UploadingSlot(DeinWackelbildSlot.TWO))
            val uploadTwoOutcome = runStageWithRetry {
                apiClient.uploadImage(createResponse.uploadUrl, createResponse.handoffToken, DeinWackelbildSlot.TWO, pair.captureFile)
            }
            val uploadTwoResponse = when (uploadTwoOutcome) {
                is StageOutcome.Restart -> return GenerationOutcome.RestartNeeded(uploadTwoOutcome.classification)
                is StageOutcome.Terminal -> return GenerationOutcome.Done(WackelbildOperationState.Failed(uploadTwoOutcome.failure))
                is StageOutcome.Success -> uploadTwoOutcome.value
            }
            if (!isValidUploadTwoResponse(uploadTwoResponse)) {
                return GenerationOutcome.Done(WackelbildOperationState.Failed(handoffFailed()))
            }

            return GenerationOutcome.Done(
                WackelbildOperationState.Ready(checkoutUrl = uploadTwoResponse.checkoutUrl!!, usedFallback = usedFallback)
            )
        }

        try {
            onPhaseChange(WackelbildOperationState.Preparing)
            val renderResult = renderPrintPair(sessionDir, operationDir, dateOverlay, printTarget)
            val (pair, usedFallback) = when (renderResult) {
                is WackelbildPrintResult.Success -> renderResult.pair to renderResult.usedFallback
                is WackelbildPrintResult.Failure -> return WackelbildOperationState.Failed(
                    WackelbildOperationFailure(WackelbildOperationFailureCategory.PREPARATION_FAILED)
                )
            }

            // Built once for the rendered pair and reused by every generation/restart below, since
            // restarts never re-render -- the pair (and therefore its geometry) never changes. Built
            // before the fallback dialog so a pair that does not honor the target the user saw fails
            // locally without first prompting for consent -- and before any network call.
            val createRequest = buildCreateHandoffRequest(readPairDimensions(pair), printTarget)
                ?: return WackelbildOperationState.Failed(
                    WackelbildOperationFailure(WackelbildOperationFailureCategory.PREPARATION_FAILED)
                )

            if (usedFallback) {
                onPhaseChange(WackelbildOperationState.AwaitingFallbackConfirmation)
                awaitFallbackConfirmation() // suspends; zero network calls happen before this returns
            }

            var generation = 1
            while (true) {
                val idempotencyKey = idempotencyKeyFactory()
                when (val outcome = runOneGeneration(idempotencyKey, pair, usedFallback, createRequest)) {
                    is GenerationOutcome.Done -> {
                        if (outcome.state is WackelbildOperationState.Ready) {
                            // Cleanup happens immediately once both uploads are confirmed accepted,
                            // before the Ready state is ever exposed -- never held through checkout.
                            tempFileManager.deleteOperationDir(operationDir)
                        }
                        return outcome.state
                    }
                    is GenerationOutcome.RestartNeeded -> {
                        if (generation >= MAX_HANDOFF_GENERATIONS) {
                            return WackelbildOperationState.Failed(mapToOperationFailure(outcome.classification))
                        }
                        generation += 1
                        // loop again: new idempotency key, same operationDir/pair, no re-render
                    }
                }
            }
        } finally {
            // Runs on every path: success (already deleted above, idempotent), terminal failure,
            // or cancellation at any suspension point (render, fallback wait, retry delay, or an
            // in-flight API call). NonCancellable ensures this always completes even though the
            // surrounding coroutine may already be cancelled.
            withContext(NonCancellable) {
                tempFileManager.deleteOperationDir(operationDir)
            }
        }
    }

    /** The rendered pair's pixel dimensions, or `null` when either file is unreadable or the two
     * differ -- in which case no geometry-derived configuration is trusted or sent. The read is a
     * blocking file operation, hence [Dispatchers.IO]. */
    private suspend fun readPairDimensions(pair: WackelbildPrintPair): Pair<Int, Int>? =
        withContext(Dispatchers.IO) {
            val reference = readImageDimensions(pair.referenceFile) ?: return@withContext null
            val capture = readImageDimensions(pair.captureFile) ?: return@withContext null
            if (reference == capture) reference else null
        }
}

/**
 * Builds the create-handoff request for a rendered pair of [dimensions] (`null` = unreadable or
 * mismatched). `direction` is always `horizontal`. `orientation` follows the rendered pixel
 * dimensions (omitted for a square or unknown geometry). `format` is [target]'s slug -- never
 * re-derived from the dimensions -- and is omitted when there is no target.
 *
 * With a [target], the rendered dimensions must be readable and match the target's aspect
 * ([WackelbildPrintTarget.matchesOutput]); otherwise this returns `null`, because a pair that does
 * not honor the crop the user was shown must not be sent.
 */
internal fun buildCreateHandoffRequest(
    dimensions: Pair<Int, Int>?,
    target: WackelbildPrintTarget?
): CreateHandoffRequest? {
    val trusted = dimensions?.takeIf { it.first > 0 && it.second > 0 }
    if (target != null && (trusted == null || !target.matchesOutput(trusted.first, trusted.second))) return null
    val (width, height) = trusted ?: return CreateHandoffRequest(direction = DIRECTION_HORIZONTAL)
    return CreateHandoffRequest(
        format = target?.slug,
        orientation = when {
            width < height -> ORIENTATION_PORTRAIT
            width > height -> ORIENTATION_LANDSCAPE
            else -> null
        },
        direction = DIRECTION_HORIZONTAL
    )
}

private fun isValidCreateResponse(response: CreateHandoffResponse): Boolean =
    response.status == "awaiting_files"
// handoffToken/uploadUrl non-blank and uploadUrl HTTPS-valid are already parser-guaranteed (Block 7).

private fun isValidUploadOneResponse(response: UploadResponse): Boolean =
    response.status == "awaiting_files" &&
        response.uploadedSlots.contains(DeinWackelbildSlot.ONE.wireValue) &&
        response.checkoutUrl == null

private fun isValidUploadTwoResponse(response: UploadResponse): Boolean =
    response.status == "ready" &&
        response.uploadedSlots.contains(DeinWackelbildSlot.ONE.wireValue) &&
        response.uploadedSlots.contains(DeinWackelbildSlot.TWO.wireValue) &&
        response.checkoutUrl != null

private fun handoffFailed(): WackelbildOperationFailure =
    WackelbildOperationFailure(WackelbildOperationFailureCategory.HANDOFF_FAILED)

/** Maps one raw classification to the operation-level failure category. Only reached for
 * non-retryable, non-restart classifications, or for a restart classification once the handoff
 * generation budget is exhausted (in which case `EXPIRED_HANDOFF`/`INCOMPLETE_HANDOFF` correctly
 * resolve to `HANDOFF_FAILED` below). */
private fun mapToOperationFailure(classification: DeinWackelbildErrorClassification): WackelbildOperationFailure {
    val category = when (classification) {
        DeinWackelbildErrorClassification.RETRYABLE_NETWORK -> WackelbildOperationFailureCategory.NETWORK_UNAVAILABLE
        DeinWackelbildErrorClassification.RETRYABLE_SERVER,
        DeinWackelbildErrorClassification.RATE_LIMITED -> WackelbildOperationFailureCategory.SERVER_TEMPORARY
        DeinWackelbildErrorClassification.INTEGRATION_UNAVAILABLE -> WackelbildOperationFailureCategory.INTEGRATION_UNAVAILABLE
        DeinWackelbildErrorClassification.FILE_TOO_LARGE,
        DeinWackelbildErrorClassification.INVALID_IMAGE,
        DeinWackelbildErrorClassification.DIMENSION_MISMATCH,
        DeinWackelbildErrorClassification.PERMANENT_LOCAL -> WackelbildOperationFailureCategory.INVALID_LOCAL_OUTPUT
        DeinWackelbildErrorClassification.INVALID_REQUEST,
        DeinWackelbildErrorClassification.EXPIRED_HANDOFF,
        DeinWackelbildErrorClassification.INCOMPLETE_HANDOFF,
        DeinWackelbildErrorClassification.MALFORMED_RESPONSE,
        DeinWackelbildErrorClassification.UNEXPECTED_HTTP_STATUS -> WackelbildOperationFailureCategory.HANDOFF_FAILED
    }
    return WackelbildOperationFailure(category, classification)
}

private sealed class StageOutcome<out T> {
    data class Success<T>(val value: T) : StageOutcome<T>()
    data class Restart(val classification: DeinWackelbildErrorClassification) : StageOutcome<Nothing>()
    data class Terminal(val failure: WackelbildOperationFailure) : StageOutcome<Nothing>()
}

private sealed class GenerationOutcome {
    data class Done(val state: WackelbildOperationState) : GenerationOutcome()
    data class RestartNeeded(val classification: DeinWackelbildErrorClassification) : GenerationOutcome()
}
