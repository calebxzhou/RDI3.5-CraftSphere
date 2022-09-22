package calebzhou.rdi.core.client.mixin.emoji;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Function;

/**
 * Created by calebzhou on 2022-09-22,23:32.
 */

@Mixin(Font.class)
public interface AccessFont {
	@Accessor
	Function<ResourceLocation, FontSet> getFonts();
	@Accessor
	boolean getFilterFishyGlyphs();

	@Invoker
	FontSet invokeGetFontSet(ResourceLocation fontLocation);

	@Invoker
	void invokeRenderChar(
			BakedGlyph glyph,
			boolean bold,
			boolean italic,
			float boldOffset,
			float x,
			float y,
			Matrix4f matrix,
			VertexConsumer buffer,
			float red,
			float green,
			float blue,
			float alpha,
			int packedLight
	);
}
