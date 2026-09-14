// path: app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt
package com.isardomains.sameview.ui.wackelbild

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.isardomains.sameview.R
import com.isardomains.sameview.ui.settings.SettingsSwitchRow
import com.isardomains.sameview.ui.theme.SameViewAppSurface
import com.isardomains.sameview.ui.theme.SameViewSettingsSecondaryText
import java.io.File
import kotlin.math.abs
import kotlinx.coroutines.flow.Flow

// Reuses CreateVideoScreen's own calibrated "media preview sharing a content column with
// sibling controls" ratio (see CreateVideoScreen.kt's `maxCardHeight = maxHeight * 0.62f`) — not
// a newly invented value. Acts as a secondary upper-bound ceiling only: the primary bound is the
// height actually remaining after the lower controls stack has claimed its own natural height
// (see the preview's `Modifier.weight(1f, fill = false)` call site below and
// `WackelbildPreview`'s `effectiveHeight` calculation) — this constant only prevents the preview
// from growing unreasonably large on layouts where much more vertical space happens to remain
// than the controls stack actually needs.
private const val PREVIEW_HEIGHT_FRACTION_OF_CONTENT = 0.62f
private const val SWIPE_THRESHOLD_DP = 24
private val DATE_BADGE_CORNER_RADIUS = 6.dp
private val DATE_BADGE_HORIZONTAL_PADDING = 8.dp
private val DATE_BADGE_VERTICAL_PADDING = 4.dp
private val DATE_BADGE_IMAGE_EDGE_MARGIN = 8.dp

// TODO(real-device tuning): candidate starting value pending the mandatory real-device tuning
// pass -- see DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md §7.7/§25/§30. Deliberately small/subtle
// per DEINWACKELBILD_INTEGRATION_V1.md §8.9 -- safer to start too subtle and tune upward.
private const val PERSPECTIVE_MAX_ROTATION_DEGREES = 6f

// TODO(real-device tuning): candidate starting value -- Compose's own DefaultCameraDistance (8f),
// scaled by density per the standard adjustment for that otherwise-too-flat default.
private const val PERSPECTIVE_CAMERA_DISTANCE_FACTOR = 8f

// TODO(real-device tuning): ridge spacing/opacity, deliberately subtle per
// DEINWACKELBILD_INTEGRATION_V1.md §8.10 -- not one of the four values explicitly pre-approved,
// added here only because Kotlin requires concrete constants; equally open to real-device tuning.
private val RIDGE_LINE_SPACING = 6.dp
private const val RIDGE_LINE_ALPHA = 0.10f

// TODO(real-device tuning): outer preview edge, deliberately subtle per
// DEINWACKELBILD_INTEGRATION_V1.md §8.11 -- open to real-device tuning.
private val PREVIEW_BORDER_WIDTH = 1.dp
private val PREVIEW_BORDER_COLOR = Color.White.copy(alpha = 0.85f)
private val PREVIEW_CORNER_RADIUS = 6.dp

/** Test-observability-only custom semantics property carrying the current continuous preview
 * blend fraction (§7.7). Never read by TalkBack (an unknown custom key is not announced by
 * accessibility services) -- exists solely so instrumentation tests can assert blend dominance
 * without a pixel-level alpha inspection API. `internal` so [WackelbildScreenTest] (same module)
 * can read it; never referenced from [WackelbildScreenContent]'s TalkBack-facing semantics. */
internal val PreviewBlendFractionKey = SemanticsPropertyKey<Float>("PreviewBlendFraction")

