package calebxzhou.rdi.mixin;

import calebxzhou.libertorch.mc.gui.LtMcUiRenderer;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static calebxzhou.rdi.consts.RdiConsts.MODID;

/**
 * Created  on 2023-04-08,20:37.
 */
@Mixin(AbstractWidget.class)
public abstract class mScreenRenderer {
	@Shadow
	protected float alpha;

	@Shadow
	protected abstract void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY);

	@Overwrite
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		LtMcUiRenderer.renderButton(poseStack,(AbstractWidget) (Object)this,alpha,()-> renderBg(poseStack, Minecraft.getInstance(), mouseX, mouseY));
	}
}
@Mixin(AbstractSliderButton.class)
class mWidgetRenderer2{
	@Shadow
	protected double value;

	@Overwrite
	public void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
		AbstractSliderButton btn = (AbstractSliderButton)(Object)this;
		LtMcUiRenderer.renderSlider(poseStack,btn.x,btn.y,btn.getWidth(),btn.getHeight(),value,btn.isHoveredOrFocused());
	}
}
@Mixin(KeyBindsList.KeyEntry.class)
class mKeyBindScreenRenderer{
	/*@ModifyConstant(method = "render",constant = @Constant(intValue = 0xFFFFFF))
	private int noWhite(int i){
		return 0x000000;
	}*/

}
@Mixin(KeyBindsList.CategoryEntry.class)
class mKeyBindScreenRenderer2{
	/*@ModifyConstant(method = "render",constant = @Constant(intValue = 0xFFFFFF))
	private int noWhite(int i){
		return 0x000000;
	}*/

}
@Mixin(Screen.class)
class mNoDirtBackground {
	@Shadow
	protected Minecraft minecraft;

	@Shadow
	public int width;

	@Shadow
	public int height;

	@Inject(method = "renderBackground(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",at=@At("HEAD"), cancellable = true)
	public void renderBackground(PoseStack poseStack, int vOffset, CallbackInfo ci) {
		boolean isInGame = this.minecraft.level != null;
		LtMcUiRenderer.renderBg(poseStack,width,height,isInGame);
		ci.cancel();

	}
	@Inject(method = "renderDirtBackground",at=@At("HEAD"), cancellable = true)
	public void renderDirtBackground(int vOffset, CallbackInfo ci) {
		LtMcUiRenderer.renderBg();
		ci.cancel();
	}
}
@Mixin(GuiComponent.class)
class mNoDirtBackground2{
	@Final
	@Shadow
	@Mutable
	public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation(MODID,"textures/alpha1.png");

}
@Mixin(Minecraft.class)
class mNoScreenFpsLimit{
	@Shadow
	@Nullable
	public ClientLevel level;

	@Shadow
	@Nullable
	public Screen screen;

	@Shadow
	private @Nullable Overlay overlay;

	@Shadow
	@Final
	private Window window;
	@Overwrite
	private int getFramerateLimit() {
		return level != null || screen == null && overlay == null ? window.getFramerateLimit() : Integer.MAX_VALUE;
	}
}
