package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.RdiCore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created by calebzhou on 2022-09-19,19:04.
 */
@Mixin(Gui.class)
public class mDebugOverlay {

	@Redirect(method = "render",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void renderRdiDebug(DebugScreenOverlay instance, PoseStack poseStack){
		RdiCore.RDI_DEBUG_OVERLAY.render(poseStack);
	}


}