/**
 * DeinWackelbild entry destination (Block 3 scope).
 *
 * Local tilt/swipe preview switching directly between the session's persisted `reference.jpg`
 * and `capture.jpg`. No date/HQ/network behavior exists yet — those are added in later blocks.
 *
 * @param viewModel Hilt ViewModel; owns file resolution and the tilt/swipe interaction state.
 * @param windowWidthSizeClass Used for the Expanded (>= 840 dp) max-width constraint.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun WackelbildScreen(
    onBack: () -> Unit,
    viewModel: WackelbildViewModel = hiltViewModel(),
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    val visibleImage by viewModel.visibleImage.collectAsState()
    val previewBlendFraction by viewModel.previewBlendFraction.collectAsState()
    val dateOverlayEnabled by viewModel.dateOverlayEnabled.collectAsState()
    val isDateOverlayAvailable by viewModel.isDateOverlayAvailable.collectAsState()
    val referenceDateBadgeText by viewModel.referenceDateBadgeText.collectAsState()
    val captureDateBadgeText by viewModel.captureDateBadgeText.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val customTabOpenFailure by viewModel.customTabOpenFailure.collectAsState()
    WackelbildScreenContent(
        referenceFile = viewModel.referenceFile,
        captureFile = viewModel.captureFile,
        visibleImage = visibleImage,
        previewBlendFraction = previewBlendFraction,
        isSensorAvailable = viewModel.isSensorAvailable,
        dateOverlayEnabled = dateOverlayEnabled,
        isDateOverlayAvailable = isDateOverlayAvailable,
        referenceDateBadgeText = referenceDateBadgeText,
        captureDateBadgeText = captureDateBadgeText,
        operationState = operationState,
        customTabOpenFailure = customTabOpenFailure,
        launchCustomTabEvent = viewModel.launchCustomTabEvent,
        onDateOverlayToggled = viewModel::onDateOverlayToggled,
        onSwipeDetected = viewModel::onSwipeDetected,
        onAccessibilityToggle = viewModel::onAccessibilityToggle,
        onScreenActive = viewModel::onScreenActive,
        onScreenInactive = viewModel::onScreenInactive,
        onScreenLeft = viewModel::onScreenLeft,
        onStartOperation = viewModel::startOperation,
        onConfirmFallback = viewModel::confirmFallbackAndContinue,
        onCancelOperation = viewModel::cancelOperation,
        onCustomTabLaunchResult = viewModel::onCustomTabLaunchResult,
        onRetryOpenCheckoutUrl = viewModel::retryOpenCheckoutUrl,
        onBack = onBack,
        windowWidthSizeClass = windowWidthSizeClass
    )
}

/**
 * Stateless content for [WackelbildScreen], taking file/state/callbacks directly rather than a
 * Hilt ViewModel. Kept `internal` so instrumentation tests can exercise the exact production UI
 * without needing a Hilt-backed [WackelbildViewModel]/[androidx.lifecycle.SavedStateHandle].
 *
 * This composable owns lifecycle observation (`ON_RESUME`/`ON_PAUSE`/disposal) itself and calls
 * [onScreenActive]/[onScreenInactive]/[onScreenLeft] directly — [WackelbildViewModel] has no
 * dependency on any Android lifecycle type.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun WackelbildScreenContent(
    referenceFile: File,
    captureFile: File,
    visibleImage: WackelbildImageSide,
    previewBlendFraction: Float,
    isSensorAvailable: Boolean,
    dateOverlayEnabled: Boolean,
    isDateOverlayAvailable: Boolean,
    referenceDateBadgeText: String?,
    captureDateBadgeText: String?,
    operationState: WackelbildOperationState,
    customTabOpenFailure: String?,
    launchCustomTabEvent: Flow<String>,
    onDateOverlayToggled: (Boolean) -> Unit,
    onSwipeDetected: () -> Unit,
    onAccessibilityToggle: () -> Unit,
    onScreenActive: () -> Unit,
    onScreenInactive: () -> Unit,
    onScreenLeft: () -> Unit,
    onStartOperation: () -> Unit,
    onConfirmFallback: () -> Unit,
    onCancelOperation: () -> Unit,
    onCustomTabLaunchResult: (String, Boolean) -> Unit,
    onRetryOpenCheckoutUrl: () -> Unit,
    onBack: () -> Unit,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    onLaunchCustomTab: (Context, String) -> Boolean = { context, url ->
        WackelbildCustomTabLauncher().launch(context, url)
    }
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnScreenActive by rememberUpdatedState(onScreenActive)
    val currentOnScreenInactive by rememberUpdatedState(onScreenInactive)
    val currentOnScreenLeft by rememberUpdatedState(onScreenLeft)

    // Screen/composable-owned lifecycle observation — active while resumed, stopped while
    // paused/backgrounded, fully released when this screen leaves composition.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> currentOnScreenActive()
                Lifecycle.Event.ON_PAUSE -> currentOnScreenInactive()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    DisposableEffect(Unit) {
        onDispose { currentOnScreenLeft() }
    }

    // Collects one-shot Custom Tab launch requests and reports the real outcome back to the
    // ViewModel. A Channel-backed Flow is consumed, not replayed -- recomposition/rotation cannot
    // redeliver an already-consumed element (see WackelbildViewModel.launchCustomTabEvent).
    val context = LocalContext.current
    LaunchedEffect(launchCustomTabEvent) {
        launchCustomTabEvent.collect { checkoutUrl ->
            val success = onLaunchCustomTab(context, checkoutUrl)
            onCustomTabLaunchResult(checkoutUrl, success)
        }
    }

    val isBusy = operationState is WackelbildOperationState.Preparing ||
        operationState is WackelbildOperationState.CreatingHandoff ||
        operationState is WackelbildOperationState.UploadingSlot
    val isAwaitingFallback = operationState is WackelbildOperationState.AwaitingFallbackConfirmation
    // Editable only when there is no active/pending operation at all -- covers the whole window
    // from CTA-tap through Ready/launch-pending/open-failure, re-enabled only on Idle or Failed.
    val isDateToggleEditable = isDateOverlayAvailable &&
        (operationState is WackelbildOperationState.Idle || operationState is WackelbildOperationState.Failed)

    var showCancelTransferDialog by remember { mutableStateOf(false) }
    // Dismiss automatically if the busy state ends from underneath it (mirrors CreateVideoScreen's
    // identical `LaunchedEffect(state) { if (state !is Rendering) showCancelDialog = false }`).
    LaunchedEffect(operationState) {
        if (!isBusy) showCancelTransferDialog = false
    }

    fun handleBackPressed() {
        when {
            isAwaitingFallback -> onCancelOperation()
            isBusy -> showCancelTransferDialog = true
            else -> onBack()
        }
    }
    BackHandler(enabled = true) { handleBackPressed() }

    if (isAwaitingFallback) {
        AlertDialog(
            onDismissRequest = onCancelOperation,
            title = { Text(stringResource(R.string.wackelbild_quality_fallback_title)) },
            text = { Text(stringResource(R.string.wackelbild_quality_fallback_message)) },
            confirmButton = {
                TextButton(
                    onClick = onConfirmFallback,
                    modifier = Modifier.testTag("wackelbild_fallback_continue_button")
                ) {
                    Text(stringResource(R.string.wackelbild_quality_fallback_continue))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancelOperation,
                    modifier = Modifier.testTag("wackelbild_fallback_cancel_button")
                ) {
                    Text(stringResource(R.string.wackelbild_quality_fallback_cancel))
                }
            },
            modifier = Modifier.testTag("wackelbild_fallback_dialog")
        )
    }

    if (showCancelTransferDialog) {
        AlertDialog(
            onDismissRequest = { showCancelTransferDialog = false },
            title = { Text(stringResource(R.string.wackelbild_cancel_transfer_title)) },
            text = { Text(stringResource(R.string.wackelbild_cancel_transfer_message)) },
            confirmButton = {
                TextButton(
                    onClick = { showCancelTransferDialog = false },
                    modifier = Modifier.testTag("wackelbild_cancel_transfer_continue_button")
                ) {
                    Text(stringResource(R.string.wackelbild_cancel_transfer_continue))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCancelTransferDialog = false
                        onCancelOperation()
                    },
                    modifier = Modifier.testTag("wackelbild_cancel_transfer_stop_button")
                ) {
                    Text(stringResource(R.string.wackelbild_cancel_transfer_stop))
                }
            },
            modifier = Modifier.testTag("wackelbild_cancel_transfer_dialog")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wackelbild_screen_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = { handleBackPressed() },
                        modifier = Modifier.testTag("wackelbild_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val contentMaxWidth =
            if (windowWidthSizeClass == WindowWidthSizeClass.Expanded) 680.dp else Dp.Unspecified

        // The actual available content-area height (post top-bar, post-padding) — the basis for
        // the preview's height cap below, never full device/window height including system bars.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("wackelbild_content_area")
        ) {
            val availableContentHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("wackelbild_screen_root"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Structurally outside the scrollable content below (see WackelbildInteractionHint
                // Column) so the swipe gesture never contends with vertical scroll.
                WackelbildPreview(
                    referenceFile = referenceFile,
                    captureFile = captureFile,
                    visibleImage = visibleImage,
                    previewBlendFraction = previewBlendFraction,
                    dateOverlayEnabled = dateOverlayEnabled,
                    referenceDateBadgeText = referenceDateBadgeText,
                    captureDateBadgeText = captureDateBadgeText,
                    availableContentHeight = availableContentHeight,
                    onSwipeDetected = onSwipeDetected,
                    onAccessibilityToggle = onAccessibilityToggle,
                    // weight(1f, fill = false): the preview receives only whatever height remains
                    // after the lower controls Column below (now non-weighted) has already
                    // claimed its own natural height — it is not forced to consume all of that
                    // remainder (fill = false), only bounded by it. This is the mechanism named
                    // in DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md §6.3 and lets
                    // WackelbildPreview's own effectiveHeight calculation read the true
                    // remaining-height constraint directly from Compose's own layout pass,
                    // without a second measurement pass or new state.
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .widthIn(max = contentMaxWidth)
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .widthIn(max = contentMaxWidth)
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WackelbildDateToggleRow(
                        checked = dateOverlayEnabled,
                        enabled = isDateToggleEditable,
                        onCheckedChange = onDateOverlayToggled
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WackelbildInteractionHint(isSensorAvailable = isSensorAvailable)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.wackelbild_transfer_disclosure),
                        style = MaterialTheme.typography.bodySmall,
                        color = SameViewSettingsSecondaryText,
                        modifier = Modifier.testTag("wackelbild_transfer_disclosure")
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WackelbildOrderArea(
                        operationState = operationState,
                        customTabOpenFailure = customTabOpenFailure,
                        onStartOperation = onStartOperation,
                        onRetryOpenCheckoutUrl = onRetryOpenCheckoutUrl
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * CTA / busy / fallback-adjacent / error / Custom-Tab-open-failure area (Block 11).
 *
 * Collapses every busy [WackelbildOperationState] (`Preparing`, `CreatingHandoff`,
 * `UploadingSlot(*)`, and `AwaitingFallbackConfirmation` while its own dialog is visible on top)
 * into one undifferentiated spinner + [R.string.wackelbild_loading_preparing] presentation — no
 * phase name, no upload slot, no percentage is ever exposed (spec §12). `Ready` is a transient
 * sub-frame state with nothing to show (no intermediate success screen, spec §12/§14).
 */
