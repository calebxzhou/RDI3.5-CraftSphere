package calebxzhou.rdi.mixin.gameplay;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Created  on 2022-11-02,17:04.
 */

@Mixin(Spider.class)
public class mSpider extends Monster {
	private mSpider(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
	}

	@Overwrite
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 32.0).add(Attributes.MOVEMENT_SPEED, 0.7F);
	}

	@Overwrite
	public void registerGoals() {
		Spider spider = (Spider)(Object)this;
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(2, new Spider.SpiderAttackGoal(spider));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 32.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new Spider.SpiderTargetGoal(spider, Player.class));
		this.targetSelector.addGoal(3, new Spider.SpiderTargetGoal(spider, IronGolem.class));
	}

}
