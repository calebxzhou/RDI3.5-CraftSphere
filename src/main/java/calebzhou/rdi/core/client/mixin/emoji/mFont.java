package calebzhou.rdi.core.client.mixin.emoji;

import calebzhou.rdi.core.client.emoji.render.EmojiFontRenderer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by calebzhou on 2022-09-22,23:19.
 */
@Mixin(Font.class)
public class mFont {
	@Inject(method = "renderText(Ljava/lang/String;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZII)F"
	,at = @At(value = "HEAD"), cancellable = true)
	private void renderEmojiFont(String text, float x, float y, int color, boolean dropShadow, Matrix4f pose, MultiBufferSource bufferSource, boolean seeThrough, int backgroundColor, int packedLightCoords, CallbackInfoReturnable<Float> cir){
		if((Font)(Object)this instanceof EmojiFontRenderer fontRenderer){
			float f = fontRenderer.renderText(text, x, y, color, dropShadow, pose, bufferSource, seeThrough, backgroundColor, packedLightCoords);
			cir.setReturnValue(f);
		}
	}
}
