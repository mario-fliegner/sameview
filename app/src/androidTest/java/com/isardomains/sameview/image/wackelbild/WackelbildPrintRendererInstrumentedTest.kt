// path: app/src/androidTest/java/com/isardomains/sameview/image/wackelbild/WackelbildPrintRendererInstrumentedTest.kt
package com.isardomains.sameview.image.wackelbild

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.isardomains.sameview.image.ShareComparisonStyle
import com.isardomains.sameview.image.ShareImageRenderer
import com.isardomains.sameview.image.ShareQuality
import com.isardomains.sameview.image.ShareRenderConfig
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Focused Wackelbild print-renderer instrumentation test using the real Android bitmap/JPEG/EXIF
 * stack. Complements the pure-JVM `WackelbildDimensionResolverTest`/`DateBadgeRendererTest`/
 * `WackelbildPairSizeLoopTest` — those cover orchestration/math in isolation; this covers actual
 * decode/render/encode behavior end-to-end. All fixtures are current-release-shaped (v6-style
 * metadata) — no legacy v2-v4 fixture is used anywhere in this file.
 */
@RunWith(AndroidJUnit4::class)
class WackelbildPrintRendererInstrumentedTest {

    private lateinit var context: Context
    private lateinit var sessionDir: File
    private lateinit var outputDir: File

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        sessionDir = File(context.filesDir, "wb_print_test_${System.currentTimeMillis()}")
        sessionDir.mkdirs()
        outputDir = File(context.cacheDir, "wb_print_test_out_${System.currentTimeMillis()}")
        outputDir.mkdirs()
    }

    @After
    fun tearDown() {
        sessionDir.deleteRecursively()
        outputDir.deleteRecursively()
    }

    private fun renderer() = WackelbildPrintRenderer()

    // ── HQ success / mapping / pair validity ─────────────────────────────────

    @Test
    fun hqPath_validSession_producesSuccessNotFallback() = runBlocking {
        createHqSession(sessionDir)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null)
        assertTrue(result is WackelbildPrintResult.Success)
        assertEquals(false, (result as WackelbildPrintResult.Success).usedFallback)
    }

    @Test
    fun hqPath_producesExactlyTwoValidJpegFiles() = runBlocking {
        createHqSession(sessionDir)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null) as WackelbildPrintResult.Success
        assertTrue(result.pair.referenceFile.isFile)
        assertTrue(result.pair.captureFile.isFile)
        assertNotNull(BitmapFactory.decodeFile(result.pair.referenceFile.absolutePath))
        assertNotNull(BitmapFactory.decodeFile(result.pair.captureFile.absolutePath))
    }

    @Test
    fun hqPath_bothOutputs_identicalDimensions() = runBlocking {
        createHqSession(sessionDir)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null) as WackelbildPrintResult.Success
        assertEquals(decodeDims(result.pair.referenceFile), decodeDims(result.pair.captureFile))
    }

    @Test
    fun hqPath_outputs_independentDistinctContent_noSbsComposite() = runBlocking {
        createHqSession(sessionDir) // reference=blue, capture=orange
        val result = renderer().renderPrintPair(sessionDir, outputDir, null) as WackelbildPrintResult.Success
        val refBmp = BitmapFactory.decodeFile(result.pair.referenceFile.absolutePath)
        val capBmp = BitmapFactory.decodeFile(result.pair.captureFile.absolutePath)
        val refPixel = refBmp.getPixel(refBmp.width / 2, refBmp.height / 2)
        val capPixel = capBmp.getPixel(capBmp.width / 2, capBmp.height / 2)
        assertNotEquals("Reference and Capture must be independent images, not a composite", refPixel, capPixel)
        refBmp.recycle(); capBmp.recycle()
    }

    @Test
    fun hqPath_referenceContent_matchesReferenceOriginalColour() = runBlocking {
        createHqSession(sessionDir) // reference-original=blue
        val result = renderer().renderPrintPair(sessionDir, outputDir, null) as WackelbildPrintResult.Success
        val bmp = BitmapFactory.decodeFile(result.pair.referenceFile.absolutePath)
        assertTrue("Reference output must be blue-dominant",
            Color.blue(bmp.getPixel(bmp.width / 2, bmp.height / 2)) > Color.red(bmp.getPixel(bmp.width / 2, bmp.height / 2)))
        bmp.recycle()
    }

    @Test
    fun hqPath_captureContent_matchesCaptureOriginalColour_directUncropped() = runBlocking {
        createHqSession(sessionDir) // capture-original=orange
        val result = renderer().renderPrintPair(sessionDir, outputDir, null) as WackelbildPrintResult.Success
        val bmp = BitmapFactory.decodeFile(result.pair.captureFile.absolutePath)
        val pixel = bmp.getPixel(bmp.width / 2, bmp.height / 2)
        assertTrue("Capture output must be orange (red-dominant), directly reflecting the " +
            "uncropped capture-original source", Color.red(pixel) > Color.blue(pixel))
        bmp.recycle()
    }

    @Test
    fun hqPath_dimensions_largerThanViewport_whenSourcesAllowIt() = runBlocking {
        createHqSession(sessionDir) // both originals are 2x viewport
        val result = renderer().renderPrintPair(sessionDir, outputDir, null) as WackelbildPrintResult.Success
        val (w, h) = decodeDims(result.pair.referenceFile)
        assertTrue("HQ output must exceed the 200x300 viewport, was ${w}x$h", maxOf(w, h) > 300)
    }

    // ── Capture ratio guard -> fallback (§9.3 Correction A/G) ────────────────

    @Test
    fun captureRatioMismatch_routesToFallback_neverCropsOrStretches() = runBlocking {
        createRatioMismatchSession(sessionDir)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null)
        assertTrue(result is WackelbildPrintResult.Success)
        assertTrue("must route to fallback, not attempt a crop/stretch of the mismatched original",
            (result as WackelbildPrintResult.Success).usedFallback)
    }

    // ── Near-ratio-mismatch (Block 5E) -> HQ rejected, no stretch ────────────

    @Test
    fun captureNearRatioMismatch_hqRejected_routesToFallback_noStretch() = runBlocking {
        // viewport 600x400 (min=400, dynamic tolerance = 1/400 = 0.25%); capture-original is
        // 600x403 -- a genuine ~0.745% ratio difference that the disproven flat 2% tolerance
        // would have wrongly accepted (and then non-uniformly stretched via ImageDecoder's
        // setTargetSize). The corrected dynamic tolerance must reject this and route to fallback.
        createNearRatioMismatchSession(sessionDir)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null)
        assertTrue(result is WackelbildPrintResult.Success)
        val success = result as WackelbildPrintResult.Success
        assertTrue(
            "a genuine ~0.745% Capture ratio mismatch (well outside the 0.25% dynamic tolerance " +
                "for this viewport) must be rejected by the HQ guard and routed to fallback",
            success.usedFallback
        )
        // The frozen reference.jpg/capture.jpg pair (both exactly 600x400, independent of the
        // mismatched capture-original) is itself ratio-compatible, so fallback succeeds cleanly
        // and the output dims must be exactly the frozen pair's own dims -- never a value derived
        // by stretching the mismatched HQ source.
        val (w, h) = decodeDims(success.pair.referenceFile)
        assertEquals(600, w)
        assertEquals(400, h)
        assertEquals(Pair(600, 400), decodeDims(success.pair.captureFile))
    }

    // ── Broken HQ source -> fallback (current-release fixture, no legacy) ────

    @Test
    fun missingCaptureOriginal_routesToFallback() = runBlocking {
        createBrokenHqSessionMissingCaptureOriginal(sessionDir)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null)
        assertTrue(result is WackelbildPrintResult.Success)
        assertTrue((result as WackelbildPrintResult.Success).usedFallback)
    }

    @Test
    fun missingOverlayMetadata_routesToFallback() = runBlocking {
        createBrokenHqSessionMissingOverlay(sessionDir)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null)
        assertTrue(result is WackelbildPrintResult.Success)
        assertTrue((result as WackelbildPrintResult.Success).usedFallback)
    }

    // ── Fallback dimension/ratio cases (§9.5 Correction J Case A/B/C) ────────

    @Test
    fun fallback_identicalFrozenDimensions_succeeds() = runBlocking {
        createFallbackOnlySession(sessionDir, refDims = 200 to 300, capDims = 200 to 300)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null)
        assertTrue(result is WackelbildPrintResult.Success)
        assertTrue((result as WackelbildPrintResult.Success).usedFallback)
    }

    @Test
    fun fallback_compatibleRatioDifferentDimensions_succeeds_noUpscale() = runBlocking {
        createFallbackOnlySession(sessionDir, refDims = 200 to 300, capDims = 400 to 600) // same 2:3 ratio
        val result = renderer().renderPrintPair(sessionDir, outputDir, null) as WackelbildPrintResult.Success
        assertTrue(result.usedFallback)
        val (w, h) = decodeDims(result.pair.referenceFile)
        assertTrue("must use the weaker (smaller) source's dims, never upscale it", w <= 200 && h <= 300)
        assertEquals(decodeDims(result.pair.captureFile), Pair(w, h))
    }

    @Test
    fun fallback_incompatibleRatio_hardFails_noOutputPair() = runBlocking {
        createFallbackOnlySession(sessionDir, refDims = 200 to 300, capDims = 300 to 200) // transposed ratio
        val result = renderer().renderPrintPair(sessionDir, outputDir, null)
        assertTrue(result is WackelbildPrintResult.Failure)
        assertEquals(WackelbildPrintFailureReason.PERMANENT_NO_VALID_SOURCE, (result as WackelbildPrintResult.Failure).reason)
    }

    // ── Privacy: no sensitive metadata in output ──────────────────────────────

    @Test
    fun output_hasNoSensitiveExifMetadata() = runBlocking {
        createHqSessionWithSensitiveExif(sessionDir)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null) as WackelbildPrintResult.Success
        assertNoSensitiveExif(result.pair.referenceFile)
        assertNoSensitiveExif(result.pair.captureFile)
    }

    // ── Immutability: session files never modified ────────────────────────────

    @Test
    fun render_neverModifiesPersistedSessionFiles() = runBlocking {
        createHqSession(sessionDir)
        val filesToCheck = listOf("reference.jpg", "capture.jpg", "reference-original.jpg", "capture-original.jpg", "metadata.json")
        val before = filesToCheck.associateWith { sha256(File(sessionDir, it)) }
        renderer().renderPrintPair(sessionDir, outputDir, null)
        val after = filesToCheck.associateWith { sha256(File(sessionDir, it)) }
        assertEquals(before, after)
    }

    @Test
    fun render_neverModifiesPersistedSessionFiles_evenOnFallback() = runBlocking {
        createRatioMismatchSession(sessionDir) // forces fallback path
        val filesToCheck = listOf("reference.jpg", "capture.jpg", "reference-original.jpg", "capture-original.jpg", "metadata.json")
        val before = filesToCheck.associateWith { sha256(File(sessionDir, it)) }
        renderer().renderPrintPair(sessionDir, outputDir, null)
        val after = filesToCheck.associateWith { sha256(File(sessionDir, it)) }
        assertEquals(before, after)
    }

    // ── Date badge: drawn before file-size validation (Block 5B/C Correction A) ─

    @Test
    fun dateOverlay_on_changesOutputVsOff() = runBlocking {
        createHqSession(sessionDir)
        val offDir = File(outputDir, "off").apply { mkdirs() }
        val onDir = File(outputDir, "on").apply { mkdirs() }
        val withoutBadge = renderer().renderPrintPair(sessionDir, offDir, null) as WackelbildPrintResult.Success
        val withBadge = renderer().renderPrintPair(
            sessionDir, onDir, WackelbildDateOverlay("2008 -> 2026", "2026-08-29")
        ) as WackelbildPrintResult.Success
        assertNotEquals(
            "date ON must change the encoded candidate file's measured size vs date OFF -- proving " +
                "File.length() is measured on the badge-included final output, not a pre-badge candidate",
            withoutBadge.pair.referenceFile.length(),
            withBadge.pair.referenceFile.length()
        )
    }

    @Test
    fun dateOverlay_captureTextNull_captureUnaffected_referenceStillGetsBadge() = runBlocking {
        createHqSession(sessionDir)
        val offDir = File(outputDir, "off2").apply { mkdirs() }
        val partialDir = File(outputDir, "partial").apply { mkdirs() }
        val withoutBadge = renderer().renderPrintPair(sessionDir, offDir, null) as WackelbildPrintResult.Success
        val partial = renderer().renderPrintPair(
            sessionDir, partialDir, WackelbildDateOverlay(referenceText = "2008 -> 2026", captureText = null)
        ) as WackelbildPrintResult.Success
        assertNotEquals(
            "Reference badge must still be drawn when only Capture's date is null",
            withoutBadge.pair.referenceFile.length(),
            partial.pair.referenceFile.length()
        )
    }

    // ── Print-format crop (WackelbildPrintTarget) ─────────────────────────────
    // Frames are banded: the outer 5% along the long axis is red, the rest green. The 2:3 crop of a
    // 9:16 / 16:9 frame removes ~7.8% at each end, so a correctly centered crop shows only green at
    // the output's long-axis edges, while a missing/mis-anchored crop would show red there.

    private fun printTarget(width: Int, height: Int): WackelbildPrintTarget =
        checkNotNull(WackelbildPrintTarget.select(width, height))

    private fun isGreen(pixel: Int) = Color.green(pixel) > Color.red(pixel) + 60
    private fun isRed(pixel: Int) = Color.red(pixel) > Color.green(pixel) + 60

    /** True when the outer long-axis edges of [file]'s output show the green center (bands cropped). */
    private fun assertLongAxisEdgesAreGreen(file: File, portrait: Boolean) {
        val bmp = BitmapFactory.decodeFile(file.absolutePath)
        try {
            val w = bmp.width
            val h = bmp.height
            if (portrait) {
                assertTrue("top edge must be green (cropped in)", isGreen(bmp.getPixel(w / 2, 2)))
                assertTrue("bottom edge must be green (cropped in)", isGreen(bmp.getPixel(w / 2, h - 3)))
            } else {
                assertTrue("left edge must be green (cropped in)", isGreen(bmp.getPixel(2, h / 2)))
                assertTrue("right edge must be green (cropped in)", isGreen(bmp.getPixel(w - 3, h / 2)))
            }
            assertTrue("center must be green", isGreen(bmp.getPixel(w / 2, h / 2)))
        } finally {
            bmp.recycle()
        }
    }

    @Test
    fun target_hqPortrait9x16_outputsAre2x3_identicalDimensions_centeredCropOnBothSides() = runBlocking {
        createBandedHqSession(sessionDir, 180, 320)
        val target = printTarget(180, 320)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null, target) as WackelbildPrintResult.Success

        assertEquals(false, result.usedFallback)
        val referenceDims = decodeDims(result.pair.referenceFile)
        assertEquals(referenceDims, decodeDims(result.pair.captureFile))
        assertTrue("output ${referenceDims} must be portrait", referenceDims.first < referenceDims.second)
        assertTrue("output ${referenceDims} must match the 2:3 target", target.matchesOutput(referenceDims.first, referenceDims.second))
        assertLongAxisEdgesAreGreen(result.pair.referenceFile, portrait = true)
        assertLongAxisEdgesAreGreen(result.pair.captureFile, portrait = true)
    }

    @Test
    fun target_hqLandscape16x9_outputsAre3x2_identicalDimensions_centeredCropOnBothSides() = runBlocking {
        createBandedHqSession(sessionDir, 320, 180)
        val target = printTarget(320, 180)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null, target) as WackelbildPrintResult.Success

        assertEquals(false, result.usedFallback)
        val referenceDims = decodeDims(result.pair.referenceFile)
        assertEquals(referenceDims, decodeDims(result.pair.captureFile))
        assertTrue("output ${referenceDims} must be landscape", referenceDims.first > referenceDims.second)
        assertTrue("output ${referenceDims} must match the 3:2 target", target.matchesOutput(referenceDims.first, referenceDims.second))
        assertLongAxisEdgesAreGreen(result.pair.referenceFile, portrait = false)
        assertLongAxisEdgesAreGreen(result.pair.captureFile, portrait = false)
    }

    @Test
    fun target_fallbackPortrait9x16_obeysTheTarget_identicalDimensions_centeredCrop() = runBlocking {
        createBandedFallbackSession(sessionDir, 180, 320)
        val target = printTarget(180, 320)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null, target) as WackelbildPrintResult.Success

        assertTrue("no HQ originals -> fallback", result.usedFallback)
        val referenceDims = decodeDims(result.pair.referenceFile)
        assertEquals(referenceDims, decodeDims(result.pair.captureFile))
        assertTrue("output ${referenceDims} must match the 2:3 target", target.matchesOutput(referenceDims.first, referenceDims.second))
        assertLongAxisEdgesAreGreen(result.pair.referenceFile, portrait = true)
        assertLongAxisEdgesAreGreen(result.pair.captureFile, portrait = true)
    }

    @Test
    fun target_fallbackLandscape16x9_obeysTheTarget_identicalDimensions_centeredCrop() = runBlocking {
        createBandedFallbackSession(sessionDir, 320, 180)
        val target = printTarget(320, 180)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null, target) as WackelbildPrintResult.Success

        assertTrue(result.usedFallback)
        val referenceDims = decodeDims(result.pair.referenceFile)
        assertEquals(referenceDims, decodeDims(result.pair.captureFile))
        assertTrue(target.matchesOutput(referenceDims.first, referenceDims.second))
        assertLongAxisEdgesAreGreen(result.pair.referenceFile, portrait = false)
        assertLongAxisEdgesAreGreen(result.pair.captureFile, portrait = false)
    }

    @Test
    fun target_frameAlreadyMatching_2x3_noCropApplied_outputKeepsTheFullFrame() = runBlocking {
        createBandedHqSession(sessionDir, 200, 300) // exactly 2:3
        val target = printTarget(200, 300)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null, target) as WackelbildPrintResult.Success

        val dims = decodeDims(result.pair.referenceFile)
        assertTrue(target.matchesOutput(dims.first, dims.second))
        // Nothing was cropped, so the red long-axis bands are still at the edges.
        val bmp = BitmapFactory.decodeFile(result.pair.referenceFile.absolutePath)
        assertTrue("top band must still be present", isRed(bmp.getPixel(bmp.width / 2, 2)))
        assertTrue("bottom band must still be present", isRed(bmp.getPixel(bmp.width / 2, bmp.height - 3)))
        bmp.recycle()
    }

    @Test
    fun target_null_hqOutputKeepsTheFullSessionFrame_noCrop() = runBlocking {
        createBandedHqSession(sessionDir, 180, 320)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null) as WackelbildPrintResult.Success

        val (w, h) = decodeDims(result.pair.referenceFile)
        assertEquals("full 9:16 frame kept", 180.0 / 320.0, w.toDouble() / h, 0.01)
        val bmp = BitmapFactory.decodeFile(result.pair.referenceFile.absolutePath)
        assertTrue("top band must be present (no crop)", isRed(bmp.getPixel(bmp.width / 2, 2)))
        assertTrue("bottom band must be present (no crop)", isRed(bmp.getPixel(bmp.width / 2, bmp.height - 3)))
        bmp.recycle()
    }

    // ── Date badge is drawn after the crop, so it sits inside the final output ──

    /** Counts the badge's dark navy (SameViewAppSurface) pixels in the bottom-right of [file]. */
    private fun badgePixelCount(file: File): Int {
        val bmp = BitmapFactory.decodeFile(file.absolutePath)
        try {
            var count = 0
            for (y in (bmp.height * 0.85f).toInt() until bmp.height) {
                for (x in (bmp.width * 0.5f).toInt() until bmp.width) {
                    val p = bmp.getPixel(x, y)
                    if (Color.red(p) < 70 && Color.green(p) < 80 && Color.blue(p) < 100) count++
                }
            }
            return count
        } finally {
            bmp.recycle()
        }
    }

    @Test
    fun target_dateBadge_isInsideTheCroppedOutput_onBothSides() = runBlocking {
        createBandedHqSession(sessionDir, 180, 320)
        val target = printTarget(180, 320)
        val withDir = File(outputDir, "with").apply { mkdirs() }
        val withoutDir = File(outputDir, "without").apply { mkdirs() }

        val without = renderer().renderPrintPair(sessionDir, withoutDir, null, target) as WackelbildPrintResult.Success
        val with = renderer().renderPrintPair(
            sessionDir, withDir, WackelbildDateOverlay("2008 -> 2026", "2026-08-29"), target
        ) as WackelbildPrintResult.Success

        assertEquals(0, badgePixelCount(without.pair.referenceFile))
        assertEquals(0, badgePixelCount(without.pair.captureFile))
        assertTrue("reference badge must be inside the cropped output", badgePixelCount(with.pair.referenceFile) > 20)
        assertTrue("capture badge must be inside the cropped output", badgePixelCount(with.pair.captureFile) > 20)
        // Badge drawn *after* the crop: its bottom-right anchor is the cropped output's corner, and the
        // output still has the target aspect.
        val (w, h) = decodeDims(with.pair.referenceFile)
        assertTrue(target.matchesOutput(w, h))
    }

    @Test
    fun target_dateBadge_isInsideTheCroppedOutput_onFallbackToo() = runBlocking {
        createBandedFallbackSession(sessionDir, 180, 320)
        val target = printTarget(180, 320)
        val with = renderer().renderPrintPair(
            sessionDir, outputDir, WackelbildDateOverlay("2008 -> 2026", "2026-08-29"), target
        ) as WackelbildPrintResult.Success

        assertTrue(with.usedFallback)
        assertTrue(badgePixelCount(with.pair.referenceFile) > 20)
        assertTrue(badgePixelCount(with.pair.captureFile) > 20)
    }

    // ── Sources are never mutated by a cropped render ─────────────────────────

    @Test
    fun target_render_neverModifiesPersistedSessionFiles_hqAndFallback() = runBlocking {
        val filesToCheck = listOf("reference.jpg", "capture.jpg", "reference-original.jpg", "capture-original.jpg", "metadata.json")

        createBandedHqSession(sessionDir, 180, 320)
        val hqBefore = filesToCheck.associateWith { sha256(File(sessionDir, it)) }
        renderer().renderPrintPair(sessionDir, outputDir, null, printTarget(180, 320))
        assertEquals(hqBefore, filesToCheck.associateWith { sha256(File(sessionDir, it)) })

        val fallbackDir = File(context.filesDir, "wb_print_test_fb_${System.currentTimeMillis()}").apply { mkdirs() }
        try {
            createBandedFallbackSession(fallbackDir, 180, 320)
            val fallbackFiles = listOf("reference.jpg", "capture.jpg", "metadata.json")
            val fbBefore = fallbackFiles.associateWith { sha256(File(fallbackDir, it)) }
            renderer().renderPrintPair(fallbackDir, File(outputDir, "fb").apply { mkdirs() }, null, printTarget(180, 320))
            assertEquals(fbBefore, fallbackFiles.associateWith { sha256(File(fallbackDir, it)) })
        } finally {
            fallbackDir.deleteRecursively()
        }
    }

    @Test
    fun target_outputsStillHaveNoSensitiveExif() = runBlocking {
        createBandedHqSession(sessionDir, 180, 320)
        val result = renderer().renderPrintPair(sessionDir, outputDir, null, printTarget(180, 320)) as WackelbildPrintResult.Success
        assertNoSensitiveExif(result.pair.referenceFile)
        assertNoSensitiveExif(result.pair.captureFile)
    }

    // ── Production target resolver (real metadata / real JPEG dimensions) ─────
    // `resolveWackelbildPrintTarget` is what the ViewModel runs once, before the CTA: the session
    // viewport must be confirmed by reference.jpg AND capture.jpg, otherwise there is no target.

    @Test
    fun resolver_consistentSession_selectsTheTargetFromTheViewport() {
        createBandedFallbackSession(sessionDir, 180, 320)
        val target = com.isardomains.sameview.ui.wackelbild.resolveWackelbildPrintTarget(sessionDir)
        assertEquals(WackelbildPrintTarget.select(180, 320), target)
        assertEquals("10x15", target?.slug)
    }

    @Test
    fun resolver_landscapeSession_selectsTheSameFamily() {
        createBandedFallbackSession(sessionDir, 320, 180)
        val target = com.isardomains.sameview.ui.wackelbild.resolveWackelbildPrintTarget(sessionDir)
        assertEquals(WackelbildPrintTarget.select(320, 180), target)
        assertEquals("10x15", target?.slug)
    }

    @Test
    fun resolver_captureJpgWithADifferentAspect_returnsNoTarget() {
        // Viewport = reference dims (200x300); capture.jpg is 300x200 -- the preview crop and the
        // print crop could differ, so no target.
        createFallbackOnlySession(sessionDir, refDims = 200 to 300, capDims = 300 to 200)
        assertEquals(null, com.isardomains.sameview.ui.wackelbild.resolveWackelbildPrintTarget(sessionDir))
    }

    @Test
    fun resolver_missingReferenceJpg_returnsNoTarget() {
        createBandedFallbackSession(sessionDir, 180, 320)
        File(sessionDir, "reference.jpg").delete()
        assertEquals(null, com.isardomains.sameview.ui.wackelbild.resolveWackelbildPrintTarget(sessionDir))
    }

    @Test
    fun resolver_missingViewportMetadata_fallsBackToTheCaptureDimensions() {
        createBandedFallbackSession(sessionDir, 180, 320)
        File(sessionDir, "metadata.json").delete()
        val target = com.isardomains.sameview.ui.wackelbild.resolveWackelbildPrintTarget(sessionDir)
        assertEquals(WackelbildPrintTarget.select(180, 320), target)
    }

    @Test
    fun resolver_neverModifiesSessionFiles() {
        createBandedFallbackSession(sessionDir, 180, 320)
        val files = listOf("reference.jpg", "capture.jpg", "metadata.json")
        val before = files.associateWith { sha256(File(sessionDir, it)) }
        com.isardomains.sameview.ui.wackelbild.resolveWackelbildPrintTarget(sessionDir)
        assertEquals(before, files.associateWith { sha256(File(sessionDir, it)) })
    }

    // ── Existing Share Image regression: widened methods stay reachable ──────

    @Test
    fun shareImageRenderer_widenedMethods_stillReachableAndFunctional() {
        // Block body (not `= runBlocking { ... }`) so this function's JVM return type is always
        // Unit/void regardless of the block's last expression type -- required for JUnit4's
        // "test methods must be void" validation, which mangled-return-type expression bodies
        // (e.g. a trailing Result<T>/Any-typed expression) silently violate.
        runBlocking {
            createHqSession(sessionDir)
            val config = ShareRenderConfig(
                style = ShareComparisonStyle.SLIDER,
                quality = ShareQuality.ORIGINAL,
                captionData = null,
                sessionDir = sessionDir,
                exportTimestamp = "20260829_120000",
                captureOriginalFile = File(sessionDir, "capture-original.jpg")
            )
            val resolver = context.contentResolver
            val uri = ShareImageRenderer().render(config, resolver)
            assertNotNull(uri)
            try {
                resolver.delete(uri, null, null)
            } catch (_: Exception) {
                // best-effort cleanup only
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun decodeDims(file: File): Pair<Int, Int> {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, opts)
        return Pair(opts.outWidth, opts.outHeight)
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun assertNoSensitiveExif(file: File) {
        val exif = ExifInterface(file.absolutePath)
        val sensitiveTags = listOf(
            ExifInterface.TAG_GPS_LATITUDE, ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_DATETIME_ORIGINAL, ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_MAKE, ExifInterface.TAG_MODEL, ExifInterface.TAG_SOFTWARE,
            ExifInterface.TAG_LENS_MAKE, ExifInterface.TAG_LENS_MODEL,
            // Block 5E: identifier-sensitive tags confirmed inspectable via getAttribute() in the
            // project's actual androidx.exifinterface:1.3.7 dependency (verified against its
            // sources jar, not assumed) -- previously omitted from this check without evidence.
            ExifInterface.TAG_MAKER_NOTE, ExifInterface.TAG_BODY_SERIAL_NUMBER,
            ExifInterface.TAG_LENS_SERIAL_NUMBER, ExifInterface.TAG_IMAGE_UNIQUE_ID,
            ExifInterface.TAG_CAMERA_OWNER_NAME
        )
        sensitiveTags.forEach { tag ->
            assertTrue("$tag must not be present in output", exif.getAttribute(tag).isNullOrEmpty())
        }
    }

    private fun writeSyntheticJpeg(file: File, color: Int, width: Int, height: Int) {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(bmp).apply { drawColor(color) }
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        bmp.recycle()
    }

    /** viewport 200x300, both HQ originals 2x (400x600) so the resolver never binds at 1x. */
    private fun createHqSession(dir: File) {
        writeSyntheticJpeg(File(dir, "reference.jpg"), Color.rgb(80, 120, 180), 200, 300)
        writeSyntheticJpeg(File(dir, "capture.jpg"), Color.rgb(180, 120, 80), 200, 300)
        writeSyntheticJpeg(File(dir, "reference-original.jpg"), Color.rgb(80, 120, 180), 400, 600)
        writeSyntheticJpeg(File(dir, "capture-original.jpg"), Color.rgb(180, 120, 80), 400, 600)
        File(dir, "metadata.json").writeText(
            """{"version":6,"session":{"id":"wb-hq-test","createdAtMs":1000},""" +
                """"viewport":{"width":200,"height":300,"orientation":"PORTRAIT"},""" +
                """"overlay":{"scale":1.0,"offsetX":0.0,"offsetY":0.0,"displayMode":"COMPARE_WITH_PREVIEW"},""" +
                """"files":{"capture":"capture.jpg","reference":"reference.jpg",""" +
                """"referenceOriginal":"reference-original.jpg","captureOriginal":"capture-original.jpg"},""" +
                """"capture":{"timestampMs":1000}}"""
        )
    }

    private fun createHqSessionWithSensitiveExif(dir: File) {
        createHqSession(dir)
        val captureOriginal = File(dir, "capture-original.jpg")
        val exif = ExifInterface(captureOriginal.absolutePath)
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "48/1,8/1,0/1")
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N")
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "11/1,34/1,0/1")
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E")
        exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, "2026:08:29 12:00:00")
        exif.setAttribute(ExifInterface.TAG_MAKE, "SyntheticCam")
        exif.setAttribute(ExifInterface.TAG_MODEL, "Model X")
        exif.setAttribute(ExifInterface.TAG_SOFTWARE, "TestSoftware 1.0")
        exif.saveAttributes()
    }

    /** capture-original is LANDSCAPE 600x400 while the viewport is PORTRAIT 200x300 -- a gross,
     * genuinely incompatible ratio mismatch (not benign encoder padding). */
    private fun createRatioMismatchSession(dir: File) {
        writeSyntheticJpeg(File(dir, "reference.jpg"), Color.rgb(80, 120, 180), 200, 300)
        writeSyntheticJpeg(File(dir, "capture.jpg"), Color.rgb(180, 120, 80), 200, 300)
        writeSyntheticJpeg(File(dir, "reference-original.jpg"), Color.rgb(80, 120, 180), 400, 600)
        writeSyntheticJpeg(File(dir, "capture-original.jpg"), Color.rgb(180, 120, 80), 600, 400)
        File(dir, "metadata.json").writeText(
            """{"version":6,"session":{"id":"wb-mismatch-test","createdAtMs":1000},""" +
                """"viewport":{"width":200,"height":300,"orientation":"PORTRAIT"},""" +
                """"overlay":{"scale":1.0,"offsetX":0.0,"offsetY":0.0,"displayMode":"COMPARE_WITH_PREVIEW"},""" +
                """"files":{"capture":"capture.jpg","reference":"reference.jpg",""" +
                """"referenceOriginal":"reference-original.jpg","captureOriginal":"capture-original.jpg"},""" +
                """"capture":{"timestampMs":1000}}"""
        )
    }

    /** viewport 600x400 landscape; reference.jpg/capture.jpg/reference-original.jpg all exactly
     * match that ratio (2x for the original); capture-original.jpg is 600x403 -- a genuine
     * ~0.745% ratio mismatch, well outside the dynamic tolerance (1/400 = 0.25%) but far smaller
     * than the existing gross-mismatch fixture, specifically exercising the Block 5E boundary. */
    private fun createNearRatioMismatchSession(dir: File) {
        writeSyntheticJpeg(File(dir, "reference.jpg"), Color.rgb(80, 120, 180), 600, 400)
        writeSyntheticJpeg(File(dir, "capture.jpg"), Color.rgb(180, 120, 80), 600, 400)
        writeSyntheticJpeg(File(dir, "reference-original.jpg"), Color.rgb(80, 120, 180), 1200, 800)
        writeSyntheticJpeg(File(dir, "capture-original.jpg"), Color.rgb(180, 120, 80), 600, 403)
        File(dir, "metadata.json").writeText(
            """{"version":6,"session":{"id":"wb-near-mismatch-test","createdAtMs":1000},""" +
                """"viewport":{"width":600,"height":400,"orientation":"LANDSCAPE"},""" +
                """"overlay":{"scale":1.0,"offsetX":0.0,"offsetY":0.0,"displayMode":"COMPARE_WITH_PREVIEW"},""" +
                """"files":{"capture":"capture.jpg","reference":"reference.jpg",""" +
                """"referenceOriginal":"reference-original.jpg","captureOriginal":"capture-original.jpg"},""" +
                """"capture":{"timestampMs":1000}}"""
        )
    }

    private fun createBrokenHqSessionMissingCaptureOriginal(dir: File) {
        writeSyntheticJpeg(File(dir, "reference.jpg"), Color.rgb(80, 120, 180), 200, 300)
        writeSyntheticJpeg(File(dir, "capture.jpg"), Color.rgb(180, 120, 80), 200, 300)
        writeSyntheticJpeg(File(dir, "reference-original.jpg"), Color.rgb(80, 120, 180), 400, 600)
        // No capture-original.jpg, and files.captureOriginal is not declared -> hasHqCaptureSource=false.
        File(dir, "metadata.json").writeText(
            """{"version":6,"session":{"id":"wb-broken-test","createdAtMs":1000},""" +
                """"viewport":{"width":200,"height":300,"orientation":"PORTRAIT"},""" +
                """"overlay":{"scale":1.0,"offsetX":0.0,"offsetY":0.0,"displayMode":"COMPARE_WITH_PREVIEW"},""" +
                """"files":{"capture":"capture.jpg","reference":"reference.jpg",""" +
                """"referenceOriginal":"reference-original.jpg"},""" +
                """"capture":{"timestampMs":1000}}"""
        )
    }

    private fun createBrokenHqSessionMissingOverlay(dir: File) {
        writeSyntheticJpeg(File(dir, "reference.jpg"), Color.rgb(80, 120, 180), 200, 300)
        writeSyntheticJpeg(File(dir, "capture.jpg"), Color.rgb(180, 120, 80), 200, 300)
        writeSyntheticJpeg(File(dir, "reference-original.jpg"), Color.rgb(80, 120, 180), 400, 600)
        writeSyntheticJpeg(File(dir, "capture-original.jpg"), Color.rgb(180, 120, 80), 400, 600)
        File(dir, "metadata.json").writeText(
            """{"version":6,"session":{"id":"wb-nooverlay-test","createdAtMs":1000},""" +
                """"viewport":{"width":200,"height":300,"orientation":"PORTRAIT"},""" +
                """"files":{"capture":"capture.jpg","reference":"reference.jpg",""" +
                """"referenceOriginal":"reference-original.jpg","captureOriginal":"capture-original.jpg"},""" +
                """"capture":{"timestampMs":1000}}"""
        )
    }

    /** No HQ originals/overlay at all -> hasHqCaptureSource=false -> straight to fallback, whose
     * dimension/ratio Case A/B/C logic operates purely on the given reference.jpg/capture.jpg dims. */
    private fun createFallbackOnlySession(dir: File, refDims: Pair<Int, Int>, capDims: Pair<Int, Int>) {
        writeSyntheticJpeg(File(dir, "reference.jpg"), Color.rgb(80, 120, 180), refDims.first, refDims.second)
        writeSyntheticJpeg(File(dir, "capture.jpg"), Color.rgb(180, 120, 80), capDims.first, capDims.second)
        File(dir, "metadata.json").writeText(
            """{"version":6,"session":{"id":"wb-fallback-only-test","createdAtMs":1000},""" +
                """"viewport":{"width":${refDims.first},"height":${refDims.second},"orientation":"PORTRAIT"},""" +
                """"files":{"capture":"capture.jpg","reference":"reference.jpg"},""" +
                """"capture":{"timestampMs":1000}}"""
        )
    }

    /** Frame whose outer 5% along the long axis is red and whose center is green. */
    private fun writeEdgeBandedJpeg(file: File, width: Int, height: Int) {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.rgb(40, 170, 80))
        val paint = android.graphics.Paint().apply { color = Color.rgb(210, 40, 40) }
        if (height >= width) {
            val band = height * 0.05f
            canvas.drawRect(0f, 0f, width.toFloat(), band, paint)
            canvas.drawRect(0f, height - band, width.toFloat(), height.toFloat(), paint)
        } else {
            val band = width * 0.05f
            canvas.drawRect(0f, 0f, band, height.toFloat(), paint)
            canvas.drawRect(width - band, 0f, width.toFloat(), height.toFloat(), paint)
        }
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        bmp.recycle()
    }

    /** Banded HQ session: viewport [viewportW] x [viewportH], originals at 2x, all four files banded. */
    private fun createBandedHqSession(dir: File, viewportW: Int, viewportH: Int) {
        writeEdgeBandedJpeg(File(dir, "reference.jpg"), viewportW, viewportH)
        writeEdgeBandedJpeg(File(dir, "capture.jpg"), viewportW, viewportH)
        writeEdgeBandedJpeg(File(dir, "reference-original.jpg"), viewportW * 2, viewportH * 2)
        writeEdgeBandedJpeg(File(dir, "capture-original.jpg"), viewportW * 2, viewportH * 2)
        val orientation = if (viewportH >= viewportW) "PORTRAIT" else "LANDSCAPE"
        File(dir, "metadata.json").writeText(
            """{"version":6,"session":{"id":"wb-banded-hq-test","createdAtMs":1000},""" +
                """"viewport":{"width":$viewportW,"height":$viewportH,"orientation":"$orientation"},""" +
                """"overlay":{"scale":1.0,"offsetX":0.0,"offsetY":0.0,"displayMode":"COMPARE_WITH_PREVIEW"},""" +
                """"files":{"capture":"capture.jpg","reference":"reference.jpg",""" +
                """"referenceOriginal":"reference-original.jpg","captureOriginal":"capture-original.jpg"},""" +
                """"capture":{"timestampMs":1000}}"""
        )
    }

    /** Banded session with no HQ originals/overlay -> straight to the fallback path. */
    private fun createBandedFallbackSession(dir: File, viewportW: Int, viewportH: Int) {
        writeEdgeBandedJpeg(File(dir, "reference.jpg"), viewportW, viewportH)
        writeEdgeBandedJpeg(File(dir, "capture.jpg"), viewportW, viewportH)
        val orientation = if (viewportH >= viewportW) "PORTRAIT" else "LANDSCAPE"
        File(dir, "metadata.json").writeText(
            """{"version":6,"session":{"id":"wb-banded-fallback-test","createdAtMs":1000},""" +
                """"viewport":{"width":$viewportW,"height":$viewportH,"orientation":"$orientation"},""" +
                """"files":{"capture":"capture.jpg","reference":"reference.jpg"},""" +
                """"capture":{"timestampMs":1000}}"""
        )
    }
}
