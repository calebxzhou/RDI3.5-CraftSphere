package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.screen.LoadingOverlay;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.resource.ResourceReloadLogger;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Mixin(MinecraftClient.class)
public abstract class MixinNewLoadingOverlay {

    //不显示mc本身的加载画面
    @Redirect(method = "Lnet/minecraft/client/MinecraftClient;<init>(Lnet/minecraft/client/RunArgs;)V",
    at=@At(value = "INVOKE",target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V"))
    private void notDisplayOverlay(MinecraftClient instance, Overlay overlay){
        SplashOverlay splashOverlay = ((SplashOverlay) overlay);
        AccessSplashOverlay accessSplashOverlay = ((AccessSplashOverlay) splashOverlay);
        instance.setOverlay(new LoadingOverlay(
                instance,
                accessSplashOverlay.getReload(),
                accessSplashOverlay.getExceptionHandler())
        );
    }
}

@Mixin(SplashOverlay.class)
interface AccessSplashOverlay{
    @Accessor
    ResourceReload getReload();
    @Accessor
    Consumer<Optional<Throwable>> getExceptionHandler();
}