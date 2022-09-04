package calebzhou.rdi.craftsphere.mixin.emoji;


import calebzhou.rdi.craftsphere.emojiful.render.EmojiFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class mMinecraft {
    @Redirect(method = "<init>",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/font/FontManager;createFont()Lnet/minecraft/client/gui/Font;"))
    private Font rdi_useEmojiFontRenderer_onInit(FontManager instance){
        return EmojiFontRenderer.createInstance(instance.createFont());
    }

}
