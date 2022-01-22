package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.screen.PauseScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MixinPauseMenu {
    @Redirect(method="Lnet/minecraft/client/MinecraftClient;openPauseMenu(Z)V",
    at=@At(value="INVOKE",target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"
    ,ordinal = 0))
    private void openPause(MinecraftClient instance, Screen screen,boolean pause){
        instance.setScreen(new PauseScreen(pause));
    }

    @Redirect(method="Lnet/minecraft/client/MinecraftClient;openPauseMenu(Z)V",
            at=@At(value="INVOKE",target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"
                    ,ordinal = 1))
    private void openPause2(MinecraftClient instance, Screen screen){
        instance.setScreen(new PauseScreen(true));
    }
}
