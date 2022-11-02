package calebzhou.rdi.core.client.mixin.gameplay;

import calebzhou.rdi.core.server.RdiCoreServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntity.class)
public class MixinQuickDrown {
    private static final int MAX_AIR = 300;
    //更容易溺水
    @Overwrite
    public int decreaseAirSupply(int air) {
        int i = EnchantmentHelper.getRespiration((LivingEntity)(Object) this);
        if (i > 0 && RdiCoreServer.RANDOM.nextInt(i + 1) > 0) {
            return air;
        }
        return air - 5;
    }
    //更不容易恢复氧气
    @Overwrite
    public int increaseAirSupply(int air) {
        return Math.min(air + 2, MAX_AIR);
    }
}
@Mixin(LivingEntity.class)
class MixinQuickDrown2 {
    @ModifyConstant(method = "Lnet/minecraft/world/entity/LivingEntity;baseTick()V",
    constant = @Constant(floatValue = 2.0f))
    private static float drownDeath(float constant){
        return 5.0f;
    }
}
