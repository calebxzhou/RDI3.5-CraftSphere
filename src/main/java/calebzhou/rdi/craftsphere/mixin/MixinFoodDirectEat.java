package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Item.class)
public class MixinFoodDirectEat {
    /**
     * @author
     * 食物直接吃 没有延迟
     */
    @Overwrite
    public int getUseDuration(ItemStack stack) {
        if (stack.getItem().isEdible()) {
            return 1;
        } else {
            return 0;
        }
    }
}
