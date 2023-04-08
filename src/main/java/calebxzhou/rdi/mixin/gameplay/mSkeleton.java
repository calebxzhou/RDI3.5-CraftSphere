package calebxzhou.rdi.mixin.gameplay;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractSkeleton.class)
public abstract class mSkeleton extends Monster{
	private mSkeleton(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
	}

	@ModifyConstant(
            method = "reassessWeaponGoal()V",
            constant = @Constant(intValue = 20)
    )
    private static int changeAtkSpeed(int spd){
        return 2;
    }
    /**
     * @author
     */
    @Overwrite
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.MAX_HEALTH,50);
    }
	@Overwrite
	public void registerGoals() {
		this.goalSelector.addGoal(1, new RestrictSunGoal(this));
		this.goalSelector.addGoal(5, new AvoidEntityGoal(this, Wolf.class, 6.0F, 1.0, 1.2));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 32.0F));
		this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, false));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, false));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR));
	}
}
@Mixin(RangedBowAttackGoal.class)
class mBowAttackGoal {
	@ModifyConstant(method = "tick",constant = @Constant(intValue = 20,ordinal = 2))
	private int moreFrequentAttack(int constant){
		return 2;
	}
}
