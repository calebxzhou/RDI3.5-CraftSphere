package calebxzhou.libertorch.mc.gui.components

import calebxzhou.libertorch.mc.gui.LtTheme
import calebxzhou.rdi.util.McGl
import com.mojang.blaze3d.vertex.PoseStack

/**
 * Created  on 2023-04-10,19:46.
 */
open class LtSlider {
    companion object{
        @JvmStatic
        fun render(
            poseStack: PoseStack,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            value: Double,
            isHover: Boolean
        ) {
            if (isHover) {
                //鼠标悬浮在滑条上面，就画一个滑块
                val xPos = (x + value * (width - 8)).toInt()
                McGl.rect(
                    poseStack,
                    xPos, y, 3, height, 1, LtTheme.now.sliderHandleColor
                )
            }
        }
    }
}
