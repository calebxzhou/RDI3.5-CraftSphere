package calebzhou.rdi.core.client.mixin;

import net.minecraft.client.gui.font.FontTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Created by calebzhou on 2022-09-10,22:48.
 */
@Mixin(FontTexture.class)
public class mFontTexture {
	@ModifyConstant(method = "<init>",constant = @Constant(intValue = 256))
	private int d(int constant){
		return 2048;
	}

	@ModifyConstant(method = "add",constant = @Constant(floatValue = 256f))
	private float dd(float constant){
		return 2048f;
	}

}
