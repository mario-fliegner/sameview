// path: app/src/main/java/com/isardomains/sameview/ui/wackelbild/TiltBlendMapper.kt
package com.isardomains.sameview.ui.wackelbild

/**
 * Maps a stream of device-roll readings into a continuous preview-blend fraction, for the
 * lenticular preview's tilt-driven Reference/Capture blend (`DEINWACKELBILD_INTEGRATION_V1.md`
 * §8.1/§8.3). Complements, and does not replace, [TiltHysteresisStateMachine] -- that class still
 * owns the discrete state used for accessibility identity and sensor/manual arbitration
 * (`WackelbildViewModel`); this class only ever produces the continuous rendering weight.
 *
 * Output convention: `0f` = full Reference, `0.5f` = calibrated neutral, `1f` = full Capture --
 * matches [TiltHysteresisStateMachine]'s existing sign convention (`delta > +thresholdDegrees`
 * already means "toward Capture"), so no inversion is applied here.
 *
 * Smooths the already angle-wrap-safe *delta* (roll relative to the caller's calibrated neutral),
 * never the raw roll angle directly -- a plain EMA over raw roll is invalid across the ±180° wrap
 * boundary (e.g. 179° -> -179° is physically a ~2° change but numerically a ~358° jump). Smoothing
 * the wrapped delta avoids that for the normal operating range, since delta stays well inside
 * [maxUsefulTiltDegrees], far from the wrap boundary; an extreme whole-device spin within a single
 * sensor event is not specially handled here, matching this codebase's existing scope (neither
 * does [TiltHysteresisStateMachine] handle sub-sample extreme spin rates).
 */
class TiltBlendMapper(
    private val maxUsefulTiltDegrees: Float = MAX_USEFUL_TILT_DEGREES,
    private val emaAlpha: Float = EMA_ALPHA
) {

    private var smoothedDelta: Float? = null

    /**
     * Feeds one new raw roll reading and returns the current continuous blend fraction, clamped
     * to `[0f, 1f]`.
     *
     * The first call after construction/[reset] initializes the smoothing state directly to that
     * reading's wrapped delta (not blended from zero), avoiding a slow ramp-up bias.
     */
    fun onRawRollDegrees(rawRollDegrees: Float, neutralRollDegrees: Float): Float {
        val wrappedDelta = TiltHysteresisStateMachine.wrapAngleDegrees(rawRollDegrees - neutralRollDegrees)
        val previous = smoothedDelta
        val updated = if (previous == null) {
            wrappedDelta
        } else {
            emaAlpha * wrappedDelta + (1f - emaAlpha) * previous
        }
        smoothedDelta = updated
        return fractionFor(updated)
    }

    /**
     * Clears smoothing history. Called whenever the caller's neutral calibration is reset (screen
     * activation/deactivation/leave), so stale pre-recalibration history never leaks into a new
     * calibration -- the next [onRawRollDegrees] call re-initializes fresh, per its own doc above.
     */
    fun reset() {
        smoothedDelta = null
    }

    /**
     * Seeds the smoothing state to exactly the delta that produces [fraction], for the
     * seed-and-freeze manual-override design (`WackelbildViewModel.manualToggle`): pinning the
     * exposed blend to a manually selected endpoint while sensor feeding is paused, so that when
     * feeding resumes later it starts converging from the pinned endpoint rather than jumping to
     * whatever the live sensor position happens to be. Neutral-independent by construction -- no
     * neutral value is needed here since seeding operates directly in delta space.
     */
    fun seedToFraction(fraction: Float) {
        smoothedDelta = (fraction.coerceIn(0f, 1f) - 0.5f) * 2f * maxUsefulTiltDegrees
    }

    private fun fractionFor(delta: Float): Float =
        (0.5f + delta / (2f * maxUsefulTiltDegrees)).coerceIn(0f, 1f)

    companion object {
        // TODO(real-device tuning): candidate starting value pending the mandatory real-device
        // tuning pass -- see DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md §7.7/§25/§30. Chosen larger
        // than the locked discrete TiltHysteresisStateMachine.THRESHOLD_DEGREES (9f) so the
        // continuous blend has ramp room beyond the discrete switch point.
        const val MAX_USEFUL_TILT_DEGREES = 24f

        // TODO(real-device tuning): candidate starting value pending the mandatory real-device
        // tuning pass -- see DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md §7.7/§25/§30.
        const val EMA_ALPHA = 0.2f
    }
}
