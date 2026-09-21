// path: app/src/test/java/com/isardomains/sameview/image/wackelbild/WackelbildPrintTargetTest.kt
package com.isardomains.sameview.image.wackelbild

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** Pure JVM tests for [WackelbildPrintTarget]: family selection, centered crop, output check. */
class WackelbildPrintTargetTest {

    private fun select(width: Int, height: Int): WackelbildPrintTarget =
        checkNotNull(WackelbildPrintTarget.select(width, height)) { "expected a target for ${width}x$height" }

    // ── Family selection ─────────────────────────────────────────────────────

    @Test
    fun select_9x16AndItsLandscapeTwin_choose10x15() {
        assertEquals("10x15", select(1080, 1920).slug)
        assertEquals("10x15", select(1920, 1080).slug)
    }

    @Test
    fun select_2x3_chooses10x15() {
        assertEquals("10x15", select(1080, 1620).slug)
        assertEquals("10x15", select(1620, 1080).slug)
    }

    @Test
    fun select_aSeriesLike_choosesA6() {
        assertEquals("a6", select(1000, 1414).slug)
        assertEquals("a6", select(1414, 1000).slug)
    }

    @Test
    fun select_a6OwnRatio_choosesA6() {
        assertEquals("a6", select(1050, 1490).slug) // 10.5 : 14.9
    }

    @Test
    fun select_3x4AndItsLandscapeTwin_choose15x20() {
        assertEquals("15x20", select(1080, 1440).slug)
        assertEquals("15x20", select(1440, 1080).slug)
    }

    @Test
    fun select_4x5AndItsLandscapeTwin_choose15x20() {
        assertEquals("15x20", select(1080, 1350).slug)
        assertEquals("15x20", select(1350, 1080).slug)
    }

    @Test
    fun select_square_chooses15x15() {
        assertEquals("15x15", select(1080, 1080).slug)
    }

    @Test
    fun select_isOrientationIndependent_portraitAndLandscapeShareTheFamily() {
        for ((w, h) in listOf(1080 to 1920, 1000 to 1414, 1080 to 1440, 1080 to 1350, 900 to 1000)) {
            assertEquals("${w}x$h", select(w, h).slug, select(h, w).slug)
        }
    }

    @Test
    fun select_nonPositiveDimensions_returnNull() {
        assertNull(WackelbildPrintTarget.select(0, 1920))
        assertNull(WackelbildPrintTarget.select(1080, 0))
        assertNull(WackelbildPrintTarget.select(-1080, 1920))
        assertNull(WackelbildPrintTarget.select(1080, -1))
    }

    @Test
    fun select_carriesTheSlugRatioAndTheExactFrame() {
        val target = select(1080, 1920)
        assertEquals(10.0 / 15.0, target.shortOverLong, 0.0)
        assertEquals(1080, target.frameWidth)
        assertEquals(1920, target.frameHeight)
        assertEquals(10.5 / 14.9, select(1050, 1490).shortOverLong, 0.0)
    }

    @Test
    fun select_boundaries_switchExactlyAtTheGeometricMeansOfNeighbouringRatios() {
        // Boundaries sqrt(t_a * t_b): 0.685419 (10x15|a6), 0.726996 (a6|15x20), 0.866025 (15x20|15x15).
        assertEquals("10x15", select(685, 1000).slug)
        assertEquals("a6", select(686, 1000).slug)
        assertEquals("a6", select(726, 1000).slug)
        assertEquals("15x20", select(727, 1000).slug)
        assertEquals("15x20", select(866, 1000).slug)
        assertEquals("15x15", select(867, 1000).slug)
    }

    @Test
    fun select_isMonotonicInTheRatio_soTheFixedFamilyOrderIsTheOnlyTieBreak() {
        // No integer frame can sit exactly on a boundary (each squared boundary is irrational), so a
        // strict '<' over the fixed ascending family order never has to break a real tie. Sweeping the
        // ratio proves the mapping is a clean monotonic staircase: 10x15 -> a6 -> 15x20 -> 15x15.
        val order = listOf("10x15", "a6", "15x20", "15x15")
        var lastIndex = 0
        for (short in 1..1000) {
            val index = order.indexOf(select(short, 1000).slug)
            assertTrue("ratio $short/1000 went backwards ($index < $lastIndex)", index >= lastIndex)
            lastIndex = index
        }
        assertEquals(order.lastIndex, lastIndex)
    }

    // ── Aspect ───────────────────────────────────────────────────────────────

    @Test
    fun aspect_isWidthOverHeightInTheFramesOrientation() {
        assertEquals(2.0 / 3.0, select(1080, 1920).aspect, 1e-12)   // portrait: t
        assertEquals(3.0 / 2.0, select(1920, 1080).aspect, 1e-12)   // landscape: 1 / t
        assertEquals(1.0, select(1080, 1080).aspect, 1e-12)         // square
        assertEquals(0.75, select(1080, 1440).aspect, 1e-12)
        assertEquals(4.0 / 3.0, select(1440, 1080).aspect, 1e-12)
    }

    // ── Centered crop ────────────────────────────────────────────────────────

    @Test
    fun cropRect_portrait9x16To2x3_cropsTopAndBottomEqually() {
        assertEquals(WackelbildCropRect(0, 150, 1080, 1620), select(1080, 1920).cropRect(1080, 1920))
    }

    @Test
    fun cropRect_landscape16x9To3x2_cropsLeftAndRightEqually() {
        assertEquals(WackelbildCropRect(150, 0, 1620, 1080), select(1920, 1080).cropRect(1920, 1080))
    }

