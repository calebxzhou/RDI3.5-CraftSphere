package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.texture.LogoTexture;
import calebzhou.rdi.craftsphere.texture.Textures;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }
    //不显示左下角和右下角的内容
    /**
     * @author
     */
    @Overwrite
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Textures.TITLE_SCREEN);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        drawTexture(matrices, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);

        RenderSystem.setShaderTexture(0, Textures.TITLE_LOGO);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5f);
        int j = this.width / 2 - 137;
            this.drawWithOutline(j, 30, (x, y) -> {
                this.drawTexture(matrices, x + 0, y, 0, 0, 99, 44);
                this.drawTexture(matrices, x + 99, y, 129, 0, 27, 44);
                this.drawTexture(matrices, x + 99 + 26, y, 126, 0, 3, 44);
                this.drawTexture(matrices, x + 99 + 26 + 3, y, 99, 0, 26, 44);
                this.drawTexture(matrices, x + 155, y, 0, 45, 155, 44);
            });

/*
        if (this.backgroundFadeStart == 0L && this.doBackgroundFade) {
            this.backgroundFadeStart = Util.getMeasuringTimeMs();
        }

        float f = this.doBackgroundFade ? (float)(Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;
        this.backgroundRenderer.render(delta, MathHelper.clamp(f, 0.0F, 1.0F));
*/

         float g = 1.0F;
        int l = MathHelper.ceil(g * 255.0F) << 24;
        if ((l & -67108864) != 0) {

            Iterator var12 = this.children().iterator();

            while(var12.hasNext()) {
                Element element = (Element)var12.next();
                if (element instanceof ClickableWidget) {
                    ((ClickableWidget)element).setAlpha(g);
                }
            }

            super.render(matrices, mouseX, mouseY, delta);
            /*if (this.areRealmsNotificationsEnabled() && g >= 1.0F) {
                this.realmsNotificationGui.render(matrices, mouseX, mouseY, delta);
            }*/

        }
    }
    /**
     * @author
     * 去掉演示模式
     */
    @Overwrite
    private void initWidgetsDemo(int y, int spacingY) {}
    //去掉领域服
    @Inject(method = "Lnet/minecraft/client/gui/screen/TitleScreen;initWidgetsNormal(II)V",
            at=@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/screen/TitleScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;",
            ordinal = 2), cancellable = true)
    private void noRealmsButton(CallbackInfo ci){
        ci.cancel();
    }
}
@Mixin(MinecraftClient.class)
class TitleScreenClientMixin{
    /**
     * @author
     * 不检查是否允许多人游戏
     */
    @Overwrite
    public boolean isMultiplayerEnabled() {
        return true;
    }
    /**
     * @author
     * 不开启领域服
     */
    @Overwrite
    public boolean isRealmsEnabled() {
        return false;
    }
}
