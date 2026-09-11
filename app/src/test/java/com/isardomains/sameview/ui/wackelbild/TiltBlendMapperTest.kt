// path: app/src/test/java/com/isardomains/sameview/ui/wackelbild/TiltBlendMapperTest.kt
package com.isardomains.sameview.ui.wackelbild

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TiltBlendMapperTest {

    private fun mapper(maxUsefulTiltDegrees: Float = 24f, emaAlpha: Float = 0.2f) =
        TiltBlendMapper(maxUsefulTiltDegrees = maxUsefulTiltDegrees, emaAlpha = emaAlpha)

    // --- neutral / first reading ---

    @Test
    fun firstReading_atNeutral_returnsHalf() {
        val fraction = mapper().onRawRollDegrees(rawRollDegrees = 0f, neutralRollDegrees = 0f)
        assertEquals(0.5f, fraction, 0.0001f)
    }

    @Test
    fun firstReading_towardCapture_returnsAboveHalf() {
        // wrappedDelta = 9, first call initializes smoothedDelta directly to it (no blend from
        // zero) -> fraction = 0.5 + 9/48.
        val fraction = mapper().onRawRollDegrees(rawRollDegrees = 9f, neutralRollDegrees = 0f)
        assertEquals(0.5f + 9f / 48f, fraction, 0.0001f)
    }

    @Test
    fun firstReading_towardReference_returnsBelowHalf() {
        val fraction = mapper().onRawRollDegrees(rawRollDegrees = -9f, neutralRollDegrees = 0f)
        assertEquals(0.5f - 9f / 48f, fraction, 0.0001f)
    }

    // --- monotonicity ---

    @Test
    fun monotonic_increasingTiltTowardCapture_increasesFraction() {
        val m = mapper()
        var previous = m.onRawRollDegrees(0f, 0f)
        for (raw in listOf(4f, 8f, 12f, 16f, 20f)) {
            val next = m.onRawRollDegrees(raw, 0f)
            assertTrue("expected $next >= $previous for raw=$raw", next >= previous)
            previous = next
        }
    }

    @Test
    fun monotonic_decreasingTiltTowardReference_decreasesFraction() {
        val m = mapper()
        var previous = m.onRawRollDegrees(0f, 0f)
        for (raw in listOf(-4f, -8f, -12f, -16f, -20f)) {
            val next = m.onRawRollDegrees(raw, 0f)
            assertTrue("expected $next <= $previous for raw=$raw", next <= previous)
            previous = next
        }
    }

    // --- clamping / endpoints ---

    @Test
    fun clamping_wellBeyondMaxUsefulTilt_clampsToFullCapture() {
        val fraction = mapper().onRawRollDegrees(rawRollDegrees = 100f, neutralRollDegrees = 0f)
        assertEquals(1f, fraction, 0.0001f)
    }

    @Test
    fun clamping_wellBeyondMaxUsefulTilt_negative_clampsToFullReference() {
        val fraction = mapper().onRawRollDegrees(rawRollDegrees = -100f, neutralRollDegrees = 0f)
        assertEquals(0f, fraction, 0.0001f)
    }

    // --- angle-wrap safety ---

    @Test
    fun angleWrap_acrossBoundary_treatsAsSmallPhysicalDelta() {
        // raw=-170, neutral=170: naive (unwrapped) delta would be -340 (clamps to full Reference,
        // 0f), but the true physical difference is only 20 degrees (170 -> 180/-180 -> -170).
        // wrapAngleDegrees(-340) == 20, so this must resolve near the Capture side, not clamp to
        // full Reference -- proving the mapper smooths the wrapped delta, not the raw difference.
        val fraction = mapper().onRawRollDegrees(rawRollDegrees = -170f, neutralRollDegrees = 170f)
        assertEquals(0.5f + 20f / 48f, fraction, 0.0001f)
    }

    // --- reset() ---

    @Test
    fun reset_clearsSmoothingHistory_nextReadingReinitializesFresh() {
        val m = mapper()
        m.onRawRollDegrees(20f, 0f)
        m.onRawRollDegrees(20f, 0f)
        m.reset()

        val afterReset = m.onRawRollDegrees(-15f, 0f)
        val freshMapperSameReading = mapper().onRawRollDegrees(-15f, 0f)

        // Identical to a brand-new mapper's first reading -- no blending with pre-reset history.
        assertEquals(freshMapperSameReading, afterReset, 0.0001f)
    }

    // --- seedToFraction() ---

    @Test
    fun seedToFraction_half_isNeutral_andNextZeroDeltaReadingStaysNeutral() {
        val m = mapper()
        m.seedToFraction(0.5f)
        val fraction = m.onRawRollDegrees(rawRollDegrees = 0f, neutralRollDegrees = 0f)
        assertEquals(0.5f, fraction, 0.0001f)
    }

    @Test
    fun seedToFraction_zero_blendsWithSubsequentReading_ratherThanBeingIgnoredOrOverwritten() {
        val m = mapper()
        m.seedToFraction(0f) // seeds smoothedDelta = -24
        // A reading whose own unblended fraction would be 1f (fresh mapper first-call value).
        val fraction = m.onRawRollDegrees(rawRollDegrees = 24f, neutralRollDegrees = 0f)
        // 0.2*24 + 0.8*(-24) = -14.4 -> fraction = 0.5 - 14.4/48 = 0.2 -- strictly between the
        // pinned seed (0f) and the raw reading's own unblended value (1f), proving the seed was
        // applied and blended, not ignored and not simply overwritten wholesale.
        assertEquals(0.2f, fraction, 0.0001f)
        assertTrue(fraction > 0f)
        assertTrue(fraction < 1f)
    }

    @Test
    fun seedToFraction_one_blendsSymmetrically() {
        val m = mapper()
        m.seedToFraction(1f) // seeds smoothedDelta = +24
        val fraction = m.onRawRollDegrees(rawRollDegrees = -24f, neutralRollDegrees = 0f)
        // 0.2*(-24) + 0.8*24 = 14.4 -> fraction = 0.5 + 14.4/48 = 0.8
        assertEquals(0.8f, fraction, 0.0001f)
        assertTrue(fraction < 1f)
        assertTrue(fraction > 0f)
    }

    // --- bounded first resumed step (manual-override release, §7.7) ---

    @Test
    fun boundedFirstResumedStep_referenceToCapture_matchesWorkedExample() {
        val m = mapper()
        m.seedToFraction(0f) // manual Reference pin
        // Release fires on a re-arm TOWARD_CAPTURE transition at delta ~= +9 degrees.
        val firstStep = m.onRawRollDegrees(rawRollDegrees = 9f, neutralRollDegrees = 0f)
        // 0.2*9 + 0.8*(-24) = -17.4 -> fraction = 0.5 - 17.4/48 ~= 0.1375
        assertEquals(0.1375f, firstStep, 0.001f)
        // A bounded first step, not a jump to the live raw value's own fraction (~0.6875) and not
        // still pinned at the old endpoint (0f).
        assertTrue(firstStep > 0f)
        assertTrue(firstStep < 0.5f + 9f / 48f)
    }

    @Test
    fun boundedFirstResumedStep_captureToReference_matchesWorkedExample() {
        val m = mapper()
        m.seedToFraction(1f) // manual Capture pin
        val firstStep = m.onRawRollDegrees(rawRollDegrees = -9f, neutralRollDegrees = 0f)
        // 0.2*(-9) + 0.8*24 = 17.4 -> fraction = 0.5 + 17.4/48 ~= 0.8625
        assertEquals(0.8625f, firstStep, 0.001f)
        assertTrue(firstStep < 1f)
        assertTrue(firstStep > 0.5f - 9f / 48f)
    }

    // --- convergence / no overshoot for held input ---

    @Test
    fun convergence_heldInputAfterResume_monotonicallyApproachesLiveValue_noOvershoot() {
        val m = mapper()
        m.seedToFraction(0f)
        val liveValue = 0.5f + 9f / 48f // steady-state fraction for a held raw=9 reading

        var previous = m.onRawRollDegrees(9f, 0f)
        assertTrue(previous < liveValue)
        // alpha=0.2 -> (1-alpha)^k decays geometrically; 30 held ticks is comfortably enough to
        // converge within the tolerance below without the test depending on a razor-thin margin.
        repeat(30) {
            val next = m.onRawRollDegrees(9f, 0f)
            assertTrue("expected monotonic increase toward $liveValue", next >= previous)
            assertTrue("must not overshoot the held live value", next <= liveValue + 0.0001f)
            previous = next
        }
        // After enough held ticks, converges close to the live value.
        assertEquals(liveValue, previous, 0.01f)
    }

    // --- noisy-input stability ---

    @Test
    fun noisyInput_stability_boundedPerTickChange() {
        val m = mapper()
        // Settle near delta ~= 10 first.
        repeat(10) { m.onRawRollDegrees(10f, 0f) }

        val noisySequence = listOf(11f, 9f, 10f, 12f, 8f, 10f, 9f, 11f)
        var previous = m.onRawRollDegrees(noisySequence.first(), 0f)
        for (raw in noisySequence.drop(1)) {
            val next = m.onRawRollDegrees(raw, 0f)
            assertTrue(
                "per-tick fraction change too large for noisy input: |${next - previous}|",
                abs(next - previous) < 0.05f
            )
            previous = next
        }
    }
}
