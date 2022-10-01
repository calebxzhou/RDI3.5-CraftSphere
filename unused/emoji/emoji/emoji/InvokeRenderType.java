package calebzhou.rdi.core.client.mixin.emoji;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Created by calebzhou on 2022-09-22,23:39.
 */
@Mixin(RenderType.class)
public interface InvokeRenderType {
	@Invoker
	static RenderType.CompositeRenderType invokeCreate(
			String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderType.CompositeState state
	) {
		return null;
	}
}
