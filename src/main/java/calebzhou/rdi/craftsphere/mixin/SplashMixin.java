package calebzhou.rdi.craftsphere.mixin;


import calebzhou.rdi.craftsphere.texture.LogoTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public class SplashMixin {
    // 黑色 加载画面
    @Shadow
    @Final
    @Mutable
    static Identifier LOGO;
    @Shadow
    @Final
    @Mutable
    private static int MOJANG_RED = 0x00038cfc;

    @Inject(method = "init(Lnet/minecraft/client/MinecraftClient;)V",at=@At("HEAD"), cancellable = true)
    private static void init(MinecraftClient client, CallbackInfo ci){
        LOGO= new Identifier("splash.png");
        client.getTextureManager().registerTexture(LOGO, new LogoTexture());

        ci.cancel();

    }

    @Inject(method = "Lnet/minecraft/client/gui/screen/SplashOverlay;render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V",
    at = @At("TAIL"))
    private void rend(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci){
        int width = MinecraftClient.getInstance().getWindow().getWidth();
        int height = MinecraftClient.getInstance().getWindow().getHeight();
       MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices,"123",width/2,height-30,0xFFFFFFFF,false);
    }

}
