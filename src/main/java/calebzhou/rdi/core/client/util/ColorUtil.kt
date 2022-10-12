package calebzhou.rdi.core.client.util

import com.google.common.annotations.VisibleForTesting
import kotlin.math.roundToInt

object ColorUtil {
    fun interpolate(colorStart: Int, colorEnd: Int, offset: Float): Int {
        require(!(offset < 0 || offset > 1)) { "Offset must be between 0.0 and 1.0" }
        val redDiff = getRed(colorEnd) - getRed(colorStart)
        val greenDiff = getGreen(colorEnd) - getGreen(colorStart)
        val blueDiff = getBlue(colorEnd) - getBlue(colorStart)
        val newRed = (getRed(colorStart) + redDiff * offset).roundToInt()
        val newGreen = (getGreen(colorStart) + greenDiff * offset).roundToInt()
        val newBlue = (getBlue(colorStart) + blueDiff * offset).roundToInt()
        return newRed shl 16 or (newGreen shl 8) or newBlue
    }

    @VisibleForTesting
    fun getRed(color: Int): Int {
        return color shr 16 and 0xFF
    }

    @VisibleForTesting
    fun getGreen(color: Int): Int {
        return color shr 8 and 0xFF
    }

    @VisibleForTesting
    fun getBlue(color: Int): Int {
        return color and 0xFF
    }
}
