package calebzhou.rdi.core.client.mixin;

import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Created by calebzhou on 2022-09-19,19:04.
 */
@Mixin(DebugScreenOverlay.class)
public class mDebugOverlay {

	/*@Redirect(method = "render",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void renderRdiDebug(DebugScreenOverlay instance, PoseStack poseStack){
		RdiCore.RDI_DEBUG_OVERLAY.render(poseStack);
	}*/
	@Redirect(method = "drawGameInformation",at = @At(value = "INVOKE",target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
	private boolean lessInfo1(List instance, Object e){
		return false;
	}
}
