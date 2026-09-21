// path: app/src/main/java/com/isardomains/sameview/image/wackelbild/WackelbildPrintTarget.kt
package com.isardomains.sameview.image.wackelbild

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** A pixel rectangle inside a frame: `[left, left + width) × [top, top + height)`. */
data class WackelbildCropRect(val left: Int, val top: Int, val width: Int, val height: Int)

/**
 * The single Wackelbild print-format decision for one session: which DeinWackelbild format the
 * preview shows and the transfer JPEGs are cropped to, and which `format` slug is handed off.
 *
 * Selected exactly once from the session's stable integer frame (never from rounded output JPEG
 * dimensions) and then carried unchanged through preview, renderer and handoff. Pure Kotlin --
 * no Android dependency -- so the whole rule is JVM-unit-testable.
 *
 * [shortOverLong] is the chosen slug's own orientation-independent ratio; [frameWidth]/
 * [frameHeight] are the exact frame the selection was made from and fix the target's orientation.
 */
data class WackelbildPrintTarget(
    val slug: String,
    val shortOverLong: Double,
    val frameWidth: Int,
    val frameHeight: Int
) {

    /** Target width / height in the frame's orientation -- the preview box aspect. */
    val aspect: Double
        get() = aspectFor(frameWidth, frameHeight)

    /**
     * The centered crop that turns a [width] × [height] render of the session frame into this
     * target's aspect, cropping only the excess axis -- never stretching, never letterboxing.
     * Returns `null` when the frame already has the target aspect (nothing to crop). The cropped
     * axis is made even (same rule as [WackelbildDimensionResolver.makeEven]); the other axis keeps
     * its full extent. Deterministic in `(width, height)` alone, so the Reference and Capture of
     * one pair -- rendered at the same dimensions -- always receive the identical rectangle.
     */
    fun cropRect(width: Int, height: Int): WackelbildCropRect? {
        require(width > 0 && height > 0) { "frame dimensions must be positive: ${width}x$height" }
        val targetAspect = aspectFor(width, height)
        val frameAspect = width.toDouble() / height
        val cropWidth: Int
        val cropHeight: Int
        if (frameAspect > targetAspect) {
            // Frame is relatively too wide: keep full height, crop width.
            cropWidth = WackelbildDimensionResolver.makeEven((height * targetAspect).roundToInt()).coerceAtMost(width)
            cropHeight = height
        } else {
            // Frame is relatively too tall (or equal): keep full width, crop height.
            cropWidth = width
            cropHeight = WackelbildDimensionResolver.makeEven((width / targetAspect).roundToInt()).coerceAtMost(height)
        }
        if (cropWidth == width && cropHeight == height) return null
        return WackelbildCropRect(
            left = (width - cropWidth) / 2,
            top = (height - cropHeight) / 2,
            width = cropWidth,
            height = cropHeight
        )
    }

    /**
     * True when a rendered [width] × [height] output has this target's aspect within the crop's own
     * integer rounding: `|w/h − aspect| / aspect ≤ 2 / min(w, h)`. A rounded/even-adjusted crop
     * deviates by at most 1.5 px on one axis, so 2 px is the minimal allowance; the check also
     * rejects an orientation swap, because a swapped aspect is its reciprocal.
     */
    fun matchesOutput(width: Int, height: Int): Boolean {
        if (width <= 0 || height <= 0) return false
        val relativeError = abs(width.toDouble() / height - aspect) / aspect
        return relativeError <= OUTPUT_ROUNDING_ALLOWANCE_PX / min(width, height)
    }

    private fun aspectFor(width: Int, height: Int): Double =
        if (width <= height) shortOverLong else 1.0 / shortOverLong

    companion object {
        /** Rounded crop dimensions deviate by at most 1.5 px; 2 px is the minimal allowance. */
        private const val OUTPUT_ROUNDING_ALLOWANCE_PX = 2.0

        private class Family(val slug: String, val shortOverLong: Double)

        // Fixed order (ascending ratio) doubles as the formal tie-break; the canonical slug of
        // each partner ratio family. The A-series target is A6's own ratio, 10.5 / 14.9.
        private val FAMILIES = listOf(
            Family("10x15", 10.0 / 15.0),
            Family("a6", 10.5 / 14.9),
            Family("15x20", 15.0 / 20.0),
            Family("15x15", 1.0)
        )

        /**
         * Picks the family with the smallest relative cover-crop loss `max(r, t) / min(r, t)` for
         * `r = shortSide / longSide` of the frame; strict `<`, so a tie keeps the earlier family.
         * `null` for non-positive dimensions.
         */
        fun select(frameWidth: Int, frameHeight: Int): WackelbildPrintTarget? {
            if (frameWidth <= 0 || frameHeight <= 0) return null
            val r = min(frameWidth, frameHeight).toDouble() / max(frameWidth, frameHeight)
            var best = FAMILIES.first()
            var bestLoss = Double.MAX_VALUE
            for (family in FAMILIES) {
                val loss = max(r, family.shortOverLong) / min(r, family.shortOverLong)
                if (loss < bestLoss) {
                    best = family
                    bestLoss = loss
                }
            }
            return WackelbildPrintTarget(best.slug, best.shortOverLong, frameWidth, frameHeight)
        }
    }
}
