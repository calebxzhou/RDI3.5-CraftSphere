package calebzhou.rdi.core.client.mixin;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by calebzhou on 2022-09-18,20:40.
 */
@Mixin(Window.class)
public class mBiggerWindow {

	@Shadow
	@Final
	private long window;

	@Shadow
	private int windowedWidth;

	@Shadow
	private int windowedHeight;

	@Shadow
	private int width;

	@Shadow
	private int height;

	@Inject(method = "<init>",at = @At(value = "INVOKE",target = "Lorg/lwjgl/glfw/GLFW;glfwDefaultWindowHints()V"))
	private void max(WindowEventHandler windowEventHandler, ScreenManager screenManager, DisplayData displayData, String string, String string2, CallbackInfo ci){
		windowedWidth=width=1280;
		windowedHeight=height=720;

	}
}
