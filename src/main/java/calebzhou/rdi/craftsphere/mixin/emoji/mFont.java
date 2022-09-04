package calebzhou.rdi.craftsphere.mixin.emoji;

import calebzhou.rdi.craftsphere.emojiful.render.EmojiFontRenderer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Font.class)
public class mFont {

    @Overwrite
    public final float renderText(String string, float f, float g, int i, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, boolean bl2, int j, int k) {
        return EmojiFontRenderer.getInstance().overwrite_renderText(string, f, g, i, bl, matrix4f, multiBufferSource, bl2, j, k);
    }
}