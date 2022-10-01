package calebzhou.rdi.core.client.screen;

import calebzhou.rdi.core.client.misc.ServerConnector;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class RdiLoadingOverlay extends Overlay {
	private long startTime;
	private long fadeOutStart = -1L;
    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;

    public RdiLoadingOverlay(Minecraft minecraft, ReloadInstance reload, Consumer<Optional<Throwable>> exceptionHandler) {
        //ping一下服务器提高载入速度
        ServerConnector.ping();
        this.minecraft = minecraft;
        this.reload = reload;
        this.onFinish = exceptionHandler;
		this.startTime = System.currentTimeMillis();
    }
    @Override
    public boolean isPauseScreen() {
        return true;
    }
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		int scaledWidth = this.minecraft.getWindow().getGuiScaledWidth();
		int scaledHeight = this.minecraft.getWindow().getGuiScaledHeight();
		long timeNow = System.currentTimeMillis();
		float f = this.fadeOutStart > -1L ? (float)(timeNow - this.fadeOutStart) / 1000.0F : -1.0F;
		GlStateManager._clearColor(0.9f, 0.9f, 0.9f, 1.0F);
		GlStateManager._clear(16384, Minecraft.ON_OSX);
		RenderSystem.enableBlend();
		if (f < 1.0F) {
			this.drawProgressBar(poseStack, scaledHeight/2 - 5, scaledWidth , scaledHeight/2 + 5, timeNow);
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
	//到2秒 加载条就弹回去
	float maximumLoadBarLength = 2000;
    private void drawProgressBar(PoseStack matrices, int minY, int maxX, int maxY, long timeNow) {
		float timeRatio = (timeNow - startTime) / maximumLoadBarLength;
		if(timeRatio>1){
			timeRatio = timeRatio - (int)timeRatio;
			maximumLoadBarLength = RandomUtils.nextFloat(2700f,4500f);
		}
		try {
			List<String> logLines = FileUtils.readLines(new File("./logs/latest.log"), StandardCharsets.UTF_8);
			Minecraft.getInstance().getWindow().setTitle(logLines.get(logLines.size()-1));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		int progressToDisplayPixels = Math.round (maxX*timeRatio);
        int barColor = net.minecraft.util.FastColor.ARGB32.color(255, 64, 64, 64);
        fill(matrices, 2, minY + 2, progressToDisplayPixels, maxY - 2, barColor);
    }

}
