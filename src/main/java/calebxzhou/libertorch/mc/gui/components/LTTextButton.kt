package calebxzhou.libertorch.mc.gui.components

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.util.Mth

/**
 * Created  on 2023-02-26,21:27.
 */
class LTTextButton( x: Int,  y: Int,  width: Int,  height: Int, message: Component)
    : AbstractWidget(x, y, width, height, message) {

    private val underlinedMessage: MutableComponent = ComponentUtils.mergeStyles(message.copy(), Style.EMPTY.withUnderlined(true));
    override fun updateNarration(narrationElementOutput: NarrationElementOutput) {
    }

    override fun renderButton(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        GuiComponent.drawString(
            poseStack,
            Minecraft.getInstance().font,
            if (this.isHoveredOrFocused) underlinedMessage else message,
            x,y, 16777215 or (Mth.ceil(alpha * 255.0f) shl 24)
        )
    }

}
