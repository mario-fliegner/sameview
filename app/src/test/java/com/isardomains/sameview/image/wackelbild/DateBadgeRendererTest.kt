// path: app/src/test/java/com/isardomains/sameview/image/wackelbild/DateBadgeRendererTest.kt
package com.isardomains.sameview.image.wackelbild

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions

class DateBadgeRendererTest {

    // ── computeGeometry(): output-relative fractions (Correction H) ─────────

    @Test
    fun computeGeometry_textSize_isShortEdgeTimesFraction() {
        val geometry = DateBadgeRenderer.computeGeometry(2000, 4000)
        assertEquals(2000f * DateBadgeRenderer.TEXT_SIZE_FRACTION, geometry.textSize, 0.01f)
    }

    @Test
    fun computeGeometry_edgeMargin_isShortEdgeTimesFraction() {
        val geometry = DateBadgeRenderer.computeGeometry(2000, 4000)
        assertEquals(2000f * DateBadgeRenderer.EDGE_MARGIN_FRACTION, geometry.edgeMargin, 0.01f)
    }

    @Test
    fun computeGeometry_horizontalPadding_isShortEdgeTimesFraction() {
        val geometry = DateBadgeRenderer.computeGeometry(2000, 4000)
        assertEquals(2000f * DateBadgeRenderer.PADDING_HORIZONTAL_FRACTION, geometry.paddingHorizontal, 0.01f)
    }

    @Test
    fun computeGeometry_verticalPadding_isShortEdgeTimesFraction() {
        val geometry = DateBadgeRenderer.computeGeometry(2000, 4000)
        assertEquals(2000f * DateBadgeRenderer.PADDING_VERTICAL_FRACTION, geometry.paddingVertical, 0.01f)
    }

    @Test
    fun computeGeometry_cornerRadius_isShortEdgeTimesFraction() {
        val geometry = DateBadgeRenderer.computeGeometry(2000, 4000)
        assertEquals(2000f * DateBadgeRenderer.CORNER_RADIUS_FRACTION, geometry.cornerRadius, 0.01f)
    }

    @Test
    fun computeGeometry_usesShortEdge_regardlessOfOrientation() {
        // Portrait (W < H) and Landscape (W > H) with the same short edge must produce identical
        // geometry -- same proportions for Portrait and Landscape.
        val portrait = DateBadgeRenderer.computeGeometry(1000, 2000)
        val landscape = DateBadgeRenderer.computeGeometry(2000, 1000)
        assertEquals(portrait.textSize, landscape.textSize, 0.001f)
        assertEquals(portrait.edgeMargin, landscape.edgeMargin, 0.001f)
        assertEquals(portrait.paddingHorizontal, landscape.paddingHorizontal, 0.001f)
        assertEquals(portrait.paddingVertical, landscape.paddingVertical, 0.001f)
        assertEquals(portrait.cornerRadius, landscape.cornerRadius, 0.001f)
    }

    @Test
    fun computeGeometry_scalesWithOutputResolution() {
        val small = DateBadgeRenderer.computeGeometry(1000, 1000)
        val large = DateBadgeRenderer.computeGeometry(4000, 4000)
        assertEquals(4f, large.textSize / small.textSize, 0.01f)
    }

    // ── computeBadgeRect(): bottom-right placement ────────────────────────────

    @Test
    fun computeBadgeRect_isAnchoredToBottomRight() {
        val geometry = DateBadgeRenderer.computeGeometry(2000, 3000)
        val rect = DateBadgeRenderer.computeBadgeRect(2000, 3000, textWidth = 300f, textHeight = 60f, geometry = geometry)
        val (left, top, right, bottom) = rect
        assertEquals(2000f - geometry.edgeMargin, right, 0.01f)
        assertEquals(3000f - geometry.edgeMargin, bottom, 0.01f)
        assertTrue("left must be less than right", left < right)
        assertTrue("top must be less than bottom", top < bottom)
    }

    @Test
    fun computeBadgeRect_transferEdgeMargin_isPinnedTo12Over360OfTheShortEdge_onBothAxes() {
        // Literals on purpose (not derived from EDGE_MARGIN_FRACTION): the transfer badge's margin is
        // deliberately 12/360 of the short edge -- larger than the preview's 8dp -- and an accidental
        // change of the value must fail here. 1080 * 12 / 360 = 36 px in both orientations.
        val portrait = DateBadgeRenderer.computeGeometry(1080, 1620)
        val portraitRect = DateBadgeRenderer.computeBadgeRect(1080, 1620, textWidth = 300f, textHeight = 60f, geometry = portrait)
        assertEquals(36f, portrait.edgeMargin, 0.01f)
        assertEquals(1044f, portraitRect[2], 0.01f)
        assertEquals(1584f, portraitRect[3], 0.01f)
        assertEquals("right and bottom margins must be equal (portrait)", 1080f - portraitRect[2], 1620f - portraitRect[3], 0.01f)

        val landscape = DateBadgeRenderer.computeGeometry(1620, 1080)
        val landscapeRect = DateBadgeRenderer.computeBadgeRect(1620, 1080, textWidth = 300f, textHeight = 60f, geometry = landscape)
        assertEquals(36f, landscape.edgeMargin, 0.01f)
        assertEquals(1584f, landscapeRect[2], 0.01f)
        assertEquals(1044f, landscapeRect[3], 0.01f)
        assertEquals("right and bottom margins must be equal (landscape)", 1620f - landscapeRect[2], 1080f - landscapeRect[3], 0.01f)
    }

    @Test
    fun computeBadgeRect_widthIncludesHorizontalPaddingBothSides() {
        val geometry = DateBadgeRenderer.computeGeometry(2000, 2000)
        val rect = DateBadgeRenderer.computeBadgeRect(2000, 2000, textWidth = 300f, textHeight = 60f, geometry = geometry)
        val badgeWidth = rect[2] - rect[0]
        assertEquals(300f + 2 * geometry.paddingHorizontal, badgeWidth, 0.01f)
    }

    @Test
    fun computeBadgeRect_heightIncludesVerticalPaddingBothSides() {
        val geometry = DateBadgeRenderer.computeGeometry(2000, 2000)
        val rect = DateBadgeRenderer.computeBadgeRect(2000, 2000, textWidth = 300f, textHeight = 60f, geometry = geometry)
        val badgeHeight = rect[3] - rect[1]
        assertEquals(60f + 2 * geometry.paddingVertical, badgeHeight, 0.01f)
    }

    // ── draw(): null/blank date -> no draw ────────────────────────────────────

    @Test
    fun draw_nullDate_neverTouchesBitmap() {
        val bitmap = mock<Bitmap>()
        DateBadgeRenderer.draw(bitmap, null)
        verifyNoInteractions(bitmap)
    }

    @Test
    fun draw_blankDate_neverTouchesBitmap() {
        val bitmap = mock<Bitmap>()
        DateBadgeRenderer.draw(bitmap, "   ")
        verifyNoInteractions(bitmap)
    }

    private operator fun FloatArray.component1() = this[0]
    private operator fun FloatArray.component2() = this[1]
    private operator fun FloatArray.component3() = this[2]
    private operator fun FloatArray.component4() = this[3]
}