    @Test
    fun cropRect_frameAlreadyMatchingTheTarget_isNull() {
        assertNull(select(1080, 1620).cropRect(1080, 1620))
        assertNull(select(1620, 1080).cropRect(1620, 1080))
        assertNull(select(1080, 1440).cropRect(1080, 1440))
        assertNull(select(1080, 1080).cropRect(1080, 1080))
    }

    @Test
    fun cropRect_frameRelativelyTooWide_cropsTheShortAxis() {
        // 4:5 -> 3:4 keeps the full height (1350) and trims the width; 1350 * 0.75 = 1012.5 -> 1013 -> even 1012.
        assertEquals(WackelbildCropRect(34, 0, 1012, 1350), select(1080, 1350).cropRect(1080, 1350))
        // A-like frame slightly wider than a6's own ratio.
        assertEquals(WackelbildCropRect(2, 0, 996, 1414), select(1000, 1414).cropRect(1000, 1414))
    }

    @Test
    fun cropRect_squareTarget_croppingASlightlyNarrowerFrame_keepsFullWidth() {
        assertEquals(WackelbildCropRect(0, 65, 870, 870), select(870, 1000).cropRect(870, 1000))
    }

    @Test
    fun cropRect_dependsOnlyOnDimensions_soAnyRenderScaleOfTheFrameGetsTheSameNormalizedCrop() {
        val target = select(1080, 1920)
        val small = checkNotNull(target.cropRect(540, 960))
        val large = checkNotNull(target.cropRect(2160, 3840))
        assertEquals(WackelbildCropRect(0, 75, 540, 810), small)
        assertEquals(WackelbildCropRect(0, 300, 2160, 3240), large)
        // Same normalized window: top at 150/1920 = 75/960 = 300/3840 of the height.
        assertEquals(small.top / 960.0, large.top / 3840.0, 1e-12)
    }

    @Test
    fun cropRect_isCentered_neverOffsetsByMoreThanOnePixel() {
        for ((w, h) in listOf(1080 to 1920, 1920 to 1080, 1082 to 1923, 1000 to 1414, 1080 to 1350, 900 to 1000, 4000 to 7111)) {
            val target = select(w, h)
            val crop = target.cropRect(w, h) ?: continue
            assertTrue("${w}x$h horizontal", kotlin.math.abs(crop.left - (w - crop.left - crop.width)) <= 1)
            assertTrue("${w}x$h vertical", kotlin.math.abs(crop.top - (h - crop.top - crop.height)) <= 1)
            assertTrue("${w}x$h within frame", crop.left >= 0 && crop.top >= 0 &&
                crop.left + crop.width <= w && crop.top + crop.height <= h)
        }
    }

    @Test
    fun cropRect_onlyTheCroppedAxisIsEvened_theOtherKeepsItsFullExtent() {
        val crop = checkNotNull(select(1081, 1921).cropRect(1081, 1921))
        assertEquals(1081, crop.width)          // untouched axis, odd, kept
        assertEquals(0, crop.height % 2)        // cropped axis, evened
    }

    @Test
    fun cropRect_nonPositiveFrame_throws() {
        try {
            select(1080, 1920).cropRect(0, 1920)
            fail("expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    // ── Output verification ──────────────────────────────────────────────────

    @Test
    fun matchesOutput_exactAndCroppedOutputs_areAccepted() {
        val portrait = select(1080, 1920)
        assertTrue(portrait.matchesOutput(1080, 1620))
        assertTrue(select(1920, 1080).matchesOutput(1620, 1080))
        assertTrue(select(1080, 1080).matchesOutput(1080, 1080))
    }

    @Test
    fun matchesOutput_everyCropRectOutput_isAcceptedForManyFrames() {
        val frames = listOf(
            1080 to 1920, 1920 to 1080, 1082 to 1923, 1000 to 1414, 1414 to 1000, 1080 to 1350,
            1350 to 1080, 900 to 1000, 4000 to 7111, 64 to 113, 1080 to 1440, 1234 to 2001
        )
        for ((w, h) in frames) {
            val target = select(w, h)
            val crop = target.cropRect(w, h)
            val outW = crop?.width ?: w
            val outH = crop?.height ?: h
            assertTrue("${w}x$h -> ${outW}x$outH", target.matchesOutput(outW, outH))
        }
    }

    @Test
    fun matchesOutput_roundingAllowanceIsTwoPixels() {
        val target = select(1080, 1920) // 2:3, allowance 2 / 1080 ~ 0.185% of the aspect
        assertTrue(target.matchesOutput(1080, 1622))  // ~0.12% off
        assertFalse(target.matchesOutput(1080, 1626)) // ~0.37% off
    }

    @Test
    fun matchesOutput_uncroppedFrameAndSwappedOrientation_areRejected() {
        val portrait = select(1080, 1920)
        assertFalse(portrait.matchesOutput(1080, 1920)) // full 9:16 frame is not the 2:3 target
        assertFalse(portrait.matchesOutput(1620, 1080)) // orientation swap = reciprocal aspect
        assertFalse(select(1920, 1080).matchesOutput(1080, 1620))
    }

    @Test
    fun matchesOutput_nonPositiveOutput_isRejected() {
        val target = select(1080, 1920)
        assertFalse(target.matchesOutput(0, 1620))
        assertFalse(target.matchesOutput(1080, -1))
    }

    @Test
    fun targets_areValueEqualForTheSameFrame() {
        assertEquals(select(1080, 1920), select(1080, 1920))
        assertNotNull(WackelbildPrintTarget.select(1, 1))
    }
}
