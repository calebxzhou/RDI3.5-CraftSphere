package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.screen.NewLoadingOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(Minecraft.class)
public abstract class MixinNewLoadingOverlay {

    //不显示mc本身的加载画面
    @Redirect(method = "Lnet/minecraft/client/Minecraft;<init>(Lnet/minecraft/client/main/GameConfig;)V",
    at=@At(value = "INVOKE",target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"))
    private void notDisplayOverlay(Minecraft instance, Overlay overlay){
        LoadingOverlay splashOverlay = (( LoadingOverlay) overlay);
        AccessSplashOverlay accessSplashOverlay = ((AccessSplashOverlay) splashOverlay);
        instance.setOverlay(new NewLoadingOverlay(
                instance,
                accessSplashOverlay.getReload(),
                accessSplashOverlay.getOnFinish())
        );
        //ConnectScreen.startConnecting(NewTitleScreen.INSTANCE, Minecraft.getInstance(), ExampleMod.SERVER_ADDRESS,ExampleMod.SERVER_INFO);
    }
}

@Mixin(net.minecraft.client.gui.screens.LoadingOverlay.class)
interface AccessSplashOverlay{
    @Accessor
    ReloadInstance getReload();
    @Accessor
    Consumer<Optional<Throwable>> getOnFinish();
}