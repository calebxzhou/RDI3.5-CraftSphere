package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.RdiCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by calebzhou on 2022-09-28,9:10.
 */
@Mixin(Minecraft.class)
public class mCheckIfFontLoaded {


	@Inject(method = "<init>",at = @At(shift = At.Shift.AFTER,value = "INVOKE",target = "Lnet/minecraft/client/Minecraft;selectMainFont(Z)V"))
	private void loadFont(GameConfig gameConfig, CallbackInfo ci){
		RdiCore.isFontLoaded = true;
	}
}
