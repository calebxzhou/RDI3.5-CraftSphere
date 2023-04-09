package calebxzhou.rdi.util

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence

/**
 * Created  on 2023-04-08,20:44.
 */
object FontUt {
    fun drawCenteredString(poseStack: PoseStack, font: Font, text: String, x: Int, y: Int, color: Int) {
        font.draw(poseStack, text, (x - font.width(text) / 2).toFloat(), y.toFloat(), color)
    }

    fun drawCenteredString(poseStack: PoseStack, font: Font, text: Component, x: Int, y: Int, color: Int) {
        val formattedCharSequence = text.visualOrderText
        font.draw(
            poseStack,
            formattedCharSequence,
            (x - font.width(formattedCharSequence) / 2).toFloat(),
            y.toFloat(),
            color
        )
    }

    fun drawCenteredString(
        poseStack: PoseStack,
        font: Font,
        text: FormattedCharSequence,
        x: Int,
        y: Int,
        color: Int
    ) {
        font.draw(poseStack, text, (x - font.width(text) / 2).toFloat(), y.toFloat(), color)
    }
}
