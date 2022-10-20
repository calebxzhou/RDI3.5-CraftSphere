package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.util.DialogUtils;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Created by calebzhou on 2022-10-01,11:51.
 */

@Mixin(Window.class)
public class mAskToStopClient {


	@Overwrite
	public boolean shouldClose() {
		return DialogUtils.shouldClose((Window) (Object)this);


	}
}
