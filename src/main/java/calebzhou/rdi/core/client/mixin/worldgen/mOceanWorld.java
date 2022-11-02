package calebzhou.rdi.core.client.mixin.worldgen;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import static calebzhou.rdi.core.client.RdiSharedConstants.SEA_LEVEL;

/**
 * Created  on 2022-11-01,15:47.
 */
@Mixin(NoiseGeneratorSettings.class)
public class mOceanWorld {
	@ModifyConstant(method = "overworld",constant = @Constant(intValue = 63))
	private static int changeSeaLevel(int constant){
		return SEA_LEVEL;
	}
}
