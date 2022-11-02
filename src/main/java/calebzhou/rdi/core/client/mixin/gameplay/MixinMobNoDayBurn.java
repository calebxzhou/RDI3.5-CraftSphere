package calebzhou.rdi.core.client.mixin.gameplay;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Mob.class)
public abstract class MixinMobNoDayBurn extends LivingEntity {

    protected MixinMobNoDayBurn(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    //所有怪物不受白天影响 除了幻翼
    @Overwrite
    public boolean isSunBurnTick() {
        if(getLevel().isDay() && getType()==EntityType.PHANTOM)
            return true;
         return false;
    }
}
