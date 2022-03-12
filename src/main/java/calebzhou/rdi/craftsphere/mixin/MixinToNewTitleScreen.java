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
    @Redirect(method = "Lnet/minecraft/client/MinecraftClient;<init>(Lnet/minecraft/client/RunArgs;)V",
    at=@At(value = "INVOKE",target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private void qafdqwd(Minecraft instance, Screen screen){
        instance.setScreen(NewTitleScreen.INSTANCE);
    }
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
@Mixin(DeathScreen.class)
class MixinGoNewTitleScreen {
    @Redirect(method = "Lnet/minecraft/client/gui/screen/DeathScreen;quitLevel()V",
            at=@At(target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V",
                    value = "INVOKE"))
    private void goNewTitleScreenOnDeath(Minecraft instance, Screen screen){
        instance.setScreen(NewTitleScreen.INSTANCE);
    }
}
