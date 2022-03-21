package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.screen.NovelHud;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;

@Mixin(Gui.class)
public abstract class MixinNovelHudGui {
    @Shadow protected abstract Player getCameraPlayer();

    @Shadow private int screenWidth;

    @Shadow private int screenHeight;


    /**
     * @author 新的物品栏GUI
     */
    @Overwrite
    private void renderHotbar(float f, PoseStack poseStack) {
        NovelHud.getInstance().render(f,poseStack,getCameraPlayer(),screenWidth,screenHeight);
    }

}
@Mixin(Inventory.class)
class MixinNovelHudGui2 {
    @Shadow @Mutable
    public int selected;

    @Shadow @Final @Mutable public NonNullList<ItemStack> items;

    /**
     * @author
     */
    @Overwrite
    public void swapPaint(double d) {
        if (d > 0.0D) {
            d = 1.0D;
        }

        if (d < 0.0D) {
            d = -1.0D;
        }
        for(this.selected = (int)((double)this.selected - d); this.selected < 0; this.selected += NovelHud.STACKS_DISPLAY) {
        }

        while(this.selected >= NovelHud.STACKS_DISPLAY) {
            this.selected -= NovelHud.STACKS_DISPLAY;
        }

    }
    /**
     * @author
     */
    @Overwrite
    public static int getSelectionSize() {
        return NovelHud.STACKS_DISPLAY;
    }

    /**
     * @author
     */
    @Overwrite
    public static boolean isHotbarSlot(int i) {
        return i >= 0 && i < NovelHud.STACKS_DISPLAY;
    }

    /**
     * @author
     */
    @Overwrite
    public int getSuitableHotbarSlot() {
        int i;
        int j;
        for(i = 0; i < NovelHud.STACKS_DISPLAY; ++i) {
            j = (this.selected + i) % NovelHud.STACKS_DISPLAY;
            if (items.get(j).isEmpty()) {
                return j;
            }
        }

        for(i = 0; i < NovelHud.STACKS_DISPLAY; ++i) {
            j = (this.selected + i) % NovelHud.STACKS_DISPLAY;
            if (!items.get(j).isEnchanted()) {
                return j;
            }
        }

        return this.selected;
    }
}