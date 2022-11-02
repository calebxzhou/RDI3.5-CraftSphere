package calebzhou.rdi.core.client.mixin.gameplay;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created by calebzhou on 2022-09-22,10:42.
 */
@Mixin(Player.class)
public class mPlayerAttributes {
	//最大血量50
	@Overwrite
	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes()
				.add(Attributes.ATTACK_DAMAGE, 2.0)
				.add(Attributes.MOVEMENT_SPEED, 0.15F)
				.add(Attributes.MAX_HEALTH,50)
				.add(Attributes.ATTACK_SPEED)
				.add(Attributes.LUCK);
	}
}
@Mixin(PlayerList.class)
class mSpawnHealth{
	//复活50血
	@Redirect(method = "respawn", at = @At(value = "INVOKE",target = "Lnet/minecraft/server/level/ServerPlayer;getHealth()F"))
	private float _50hp(ServerPlayer instance){
		return 50;
	}
}