@Composable
private fun WackelbildOrderArea(
    operationState: WackelbildOperationState,
    customTabOpenFailure: String?,
    onStartOperation: () -> Unit,
    onRetryOpenCheckoutUrl: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        when {
            customTabOpenFailure != null -> {
                Text(
                    text = stringResource(R.string.wackelbild_custom_tab_open_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("wackelbild_custom_tab_open_failed_text")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRetryOpenCheckoutUrl,
                    modifier = Modifier.testTag("wackelbild_custom_tab_open_retry_button")
                ) {
                    Text(stringResource(R.string.wackelbild_custom_tab_open_retry))
                }
            }
            operationState is WackelbildOperationState.Preparing ||
                operationState is WackelbildOperationState.CreatingHandoff ||
                operationState is WackelbildOperationState.UploadingSlot ||
                operationState is WackelbildOperationState.AwaitingFallbackConfirmation -> {
                CircularProgressIndicator(
                    modifier = Modifier.testTag("wackelbild_loading_spinner")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.wackelbild_loading_preparing),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("wackelbild_loading_text")
                )
            }
            operationState is WackelbildOperationState.Failed -> {
                val category = operationState.failure.category
                val errorTextRes = when (category) {
                    WackelbildOperationFailureCategory.NETWORK_UNAVAILABLE -> R.string.wackelbild_error_no_internet
                    WackelbildOperationFailureCategory.SERVER_TEMPORARY -> R.string.wackelbild_error_transfer_failed
                    WackelbildOperationFailureCategory.INTEGRATION_UNAVAILABLE ->
                        R.string.wackelbild_error_integration_unavailable
                    WackelbildOperationFailureCategory.PREPARATION_FAILED,
                    WackelbildOperationFailureCategory.INVALID_LOCAL_OUTPUT,
                    WackelbildOperationFailureCategory.HANDOFF_FAILED -> R.string.wackelbild_error_preparation_failed
                }
                Text(
                    text = stringResource(errorTextRes),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("wackelbild_error_text")
                )
                Spacer(modifier = Modifier.height(8.dp))
                when (category) {
                    WackelbildOperationFailureCategory.NETWORK_UNAVAILABLE,
                    WackelbildOperationFailureCategory.SERVER_TEMPORARY -> {
                        Button(
                            onClick = onStartOperation,
                            modifier = Modifier.testTag("wackelbild_error_retry_button")
                        ) {
                            Text(stringResource(R.string.wackelbild_error_retry))
                        }
                    }
                    else -> {
                        Button(
                            onClick = onStartOperation,
                            modifier = Modifier.testTag("wackelbild_cta_button")
                        ) {
                            Text(stringResource(R.string.wackelbild_cta_order))
                        }
                    }
                }
            }
            operationState is WackelbildOperationState.Ready -> {
                // Sub-frame transient window between Ready and the launch-result callback -- no
                // intermediate success screen, nothing to render here (spec §12/§14).
            }
            else -> { // Idle
                Button(
                    onClick = onStartOperation,
                    modifier = Modifier.testTag("wackelbild_cta_button")
                ) {
                    Text(stringResource(R.string.wackelbild_cta_order))
                }
            }
        }
    }
}

