package calebxzhou.rdi.mixin;

import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.lwjgl.stb.STBTTFontinfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.ByteBuffer;

/**
 * Created by calebzhou on 2022-09-20,11:00.
 */
@Mixin(TrueTypeGlyphProvider.class)
public interface AccessTrueTypeGlyphProvider {
	@Accessor
	ByteBuffer getFontMemory();
	@Accessor
	STBTTFontinfo getFont();
	@Accessor
	float getOversample();
	@Accessor
	IntSet getSkip();
	@Accessor
	 float getShiftX();
	@Accessor
	float getShiftY();
	@Accessor
	 float getPointScale();
	@Accessor
	 float getAscent();
}
