package calebzhou.rdi.core.client.mixin.gameplay;

import net.minecraft.world.level.block.SweetBerryBushBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SweetBerryBushBlock.class)
public class mSweetBerry {
    @ModifyConstant(method = "entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V",
    constant = @Constant(floatValue = 1.0f))
    private static float modifyDamage(float f){
        return 5.5f;
    }
}
