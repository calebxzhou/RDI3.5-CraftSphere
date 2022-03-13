package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.module.NewTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class MixinToNewTitleScreen {
    @Redirect(method = "Lnet/minecraft/client/Minecraft;<init>(Lnet/minecraft/client/main/GameConfig;)V",
    at=@At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void qafdqwd(Minecraft instance, Screen screen){
        instance.setScreen(NewTitleScreen.INSTANCE);
    }
    /**
     * @author
     * 不开启领域服
     */
    @Overwrite
    public boolean isConnectedToRealms() {
        return false;
    }
}
@Mixin(DeathScreen.class)
class MixinGoNewTitleScreen {
    @Redirect(method = "Lnet/minecraft/client/gui/screens/DeathScreen;exitToTitleScreen()V",
            at=@At(target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
                    value = "INVOKE"))
    private void goNewTitleScreenOnDeath(Minecraft instance, Screen screen){
        instance.setScreen(NewTitleScreen.INSTANCE);
    }
}
