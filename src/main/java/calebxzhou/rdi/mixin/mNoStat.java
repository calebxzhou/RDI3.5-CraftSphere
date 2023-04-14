package calebxzhou.rdi.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Created  on 2023-04-13,21:12.
 */
@Mixin(Player.class)
public class mNoStat {
	@Overwrite
	public void awardStat(ResourceLocation statKey) {
	}
	@Overwrite
	public void awardStat(ResourceLocation stat, int increment) {
	}

	@Overwrite
	public void awardStat(Stat<?> stat) {
	}

	@Overwrite
	public void awardStat(Stat<?> stat, int increment) {
	}
}
