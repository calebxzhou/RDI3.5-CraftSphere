package calebxzhou.rdi.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created  on 2022-10-30,10:06.
 */
@Mixin(Explosion.class)
public class mLessExplode {
	@Mutable
	@Shadow
	@Final
	private Explosion.BlockInteraction blockInteraction;

	//所有爆炸都不会破坏方块
	@Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)V"
			,at=@At("TAIL"))
	private void noExplodeDestroyBlock(Level level, Entity source, DamageSource damageSource, ExplosionDamageCalculator damageCalculator, double toBlowX, double toBlowY, double toBlowZ, float radius, boolean fire, Explosion.BlockInteraction bi, CallbackInfo ci){
		blockInteraction= Explosion.BlockInteraction.NONE;
	}
}
