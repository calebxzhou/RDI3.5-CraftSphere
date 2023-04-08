package calebxzhou.rdi.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created by calebzhou on 2022-09-19,10:27.
 */
@Mixin(ChatReportScreen.class)
public class mNoReportScreen {
	@Shadow
	@Final
	@Nullable Screen lastScreen;

	@Overwrite
	public void init() {
		Minecraft.getInstance().setScreen(lastScreen);
	}
	@Overwrite
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {}
}
