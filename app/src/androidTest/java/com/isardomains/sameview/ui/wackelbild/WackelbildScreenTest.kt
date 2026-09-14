package com.isardomains.sameview.ui.wackelbild

import android.graphics.Bitmap
import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.isardomains.sameview.R
import com.isardomains.sameview.ui.theme.SameViewTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Block 3/4 instrumentation coverage for [WackelbildScreenContent].
 *
 * Exercises the exact production composable directly (no Hilt/ViewModel involved — the
 * `internal` [WackelbildScreenContent] takes files/state/callbacks directly, matching the split
 * already used elsewhere in this codebase to keep screen content testable without a Hilt
 * harness) so there is no risk of a test-only stub drifting from the real screen.
 *
 * Date-badge text is passed into [launch] pre-formatted, exactly as the real
 * [WackelbildViewModel]/[DateBadgeFormatter] would already have produced it — formatting
 * precision/locale correctness is covered by [DateBadgeFormatterTest] and
 * [WackelbildViewModelTest]; these tests confirm the screen displays whatever it is given
 * unmodified and wires the toggle/switching behavior correctly.
 */
@RunWith(AndroidJUnit4::class)
class WackelbildScreenTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private var scenario: ActivityScenario<ComponentActivity>? = null
    private val tempFiles = mutableListOf<File>()

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
        tempFiles.forEach { it.delete() }
        tempFiles.clear()
    }

    private fun wakeDevice() {
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("input keyevent KEYCODE_WAKEUP")
    }

    /** Mutable UI state a real ViewModel would own, standing in for it in these content-level tests. */
    private class WackelbildTestState(
        val visibleImage: MutableState<WackelbildImageSide>,
        val previewBlendFraction: MutableState<Float>,
        val dateOverlayEnabled: MutableState<Boolean>,
        val operationState: MutableState<WackelbildOperationState>,
        val customTabOpenFailure: MutableState<String?>,
        val startOperationCallCount: MutableState<Int>,
        val confirmFallbackCallCount: MutableState<Int>,
        val cancelOperationCallCount: MutableState<Int>,
        val retryOpenCallCount: MutableState<Int>,
        val launchedUrls: List<String>,
        val launchResultReports: List<Pair<String, Boolean>>,
        val emitLaunchEvent: (String) -> Unit
    )

    /**
     * Launches [WackelbildScreenContent] with a small stateful wrapper standing in for the real
     * ViewModel's `visibleImage`/`dateOverlayEnabled` StateFlows —
     * [onSwipeDetected]/[onAccessibilityToggle] toggle `visibleImage` exactly like
     * [WackelbildViewModel.onSwipeDetected]/[WackelbildViewModel.onAccessibilityToggle] do, and
     * the date toggle callback updates `dateOverlayEnabled` directly (the "don't enable while
     * unavailable" rule is `SettingsSwitchRow`'s own `enabled`-gated `clickable`, already
     * exercised for real here — and is separately unit-tested at the ViewModel level). Lifecycle
     * callbacks are pass-through so tests can assert on invocation directly.
     *
     * `onLaunchCustomTab` is replaced by a fake recording lambda controlled by
     * [launchCustomTabResult] -- no real Custom Tab/browser is ever launched by these tests.
     * [WackelbildTestState.emitLaunchEvent] lets a test simulate the ViewModel reaching `Ready`
     * and requesting a launch, without needing a real orchestrator/API client.
     */
    private fun launch(
        referenceFile: File,
        captureFile: File,
        isSensorAvailable: Boolean = true,
        isDateOverlayAvailable: Boolean = false,
        referenceDateBadgeText: String? = null,
        captureDateBadgeText: String? = null,
        initialOperationState: WackelbildOperationState = WackelbildOperationState.Idle,
        initialCustomTabOpenFailure: String? = null,
        launchCustomTabResult: Boolean = true,
        onBack: () -> Unit = {},
        onScreenActive: () -> Unit = {},
        onScreenInactive: () -> Unit = {},
        onScreenLeft: () -> Unit = {},
        windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
    ): WackelbildTestState {
        // A screen-off/locked device leaves the Activity below RESUMED, which in turn defers
        // Coil's lifecycle-aware image request — see the established pattern in
        // ShareComparisonScreenTest/CompareScreenTest.
        wakeDevice()
        scenario = ActivityScenario.launch(ComponentActivity::class.java)
        lateinit var testState: WackelbildTestState
        scenario?.onActivity { activity ->
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                activity.setShowWhenLocked(true)
                activity.setTurnScreenOn(true)
            }
            activity.setContent {
                SameViewTheme {
                    val visibleImage = remember { mutableStateOf(WackelbildImageSide.REFERENCE) }
                    // Mirrors WackelbildViewModel.manualToggle()'s seed-and-freeze pin: a manual
                    // selection always pins the fraction to the matching full endpoint. Default
                    // 0f pairs with the REFERENCE default above (not the real ViewModel's 0.5f
                    // neutral default -- this stateless-content harness represents a manually
                    // controlled discrete pick, not live sensor calibration).
                    val previewBlendFraction = remember { mutableStateOf(0f) }
                    val dateOverlayEnabled = remember { mutableStateOf(false) }
                    val operationState = remember { mutableStateOf(initialOperationState) }
                    val customTabOpenFailure = remember { mutableStateOf(initialCustomTabOpenFailure) }
                    val startOperationCallCount = remember { mutableStateOf(0) }
                    val confirmFallbackCallCount = remember { mutableStateOf(0) }
                    val cancelOperationCallCount = remember { mutableStateOf(0) }
                    val retryOpenCallCount = remember { mutableStateOf(0) }
                    val launchedUrls = remember { mutableStateListOf<String>() }
                    val launchResultReports = remember { mutableStateListOf<Pair<String, Boolean>>() }
                    val launchEvent = remember { MutableSharedFlow<String>(extraBufferCapacity = 8) }
                    val scope = rememberCoroutineScope()

                    testState = WackelbildTestState(
                        visibleImage = visibleImage,
                        previewBlendFraction = previewBlendFraction,
                        dateOverlayEnabled = dateOverlayEnabled,
                        operationState = operationState,
                        customTabOpenFailure = customTabOpenFailure,
                        startOperationCallCount = startOperationCallCount,
                        confirmFallbackCallCount = confirmFallbackCallCount,
                        cancelOperationCallCount = cancelOperationCallCount,
                        retryOpenCallCount = retryOpenCallCount,
                        launchedUrls = launchedUrls,
                        launchResultReports = launchResultReports,
                        emitLaunchEvent = { url -> scope.launch { launchEvent.emit(url) } }
                    )

                    WackelbildScreenContent(
                        referenceFile = referenceFile,
                        captureFile = captureFile,
                        visibleImage = visibleImage.value,
                        previewBlendFraction = previewBlendFraction.value,
                        isSensorAvailable = isSensorAvailable,
                        dateOverlayEnabled = dateOverlayEnabled.value,
                        isDateOverlayAvailable = isDateOverlayAvailable,
                        referenceDateBadgeText = referenceDateBadgeText,
                        captureDateBadgeText = captureDateBadgeText,
                        operationState = operationState.value,
                        customTabOpenFailure = customTabOpenFailure.value,
                        launchCustomTabEvent = launchEvent,
                        onDateOverlayToggled = { dateOverlayEnabled.value = it },
                        onSwipeDetected = {
                            visibleImage.value = visibleImage.value.opposite()
                            previewBlendFraction.value = if (visibleImage.value == WackelbildImageSide.REFERENCE) 0f else 1f
                        },
                        onAccessibilityToggle = {
                            visibleImage.value = visibleImage.value.opposite()
                            previewBlendFraction.value = if (visibleImage.value == WackelbildImageSide.REFERENCE) 0f else 1f
                        },
                        onScreenActive = onScreenActive,
                        onScreenInactive = onScreenInactive,
                        onScreenLeft = onScreenLeft,
                        onStartOperation = { startOperationCallCount.value++ },
                        onConfirmFallback = { confirmFallbackCallCount.value++ },
                        onCancelOperation = {
                            cancelOperationCallCount.value++
                            operationState.value = WackelbildOperationState.Idle
                        },
                        onCustomTabLaunchResult = { url, success -> launchResultReports.add(url to success) },
                        onRetryOpenCheckoutUrl = {
                            retryOpenCallCount.value++
                            launchedUrls.lastOrNull()?.let { url -> scope.launch { launchEvent.emit(url) } }
                        },
                        onBack = onBack,
                        windowWidthSizeClass = windowWidthSizeClass,
                        onLaunchCustomTab = { _, url ->
                            launchedUrls.add(url)
                            launchCustomTabResult
                        }
                    )
                }
            }
        }
        composeRule.waitForIdle()
        waitForPreviewResolved()
        return testState
    }

    private fun WackelbildImageSide.opposite(): WackelbildImageSide =
        if (this == WackelbildImageSide.REFERENCE) WackelbildImageSide.CAPTURE else WackelbildImageSide.REFERENCE

    /**
     * Coil decodes both files asynchronously. Poll (with real, non-Compose-clock delays) until
     * the preview has settled into either its success or fallback state before assertions run,
     * bounded well above the near-instant local-file decode time.
     */
    private fun waitForPreviewResolved() {
        val deadline = System.currentTimeMillis() + 5000
        while (System.currentTimeMillis() < deadline) {
            composeRule.waitForIdle()
            val resolved = composeRule
                .onAllNodesWithTag("wackelbild_reference_image")
                .fetchSemanticsNodes().isNotEmpty() ||
                composeRule
                    .onAllNodesWithTag("wackelbild_capture_image")
                    .fetchSemanticsNodes().isNotEmpty() ||
                composeRule
                    .onAllNodesWithTag("wackelbild_preview_fallback")
                    .fetchSemanticsNodes().isNotEmpty()
            if (resolved) return
            Thread.sleep(100)
        }
        composeRule.waitForIdle()
    }

    private fun createJpeg(width: Int, height: Int): File {
        val file = File.createTempFile("wackelbild_img", ".jpg", context.cacheDir)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.DKGRAY)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        bitmap.recycle()
        tempFiles.add(file)
        return file
    }

    private fun createCorruptFile(): File {
        val file = File.createTempFile("wackelbild_img_corrupt", ".jpg", context.cacheDir)
        file.writeBytes("not a real image".toByteArray())
        tempFiles.add(file)
        return file
    }

    private fun missingFile(): File =
        File(context.cacheDir, "wackelbild_does_not_exist_${System.nanoTime()}.jpg")

    private fun validReference(): File = createJpeg(320, 400)
    private fun validCapture(): File = createJpeg(320, 400)

    /** Large enough (9:16-ish) that the pre-fix flat 500dp cap would have engaged. */
    private fun tallPortraitImage(): File = createJpeg(1080, 1920)

    private fun assertBadgeInsideImage(imageTag: String, toleranceDp: Float = 2f) {
        val imageBounds = composeRule.onNodeWithTag(imageTag).getUnclippedBoundsInRoot()
        val badgeBounds = composeRule.onNodeWithTag("wackelbild_date_badge").getUnclippedBoundsInRoot()

        assertTrue(
            "badge left (${badgeBounds.left.value}) must be >= image left (${imageBounds.left.value})",
            badgeBounds.left.value >= imageBounds.left.value - toleranceDp
        )
        assertTrue(
            "badge top (${badgeBounds.top.value}) must be >= image top (${imageBounds.top.value})",
            badgeBounds.top.value >= imageBounds.top.value - toleranceDp
        )
        assertTrue(
            "badge right (${badgeBounds.right.value}) must be <= image right (${imageBounds.right.value})",
            badgeBounds.right.value <= imageBounds.right.value + toleranceDp
        )
        assertTrue(
            "badge bottom (${badgeBounds.bottom.value}) must be <= image bottom (${imageBounds.bottom.value})",
            badgeBounds.bottom.value <= imageBounds.bottom.value + toleranceDp
        )
    }

    private fun assertBadgeEdgeSpacing(imageTag: String, expectedMarginDp: Float = 8f, toleranceDp: Float = 2f) {
        val imageBounds = composeRule.onNodeWithTag(imageTag).getUnclippedBoundsInRoot()
        val badgeBounds = composeRule.onNodeWithTag("wackelbild_date_badge").getUnclippedBoundsInRoot()
        val rightSpacing = imageBounds.right.value - badgeBounds.right.value
        val bottomSpacing = imageBounds.bottom.value - badgeBounds.bottom.value
        assertEquals(expectedMarginDp, rightSpacing, toleranceDp)
        assertEquals(expectedMarginDp, bottomSpacing, toleranceDp)
    }

    private fun SemanticsNodeInteraction.performCustomAccessibilityAction(label: String) {
        val node = fetchSemanticsNode()
        val actions = node.config[SemanticsActions.CustomActions]
        val match = actions.firstOrNull { it.label == label }
            ?: error("Custom accessibility action '$label' not found")
        match.action?.invoke()
    }

    // --- Title ---

    @Test
    fun screenTitle_isDisplayed() {
        launch(referenceFile = validReference(), captureFile = validCapture())
        val expectedTitle = context.getString(R.string.wackelbild_screen_title)
        composeRule.onNodeWithText(expectedTitle).assertIsDisplayed()
    }

    // --- Back ---

    @Test
    fun backButton_invokesCallback() {
        var backInvoked = false
        launch(referenceFile = validReference(), captureFile = validCapture(), onBack = { backInvoked = true })
        composeRule.onNodeWithTag("wackelbild_back_button").performClick()
        composeRule.waitForIdle()
        assertTrue("Back callback should be invoked", backInvoked)
    }

    // --- Initial visible image ---

    @Test
    fun initialVisibleImage_isReference() {
        // Both image nodes now always coexist in the composition (spec §8.1) -- dominance is
        // expressed via alpha/the blend fraction, not via one node's presence/absence.
        launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_reference_image").assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_capture_image").assertIsDisplayed()
        assertEquals(
            0f,
            composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
                .fetchSemanticsNode().config[PreviewBlendFractionKey],
            0.0001f
        )
    }

    // --- Valid Capture displayed after toggle ---

    @Test
    fun validCapture_displaysAfterToggle() {
        val state = launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
            .performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        assertEquals(WackelbildImageSide.CAPTURE, state.visibleImage.value)
        // Both image nodes coexist (spec §8.1); the manual endpoint pins full Capture dominance.
        composeRule.onNodeWithTag("wackelbild_capture_image").assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_reference_image").assertIsDisplayed()
        assertEquals(
            1f,
            composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
                .fetchSemanticsNode().config[PreviewBlendFractionKey],
            0.0001f
        )
    }

    // --- Continuous blend / ridge overlay / perspective (§7.7 revision) ---

    @Test
    fun ridgeOverlay_isPresentInComposition() {
        launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_ridge_overlay").assertIsDisplayed()
    }

    @Test
    fun previewContainer_remainsDisplayed_atFullReferenceEndpoint() {
        launch(referenceFile = validReference(), captureFile = validCapture())
        // Default harness state is already the full-Reference endpoint (0f) -- the perspective
        // graphicsLayer transform at this extreme must not crash or hide the container. Exact
        // rotated/clipped geometry is not asserted here (Compose's bounds APIs report pre-
        // transform layout bounds, not the painted, rotated appearance) -- real-device visual
        // validation remains required for clipping/subtlety per the implementation plan.
        composeRule.onNodeWithTag("wackelbild_reference_preview_container").assertIsDisplayed()
    }

    @Test
    fun previewContainer_remainsDisplayed_atFullCaptureEndpoint() {
        launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
            .performTouchInput { swipeLeft() } // -> full Capture endpoint (1f)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_reference_preview_container").assertIsDisplayed()
    }

    // --- Missing/corrupt Capture -> same unified fallback ---

    @Test
    fun missingCapture_showsFallback_noInteractionUi() {
        launch(referenceFile = validReference(), captureFile = missingFile())
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_preview_fallback").assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_reference_image").assertDoesNotExist()
        composeRule.onNodeWithTag("wackelbild_preview_interactive_area").assertDoesNotExist()
    }

    @Test
    fun corruptCapture_showsSameFallback() {
        launch(referenceFile = validReference(), captureFile = createCorruptFile())
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_preview_fallback").assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_reference_image").assertDoesNotExist()
    }

    @Test
    fun missingReferenceFile_showsFallback() {
        launch(referenceFile = missingFile(), captureFile = validCapture())
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_preview_fallback").assertIsDisplayed()
        val expectedTitle = context.getString(R.string.wackelbild_preview_error_title)
        composeRule.onNodeWithText(expectedTitle).assertIsDisplayed()
    }

    @Test
    fun corruptReferenceFile_showsSameFallback() {
        launch(referenceFile = createCorruptFile(), captureFile = validCapture())
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_preview_fallback").assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_reference_image").assertDoesNotExist()
    }

    @Test
    fun fallbackState_backButtonStillAvailable() {
        var backInvoked = false
        launch(referenceFile = missingFile(), captureFile = validCapture(), onBack = { backInvoked = true })
        composeRule.onNodeWithTag("wackelbild_back_button").performClick()
        composeRule.waitForIdle()
        assertTrue("Back callback should still work in the fallback state", backInvoked)
    }

    // --- Swipe toggles ---

    @Test
    fun horizontalSwipe_left_togglesOnce() {
        val state = launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
            .performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        assertEquals(WackelbildImageSide.CAPTURE, state.visibleImage.value)
    }

    @Test
    fun horizontalSwipe_reverseDirection_alsoTogglesOnce() {
        val state = launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
            .performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        assertEquals(WackelbildImageSide.CAPTURE, state.visibleImage.value)
    }

    @Test
    fun verticalGesture_doesNotToggle() {
        val state = launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
            .performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        assertEquals(WackelbildImageSide.REFERENCE, state.visibleImage.value)
    }

    // --- Sensor-availability hint text ---

    @Test
    fun sensorAvailable_showsTiltHint() {
        launch(referenceFile = validReference(), captureFile = validCapture(), isSensorAvailable = true)
        val tiltTitle = context.getString(R.string.wackelbild_hint_tilt_title)
        composeRule.onNodeWithText(tiltTitle).assertIsDisplayed()
    }

    @Test
    fun sensorUnavailable_showsSwipeHint() {
        launch(referenceFile = validReference(), captureFile = validCapture(), isSensorAvailable = false)
        val swipeTitle = context.getString(R.string.wackelbild_hint_swipe_title)
        composeRule.onNodeWithText(swipeTitle).assertIsDisplayed()
    }

    // --- Accessibility action ---

    @Test
    fun accessibilityAction_togglesImage() {
        val state = launch(referenceFile = validReference(), captureFile = validCapture())
        val toggleLabel = context.getString(R.string.wackelbild_accessibility_toggle_action)
        composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
            .performCustomAccessibilityAction(toggleLabel)
        composeRule.waitForIdle()
        assertEquals(WackelbildImageSide.CAPTURE, state.visibleImage.value)
    }

    // --- Lifecycle callbacks ---

    @Test
    fun onResume_invokesOnScreenActive() {
        var activeCount = 0
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            onScreenActive = { activeCount++ }
        )
        // ActivityScenario.launch() already drives the Activity to RESUMED, which is what
        // triggers the ON_RESUME lifecycle event this screen observes.
        assertTrue("onScreenActive should be invoked at least once on resume", activeCount >= 1)
    }

    @Test
    fun moveToCreated_invokesOnScreenInactive() {
        var inactiveCount = 0
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            onScreenInactive = { inactiveCount++ }
        )
        scenario?.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        assertTrue("onScreenInactive should be invoked on pause", inactiveCount >= 1)
    }

    @Test
    fun closingScenario_invokesOnScreenLeft() {
        var leftCount = 0
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            onScreenLeft = { leftCount++ }
        )
        scenario?.close()
        scenario = null
        assertEquals(1, leftCount)
    }

    // --- Existing Block 2 aspect-ratio / no-crop coverage, still green ---

    @Test
    fun portraitReference_previewContainerMatchesIntrinsicRatio() {
        // 320x400 => ratio 0.8, moderate enough that the 62%-of-content-area height cap is
        // unlikely to engage on standard test-device widths, so the assertion below reflects
        // the width-driven (uncapped) case; the cap-engaged case is covered separately by
        // preview_tallPortraitImage_heightDoesNotExceed62PercentOfAvailableContent below, and
        // ratio preservation under the cap is proven there via effectiveWidth = effectiveHeight
        // * ratio regardless of which branch engages.
        launch(referenceFile = createJpeg(320, 400), captureFile = createJpeg(320, 400))
        composeRule.waitForIdle()

        val bounds = composeRule.onNodeWithTag("wackelbild_reference_preview_container")
            .getUnclippedBoundsInRoot()
        val widthDp = (bounds.right - bounds.left).value
        val heightDp = (bounds.bottom - bounds.top).value
        val actualRatio = widthDp / heightDp
        assertEquals(0.8f, actualRatio, 0.05f)
        assertTrue("Portrait preview must be taller than wide", heightDp > widthDp)
    }

    @Test
    fun landscapeReference_previewContainerMatchesIntrinsicRatio() {
        launch(referenceFile = createJpeg(400, 300), captureFile = createJpeg(400, 300))
        composeRule.waitForIdle()

        val bounds = composeRule.onNodeWithTag("wackelbild_reference_preview_container")
            .getUnclippedBoundsInRoot()
        val widthDp = (bounds.right - bounds.left).value
        val heightDp = (bounds.bottom - bounds.top).value
        val actualRatio = widthDp / heightDp
        assertEquals(400f / 300f, actualRatio, 0.05f)
        assertTrue("Landscape preview must be wider than tall", widthDp > heightDp)
    }

    @Test
    fun referenceImage_fillsPreviewContainer_noAdditionalCrop() {
        launch(referenceFile = createJpeg(400, 300), captureFile = createJpeg(400, 300))
        composeRule.waitForIdle()

        val containerBounds = composeRule.onNodeWithTag("wackelbild_reference_preview_container")
            .getUnclippedBoundsInRoot()
        val imageBounds = composeRule.onNodeWithTag("wackelbild_reference_image")
            .getUnclippedBoundsInRoot()

        val containerW = (containerBounds.right - containerBounds.left).value
        val containerH = (containerBounds.bottom - containerBounds.top).value
        val imageW = (imageBounds.right - imageBounds.left).value
        val imageH = (imageBounds.bottom - imageBounds.top).value
        assertEquals(containerW, imageW, 1f)
        assertEquals(containerH, imageH, 1f)
    }

    // --- Date toggle (Block 4) ---

    @Test
    fun dateToggle_isVisible() {
        launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_date_toggle").assertIsDisplayed()
    }

    @Test
    fun dateToggle_defaultsOff() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008"
        )
        // ToggleableState lives on SettingsSwitchRow's inner Switch, which is its own semantics
        // merge boundary -- the row's own (merged) node does not carry it.
        composeRule.onNodeWithTag("wackelbild_date_toggle").onChild().assertIsOff()
    }

    @Test
    fun dateToggle_withUsableReferenceDate_isEnabled() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008"
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").assertIsEnabled()
    }

    @Test
    fun dateToggle_withoutReferenceDate_isDisabled() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = false
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").assertIsNotEnabled()
    }

    @Test
    fun dateToggle_disabled_showsSupportingHintText() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = false
        )
        val expectedHint = context.getString(R.string.wackelbild_date_unavailable_hint)
        composeRule.onNodeWithTag("wackelbild_date_unavailable_hint").assertIsDisplayed()
        composeRule.onNodeWithText(expectedHint).assertIsDisplayed()
    }

    @Test
    fun dateBadge_overlayOff_noBadgeShown() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008"
        )
        composeRule.onNodeWithTag("wackelbild_date_badge").assertDoesNotExist()
    }

    @Test
    fun dateBadge_overlayOn_referenceVisible_showsReferenceBadge() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008"
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_date_badge").assertIsDisplayed()
        composeRule.onNodeWithText("2008").assertIsDisplayed()
    }

    @Test
    fun dateBadge_switchToCapture_updatesImmediately() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008",
            captureDateBadgeText = "3 Aug 2026"
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
            .performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("3 Aug 2026").assertIsDisplayed()
        composeRule.onNodeWithText("2008").assertDoesNotExist()
    }

    @Test
    fun dateBadge_switchBackToReference_referenceBadgeReturns() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008",
            captureDateBadgeText = "3 Aug 2026"
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").performClick()
        composeRule.waitForIdle()
        val interactiveArea = composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
        interactiveArea.performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        interactiveArea.performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("2008").assertIsDisplayed()
        composeRule.onNodeWithText("3 Aug 2026").assertDoesNotExist()
    }

    @Test
    fun dateBadge_referenceYearOnly_displaysOnlyYear() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008"
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("2008").assertIsDisplayed()
    }

    @Test
    fun dateBadge_referenceYearMonth_doesNotInventADay() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "Jun 2008"
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").performClick()
        composeRule.waitForIdle()
        // Exactly the given year-month text, displayed verbatim -- no day number appended.
        composeRule.onNodeWithText("Jun 2008").assertIsDisplayed()
    }

    @Test
    fun dateBadge_missingCaptureTimestamp_toggleStaysEnabled_noInventedCaptureBadge() {
        val state = launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008",
            captureDateBadgeText = null
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").assertIsEnabled()
        composeRule.onNodeWithTag("wackelbild_date_toggle").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("2008").assertIsDisplayed()

        composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
            .performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        assertEquals(WackelbildImageSide.CAPTURE, state.visibleImage.value)
        composeRule.onNodeWithTag("wackelbild_capture_image").assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_date_badge").assertDoesNotExist()
    }

    @Test
    fun dateToggleRow_present_swipeStillTogglesImage() {
        val state = launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008"
        )
        composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
            .performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        assertEquals(WackelbildImageSide.CAPTURE, state.visibleImage.value)
    }

    // --- Preview size (Block 4C/4D layout fix) ---

    @Test
    fun preview_tallPortraitImage_heightDoesNotExceed62PercentOfAvailableContent() {
        launch(referenceFile = tallPortraitImage(), captureFile = tallPortraitImage())
        composeRule.waitForIdle()

        val contentBounds = composeRule.onNodeWithTag("wackelbild_content_area").getUnclippedBoundsInRoot()
        val previewBounds = composeRule.onNodeWithTag("wackelbild_reference_preview_container")
            .getUnclippedBoundsInRoot()
        val contentHeightDp = (contentBounds.bottom - contentBounds.top).value
        val previewHeightDp = (previewBounds.bottom - previewBounds.top).value
        val maxAllowedDp = contentHeightDp * 0.62f

        assertTrue(
            "Preview height ($previewHeightDp dp) must not exceed 62% of available content " +
                "height ($contentHeightDp dp -> max $maxAllowedDp dp)",
            previewHeightDp <= maxAllowedDp + 1f
        )
        assertTrue("Preview must remain non-zero/visible", previewHeightDp > 0f)
    }

    @Test
    fun dateToggleRow_isDisplayedWithoutScrolling_forTallPortraitImage() {
        launch(
            referenceFile = tallPortraitImage(),
            captureFile = tallPortraitImage(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008"
        )
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_date_toggle").assertIsDisplayed()
    }

    @Test
    fun interactionHint_isDisplayedWithoutScrolling_forTallPortraitImage() {
        launch(referenceFile = tallPortraitImage(), captureFile = tallPortraitImage())
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_hint_title").assertIsDisplayed()
    }

    // --- CTA fits without routine scrolling (remaining-height-aware preview sizing) ---

    @Test
    fun cta_isDisplayedWithoutScrolling_forTallPortraitImage_noReferenceDate() {
        // Worst-case lower stack: no reference date means the extra
        // "Add a reference date to show the date." helper line is present, in addition to the
        // tallest/most constraining preview case.
        launch(referenceFile = tallPortraitImage(), captureFile = tallPortraitImage())
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_cta_button").assertIsDisplayed()
    }

    @Test
    fun cta_isDisplayedWithoutScrolling_forTallPortraitImage_withReferenceDate() {
        launch(
            referenceFile = tallPortraitImage(),
            captureFile = tallPortraitImage(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008"
        )
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_cta_button").assertIsDisplayed()
    }

    @Test
    fun cta_isDisplayedWithoutScrolling_forLandscapeSourceImage() {
        launch(referenceFile = createJpeg(400, 300), captureFile = createJpeg(400, 300))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_cta_button").assertIsDisplayed()
    }

    // --- Preview -> interaction-hint -> date-group spacing (hint now sits above the date group) ---

    @Test
    fun previewToHintGap_isApproximatelySixteenDp() {
        // No spacer is added here -- the gap is entirely the preview's own existing
        // Modifier.padding(16.dp) (predating this fix, unrelated to the hint), since
        // "wackelbild_reference_preview_container" is tightly sized to effectiveWidth/
        // effectiveHeight and sits inside that padding, not at its outer edge.
        launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.waitForIdle()
        val previewBottom = composeRule.onNodeWithTag("wackelbild_reference_preview_container")
            .getUnclippedBoundsInRoot().bottom.value
        val hintTop = composeRule.onNodeWithTag("wackelbild_hint_title")
            .getUnclippedBoundsInRoot().top.value
        assertEquals(16f, hintTop - previewBottom, 3f)
    }

    @Test
    fun hintToDateGroup_spacingIsApproximatelySixteenDp() {
        // The hint now sits directly below the preview and above the date-toggle row --
        // unconditionally, regardless of whether the date-unavailable helper text later renders
        // below the toggle. Only one variant is needed (unlike the old date-group -> hint
        // ordering, where the hint's neighbor above it depended on helper visibility).
        launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.waitForIdle()
        val hintBottom = composeRule.onNodeWithTag("wackelbild_hint_title")
            .getUnclippedBoundsInRoot().bottom.value
        val dateGroupTop = composeRule.onNodeWithTag("wackelbild_date_toggle")
            .getUnclippedBoundsInRoot().top.value
        assertEquals(16f, dateGroupTop - hintBottom, 3f)
    }

    // --- Date badge position (Block 4C/4D layout fix) ---

    @Test
    fun dateBadge_boundsFullyInsideImage_portrait() {
        launch(
            referenceFile = tallPortraitImage(),
            captureFile = tallPortraitImage(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008"
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").performClick()
        composeRule.waitForIdle()
        assertBadgeInsideImage("wackelbild_reference_image")
    }

    @Test
    fun dateBadge_rightBottomSpacing_isApproximatelyEightDp_portrait() {
        launch(
            referenceFile = tallPortraitImage(),
            captureFile = tallPortraitImage(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008"
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").performClick()
        composeRule.waitForIdle()
        assertBadgeEdgeSpacing("wackelbild_reference_image")
    }

    @Test
    fun dateBadge_boundsFullyInsideImage_landscape() {
        launch(
            referenceFile = createJpeg(400, 300),
            captureFile = createJpeg(400, 300),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008"
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").performClick()
        composeRule.waitForIdle()
        assertBadgeInsideImage("wackelbild_reference_image")
    }

    @Test
    fun dateBadge_rightBottomSpacing_isApproximatelyEightDp_landscape() {
        launch(
            referenceFile = createJpeg(400, 300),
            captureFile = createJpeg(400, 300),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008"
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").performClick()
        composeRule.waitForIdle()
        assertBadgeEdgeSpacing("wackelbild_reference_image")
    }

    @Test
    fun dateBadge_afterSwitchingToCapture_boundsFullyInsideImage_andSpacingHolds() {
        launch(
            referenceFile = tallPortraitImage(),
            captureFile = tallPortraitImage(),
            isDateOverlayAvailable = true,
            referenceDateBadgeText = "2008",
            captureDateBadgeText = "3 Aug 2026"
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_preview_interactive_area")
            .performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        assertBadgeInsideImage("wackelbild_capture_image")
        assertBadgeEdgeSpacing("wackelbild_capture_image")
    }

    // --- No Block 5 (order/upload/network) UI exists yet ---

    @Test
    fun noBlock5Ui_existsYet() {
        launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("wackelbild_order_button").assertDoesNotExist()
        composeRule.onNodeWithTag("wackelbild_loading_spinner").assertDoesNotExist()
    }

    // --- Responsive: Compact and Expanded remain usable ---

    @Test
    fun compactWidth_screenRendersAndReferenceIsDisplayed() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            windowWidthSizeClass = WindowWidthSizeClass.Compact
        )
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_screen_root").assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_reference_image").assertIsDisplayed()
    }

    @Test
    fun expandedWidth_screenRendersAndReferenceIsDisplayed() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            windowWidthSizeClass = WindowWidthSizeClass.Expanded
        )
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_screen_root").assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_reference_image").assertIsDisplayed()
    }

    /**
     * Verifies the Expanded width lane (680.dp `contentMaxWidth`) still composes correctly with
     * the remaining-height-aware preview sizing for the tallest (portrait-source) case. NOTE:
     * this only exercises the WIDTH lane override via [WindowWidthSizeClass.Expanded] passed
     * into the composable under test -- the instrumentation test still runs on the real/emulated
     * test device's actual window, so it does NOT validate real large-viewport HEIGHT behavior on
     * a physically large tablet (no tablet managed device exists in this repo's Gradle
     * `testOptions.managedDevices` config). That remains a manual/emulator validation item.
     */
    @Test
    fun expandedWidth_tallPortraitImage_ctaDisplayedAndScreenComposesCorrectly() {
        launch(
            referenceFile = tallPortraitImage(),
            captureFile = tallPortraitImage(),
            windowWidthSizeClass = WindowWidthSizeClass.Expanded
        )
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_screen_root").assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_reference_preview_container").assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_cta_button").assertIsDisplayed()
    }

    // === Block 11: CTA / disclosure / consent ===================================================

    @Test
    fun disclosure_isVisible() {
        launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_transfer_disclosure").assertIsDisplayed()
    }

    @Test
    fun cta_visibleInIdle_withExactText() {
        launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_cta_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_cta_order)).assertIsDisplayed()
    }

    @Test
    fun cta_tap_invokesStartOperationExactlyOnce_noConsentDialog() {
        val state = launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_cta_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(1, state.startOperationCallCount.value)
        // No separate consent dialog exists at all -- the CTA tap itself is consent (spec §10).
        composeRule.onNodeWithTag("wackelbild_fallback_dialog").assertDoesNotExist()
        composeRule.onNodeWithTag("wackelbild_cancel_transfer_dialog").assertDoesNotExist()
    }

    @Test
    fun screenOpen_doesNotStartOperation() {
        val state = launch(referenceFile = validReference(), captureFile = validCapture())
        assertEquals(0, state.startOperationCallCount.value)
    }

    @Test
    fun dateToggleAndPreviewInteraction_doNotStartOperation() {
        val state = launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").performClick()
        composeRule.onNodeWithTag("wackelbild_preview_interactive_area").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        assertEquals(0, state.startOperationCallCount.value)
    }

    // === Block 11: Busy presentation ============================================================

    @Test
    fun preparing_showsSpinnerAndCollapsedCopy_ctaAbsent() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Preparing
        )
        composeRule.onNodeWithTag("wackelbild_loading_spinner").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_loading_preparing))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_cta_button").assertDoesNotExist()
    }

    @Test
    fun creatingHandoff_showsSameCollapsedCopy() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.CreatingHandoff
        )
        composeRule.onNodeWithTag("wackelbild_loading_spinner").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_loading_preparing))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun uploadingSlotOne_showsSameCollapsedCopy_noSlotDetail() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.UploadingSlot(
                com.isardomains.sameview.net.deinwackelbild.DeinWackelbildSlot.ONE
            )
        )
        composeRule.onNodeWithTag("wackelbild_loading_spinner").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_loading_preparing))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun uploadingSlotTwo_showsSameCollapsedCopy_noSlotDetail() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.UploadingSlot(
                com.isardomains.sameview.net.deinwackelbild.DeinWackelbildSlot.TWO
            )
        )
        composeRule.onNodeWithTag("wackelbild_loading_spinner").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_loading_preparing))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun busy_dateToggleDisabled() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true,
            initialOperationState = WackelbildOperationState.Preparing
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").assertIsNotEnabled()
    }

    @Test
    fun busy_tiltSwipePreviewRemainsUsable() {
        val state = launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Preparing
        )
        composeRule.onNodeWithTag("wackelbild_preview_interactive_area").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        assertEquals(WackelbildImageSide.CAPTURE, state.visibleImage.value)
    }

    // === Block 11: Fallback confirmation ========================================================

    @Test
    fun fallbackDialog_shownOnlyWhileAwaitingFallbackConfirmation_withExactCopy() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.AwaitingFallbackConfirmation
        )
        composeRule.onNodeWithTag("wackelbild_fallback_dialog").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_quality_fallback_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_quality_fallback_message)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_quality_fallback_cancel)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_quality_fallback_continue)).assertIsDisplayed()
    }

    @Test
    fun fallbackDialog_notShownOnScreenOpen() {
        launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_fallback_dialog").assertDoesNotExist()
    }

    @Test
    fun fallbackContinue_invokesConfirmFallbackExactlyOnce() {
        val state = launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.AwaitingFallbackConfirmation
        )
        composeRule.onNodeWithTag("wackelbild_fallback_continue_button").performClick()
        composeRule.waitForIdle()
        assertEquals(1, state.confirmFallbackCallCount.value)
        assertEquals(0, state.cancelOperationCallCount.value)
    }

    @Test
    fun fallbackCancel_invokesCancelOperation() {
        val state = launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.AwaitingFallbackConfirmation
        )
        composeRule.onNodeWithTag("wackelbild_fallback_cancel_button").performClick()
        composeRule.waitForIdle()
        assertEquals(1, state.cancelOperationCallCount.value)
    }

    @Test
    fun backDuringFallback_behavesLikeCancel_noGenericDialogStacked() {
        val state = launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.AwaitingFallbackConfirmation
        )
        composeRule.onNodeWithTag("wackelbild_back_button").performClick()
        composeRule.waitForIdle()
        assertEquals(1, state.cancelOperationCallCount.value)
        composeRule.onNodeWithTag("wackelbild_cancel_transfer_dialog").assertDoesNotExist()
    }

    // === Block 11: Generic transfer cancellation ================================================

    @Test
    fun backDuringPreparing_showsGenericCancelDialog_withExactCopy() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Preparing
        )
        composeRule.onNodeWithTag("wackelbild_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_cancel_transfer_dialog").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_cancel_transfer_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_cancel_transfer_message)).assertIsDisplayed()
    }

    @Test
    fun backDuringCreatingHandoff_showsGenericCancelDialog() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.CreatingHandoff
        )
        composeRule.onNodeWithTag("wackelbild_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_cancel_transfer_dialog").assertIsDisplayed()
    }

    @Test
    fun backDuringUploadingSlot_showsGenericCancelDialog() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.UploadingSlot(
                com.isardomains.sameview.net.deinwackelbild.DeinWackelbildSlot.ONE
            )
        )
        composeRule.onNodeWithTag("wackelbild_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_cancel_transfer_dialog").assertIsDisplayed()
    }

    @Test
    fun keepTransferring_dismissesDialogOnly_noCancel() {
        val state = launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Preparing
        )
        composeRule.onNodeWithTag("wackelbild_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_cancel_transfer_continue_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_cancel_transfer_dialog").assertDoesNotExist()
        assertEquals(0, state.cancelOperationCallCount.value)
    }

    @Test
    fun cancelTransfer_invokesCancelOperation() {
        val state = launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Preparing
        )
        composeRule.onNodeWithTag("wackelbild_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("wackelbild_cancel_transfer_stop_button").performClick()
        composeRule.waitForIdle()
        assertEquals(1, state.cancelOperationCallCount.value)
    }

    @Test
    fun failedState_backNavigatesNormally_noDialog() {
        var backInvoked = false
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Failed(
                WackelbildOperationFailure(WackelbildOperationFailureCategory.PREPARATION_FAILED)
            ),
            onBack = { backInvoked = true }
        )
        composeRule.onNodeWithTag("wackelbild_back_button").performClick()
        composeRule.waitForIdle()
        assertTrue(backInvoked)
        composeRule.onNodeWithTag("wackelbild_cancel_transfer_dialog").assertDoesNotExist()
    }

    // === Block 11: Failure copy mapping ==========================================================

    @Test
    fun networkUnavailable_exactCopyAndRetryAction() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Failed(
                WackelbildOperationFailure(WackelbildOperationFailureCategory.NETWORK_UNAVAILABLE)
            )
        )
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_error_no_internet)).assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_error_retry_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_error_retry)).assertIsDisplayed()
    }

    @Test
    fun serverTemporary_exactCopyAndRetryAction() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Failed(
                WackelbildOperationFailure(WackelbildOperationFailureCategory.SERVER_TEMPORARY)
            )
        )
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_error_transfer_failed)).assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_error_retry_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun integrationUnavailable_exactCopy_noDedicatedRetryButton() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Failed(
                WackelbildOperationFailure(WackelbildOperationFailureCategory.INTEGRATION_UNAVAILABLE)
            )
        )
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_error_integration_unavailable))
            .assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_error_retry_button").assertDoesNotExist()
        // The normal CTA is what's available again -- not a dedicated retry action.
        composeRule.onNodeWithTag("wackelbild_cta_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun preparationFailed_genericCreationFailureCopy_noRetryButton() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Failed(
                WackelbildOperationFailure(WackelbildOperationFailureCategory.PREPARATION_FAILED)
            )
        )
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_error_preparation_failed)).assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_error_retry_button").assertDoesNotExist()
    }

    @Test
    fun invalidLocalOutput_sameGenericCreationFailureCopy() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Failed(
                WackelbildOperationFailure(WackelbildOperationFailureCategory.INVALID_LOCAL_OUTPUT)
            )
        )
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_error_preparation_failed)).assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_error_retry_button").assertDoesNotExist()
    }

    @Test
    fun handoffFailed_sameGenericCreationFailureCopy_noTechnicalDetail() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Failed(
                WackelbildOperationFailure(WackelbildOperationFailureCategory.HANDOFF_FAILED)
            )
        )
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_error_preparation_failed)).assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_error_retry_button").assertDoesNotExist()
        composeRule.onNodeWithText("HANDOFF_FAILED").assertDoesNotExist()
    }

    // === Block 11: Custom Tab launch / open-failure / retry-open ===============================

    @Test
    fun readyEvent_invokesFakeLauncherWithExactCheckoutUrl() {
        val state = launch(referenceFile = validReference(), captureFile = validCapture())
        state.emitLaunchEvent("https://deinwackelbild.de/checkout/h1?token=abc")
        composeRule.waitForIdle()
        assertEquals(listOf("https://deinwackelbild.de/checkout/h1?token=abc"), state.launchedUrls)
        assertEquals(
            listOf("https://deinwackelbild.de/checkout/h1?token=abc" to true),
            state.launchResultReports
        )
    }

    @Test
    fun recomposition_doesNotInvokeLauncherASecondTime() {
        val state = launch(referenceFile = validReference(), captureFile = validCapture())
        state.emitLaunchEvent("https://deinwackelbild.de/checkout/h1")
        composeRule.waitForIdle()
        // Force a recomposition unrelated to the launch event.
        state.dateOverlayEnabled.value = !state.dateOverlayEnabled.value
        composeRule.waitForIdle()
        assertEquals(1, state.launchedUrls.size)
    }

    @Test
    fun launcherFailure_showsExactOpenFailedCopyAndAction() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Ready("https://deinwackelbild.de/checkout/h1", usedFallback = false),
            initialCustomTabOpenFailure = "https://deinwackelbild.de/checkout/h1"
        )
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_custom_tab_open_failed)).assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_custom_tab_open_retry_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.wackelbild_custom_tab_open_retry)).assertIsDisplayed()
        // The raw checkout URL is never shown as visible text.
        composeRule.onNodeWithText("https://deinwackelbild.de/checkout/h1").assertDoesNotExist()
    }

    @Test
    fun retryOpen_invokesLauncherWithExactSameUrl_noNewOperation() {
        val state = launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Ready("https://deinwackelbild.de/checkout/h1", usedFallback = false),
            initialCustomTabOpenFailure = "https://deinwackelbild.de/checkout/h1"
        )
        // Simulate the original failed attempt already having recorded the URL once.
        state.emitLaunchEvent("https://deinwackelbild.de/checkout/h1")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("wackelbild_custom_tab_open_retry_button").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals(1, state.retryOpenCallCount.value)
        assertEquals(0, state.startOperationCallCount.value)
        assertTrue(state.launchedUrls.all { it == "https://deinwackelbild.de/checkout/h1" })
    }

    @Test
    fun launchOpenFailure_backNavigatesNormally_noExtraCancelButton() {
        var backInvoked = false
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Ready("https://deinwackelbild.de/checkout/h1", usedFallback = false),
            initialCustomTabOpenFailure = "https://deinwackelbild.de/checkout/h1",
            onBack = { backInvoked = true }
        )
        composeRule.onNodeWithTag("wackelbild_back_button").performClick()
        composeRule.waitForIdle()
        assertTrue(backInvoked)
        composeRule.onNodeWithTag("wackelbild_cancel_transfer_dialog").assertDoesNotExist()
    }

    @Test
    fun readyState_showsNoIntermediateSuccessScreen() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            initialOperationState = WackelbildOperationState.Ready("https://deinwackelbild.de/checkout/h1", usedFallback = false)
        )
        composeRule.onNodeWithTag("wackelbild_cta_button").assertDoesNotExist()
        composeRule.onNodeWithTag("wackelbild_loading_spinner").assertDoesNotExist()
        composeRule.onNodeWithTag("wackelbild_custom_tab_open_failed_text").assertDoesNotExist()
    }

    @Test
    fun readyState_dateToggleDisabled() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            isDateOverlayAvailable = true,
            initialOperationState = WackelbildOperationState.Ready("https://deinwackelbild.de/checkout/h1", usedFallback = false)
        )
        composeRule.onNodeWithTag("wackelbild_date_toggle").assertIsNotEnabled()
    }

    // === Block 11: Accessibility / layout regression =============================================

    @Test
    fun cta_hasAccessibleClickAction() {
        launch(referenceFile = validReference(), captureFile = validCapture())
        composeRule.onNodeWithTag("wackelbild_cta_button")
            .fetchSemanticsNode()
            .config
            .contains(SemanticsActions.OnClick)
            .let { assertTrue("CTA should expose a click action", it) }
    }

    @Test
    fun compactWidth_withOrderArea_screenStillRendersAndReferenceDisplayed() {
        launch(
            referenceFile = validReference(),
            captureFile = validCapture(),
            windowWidthSizeClass = WindowWidthSizeClass.Compact
        )
        composeRule.onNodeWithTag("wackelbild_reference_preview_container").assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_transfer_disclosure").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("wackelbild_cta_button").performScrollTo().assertIsDisplayed()
    }
}
