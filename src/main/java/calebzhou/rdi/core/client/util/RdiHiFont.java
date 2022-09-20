package calebzhou.rdi.core.client.util;

import calebzhou.rdi.core.client.mixin.AccessNativeImage;
import calebzhou.rdi.core.client.mixin.AccessTrueTypeGlyphProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryUtil;

import java.util.Locale;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.stb.STBTruetype.*;

/**
 * Created by calebzhou on 2022-09-20,7:51.
 */
public class RdiHiFont {
	public static void copyFromFont(NativeImage nativeImage,
									STBTTFontinfo info,
									int glyphIndex,
									int width,
									int height,
									float scaleX,
									float scaleY,
									float shiftX,
									float shiftY,
									int x,
									int y){
		AccessNativeImage aNativeImage = (AccessNativeImage)(Object) nativeImage;

		int nativeImageWidth = nativeImage.getWidth();
		int nativeImageHeight = nativeImage.getHeight();
		if (x < 0 || x + width > nativeImageWidth || y < 0 || y + height > nativeImageHeight) {
			throw new IllegalArgumentException(
					String.format(Locale.ROOT, "Out of bounds: start: (%s, %s) (size: %sx%s); size: %sx%s", x, y, width, height, nativeImageWidth, nativeImageHeight)
			);
		} else if (aNativeImage.getFormat().components() != 1) {
			throw new IllegalArgumentException("Can only write fonts into 1-component images.");
		} else {
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			nstbtt_MakeGlyphBitmapSubpixel(
					info.address(),
					aNativeImage.getPixels()+ (long)x + ((long) y * nativeImageWidth),
					width,
					height,
					nativeImageWidth,
					scaleX,
					scaleY,
					shiftX,
					shiftY,
					glyphIndex
			);
			/*STBTruetype.nstbtt_MakeGlyphBitmapSubpixel(
					info.address(),
					 aNativeImage.getPixels()+ (long)x + ((long) y * nativeImage.getWidth()),
					 width,
					 height,
					 nativeImage.getWidth(),
					 scaleX, scaleY, shiftX, shiftY, glyphIndex
			);*/
		}
	}
}
