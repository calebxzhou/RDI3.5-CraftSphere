package calebxzhou.rdi.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Created  on 2022-11-02,11:03.
 */
@Mixin(LocalPlayer.class)
public class mAutoRespawn {
	@Overwrite
	public boolean shouldShowDeathScreen() {
		return false;
	}
}
