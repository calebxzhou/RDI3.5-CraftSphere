package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.screen.NovelHud;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Gui.class)
public abstract class MixinNovelHudGui {
    @Shadow protected abstract Player getCameraPlayer();

    @Shadow private int screenWidth;

    @Shadow private int screenHeight;


    /**
     * @author
     */
    @Overwrite
    private void renderHotbar(float f, PoseStack poseStack) {
        NovelHud.getInstance().render(f,poseStack,getCameraPlayer(),screenWidth,screenHeight);
    }

}
