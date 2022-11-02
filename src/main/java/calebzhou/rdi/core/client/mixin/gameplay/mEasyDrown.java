package calebzhou.rdi.core.client.mixin.gameplay;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntity.class)
public abstract class mEasyDrown {
	@Shadow
	public abstract RandomSource getRandom();

    //更容易溺水
    @Overwrite
    public int decreaseAirSupply(int air) {
        int i = EnchantmentHelper.getRespiration((LivingEntity)(Object) this);
        if (i > 0 && getRandom().nextInt(i + 1) > 0) {
            return air;
        }
        return air - 5;
    }
    //更不容易恢复氧气
    @Overwrite
    public int increaseAirSupply(int air) {
        return Math.min(air + 2, ((Entity)(Object)this).getMaxAirSupply());
    }
}
@Mixin(LivingEntity.class)
class mEasyDrown2 {
	//缺氧气死得更快
    @ModifyConstant(method = "baseTick()V",
    constant = @Constant(floatValue = 2.0f))
    private static float drownDeath(float constant){
        return 5.0f;
    }
}
