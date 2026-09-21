// path: app/src/test/java/com/isardomains/sameview/ui/wackelbild/WackelbildHandoffOrchestratorTest.kt
package com.isardomains.sameview.ui.wackelbild

import com.isardomains.sameview.image.wackelbild.WackelbildDateOverlay
import com.isardomains.sameview.image.wackelbild.WackelbildPrintFailureReason
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
import java.nio.file.Files
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WackelbildHandoffOrchestratorTest {

    private lateinit var cacheDir: File
    private lateinit var tempFileManager: WackelbildTempFileManager
    private lateinit var sessionDir: File

    @Before
    fun setUp() {
        cacheDir = Files.createTempDirectory("wb-orch-cache-").toFile()
        tempFileManager = WackelbildTempFileManager(cacheDir)
        sessionDir = Files.createTempDirectory("wb-orch-session-").toFile()
    }

    @After
    fun tearDown() {
        cacheDir.deleteRecursively()
        sessionDir.deleteRecursively()
    }

    // ── Fakes / fixtures ─────────────────────────────────────────────────────

    private class FakeApiClient : DeinWackelbildApiClient {
        val createResponses = ArrayDeque<DeinWackelbildResult<CreateHandoffResponse>>()
        val uploadResponses = ArrayDeque<DeinWackelbildResult<UploadResponse>>()
        val createCalls = mutableListOf<String>() // idempotency keys, in call order
        val createRequests = mutableListOf<CreateHandoffRequest>() // request bodies, in call order
        val uploadCalls = mutableListOf<Triple<String, DeinWackelbildSlot, File>>() // (uploadUrl, slot, file)
        val createAttempted = CompletableDeferred<Unit>() // completed on the first createHandoff call

        override suspend fun createHandoff(
            request: CreateHandoffRequest,
            idempotencyKey: String
        ): DeinWackelbildResult<CreateHandoffResponse> {
            createCalls.add(idempotencyKey)
            createRequests.add(request)
            createAttempted.complete(Unit)
            return createResponses.removeFirstOrNull() ?: error("FakeApiClient: no more scripted create responses")
        }

        override suspend fun uploadImage(
            uploadUrl: String,
            handoffToken: String,
            slot: DeinWackelbildSlot,
            file: File
        ): DeinWackelbildResult<UploadResponse> {
            uploadCalls.add(Triple(uploadUrl, slot, file))
            return uploadResponses.removeFirstOrNull() ?: error("FakeApiClient: no more scripted upload responses")
        }
    }

    /** Hangs (via [awaitCancellation]) on the configured call instead of returning, so a test can
     * deterministically catch the orchestrator mid-flight and cancel it there. */
    private class HangingApiClient(
        private val hangOnCreate: Boolean = false,
        private val hangOnUploadSlot: DeinWackelbildSlot? = null,
        private val createResponse: DeinWackelbildResult<CreateHandoffResponse>? = null,
        private val uploadOneResponse: DeinWackelbildResult<UploadResponse>? = null
    ) : DeinWackelbildApiClient {
        val reached = CompletableDeferred<Unit>()

        override suspend fun createHandoff(
            request: CreateHandoffRequest,
            idempotencyKey: String
        ): DeinWackelbildResult<CreateHandoffResponse> {
            if (hangOnCreate) {
                reached.complete(Unit)
                awaitCancellation()
            }
            return createResponse ?: error("HangingApiClient: no create response configured")
        }

        override suspend fun uploadImage(
            uploadUrl: String,
            handoffToken: String,
            slot: DeinWackelbildSlot,
            file: File
        ): DeinWackelbildResult<UploadResponse> {
            if (slot == hangOnUploadSlot) {
                reached.complete(Unit)
                awaitCancellation()
            }
            return uploadOneResponse ?: error("HangingApiClient: no upload response configured for $slot")
        }
    }

    private fun createSuccess(
        uploadUrl: String = "https://deinwackelbild.de/upload/x",
        handoffToken: String = "tok",
        status: String = "awaiting_files"
    ): DeinWackelbildResult<CreateHandoffResponse> = DeinWackelbildResult.Success(
        CreateHandoffResponse(
            handoffId = "h1", handoffToken = handoffToken, partner = "sameview", status = status,
            expiresAt = "2026-08-30T12:00:00Z", maxFileBytes = 20_971_520L, acceptedTypes = listOf("image/jpeg"),
            uploadedSlots = emptyList(), uploadUrl = uploadUrl, checkoutUrl = null
        )
    )

    private fun uploadOneSuccess(
        status: String = "awaiting_files",
        slots: List<String> = listOf("one"),
        checkoutUrl: String? = null
    ): DeinWackelbildResult<UploadResponse> = DeinWackelbildResult.Success(UploadResponse("h1", status, slots, checkoutUrl))

    private fun uploadTwoSuccess(
        checkoutUrl: String? = "https://deinwackelbild.de/checkout/h1",
        status: String = "ready",
        slots: List<String> = listOf("one", "two")
    ): DeinWackelbildResult<UploadResponse> = DeinWackelbildResult.Success(UploadResponse("h1", status, slots, checkoutUrl))

    private fun <T> failure(classification: DeinWackelbildErrorClassification): DeinWackelbildResult<T> =
        DeinWackelbildResult.Failure(DeinWackelbildApiError(classification))

    private class RecordingRenderer(private val usedFallback: Boolean = false, private val fail: Boolean = false) {
        var callCount = 0
            private set

        /** The print target handed to each render call, in call order. */
        val receivedTargets = mutableListOf<WackelbildPrintTarget?>()

        val fn: suspend (File, File, WackelbildDateOverlay?, WackelbildPrintTarget?) -> WackelbildPrintResult =
            { _, outputDir, _, printTarget ->
            callCount++
            receivedTargets.add(printTarget)
            if (fail) {
                WackelbildPrintResult.Failure(WackelbildPrintFailureReason.PERMANENT_NO_VALID_SOURCE)
            } else {
                val ref = File(outputDir, "image_one.jpg").also { it.parentFile?.mkdirs(); it.writeBytes(byteArrayOf(1)) }
                val cap = File(outputDir, "image_two.jpg").also { it.parentFile?.mkdirs(); it.writeBytes(byteArrayOf(2)) }
                WackelbildPrintResult.Success(WackelbildPrintPair(ref, cap), usedFallback)
            }
        }
    }

    // The one-byte fixture files are not decodable JPEGs, so dimensions are always injected; the
    // default `{ null }` keeps every non-configuration test independent of Android decode stubs.
    private fun orchestrator(
        keys: MutableList<String>? = null,
        readDimensions: (File) -> Pair<Int, Int>? = { null }
    ) = WackelbildHandoffOrchestrator(
        idempotencyKeyFactory = {
            val key = "key-${(keys?.size ?: 0) + 1}"
            keys?.add(key)
            key
        },
        readImageDimensions = readDimensions
    )

    private suspend fun execute(
        orchestrator: WackelbildHandoffOrchestrator,
        apiClient: DeinWackelbildApiClient,
        renderer: RecordingRenderer,
        confirmFallback: suspend () -> Unit = {},
        phases: MutableList<WackelbildOperationState> = mutableListOf(),
        printTarget: WackelbildPrintTarget? = null
    ): WackelbildOperationState = orchestrator.execute(
        sessionDir = sessionDir,
        tempFileManager = tempFileManager,
        apiClient = apiClient,
        dateOverlay = null,
        renderPrintPair = renderer.fn,
        awaitFallbackConfirmation = confirmFallback,
        onPhaseChange = { phases.add(it) },
        printTarget = printTarget
    )

    private fun assertNoLeftoverOperationDirs() {
        val wackelbildRoot = File(cacheDir, "wackelbild")
        assertTrue(wackelbildRoot.listFiles()?.isEmpty() ?: true)
    }

    // ── Happy path ───────────────────────────────────────────────────────────

    @Test
    fun happyPath_endsInReady_withCorrectSlotMapping_andCleanup() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess(checkoutUrl = "https://deinwackelbild.de/checkout/h1"))
        }
        val renderer = RecordingRenderer(usedFallback = false)
        val phases = mutableListOf<WackelbildOperationState>()

        val result = execute(orchestrator(), apiClient, renderer, phases = phases)

        assertEquals(WackelbildOperationState.Ready("https://deinwackelbild.de/checkout/h1", usedFallback = false), result)
        assertEquals(1, renderer.callCount)
        assertEquals(DeinWackelbildSlot.ONE, apiClient.uploadCalls[0].second)
        assertEquals(DeinWackelbildSlot.TWO, apiClient.uploadCalls[1].second)
        assertEquals("image_one.jpg", apiClient.uploadCalls[0].third.name) // Reference -> slot one
        assertEquals("image_two.jpg", apiClient.uploadCalls[1].third.name) // Capture -> slot two
        assertTrue(phases.contains(WackelbildOperationState.Preparing))
        assertTrue(phases.contains(WackelbildOperationState.CreatingHandoff))
        assertTrue(phases.contains(WackelbildOperationState.UploadingSlot(DeinWackelbildSlot.ONE)))
        assertTrue(phases.contains(WackelbildOperationState.UploadingSlot(DeinWackelbildSlot.TWO)))
        assertNoLeftoverOperationDirs()
    }

    // ── Create-request configuration (format / orientation / direction) ─────
    // `format` always comes from the already-selected print target (never re-derived from the
    // rendered dimensions), `orientation` from the rendered dimensions, `direction` is always
    // horizontal. With a target, a pair that does not honor it fails locally before any network call.

    /** Serves [reference] for `image_one.jpg` and [capture] for `image_two.jpg`. */
    private fun dimensionsFor(reference: Pair<Int, Int>?, capture: Pair<Int, Int>? = reference): (File) -> Pair<Int, Int>? =
        { file -> if (file.name == "image_one.jpg") reference else capture }

    private fun targetFor(frameWidth: Int, frameHeight: Int): WackelbildPrintTarget =
        checkNotNull(WackelbildPrintTarget.select(frameWidth, frameHeight))

    private fun successfulApiClient() = FakeApiClient().apply {
        createResponses.add(createSuccess())
        uploadResponses.add(uploadOneSuccess())
        uploadResponses.add(uploadTwoSuccess())
    }

    /** Runs a successful operation and returns the single create request that was sent. */
    private suspend fun createRequestFor(
        rendered: Pair<Int, Int>?,
        target: WackelbildPrintTarget?,
        capture: Pair<Int, Int>? = rendered
    ): CreateHandoffRequest {
        val apiClient = successfulApiClient()
        val result = execute(
            orchestrator(readDimensions = dimensionsFor(rendered, capture)),
            apiClient,
            RecordingRenderer(),
            printTarget = target
        )
        assertTrue(result is WackelbildOperationState.Ready)
        return apiClient.createRequests.single()
    }

    /** Runs an operation that must fail locally: preparation failure, zero network calls, cleaned up. */
    private suspend fun assertFailsLocally(
        rendered: Pair<Int, Int>?,
        target: WackelbildPrintTarget?,
        capture: Pair<Int, Int>? = rendered,
        usedFallback: Boolean = false
    ) {
        val apiClient = FakeApiClient() // no scripted responses: any network call would throw
        var fallbackConfirmationRequested = false
        val phases = mutableListOf<WackelbildOperationState>()
        val result = execute(
            orchestrator(readDimensions = dimensionsFor(rendered, capture)),
            apiClient,
            RecordingRenderer(usedFallback = usedFallback),
            confirmFallback = { fallbackConfirmationRequested = true },
            phases = phases,
            printTarget = target
        )
        assertEquals(
            WackelbildOperationState.Failed(WackelbildOperationFailure(WackelbildOperationFailureCategory.PREPARATION_FAILED)),
            result
        )
        assertEquals(0, apiClient.createCalls.size)
        assertEquals(0, apiClient.uploadCalls.size)
        assertFalse("must fail before asking for fallback consent", fallbackConfirmationRequested)
        assertFalse(phases.contains(WackelbildOperationState.AwaitingFallbackConfirmation))
        assertNoLeftoverOperationDirs()
    }

    private fun assertConfiguration(
        request: CreateHandoffRequest,
        format: String?,
        orientation: String?
    ) {
        assertEquals("sameview", request.partner)
        assertEquals(format, request.format)
        assertEquals(orientation, request.orientation)
        assertEquals("horizontal", request.direction)
    }

    @Test
    fun configuration_9x16Portrait_sends10x15_portrait_horizontal() = runTest {
        // Frame 1080x1920 -> 2:3 target; the renderer's centered crop is 1080x1620.
        assertConfiguration(createRequestFor(1080 to 1620, targetFor(1080, 1920)), "10x15", "portrait")
    }

    @Test
    fun configuration_16x9Landscape_sends10x15_landscape_horizontal() = runTest {
        assertConfiguration(createRequestFor(1620 to 1080, targetFor(1920, 1080)), "10x15", "landscape")
    }

    @Test
    fun configuration_2x3_sends10x15_portrait_horizontal() = runTest {
        assertConfiguration(createRequestFor(1080 to 1620, targetFor(1080, 1620)), "10x15", "portrait")
    }

    @Test
    fun configuration_aSeriesLike_sendsA6() = runTest {
        // Frame 1000x1414 -> a6 target; the centered crop is 996x1414.
        assertConfiguration(createRequestFor(996 to 1414, targetFor(1000, 1414)), "a6", "portrait")
    }

    @Test
    fun configuration_3x4AndItsLandscapeTwin_send15x20() = runTest {
        assertConfiguration(createRequestFor(1080 to 1440, targetFor(1080, 1440)), "15x20", "portrait")
        assertConfiguration(createRequestFor(1440 to 1080, targetFor(1440, 1080)), "15x20", "landscape")
    }

    @Test
    fun configuration_4x5AndItsLandscapeTwin_send15x20() = runTest {
        assertConfiguration(createRequestFor(1012 to 1350, targetFor(1080, 1350)), "15x20", "portrait")
        assertConfiguration(createRequestFor(1350 to 1012, targetFor(1350, 1080)), "15x20", "landscape")
    }

    @Test
    fun configuration_square_sends15x15_withoutOrientation_horizontal() = runTest {
        assertConfiguration(createRequestFor(1080 to 1080, targetFor(1080, 1080)), "15x15", null)
    }

    @Test
    fun configuration_formatIsTheTargetsSlug_notReselectedFromTheRenderedDimensions() = runTest {
        // The target says 10x15. Rendered dimensions that would *select* a different family on their
        // own (a 3:4 pair) are rejected -- never turned into a different format.
        assertFailsLocally(rendered = 1080 to 1440, target = targetFor(1080, 1920))
    }

    @Test
    fun configuration_roundedOutputWithinTheRoundingAllowance_isAccepted() = runTest {
        // 1080x1622 is ~0.12% off 2:3, inside the 2 px allowance a rounded crop can produce.
        assertConfiguration(createRequestFor(1080 to 1622, targetFor(1080, 1920)), "10x15", "portrait")
    }

    @Test
    fun configuration_nullTarget_omitsFormat_keepsOrientationAndDirection() = runTest {
        assertConfiguration(createRequestFor(1080 to 1920, target = null), null, "portrait")
        assertConfiguration(createRequestFor(1920 to 1080, target = null), null, "landscape")
        assertConfiguration(createRequestFor(1000 to 1000, target = null), null, null)
    }

    @Test
    fun configuration_nullTarget_unreadableOrMismatchedDimensions_directionOnly() = runTest {
        assertConfiguration(createRequestFor(rendered = null, target = null), null, null)
        assertConfiguration(createRequestFor(rendered = 1080 to 1920, target = null, capture = null), null, null)
        assertConfiguration(createRequestFor(rendered = null, target = null, capture = 1080 to 1920), null, null)
        assertConfiguration(createRequestFor(rendered = 1080 to 1920, target = null, capture = 1080 to 1918), null, null)
    }

    @Test
    fun configuration_withTarget_unreadableDimensions_failLocally() = runTest {
        val target = targetFor(1080, 1920)
        assertFailsLocally(rendered = null, target = target)
        assertFailsLocally(rendered = 1080 to 1620, target = target, capture = null)
        assertFailsLocally(rendered = null, target = target, capture = 1080 to 1620)
    }

    @Test
    fun configuration_withTarget_mismatchedPairDimensions_failLocally() = runTest {
        assertFailsLocally(rendered = 1080 to 1620, target = targetFor(1080, 1920), capture = 1080 to 1618)
    }

    @Test
    fun configuration_withTarget_outputNotHonoringTheTarget_failsLocally() = runTest {
        val target = targetFor(1080, 1920)
        assertFailsLocally(rendered = 1080 to 1920, target = target) // full frame, no crop applied
        assertFailsLocally(rendered = 1080 to 1626, target = target) // outside the rounding allowance
        assertFailsLocally(rendered = 1620 to 1080, target = target) // orientation swapped
    }

    @Test
    fun configuration_withTarget_failsBeforeTheFallbackConsentDialog() = runTest {
        assertFailsLocally(rendered = 1080 to 1920, target = targetFor(1080, 1920), usedFallback = true)
    }

    @Test
    fun configuration_nonPositiveDimensions_areUntrusted() {
        assertConfiguration(checkNotNull(buildCreateHandoffRequest(0 to 1620, null)), null, null)
        assertConfiguration(checkNotNull(buildCreateHandoffRequest(1080 to -1, null)), null, null)
        assertConfiguration(checkNotNull(buildCreateHandoffRequest(null, null)), null, null)
        assertNull(buildCreateHandoffRequest(0 to 1620, targetFor(1080, 1920)))
    }

    @Test
    fun configuration_sameTargetReachesTheRendererAndTheRequest() = runTest {
        val target = targetFor(1080, 1920)
        val renderer = RecordingRenderer()
        val apiClient = successfulApiClient()

        val result = execute(
            orchestrator(readDimensions = dimensionsFor(1080 to 1620)),
            apiClient,
            renderer,
            printTarget = target
        )

        assertTrue(result is WackelbildOperationState.Ready)
        assertEquals(listOf<WackelbildPrintTarget?>(target), renderer.receivedTargets)
        assertEquals(target.slug, apiClient.createRequests.single().format)
    }

    @Test
    fun configuration_nullTargetReachesTheRendererAsNull() = runTest {
        val renderer = RecordingRenderer()
        execute(orchestrator(readDimensions = dimensionsFor(1080 to 1920)), successfulApiClient(), renderer)
        assertEquals(listOf<WackelbildPrintTarget?>(null), renderer.receivedTargets)
    }

    @Test
    fun configuration_restartReusesIdenticalRequest_dimensionsReadOnlyOnce() = runTest {
        var readCount = 0
        val reader: (File) -> Pair<Int, Int>? = { readCount++; 1080 to 1620 }
        val target = targetFor(1080, 1920)
        val renderer = RecordingRenderer()
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(failure(DeinWackelbildErrorClassification.EXPIRED_HANDOFF))
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess())
        }

        val result = execute(orchestrator(readDimensions = reader), apiClient, renderer, printTarget = target)

        assertTrue(result is WackelbildOperationState.Ready)
        assertEquals(2, apiClient.createRequests.size)
        assertEquals(apiClient.createRequests[0], apiClient.createRequests[1])
        assertConfiguration(apiClient.createRequests[1], "10x15", "portrait")
        assertEquals(1, renderer.callCount) // restarts never re-render, so the target is never re-applied
        assertEquals(2, readCount) // once per rendered file, not once per generation
    }

    // ── Fallback confirmation ────────────────────────────────────────────────

    @Test
    fun nonFallback_neverEntersAwaitingFallbackConfirmation() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess())
        }
        val phases = mutableListOf<WackelbildOperationState>()
        execute(orchestrator(), apiClient, RecordingRenderer(usedFallback = false), phases = phases)
        assertFalse(phases.contains(WackelbildOperationState.AwaitingFallbackConfirmation))
    }

    @Test
    fun fallback_entersAwaitingConfirmation_zeroApiCallsBeforeConfirm() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess())
        }
        val phases = mutableListOf<WackelbildOperationState>()
        var apiCallsBeforeConfirm = -1
        execute(
            orchestrator(), apiClient, RecordingRenderer(usedFallback = true),
            confirmFallback = { apiCallsBeforeConfirm = apiClient.createCalls.size + apiClient.uploadCalls.size },
            phases = phases
        )
        assertEquals(0, apiCallsBeforeConfirm)
        assertTrue(phases.contains(WackelbildOperationState.AwaitingFallbackConfirmation))
    }

    @Test
    fun fallback_confirmResumesSameOperation_noRerender_usedFallbackPropagates() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess(checkoutUrl = "https://deinwackelbild.de/checkout/h1"))
        }
        val renderer = RecordingRenderer(usedFallback = true)
        val result = execute(orchestrator(), apiClient, renderer, confirmFallback = { /* returns immediately: confirmed */ })
        assertEquals(1, renderer.callCount)
        assertEquals(WackelbildOperationState.Ready("https://deinwackelbild.de/checkout/h1", usedFallback = true), result)
    }

    @Test
    fun fallback_cancelWhileAwaiting_zeroNetworkCalls_cleanupRuns() = runTest {
        val apiClient = FakeApiClient() // no responses scripted -- must never be called
        var caught = false
        val job = launch {
            try {
                execute(
                    orchestrator(), apiClient, RecordingRenderer(usedFallback = true),
                    confirmFallback = { awaitCancellation() }
                )
                fail("expected cancellation")
            } catch (e: CancellationException) {
                caught = true
            }
        }
        yield() // let it reach the suspended confirmFallback
        job.cancel()
        job.join()
        assertTrue(caught)
        assertEquals(0, apiClient.createCalls.size)
        assertEquals(0, apiClient.uploadCalls.size)
        assertNoLeftoverOperationDirs()
    }

    // ── Renderer failure ─────────────────────────────────────────────────────

    @Test
    fun rendererFailure_noNetworkCall_correctFailureCategory_cleanupRuns() = runTest {
        val apiClient = FakeApiClient()
        val result = execute(orchestrator(), apiClient, RecordingRenderer(fail = true))
        assertEquals(
            WackelbildOperationState.Failed(WackelbildOperationFailure(WackelbildOperationFailureCategory.PREPARATION_FAILED)),
            result
        )
        assertEquals(0, apiClient.createCalls.size)
        assertNoLeftoverOperationDirs()
    }

    // ── Idempotency ──────────────────────────────────────────────────────────

    @Test
    fun sameKeyReusedAcrossCreateTransientRetries() = runTest {
        val keys = mutableListOf<String>()
        val apiClient = FakeApiClient().apply {
            createResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_NETWORK))
            createResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_SERVER))
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess())
        }
        execute(orchestrator(keys), apiClient, RecordingRenderer())
        assertEquals(3, apiClient.createCalls.size)
        assertEquals(1, apiClient.createCalls.toSet().size) // all 3 attempts used the identical key
        assertEquals(1, keys.size)
    }

    @Test
    fun expiredHandoff_403Or410_triggersRestart_newKey_filesReused_noRerender() = runTest {
        val keys = mutableListOf<String>()
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(failure(DeinWackelbildErrorClassification.EXPIRED_HANDOFF)) // covers both 403 and 410
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess(checkoutUrl = "https://deinwackelbild.de/checkout/h2"))
        }
        val renderer = RecordingRenderer()
        val result = execute(orchestrator(keys), apiClient, renderer)
        assertTrue(result is WackelbildOperationState.Ready)
        assertEquals(2, keys.size)
        assertEquals(keys[0], apiClient.createCalls[0])
        assertEquals(keys[1], apiClient.createCalls[1])
        assertEquals(1, renderer.callCount)
        assertEquals(apiClient.uploadCalls[0].third, apiClient.uploadCalls[2].third) // same Reference file reused
    }

    @Test
    fun incompleteHandoff409_triggersRestart_newKey() = runTest {
        val keys = mutableListOf<String>()
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(failure(DeinWackelbildErrorClassification.INCOMPLETE_HANDOFF))
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess())
        }
        val result = execute(orchestrator(keys), apiClient, RecordingRenderer())
        assertTrue(result is WackelbildOperationState.Ready)
        assertEquals(2, apiClient.createCalls.size)
        assertEquals(2, keys.size)
    }

    @Test
    fun newExecuteCall_getsNewKey() = runTest {
        val keys = mutableListOf<String>()
        val orch = orchestrator(keys)
        val apiClient1 = FakeApiClient().apply {
            createResponses.add(createSuccess()); uploadResponses.add(uploadOneSuccess()); uploadResponses.add(uploadTwoSuccess())
        }
        execute(orch, apiClient1, RecordingRenderer())
        val apiClient2 = FakeApiClient().apply {
            createResponses.add(createSuccess()); uploadResponses.add(uploadOneSuccess()); uploadResponses.add(uploadTwoSuccess())
        }
        execute(orch, apiClient2, RecordingRenderer())
        assertEquals(2, keys.size)
        assertFalse(apiClient1.createCalls[0] == apiClient2.createCalls[0])
    }

    // ── Retry ────────────────────────────────────────────────────────────────

    @Test
    fun networkFailureThenSuccess() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_NETWORK))
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess())
        }
        val result = execute(orchestrator(), apiClient, RecordingRenderer())
        assertTrue(result is WackelbildOperationState.Ready)
        assertEquals(2, apiClient.createCalls.size)
    }

    @Test
    fun server5xxThenSuccess() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_SERVER))
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess())
        }
        val result = execute(orchestrator(), apiClient, RecordingRenderer())
        assertTrue(result is WackelbildOperationState.Ready)
    }

    @Test
    fun rateLimitThenSuccess() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(failure(DeinWackelbildErrorClassification.RATE_LIMITED))
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess())
        }
        val result = execute(orchestrator(), apiClient, RecordingRenderer())
        assertTrue(result is WackelbildOperationState.Ready)
    }

    @Test
    fun exactDelaySequence_1sThen2s() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_NETWORK))
            createResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_SERVER))
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess())
        }
        execute(orchestrator(), apiClient, RecordingRenderer())
        assertEquals(3000L, testScheduler.currentTime)
    }

    @Test
    fun exactlyThreeMaxAttempts_thenTerminalFailure() = runTest {
        val apiClient = FakeApiClient().apply {
            repeat(3) { createResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_NETWORK)) }
        }
        val result = execute(orchestrator(), apiClient, RecordingRenderer())
        assertEquals(3, apiClient.createCalls.size)
        assertEquals(
            WackelbildOperationState.Failed(
                WackelbildOperationFailure(WackelbildOperationFailureCategory.NETWORK_UNAVAILABLE, DeinWackelbildErrorClassification.RETRYABLE_NETWORK)
            ),
            result
        )
    }

    @Test
    fun nonRetryable400_noRetry_immediateFailure() = runTest {
        val apiClient = FakeApiClient().apply { createResponses.add(failure(DeinWackelbildErrorClassification.INVALID_REQUEST)) }
        val result = execute(orchestrator(), apiClient, RecordingRenderer())
        assertEquals(1, apiClient.createCalls.size)
        assertEquals(WackelbildOperationFailureCategory.HANDOFF_FAILED, (result as WackelbildOperationState.Failed).failure.category)
    }

    // ── Handoff generation bound / 27-request worst case ────────────────────

    @Test
    fun generation3RestartTrigger_terminatesWithHandoffFailed_noFourthGeneration() = runTest {
        val apiClient = FakeApiClient().apply {
            repeat(3) {
                createResponses.add(createSuccess())
                uploadResponses.add(uploadOneSuccess())
                uploadResponses.add(failure(DeinWackelbildErrorClassification.EXPIRED_HANDOFF))
            }
        }
        val result = execute(orchestrator(), apiClient, RecordingRenderer())
        assertEquals(
            WackelbildOperationState.Failed(
                WackelbildOperationFailure(WackelbildOperationFailureCategory.HANDOFF_FAILED, DeinWackelbildErrorClassification.EXPIRED_HANDOFF)
            ),
            result
        )
        assertEquals(3, apiClient.createCalls.size)
    }

    @Test
    fun worstCaseSequence_reachesExactly27Requests_thenTerminates() = runTest {
        val apiClient = FakeApiClient().apply {
            repeat(3) {
                createResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_NETWORK))
                createResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_SERVER))
                createResponses.add(createSuccess())
                uploadResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_NETWORK))
                uploadResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_SERVER))
                uploadResponses.add(uploadOneSuccess())
                uploadResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_NETWORK))
                uploadResponses.add(failure(DeinWackelbildErrorClassification.RATE_LIMITED))
                uploadResponses.add(failure(DeinWackelbildErrorClassification.EXPIRED_HANDOFF))
            }
        }
        val result = execute(orchestrator(), apiClient, RecordingRenderer())
        assertEquals(9, apiClient.createCalls.size)
        assertEquals(18, apiClient.uploadCalls.size)
        assertEquals(27, apiClient.createCalls.size + apiClient.uploadCalls.size)
        assertEquals(WackelbildOperationFailureCategory.HANDOFF_FAILED, (result as WackelbildOperationState.Failed).failure.category)
    }

    // ── Semantic validation ──────────────────────────────────────────────────

    @Test
    fun createWrongStatus_terminalFailure_noUpload() = runTest {
        val apiClient = FakeApiClient().apply { createResponses.add(createSuccess(status = "something_else")) }
        val result = execute(orchestrator(), apiClient, RecordingRenderer())
        assertTrue(result is WackelbildOperationState.Failed)
        assertEquals(0, apiClient.uploadCalls.size)
    }

    @Test
    fun uploadOneWrongStatus_terminalFailure_noSlotTwo() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess(status = "ready"))
        }
        val result = execute(orchestrator(), apiClient, RecordingRenderer())
        assertTrue(result is WackelbildOperationState.Failed)
        assertEquals(1, apiClient.uploadCalls.size)
    }

    @Test
    fun uploadOneMissingSlotOne_terminalFailure() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess(slots = emptyList()))
        }
        assertTrue(execute(orchestrator(), apiClient, RecordingRenderer()) is WackelbildOperationState.Failed)
    }

    @Test
    fun uploadOnePrematureCheckout_terminalFailure() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess(checkoutUrl = "https://deinwackelbild.de/checkout/h1"))
        }
        assertTrue(execute(orchestrator(), apiClient, RecordingRenderer()) is WackelbildOperationState.Failed)
    }

    @Test
    fun uploadTwoNotReady_terminalFailure() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess(status = "awaiting_files"))
        }
        assertTrue(execute(orchestrator(), apiClient, RecordingRenderer()) is WackelbildOperationState.Failed)
    }

    @Test
    fun uploadTwoMissingSlotOne_terminalFailure() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess(slots = listOf("two")))
        }
        assertTrue(execute(orchestrator(), apiClient, RecordingRenderer()) is WackelbildOperationState.Failed)
    }

    @Test
    fun uploadTwoMissingSlotTwo_terminalFailure() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess(slots = listOf("one")))
        }
        assertTrue(execute(orchestrator(), apiClient, RecordingRenderer()) is WackelbildOperationState.Failed)
    }

    @Test
    fun uploadTwoReadyWithoutCheckout_terminalFailure() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(createSuccess())
            uploadResponses.add(uploadOneSuccess())
            uploadResponses.add(uploadTwoSuccess(checkoutUrl = null))
        }
        assertTrue(execute(orchestrator(), apiClient, RecordingRenderer()) is WackelbildOperationState.Failed)
    }

    // ── Cancellation ─────────────────────────────────────────────────────────

    @Test
    fun cancellation_duringPreparation_cleanupRuns_noReady() = runTest {
        val apiClient = FakeApiClient()
        val reached = CompletableDeferred<Unit>()
        val hangingRenderer: suspend (File, File, WackelbildDateOverlay?, WackelbildPrintTarget?) -> WackelbildPrintResult = { _, _, _, _ ->
            reached.complete(Unit)
            awaitCancellation()
        }
        var caught = false
        val job = launch {
            try {
                orchestrator().execute(sessionDir, tempFileManager, apiClient, null, hangingRenderer, {}, {})
                fail("expected cancellation")
            } catch (e: CancellationException) {
                caught = true
            }
        }
        reached.await()
        job.cancel()
        job.join()
        assertTrue(caught)
        assertNoLeftoverOperationDirs()
    }

    @Test
    fun cancellation_duringCreate_cleanupRuns_noReady() = runTest {
        val apiClient = HangingApiClient(hangOnCreate = true)
        var caught = false
        val job = launch {
            try {
                orchestrator().execute(sessionDir, tempFileManager, apiClient, null, RecordingRenderer().fn, {}, {})
                fail("expected cancellation")
            } catch (e: CancellationException) {
                caught = true
            }
        }
        apiClient.reached.await()
        job.cancel()
        job.join()
        assertTrue(caught)
        assertNoLeftoverOperationDirs()
    }

    @Test
    fun cancellation_duringRetryDelay_cleanupRuns_onlyOneAttemptMade() = runTest {
        val apiClient = FakeApiClient().apply {
            createResponses.add(failure(DeinWackelbildErrorClassification.RETRYABLE_NETWORK))
        }
        var caught = false
        val job = launch {
            try {
                execute(orchestrator(), apiClient, RecordingRenderer())
                fail("expected cancellation")
            } catch (e: CancellationException) {
                caught = true
            }
        }
        // Wait for the first attempt itself (a bare yield() is no longer enough: the pair's
        // dimensions are read on Dispatchers.IO before the create call). The orchestrator runs on
        // straight through its failure into delay(1000) before this coroutine resumes.
        apiClient.createAttempted.await()
        job.cancel()
        job.join()
        assertTrue(caught)
        assertEquals(1, apiClient.createCalls.size)
        assertNoLeftoverOperationDirs()
    }

    @Test
    fun cancellation_duringUploadOne_cleanupRuns_noReady() = runTest {
        val apiClient = HangingApiClient(hangOnUploadSlot = DeinWackelbildSlot.ONE, createResponse = createSuccess())
        var caught = false
        val job = launch {
            try {
                orchestrator().execute(sessionDir, tempFileManager, apiClient, null, RecordingRenderer().fn, {}, {})
                fail("expected cancellation")
            } catch (e: CancellationException) {
                caught = true
            }
        }
        apiClient.reached.await()
        job.cancel()
        job.join()
        assertTrue(caught)
        assertNoLeftoverOperationDirs()
    }

    @Test
    fun cancellation_duringUploadTwo_cleanupRuns_noReady() = runTest {
        val apiClient = HangingApiClient(
            hangOnUploadSlot = DeinWackelbildSlot.TWO,
            createResponse = createSuccess(),
            uploadOneResponse = uploadOneSuccess()
        )
        var caught = false
        val job = launch {
            try {
                orchestrator().execute(sessionDir, tempFileManager, apiClient, null, RecordingRenderer().fn, {}, {})
                fail("expected cancellation")
            } catch (e: CancellationException) {
                caught = true
            }
        }
        apiClient.reached.await()
        job.cancel()
        job.join()
        assertTrue(caught)
        assertNoLeftoverOperationDirs()
    }
}
