package calebzhou.rdi.core.client.mixin.emoji;


import calebzhou.rdi.core.client.emoji.EmojiClientProxy;
import calebzhou.rdi.core.client.emoji.render.EmojiFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class mMinecraft {
    @Shadow @Final @Mutable public Font font;

	@Shadow
	public abstract EntityRenderDispatcher getEntityRenderDispatcher();

	@Inject(method = "<init>",at=@At("TAIL"))
	private void useEmojiFont(GameConfig gameConfig, CallbackInfo ci){
		EmojiClientProxy.oldFontRenderer = font;
		font = EmojiFontRenderer.createInstance(font);
		//getEntityRenderDispatcher().font = font;
		/*BlockEntityRenderers.register(BlockEntityType.SIGN, context -> {
			SignRenderer signRenderer = new SignRenderer(context);
			signRenderer.font = font;
			return signRenderer;
		});*/
	}

}
