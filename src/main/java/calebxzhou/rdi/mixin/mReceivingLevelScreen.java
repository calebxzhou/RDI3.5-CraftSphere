package calebxzhou.rdi.mixin;

import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created by calebzhou on 2022-09-23,21:04.
 */
@Mixin(ReceivingLevelScreen.class)
public class mReceivingLevelScreen {
	//干掉泥土背景
	@Redirect(method = "render",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/screens/ReceivingLevelScreen;renderDirtBackground(I)V"))
	private void noDirtBg(ReceivingLevelScreen instance, int i){}
	@ModifyConstant(method = "tick",constant = @Constant(longValue = 2000L))
	private long lim(long constant){
		return 1L;
	}
}
