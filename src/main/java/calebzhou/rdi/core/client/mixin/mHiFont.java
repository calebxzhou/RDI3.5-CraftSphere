package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.util.RdiHiFont;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.stb.STBTTFontinfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created by calebzhou on 2022-09-20,7:50.
 */

@Mixin(targets = "com.mojang.blaze3d.font.TrueTypeGlyphProvider$Glyph$1")
public abstract class mHiFont implements GlyphInfo {
	@Redirect(method = "upload",at = @At(value = "INVOKE",target = "Lcom/mojang/blaze3d/platform/NativeImage;copyFromFont(Lorg/lwjgl/stb/STBTTFontinfo;IIIFFFFII)V"))
	private void rdiHiFontCopyFrom(NativeImage nativeImage, STBTTFontinfo info, int glyphIndex, int width, int height, float scaleX, float scaleY, float shiftX, float shiftY, int x, int y){
		RdiHiFont.copyFromFont(
				nativeImage, info, glyphIndex, width, height, scaleX, scaleY, shiftX, shiftY, x, y);
	}
}
