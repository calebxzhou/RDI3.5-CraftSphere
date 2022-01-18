package calebzhou.rdi.craftsphere.mixin;


import calebzhou.rdi.craftsphere.texture.LogoTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;

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
    private static int MOJANG_RED = 0x0075D2FC;

    @Inject(method = "init(Lnet/minecraft/client/MinecraftClient;)V",at=@At("HEAD"), cancellable = true)
    private static void init(MinecraftClient client, CallbackInfo ci){
        LOGO= new Identifier("splash.png");
        client.getTextureManager().registerTexture(LOGO, new LogoTexture());
        ci.cancel();
    }

}
