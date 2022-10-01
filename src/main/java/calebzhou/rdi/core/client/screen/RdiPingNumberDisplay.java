package calebzhou.rdi.core.client.screen;

import calebzhou.rdi.core.client.constant.PingColors;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;

import static net.minecraft.client.gui.GuiComponent.GUI_ICONS_LOCATION;

public final class RdiPingNumberDisplay {
	private static final int PING_TEXT_RENDER_OFFSET = -13;
	private static final int PING_BARS_WIDTH = 11;

	public static void renderPingNumber(
			Minecraft client, PoseStack matrixStack, int width, int x, int y, PlayerInfo player) {
		Font textRenderer = client.font;

		String pingString = "%dms".formatted(player.getLatency());
		int pingStringWidth = textRenderer.width(pingString);
		int pingTextColor = PingColors.getColor(player.getLatency());
		int textX = width + x - pingStringWidth + PING_TEXT_RENDER_OFFSET;
		textX += PING_BARS_WIDTH;
		// Draw the ping text for the given player
		textRenderer.drawShadow(matrixStack, pingString, (float) textX, (float) y, pingTextColor);
		// If we don't render ping bars, we need to reset the render system color so the rest
		// of the player list renders properly
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}
	public static void renderPingIcon(Screen screen, PoseStack poseStack, int width, int x, int y,int latency) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
		int lagLevel;
		if (latency < 0) {
			lagLevel = 5;
		} else if (latency < 150) {
			lagLevel = 0;
		} else if (latency < 300) {
			lagLevel = 1;
		} else if (latency < 600) {
			lagLevel = 2;
		} else if (latency < 1000) {
			lagLevel = 3;
		} else {
			lagLevel = 4;
		}
		screen.setBlitOffset(screen.getBlitOffset() + 100);
		screen.blit(poseStack, x + width - 11, y, 0, 176 + lagLevel * 8, 10, 8);
		screen.setBlitOffset(screen.getBlitOffset() - 100);
	}
}