/**
 * Local, continuously tilt-blended Reference/Capture preview (spec §8.1/§8.3, amended from the
 * original Block 3 hard-switch design). Both files are requested unconditionally, and both
 * painters are always simultaneously placed in the composition -- visual dominance is controlled
 * purely by [previewBlendFraction] via complementary `alpha` (0f = full Reference, 1f = full
 * Capture), never by conditionally composing one or the other. Also layers the preview-only
 * lenticular ridge surface (§8.10) and, on the outer container, the preview-only perspective tilt
 * (§8.9) -- both purely visual, never touching `reference.jpg`/`capture.jpg`/any persisted or
 * transfer image. Sized from the Reference image's own intrinsic aspect ratio once successfully
 * decoded — never from session metadata and never from a hardcoded fallback ratio (Reference and
 * Capture share the same aspect ratio by construction of the capture pipeline, so this stays
 * stable across the blend).
 */
@Composable
private fun WackelbildPreview(
    referenceFile: File,
    captureFile: File,
    visibleImage: WackelbildImageSide,
    previewBlendFraction: Float,
    dateOverlayEnabled: Boolean,
    referenceDateBadgeText: String?,
    captureDateBadgeText: String?,
    availableContentHeight: Dp,
    onSwipeDetected: () -> Unit,
    onAccessibilityToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var referenceLoadState by remember(referenceFile) {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }
    var captureLoadState by remember(captureFile) {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }

    // Size.ORIGINAL: this preview decides its own layout size *from* the decoded image, so the
    // request must not wait on Coil's default layout-constraints size resolver.
    val referenceRequest = remember(referenceFile) {
        ImageRequest.Builder(context).data(referenceFile).size(Size.ORIGINAL).build()
    }
    val captureRequest = remember(captureFile) {
        ImageRequest.Builder(context).data(captureFile).size(Size.ORIGINAL).build()
    }

    val referencePainter = rememberAsyncImagePainter(
        model = referenceRequest,
        onState = { referenceLoadState = it }
    )
    val capturePainter = rememberAsyncImagePainter(
        model = captureRequest,
        onState = { captureLoadState = it }
    )

    val referenceRatio = intrinsicRatioOf(referenceLoadState)
    val captureRatio = intrinsicRatioOf(captureLoadState)

    val referenceFailed = hasFailed(referenceLoadState, referenceRatio)
    val captureFailed = hasFailed(captureLoadState, captureRatio)

    if (referenceFailed || captureFailed) {
        WackelbildPreviewFallback(availableContentHeight = availableContentHeight, modifier = modifier)
        return
    }

    if (referenceRatio == null || captureRatio == null) {
        // Still loading local files — near-instant; no dedicated loading UI needed.
        return
    }

    val currentOnSwipeDetected by rememberUpdatedState(onSwipeDetected)
    val currentOnAccessibilityToggle by rememberUpdatedState(onAccessibilityToggle)
    val referenceLabel = stringResource(R.string.compare_label_reference)
    val captureLabel = stringResource(R.string.compare_label_capture)
    val toggleActionLabel = stringResource(R.string.wackelbild_accessibility_toggle_action)

    // The badge text for whichever image is currently visible — null when the overlay is off or
    // that side's date is defensively unavailable (e.g. a missing Capture timestamp never
    // invents a badge; Reference stays valid and switching back to it still works).
    val visibleBadgeText = if (dateOverlayEnabled) {
        if (visibleImage == WackelbildImageSide.REFERENCE) referenceDateBadgeText else captureDateBadgeText
    } else {
        null
    }

    BoxWithConstraints(modifier = modifier) {
        val availableW = maxWidth
        // Secondary ceiling only (see PREVIEW_HEIGHT_FRACTION_OF_CONTENT's own doc comment) —
        // `maxHeight` below (this BoxWithConstraints' own incoming height constraint) is the
        // primary bound, and already reflects the space remaining after the lower controls stack
        // claimed its natural height, via the `Modifier.weight(1f, fill = false)` applied at this
        // composable's call site.
        val maxPreviewHeight = availableContentHeight * PREVIEW_HEIGHT_FRACTION_OF_CONTENT
        val heightFromWidth = availableW / referenceRatio
        val effectiveHeight = heightFromWidth.coerceAtMost(maxHeight).coerceAtMost(maxPreviewHeight)
        // Recomputed from the (possibly capped) height, never left at the full availableW —
        // this is what guarantees the box below always matches the image's own aspect ratio,
        // whether or not the height cap engaged, so the badge anchored inside it never lands in
        // unused letterbox space.
        val effectiveWidth = effectiveHeight * referenceRatio

        // Shared shape for both the outer border and the inner content clip (spec §8.11) --
        // reusing one value avoids double-rounding drift between the two.
        val previewShape = RoundedCornerShape(PREVIEW_CORNER_RADIUS)

        // This outer BoxWithConstraints is forced to the full lane width by the caller's
        // `fillMaxWidth()` (so there is comfortable centered space around a narrower/capped
        // preview) — its own bounds are therefore NOT the image bounds. `testTag` sits on this
        // inner box instead, which is sized at exactly effectiveWidth x effectiveHeight and is
        // the actual image-bounds container that the badge (below) is anchored inside of.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(effectiveWidth)
                .height(effectiveHeight)
                // Subtle preview-only perspective tilt (spec §8.9), driven by the same
                // continuous blend fraction as the image dominance below -- purely a visual
                // transform on this Composable tree, never touches layout size/source geometry,
                // so it cannot affect the upload/print pipeline (§16, §17).
                .graphicsLayer {
                    rotationY = (previewBlendFraction - 0.5f) * 2f * PERSPECTIVE_MAX_ROTATION_DEGREES
                    cameraDistance = PERSPECTIVE_CAMERA_DISTANCE_FACTOR * density
                }
                // Subtle neutral outer edge (spec §8.11), drawn as part of this same
                // graphicsLayer-transformed node so it rotates/scales with the preview rather than
                // staying fixed to the screen. Drawn within the existing bounds (no added padding).
                .border(PREVIEW_BORDER_WIDTH, PREVIEW_BORDER_COLOR, previewShape)
                .testTag("wackelbild_reference_preview_container")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(previewShape)
                    .pointerInput(Unit) {
                        val thresholdPx = SWIPE_THRESHOLD_DP.dp.toPx()
                        var totalDx = 0f
                        var totalDy = 0f
                        var fired = false
                        detectDragGestures(
                            onDragStart = {
                                totalDx = 0f
                                totalDy = 0f
                                fired = false
                            },
                            onDrag = { change, dragAmount ->
                                totalDx += dragAmount.x
                                totalDy += dragAmount.y
                                // Only ever consume/act once the gesture is clearly horizontal —
                                // an ambiguous or clearly-vertical gesture is left unconsumed.
                                val isHorizontalIntent = abs(totalDx) > abs(totalDy)
                                if (isHorizontalIntent) {
                                    if (!fired && abs(totalDx) > thresholdPx) {
                                        fired = true
                                        currentOnSwipeDetected()
                                    }
                                    change.consume()
                                }
                            }
                        )
                    }
                    // Not merged: the child Image already sets contentDescription = null (no
                    // meaningful child semantics to fold in), and merging here would hide the
                    // Image's own testTag from onNodeWithTag's default merged-tree queries.
                    .semantics {
                        val imageLabel =
                            if (visibleImage == WackelbildImageSide.REFERENCE) referenceLabel else captureLabel
                        contentDescription = if (visibleBadgeText != null) {
                            "$imageLabel, $visibleBadgeText"
                        } else {
                            imageLabel
                        }
                        customActions = listOf(
                            CustomAccessibilityAction(toggleActionLabel) {
                                currentOnAccessibilityToggle()
                                true
                            }
                        )
                        // Test-observability only -- never read by TalkBack (see
                        // PreviewBlendFractionKey's own doc comment). The TalkBack-facing
                        // contentDescription/customActions above remain driven solely by the
                        // discrete visibleImage, per spec §8.7.
                        set(PreviewBlendFractionKey, previewBlendFraction)
                    }
                    .testTag("wackelbild_preview_interactive_area"),
                contentAlignment = Alignment.Center
            ) {
                // Both images render simultaneously (spec §8.1) -- both painters were already
                // unconditionally loaded above; this is pure compositing (alpha), no re-decode,
                // no re-render per sensor update. previewBlendFraction: 0f = full Reference,
                // 1f = full Capture (verified sign convention, not inverted -- see
                // TiltBlendMapper's own doc comment).
                Image(
                    painter = referencePainter,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(1f - previewBlendFraction)
                        .testTag("wackelbild_reference_image")
                )
                Image(
                    painter = capturePainter,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(previewBlendFraction)
                        .testTag("wackelbild_capture_image")
                )
                // Subtle, static, preview-only vertical lenticular ridge surface (spec §8.10),
                // layered above both photographs. Never reads previewBlendFraction -- purely a
                // stateless draw, independent of tilt.
                WackelbildLenticularRidgeOverlay(modifier = Modifier.fillMaxSize())
                // A separate wrapping Box for the 8dp image-edge margin, kept structurally apart
                // from WackelbildDateBadge's own styling modifiers — chaining both the outer
                // margin and the inner clip/background/padding onto one node made the badge's
                // own testTag bounds double-count both paddings. This wrapper's outer edge is
                // flush with this Box (the actual image bounds), so the badge inside it sits
                // exactly 8dp from the real image edge, not unused letterbox space.
                if (visibleBadgeText != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = DATE_BADGE_IMAGE_EDGE_MARGIN,
                                bottom = DATE_BADGE_IMAGE_EDGE_MARGIN
                            )
                    ) {
                        WackelbildDateBadge(text = visibleBadgeText)
                    }
                }
            }
        }
    }
}

