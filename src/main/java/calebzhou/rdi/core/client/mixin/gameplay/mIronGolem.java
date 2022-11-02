package calebzhou.rdi.core.client.mixin.gameplay;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IronGolem.class)
public abstract class mIronGolem extends AbstractGolem {

	protected mIronGolem(EntityType<? extends AbstractGolem> entityType, Level level) {
		super(entityType, level);
	}

	//铁傀儡增加血量
    @Overwrite
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.KNOCKBACK_RESISTANCE, 2.0)
                .add(Attributes.ATTACK_DAMAGE, 15.0);
    }
    //什么都攻击
    @Overwrite
    public boolean canAttackType(EntityType<?> entityType) {
        return true;
    }

	@Overwrite
	public void registerGoals() {
		IronGolem golem = (IronGolem)(Object)this;
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 2.0, true));
		this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9, 32.0F));
		this.goalSelector.addGoal(2, new MoveBackToVillageGoal(this, 0.6, false));
		this.goalSelector.addGoal(4, new GolemRandomStrollInVillageGoal(this, 0.6));
		this.goalSelector.addGoal(5, new OfferFlowerGoal(golem));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new DefendVillageTargetGoal(golem));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
		this.targetSelector
				.addGoal(3, new NearestAttackableTargetGoal(this, Mob.class, 5, false, false, entity -> entity instanceof Enemy));
		this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(golem, false));
	}


}
