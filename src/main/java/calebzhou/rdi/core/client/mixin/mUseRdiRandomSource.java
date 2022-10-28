package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.util.KRandomSource;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


/**
 * Created  on 2022-10-25,21:40.
 */
@Mixin(RandomSource.class)
public interface mUseRdiRandomSource {
	@Overwrite
	static RandomSource create(long seed) {
		return KRandomSource.INSTANCE;
	}
}
