package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.screen.NewTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DeathScreen.class)
public class MixinGoNewTitleScreen {
    @Redirect(method = "Lnet/minecraft/client/gui/screen/DeathScreen;quitLevel()V",
    at=@At(target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V",
    value = "INVOKE"))
    private void goNewTitleScreenOnDeath(MinecraftClient instance, Screen screen){
        instance.setScreen(NewTitleScreen.INSTANCE);
    }
}
