package calebzhou.rdi.craftsphere.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;
import java.util.function.Consumer;

import static calebzhou.rdi.craftsphere.texture.LogoTexture.LOGO;

public class LoadingOverlay extends Overlay {
    public static final CubeMapRenderer PANORAMA_CUBE_MAP = new CubeMapRenderer(new Identifier("textures/gui/title/background/panorama"));
    private static final Identifier PANORAMA_OVERLAY = new Identifier("textures/gui/title/background/panorama_overlay.png");
    private final RotatingCubeMapRenderer backgroundRenderer = new RotatingCubeMapRenderer(PANORAMA_CUBE_MAP);
    private static final int FADE_DURATION = 1000;
    private final boolean reloading=true;
    private float progress;
    private long reloadCompleteTime = -1L;
    private long reloadStartTime = -1L;
    private long backgroundFadeStart;
    private float loadProgress;


    private final MinecraftClient client;
    private final ResourceReload reload;
    private final Consumer<Optional<Throwable>> exceptionHandler;

    public LoadingOverlay(MinecraftClient client, ResourceReload reload, Consumer<Optional<Throwable>> exceptionHandler) {
        this.client=client;
        this.reload = reload;
        this.exceptionHandler = exceptionHandler;
    }
    @Override
    public boolean pausesGame() {
        return true;
    }
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices, delta);
        float h;
        int k;
        float g;
        int i = this.client.getWindow().getScaledWidth();
        int j = this.client.getWindow().getScaledHeight();
        long l = Util.getMeasuringTimeMs();
        if (this.reloading && this.reloadStartTime == -1L) {
            this.reloadStartTime = l;
        }
        float f = this.reloadCompleteTime > -1L ? (float)(l - this.reloadCompleteTime) / 1000.0f : -1.0f;
        float f2 = g = this.reloadStartTime > -1L ? (float)(l - this.reloadStartTime) / 500.0f : -1.0f;
        if (f >= 1.0f) {
            if (this.client.currentScreen != null) {
                this.client.currentScreen.render(matrices, 0, 0, delta);
            }
            k = MathHelper.ceil((1.0f - MathHelper.clamp(f - 1.0f, 0.0f, 1.0f)) * 255.0f);
            //SplashOverlay.fill(matrices, 0, 0, i, j, SplashOverlay.withAlpha(BRAND_ARGB.getAsInt(), k));
            h = 1.0f - MathHelper.clamp(f - 1.0f, 0.0f, 1.0f);
        } else{
            if (this.client.currentScreen != null && g < 1.0f) {
                this.client.currentScreen.render(matrices, mouseX, mouseY, delta);
            }
            k = MathHelper.ceil(MathHelper.clamp((double) g, 0.15, 1.0) * 255.0);
            //SplashOverlay.fill(matrices, 0, 0, i, j, SplashOverlay.withAlpha(BRAND_ARGB.getAsInt(), k));
            h = MathHelper.clamp(g, 0.0f, 1.0f);
        }
        k = (int)((double)this.client.getWindow().getScaledWidth() * 0.5);
        int m = (int)((double)this.client.getWindow().getScaledHeight() * 0.5);
        double n = Math.min((double)this.client.getWindow().getScaledWidth() * 0.75, (double)this.client.getWindow().getScaledHeight()) * 0.25;
        int p = (int)(n * 0.5);
        double d = n * 4.0;
        int q = (int)(d * 0.5);
        RenderSystem.setShaderTexture(0, LOGO);
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, h);
        SplashOverlay.drawTexture(matrices, k - q, m - p, q, (int)n, -0.0625f, 0.0f, 120, 60, 120, 120);
        SplashOverlay.drawTexture(matrices, k, m - p, q, (int)n, 0.0625f, 60.0f, 120, 60, 120, 120);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        int r = (int)((double)this.client.getWindow().getScaledHeight() * 0.8325);
        float s = this.reload.getProgress();
        this.progress = MathHelper.clamp(this.progress * 0.95f + s * 0.050000012f, 0.0f, 1.0f);
        if (f < 1.0f) {
            this.renderProgressBar(matrices, i / 2 - q, r - 5, i / 2 + q, r + 5, 1.0f - MathHelper.clamp(f, 0.0f, 1.0f));
        }
        if (f >= 2.0f) {
            this.client.setOverlay(null);
        }
        if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (!this.reloading || g >= 2.0f)) {
            try {
                this.reload.throwException();
                this.exceptionHandler.accept(Optional.empty());
            }
            catch (Throwable throwable) {
                this.exceptionHandler.accept(Optional.of(throwable));
            }
            this.reloadCompleteTime = Util.getMeasuringTimeMs();
            if (this.client.currentScreen != null) {
                this.client.currentScreen.init(this.client, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight());
            }
        }
    }
    private void renderProgressBar(MatrixStack matrices, int minX, int minY, int maxX, int maxY, float opacity) {
        int i = MathHelper.ceil((float)(maxX - minX - 2) * this.progress);
        int j = Math.round(opacity * 255.0f);
        int k = BackgroundHelper.ColorMixer.getArgb(j, 255, 255, 255);
        SplashOverlay.fill(matrices, minX + 2, minY + 2, minX + i, maxY - 2, k);
        SplashOverlay.fill(matrices, minX + 1, minY, maxX - 1, minY + 1, k);
        SplashOverlay.fill(matrices, minX + 1, maxY, maxX - 1, maxY - 1, k);
        SplashOverlay.fill(matrices, minX, minY, minX + 1, maxY, k);
        SplashOverlay.fill(matrices, maxX, minY, maxX - 1, maxY, k);
    }
    private void renderBackground(MatrixStack matrices,float delta){
        int width  = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        if (backgroundFadeStart == 0L ) {
            this.backgroundFadeStart = Util.getMeasuringTimeMs();
        }
        float f = (float)(Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0f;
        backgroundRenderer.render(delta, MathHelper.clamp(f, 0.0f, 1.0f));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (float) MathHelper.ceil(MathHelper.clamp(f, 0.0f, 1.0f)));
        TitleScreen.drawTexture(matrices, 0, 0, width, height, 0.0f, 0.0f, 16, 128, 16, 128);
        float g = MathHelper.clamp(f - 1.0f, 0.0f, 1.0f);
        int l = MathHelper.ceil(g * 255.0f) << 24;
        if ((l & 0xFC000000) == 0) {
            return;
        }
        drawTexture(matrices, 0, 0, width, height, 0.0F, 0.0F, 16, 128, 16, 128);

    }
}
