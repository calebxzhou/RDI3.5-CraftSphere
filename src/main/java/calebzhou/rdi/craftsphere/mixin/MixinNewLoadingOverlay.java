package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.screen.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.resources.ReloadInstance;

@Mixin(Minecraft.class)
public abstract class MixinNewLoadingOverlay {

    //不显示mc本身的加载画面
    @Redirect(method = "Lnet/minecraft/client/Minecraft;<init>(Lnet/minecraft/client/main/GameConfig;)V",
    at=@At(value = "INVOKE",target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"))
    private void notDisplayOverlay(Minecraft instance, Overlay overlay){
        net.minecraft.client.gui.screens.LoadingOverlay splashOverlay = ((net.minecraft.client.gui.screens.LoadingOverlay) overlay);
        AccessSplashOverlay accessSplashOverlay = ((AccessSplashOverlay) splashOverlay);
        instance.setOverlay(new LoadingOverlay(
                instance,
                accessSplashOverlay.getReload(),
                accessSplashOverlay.getOnFinish())
        );
    }
}

@Mixin(net.minecraft.client.gui.screens.LoadingOverlay.class)
interface AccessSplashOverlay{
    @Accessor
    ReloadInstance getReload();
    @Accessor
    Consumer<Optional<Throwable>> getOnFinish();
}