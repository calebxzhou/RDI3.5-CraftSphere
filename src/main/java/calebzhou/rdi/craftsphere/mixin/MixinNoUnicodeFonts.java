package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MixinNoUnicodeFonts {
    /**
     * @author
     */
    @Overwrite
    public boolean forcesUnicodeFont() {
        return false;
    }
}
@Mixin(LanguageOptionsScreen.class)
class MixinNoUnicodeFonts2{
    @Redirect(method = "Lnet/minecraft/client/gui/screen/option/LanguageOptionsScreen;init()V",
    at=@At(target = "Lnet/minecraft/client/gui/screen/option/LanguageOptionsScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;",
    value = "INVOKE",ordinal = 0))
    private Element asr1q2rd(LanguageOptionsScreen instance, Element element){
        return null;
    }
    @ModifyConstant(method = "Lnet/minecraft/client/gui/screen/option/LanguageOptionsScreen;init()V",
    constant = @Constant(intValue = 160))
    private static int ascxgfvqa(int constant){
        return 80;
    }
}
