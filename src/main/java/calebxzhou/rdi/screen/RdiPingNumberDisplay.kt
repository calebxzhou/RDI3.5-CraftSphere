package calebxzhou.rdi.screen

import calebxzhou.rdi.consts.PingColors
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.multiplayer.PlayerInfo

object RdiPingNumberDisplay {
    private const val PING_TEXT_RENDER_OFFSET = -13
    private const val PING_BARS_WIDTH = 11
    @JvmStatic
    fun renderPingNumber(
        client: Minecraft, matrixStack: PoseStack, width: Int, x: Int, y: Int, player: PlayerInfo
    ) {
        val pingString = "${player.latency}ms"
        val pingStringWidth = client.font.width(pingString)
        var textX = width + x - pingStringWidth + PING_TEXT_RENDER_OFFSET
        textX += PING_BARS_WIDTH
        // Draw the ping text for the given player
        client.font.drawShadow(matrixStack, pingString, textX.toFloat(), y.toFloat(), PingColors.getColor(player.latency))
        // If we don't render ping bars, we need to reset the render system color so the rest
        // of the player list renders properly
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
    }

    fun renderPingIcon(screen: Screen, poseStack: PoseStack, width: Int, x: Int, y: Int, latency: Int) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION)
        val lagLevel: Int = if (latency < 0) {
            5
        } else if (latency < 150) {
            0
        } else if (latency < 300) {
            1
        } else if (latency < 600) {
            2
        } else if (latency < 1000) {
            3
        } else {
            4
        }
        screen.blitOffset = screen.blitOffset + 100
        screen.blit(poseStack, x + width - 11, y, 0, 176 + lagLevel * 8, 10, 8)
        screen.blitOffset = screen.blitOffset - 100
    }
}
