package calebzhou.rdi.core.client.screen;

import calebzhou.rdi.core.client.misc.ServerConnector;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.function.Consumer;

public class RdiLoadingOverlay extends Overlay {
	private long fadeOutStart = -1L;
    private float currentProgress;

    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;

    public RdiLoadingOverlay(Minecraft minecraft, ReloadInstance reload, Consumer<Optional<Throwable>> exceptionHandler) {
        //ping一下服务器提高载入速度
        ServerConnector.ping();
        this.minecraft = minecraft;
        this.reload = reload;
        this.onFinish = exceptionHandler;
    }
    @Override
    public boolean isPauseScreen() {
        return true;
    }
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		int scaledWidth = this.minecraft.getWindow().getGuiScaledWidth();
		int scaledHeight = this.minecraft.getWindow().getGuiScaledHeight();
		long millis = Util.getMillis();
		float f = this.fadeOutStart > -1L ? (float)(millis - this.fadeOutStart) / 1000.0F : -1.0F;
		GlStateManager._clearColor(0.9f, 0.9f, 0.9f, 1.0F);
		GlStateManager._clear(16384, Minecraft.ON_OSX);
		RenderSystem.enableBlend();
		RenderSystem.blendEquation(32774);
		RenderSystem.blendFunc(770, 1);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		float t = this.reload.getActualProgress();
		this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + t * 0.050000012F, 0.0F, 1.0F);
		if (f < 1.0F) {
			this.drawProgressBar(poseStack, 0, scaledHeight/2 - 5, scaledWidth , scaledHeight/2 + 5, 1.0F - Mth.clamp(f, 0.0F, 1.0F));
		}
		if (f >= 1.0F) {
			this.minecraft.setOverlay(null);
		}
		if (this.fadeOutStart == -1L && this.reload.isDone()) {
			try {
				this.reload.checkExceptions();
				this.onFinish.accept(Optional.empty());
			} catch (Throwable var23) {
				this.onFinish.accept(Optional.of(var23));
			}

			this.fadeOutStart = Util.getMillis();
			if (this.minecraft.screen != null) {
				this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
			}
		}
    }
    private void drawProgressBar(PoseStack matrices, int minX, int minY, int maxX, int maxY, float opacity) {
        int i = Mth.ceil((float)(maxX - minX - 2) * this.currentProgress);
        int j = Math.round(opacity * 255.0f);
        int k = net.minecraft.util.FastColor.ARGB32.color(j, 64, 64, 64);
        fill(matrices, minX + 2, minY + 2, minX + i, maxY - 2, k);
    }

}
