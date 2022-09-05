package calebzhou.rdi.craftsphere.mixin.emoji;


import calebzhou.rdi.craftsphere.emojiful.EmojiClientProxy;
import calebzhou.rdi.craftsphere.emojiful.render.EmojiFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class mMinecraft {
    @Shadow @Final @Mutable public Font font;

    @Shadow @Final public FontManager fontManager;

    @Inject(method = "<init>",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/font/FontManager;createFont()Lnet/minecraft/client/gui/Font;",shift = At.Shift.AFTER))
    private void rdi_useEmojiFontRenderer_onInit(GameConfig gameConfig, CallbackInfo ci){
        EmojiClientProxy.oldFontRenderer = font;
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 5,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void rdi_useEmojiFontRenderer_onInit2(GameConfig gameConfig, CallbackInfo ci){
        font = EmojiFontRenderer.createInstance(fontManager.createFont());
    }

}