/**
 * Subtle, static, vertical lenticular ridge surface (spec §8.10) -- purely a preview-rendering
 * layer, stateless and independent of tilt/blend. Never opens a `File`/`Bitmap` handle, so it
 * structurally cannot reach `WackelbildPrintRenderer` or any persisted/transfer image (spec §16,
 * §21) -- this is a plain Compose draw on the on-screen tree only. `testTag` exists only so
 * instrumentation tests can assert its structural presence.
 */
@Composable
private fun WackelbildLenticularRidgeOverlay(modifier: Modifier = Modifier) {
    val spacingPx = with(LocalDensity.current) { RIDGE_LINE_SPACING.toPx() }
    Box(
        modifier = modifier
            .testTag("wackelbild_ridge_overlay")
            .drawBehind {
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = RIDGE_LINE_ALPHA),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += spacingPx
                }
            }
    )
}

/**
 * Fixed-geometry runtime date badge (Block 4 preview only — never rendered into a persisted or
 * transfer JPEG). Geometry/style is approved as-is, not implementation-time tunable: 6.dp corner
 * radius, 8.dp horizontal / 4.dp vertical internal padding, `labelMedium` typography,
 * [SameViewAppSurface] background, white text, no shadow, no border, no pill shape.
 */
@Composable
private fun WackelbildDateBadge(text: String) {
    Box(
        // testTag first: a testTag chained after a padding() modifier on the same node reports
        // bounds that exclude that padding (proven empirically — the badge's measured bounds
        // were inset by an extra 8dp, matching the horizontal padding below, until this was
        // reordered). Placed first, its bounds correctly cover the full clipped/background box.
        modifier = Modifier
            .testTag("wackelbild_date_badge")
            .clip(RoundedCornerShape(DATE_BADGE_CORNER_RADIUS))
            .background(SameViewAppSurface)
            .padding(
                horizontal = DATE_BADGE_HORIZONTAL_PADDING,
                vertical = DATE_BADGE_VERTICAL_PADDING
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

private fun intrinsicRatioOf(state: AsyncImagePainter.State): Float? {
    val success = state as? AsyncImagePainter.State.Success ?: return null
    val w = success.result.drawable.intrinsicWidth
    val h = success.result.drawable.intrinsicHeight
    return if (w > 0 && h > 0) w.toFloat() / h.toFloat() else null
}

private fun hasFailed(state: AsyncImagePainter.State, intrinsicRatio: Float?): Boolean =
    state is AsyncImagePainter.State.Error ||
        (state is AsyncImagePainter.State.Success && intrinsicRatio == null)

@Composable
private fun WackelbildPreviewFallback(availableContentHeight: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(availableContentHeight * PREVIEW_HEIGHT_FRACTION_OF_CONTENT)
            .testTag("wackelbild_preview_fallback"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.wackelbild_preview_error_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.wackelbild_preview_error_body),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Date-overlay toggle row, placed directly below the preview and above the interaction hint, no
 * card/Options heading — per spec §9.9. `SettingsSwitchRow` has no `supportingText` parameter, so
 * this mirrors `ShareComparisonScreen`'s local `InfoToggleRow` shape (a plain `Column` with the
 * switch row followed by a supporting `Text` shown only while unavailable) rather than modifying
 * that shared component.
 */
@Composable
private fun WackelbildDateToggleRow(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingsSwitchRow(
            label = stringResource(R.string.wackelbild_date_toggle_label),
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            testTag = "wackelbild_date_toggle"
        )
        if (!enabled) {
            Text(
                text = stringResource(R.string.wackelbild_date_unavailable_hint),
                style = MaterialTheme.typography.bodySmall,
                color = SameViewSettingsSecondaryText,
                modifier = Modifier.testTag("wackelbild_date_unavailable_hint")
            )
        }
    }
}

/** Tilt hint when a suitable sensor exists, swipe hint otherwise. Same supporting text either way. */
@Composable
private fun WackelbildInteractionHint(isSensorAvailable: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(
                if (isSensorAvailable) {
                    R.string.wackelbild_hint_tilt_title
                } else {
                    R.string.wackelbild_hint_swipe_title
                }
            ),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.testTag("wackelbild_hint_title")
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.wackelbild_hint_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("wackelbild_hint_subtitle")
        )
    }
}
