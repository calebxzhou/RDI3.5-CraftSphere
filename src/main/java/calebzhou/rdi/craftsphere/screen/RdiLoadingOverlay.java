package calebzhou.rdi.craftsphere.screen;

import calebzhou.rdi.craftsphere.misc.ServerConnector;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;

import java.util.Optional;
import java.util.function.Consumer;

import static calebzhou.rdi.craftsphere.texture.LogoTexture.LOGO;

public class RdiLoadingOverlay extends Overlay {
    public static final CubeMap PANORAMA_CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private final PanoramaRenderer backgroundRenderer = new PanoramaRenderer(PANORAMA_CUBE_MAP);
    private final boolean reloading=true;
    private float progress;
    private long reloadCompleteTime = -1L;
    private long reloadStartTime = -1L;
    private long backgroundFadeStart;
    private float loadProgress;


    private final Minecraft client;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> exceptionHandler;

    public RdiLoadingOverlay(Minecraft client, ReloadInstance reload, Consumer<Optional<Throwable>> exceptionHandler) {
        //ping一下服务器提高载入速度
        ServerConnector.ping();
        this.client=client;
        this.reload = reload;
        this.exceptionHandler = exceptionHandler;
    }
    @Override
    public boolean isPauseScreen() {
        return true;
    }
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices, delta);
        /*if (this.reloadCompleteTime > 1) {
            this.client.setOverlay(null);
        }*/
        float h;
        int k;
        float g;
        int i = this.client.getWindow().getGuiScaledWidth();
        int j = this.client.getWindow().getGuiScaledHeight();
        long l = Util.getMillis();
        if (this.reloading && this.reloadStartTime == -1L) {
            this.reloadStartTime = l;
        }
        float f = this.reloadCompleteTime > -1L ? (float)(l - this.reloadCompleteTime) / 1000.0f : -1.0f;
        float f2 = g = this.reloadStartTime > -1L ? (float)(l - this.reloadStartTime) / 500.0f : -1.0f;
        if (f >= 1.0f) {
            if (this.client.screen != null) {
                this.client.screen.render(matrices, 0, 0, delta);
            }
            k = Mth.ceil((1.0f - Mth.clamp(f - 1.0f, 0.0f, 1.0f)) * 255.0f);
            //SplashOverlay.fill(matrices, 0, 0, i, j, SplashOverlay.withAlpha(BRAND_ARGB.getAsInt(), k));
            h = 1.0f - Mth.clamp(f - 1.0f, 0.0f, 1.0f);
        } else{
            if (this.client.screen != null && g < 1.0f) {
                this.client.screen.render(matrices, mouseX, mouseY, delta);
            }
            k = Mth.ceil(Mth.clamp((double) g, 0.15, 1.0) * 255.0);
            //SplashOverlay.fill(matrices, 0, 0, i, j, SplashOverlay.withAlpha(BRAND_ARGB.getAsInt(), k));
            h = Mth.clamp(g, 0.0f, 1.0f);
        }
        k = (int)((double)this.client.getWindow().getGuiScaledWidth() * 0.5);
        int m = (int)((double)this.client.getWindow().getGuiScaledHeight() * 0.5);
        double n = Math.min((double)this.client.getWindow().getGuiScaledWidth() * 0.75, (double)this.client.getWindow().getGuiScaledHeight()) * 0.25;
        int p = (int)(n * 0.5);
        double d = n * 4.0;
        int q = (int)(d * 0.5);
        RenderSystem.setShaderTexture(0, LOGO);
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, h);
        LoadingOverlay.blit(matrices, k - q, m - p, q, (int)n, -0.0625f, 0.0f, 120, 60, 120, 120);
        LoadingOverlay.blit(matrices, k, m - p, q, (int)n, 0.0625f, 60.0f, 120, 60, 120, 120);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        int r = (int)((double)this.client.getWindow().getGuiScaledHeight() * 0.8325);
        float s = this.reload.getActualProgress();
        this.progress = Mth.clamp(this.progress * 0.95f + s * 0.050000012f, 0.0f, 1.0f);
        if (f < 1.0f) {
            this.renderProgressBar(matrices, i / 2 - q, r - 5, i / 2 + q, r + 5, 1.0f - Mth.clamp(f, 0.0f, 1.0f));
        }
        if (f >= 2.0f) {
            this.client.setOverlay(null);
        }
        if (this.reloadCompleteTime == -1L && this.reload.isDone() && (!this.reloading || g >= 2.0f)) {
            try {
                this.reload.checkExceptions();
                this.exceptionHandler.accept(Optional.empty());
            }
            catch (Throwable throwable) {
                this.exceptionHandler.accept(Optional.of(throwable));
            }
            this.reloadCompleteTime = Util.getMillis();
            if (this.client.screen != null) {
                this.client.screen.init(this.client, this.client.getWindow().getGuiScaledWidth(), this.client.getWindow().getGuiScaledHeight());
            }
        }
    }
    private void renderProgressBar(PoseStack matrices, int minX, int minY, int maxX, int maxY, float opacity) {
        int i = Mth.ceil((float)(maxX - minX - 2) * this.progress);
        int j = Math.round(opacity * 255.0f);
        int k = net.minecraft.util.FastColor.ARGB32.color(j, 255, 255, 255);
        LoadingOverlay.fill(matrices, minX + 2, minY + 2, minX + i, maxY - 2, k);
        LoadingOverlay.fill(matrices, minX + 1, minY, maxX - 1, minY + 1, k);
        LoadingOverlay.fill(matrices, minX + 1, maxY, maxX - 1, maxY - 1, k);
        LoadingOverlay.fill(matrices, minX, minY, minX + 1, maxY, k);
        LoadingOverlay.fill(matrices, maxX, minY, maxX - 1, maxY, k);
    }
    private void renderBackground(PoseStack matrices,float delta){
        int width  = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        if (backgroundFadeStart == 0L ) {
            this.backgroundFadeStart = Util.getMillis();
        }
        float f = (float)(Util.getMillis() - this.backgroundFadeStart) / 1000.0f;
        backgroundRenderer.render(delta, Mth.clamp(f, 0.0f, 1.0f));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (float) Mth.ceil(Mth.clamp(f, 0.0f, 1.0f)));
        TitleScreen.blit(matrices, 0, 0, width, height, 0.0f, 0.0f, 16, 128, 16, 128);
        float g = Mth.clamp(f - 1.0f, 0.0f, 1.0f);
        int l = Mth.ceil(g * 255.0f) << 24;
        if ((l & 0xFC000000) == 0) {
            return;
        }
        blit(matrices, 0, 0, width, height, 0.0F, 0.0F, 16, 128, 16, 128);

    }
}
