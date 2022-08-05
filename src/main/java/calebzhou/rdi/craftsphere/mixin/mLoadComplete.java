package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.misc.ServerConnector;
import calebzhou.rdi.craftsphere.screen.NewTitleScreen;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//去新的标题界面
@Mixin(Minecraft.class)
public abstract class mLoadComplete {
    @Redirect(method = "<init>(Lnet/minecraft/client/main/GameConfig;)V",
    at=@At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void goNewTitleScreen(Minecraft instance, Screen screen){
        instance.setScreen(NewTitleScreen.INSTANCE);
        ServerConnector.connect();
        }
    //不开启领域服

    @Overwrite
    public boolean isConnectedToRealms() {
        return false;
    }
}
@Mixin(DeathScreen.class)
class mDeathNoGoTitle {
    @Redirect(method = "Lnet/minecraft/client/gui/screens/DeathScreen;exitToTitleScreen()V",
            at=@At(target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V",
                    value = "INVOKE"))
    private void goNewTitleScreenOnDeath(Minecraft instance, Screen screen){
        instance.setScreen(NewTitleScreen.INSTANCE);
    }
}
