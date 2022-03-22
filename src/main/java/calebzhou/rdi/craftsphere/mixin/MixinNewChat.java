package calebzhou.rdi.craftsphere.mixin;


import calebzhou.rdi.craftsphere.screen.NovelChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public abstract class MixinNewChat {
    @Shadow public abstract void setScreen(@Nullable Screen screen);

    /**
     * @author
     */
    @Overwrite
    private void openChatScreen(String string) {
        setScreen(new NovelChatScreen(string));

    }
}
