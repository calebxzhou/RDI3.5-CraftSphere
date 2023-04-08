package calebxzhou.rdi.mixin;

import calebxzhou.rdi.screen.RdiPingNumberDisplay;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;



@Mixin(PlayerTabOverlay.class)
public abstract class mDisplayNumberPing {
	@Unique
	@Final
	private static final int PLAYER_SLOT_EXTRA_WIDTH = 45;

	@Shadow
	@Final
	private Minecraft minecraft;

	/**
	 * Increases the int constant {@code 13} in the {@link PlayerTabOverlay#render} method by
	 * {@value #PLAYER_SLOT_EXTRA_WIDTH}. This constant is used to define the width of the "slots" in the player list.
	 * In order to fit the ping text, this needs to be increased.
	 */
	@ModifyConstant(method = "render", constant = @Constant(intValue = 13))
	private int modifySlotWidthConstant(int original) {
		return original + PLAYER_SLOT_EXTRA_WIDTH;
	}

	/**
	 * Redirects the call to {@code renderLatencyIcon} in {@link PlayerTabOverlay#render} to instead call
	 * {@link RdiPingNumberDisplay#renderPingNumber}.
	 */
	@Redirect(method = "render",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;renderPingIcon(Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V"))
	private void redirectRenderLatencyIconCall(
			PlayerTabOverlay instance, PoseStack poseStack, int width, int x, int y, PlayerInfo playerInfo) {
		RdiPingNumberDisplay.renderPingNumber(minecraft, poseStack, width, x, y, playerInfo);
	}
}
