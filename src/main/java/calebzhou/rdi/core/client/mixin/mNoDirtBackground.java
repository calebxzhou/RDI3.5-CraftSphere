package calebzhou.rdi.core.client.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Created by calebzhou on 2022-10-02,12:15.
 */
@Mixin(Screen.class)
public class mNoDirtBackground {
	@Overwrite
	public void renderDirtBackground(int vOffset) {
		GlStateManager._clearColor(0.8f, 0.8f, 0.8f, 1.0F);
		GlStateManager._clear(16384, Minecraft.ON_OSX);
		RenderSystem.enableBlend();
	}
}
