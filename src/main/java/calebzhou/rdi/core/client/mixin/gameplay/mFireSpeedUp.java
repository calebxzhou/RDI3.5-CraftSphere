package calebzhou.rdi.core.client.mixin.gameplay;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FireBlock.class)
public class mFireSpeedUp {
        //火加速
        @Overwrite
		private static int getFireTickDelay(RandomSource randomSource){
            return 2 + randomSource.nextInt(10);
        }
}
