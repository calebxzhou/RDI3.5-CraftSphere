package calebzhou.rdi.core.client.mixin.gameplay;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//恶魂 增强
@Mixin(Ghast.class)
public class mGhast {

    //50血
    @Overwrite
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.FOLLOW_RANGE, 64.0);
    }
    //生成概率x4
    @Overwrite
    public static boolean checkGhastSpawnRules(EntityType<Ghast> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource) {
        return randomSource.nextInt(5) == 0 && Ghast.checkMobSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource);
    }

    //一次生成4个
    @Overwrite
    public int getMaxSpawnClusterSize() {
        return 4;
    }
}
@Mixin(Ghast.GhastMoveControl.class)
class MixinGhastMove{
    @ModifyConstant(
            method = "tick"
            ,constant = @Constant(doubleValue = 0.1D)
    )
    private static double change(double d){
        return 0.2D;
    }
}
@Mixin(Ghast.RandomFloatAroundGoal.class)
class MixinGhastFly{
    @ModifyConstant(
            method = "start"
            ,constant = @Constant(doubleValue = 1.0D)
    )
    private static double change(double d){
        return 1.5D;
    }
}
@Mixin(Ghast.GhastShootFireballGoal.class)
class MixinGhastShoot{
    @Shadow @Mutable
    public int chargeTime;
    @Shadow @Final
    private Ghast ghast;

    @ModifyConstant(
            method = "tick"
            ,constant = @Constant(intValue = 20)
    )
    private static int changeCD(int constant){
        return 11;
    }
    @ModifyConstant(
            method = "tick"
            ,constant = @Constant(doubleValue = 4.0D)
    )
    private static double changeSped(double constant){
        return 5.0D;
    }

    @Inject(
            method = "tick",
            at=@At("TAIL")
    )
    private void changeCd2(CallbackInfo ci){
        if(chargeTime<=-40){
            chargeTime=9;
            this.ghast.setCharging(true);
        }
    }
}
