package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.screen.RdiPauseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

//简单的暂停菜单
@Mixin(Minecraft.class)
public abstract class mNewPauseMenu {

    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Shadow @Final private SoundManager soundManager;

    @Shadow @Nullable public Screen screen;

    @Shadow public abstract boolean hasSingleplayerServer();

    @Shadow private @Nullable IntegratedServer singleplayerServer;

    /**
     * @author
     */
    @Overwrite
    public void pauseGame(boolean pause) {
        if (screen == null) {
            boolean bl = hasSingleplayerServer() && !singleplayerServer.isPublished();
            if (bl) {
                this.setScreen(new RdiPauseScreen(!pause));
                this.soundManager.pause();
            } else {
                this.setScreen(new RdiPauseScreen(true));
            }

        }
    }
    /*@Redirect(method="Lnet/minecraft/client/MinecraftClient;openPauseMenu(Z)V",
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
    }*/
}
