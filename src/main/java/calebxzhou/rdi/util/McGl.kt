package calebxzhou.rdi.util

import calebxzhou.libertorch.ui.DefaultColors
import calebxzhou.libertorch.ui.LtColor
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.GuiComponent

/**
 * Created  on 2023-04-08,20:50.
 */
object McGl {
    //画矩形
    fun rect(poseStack: PoseStack, x1: Int, y1: Int, width: Int, height: Int, thick: Int, color: LtColor) {
        val x2 = x1 + width
        val y2 = y1 + height
        //上边
        GuiComponent.fill(poseStack, x1, y1, x2, y1 + thick, color.opaque )
        //下边
        GuiComponent.fill(poseStack, x1, y2, x2, y2 + thick, color.opaque )
        //左边
        GuiComponent.fill(poseStack, x1, y1, x1 + thick, y2, color.opaque )
        //右边
        GuiComponent.fill(poseStack, x2, y1, x2 + thick, y2, color.opaque )
    }
}
