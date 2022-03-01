package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.screen.LoadingOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
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