package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.screen.PauseScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.server.integrated.IntegratedServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public abstract class MixinPauseMenu {
    @Shadow @Nullable public Screen currentScreen;

    @Shadow public abstract boolean isIntegratedServerRunning();

    @Shadow private @Nullable IntegratedServer server;

    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Shadow @Final private SoundManager soundManager;

    /**
     * @author
     */
    @Overwrite
    public void openPauseMenu(boolean pause) {
        if (this.currentScreen == null) {
            boolean bl = this.isIntegratedServerRunning() && !this.server.isRemote();
            if (bl) {
                this.setScreen(new PauseScreen(!pause));
                this.soundManager.pauseAll();
            } else {
                this.setScreen(new PauseScreen(true));
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
