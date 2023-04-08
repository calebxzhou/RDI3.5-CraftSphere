package calebxzhou.rdi.mixin;

import calebxzhou.rdi.screen.RdiPauseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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

    /**
     * @author
     */
    @Overwrite
    public void pauseGame(boolean pause) {
        if (screen == null) {
            if (hasSingleplayerServer()) {
                this.soundManager.pause();
            }
                this.setScreen(new RdiPauseScreen( ));
        }
    }
}
