package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
//禁止使用unicode字体，太丑了
@Mixin(Minecraft.class)
public class MixinNoUnicodeFonts {
    @Overwrite
    public boolean isEnforceUnicode() {
        return false;
    }
}
@Mixin(LanguageSelectScreen.class)
class MixinNoUnicodeFonts2{
    @Redirect(method = "init()V",    at=@At(target = "Lnet/minecraft/client/gui/screens/LanguageSelectScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",    value = "INVOKE",ordinal = 0))
    private GuiEventListener asr1q2rd(LanguageSelectScreen instance, GuiEventListener element){
        return null;
    }
    @ModifyConstant(method = "init()V",    constant = @Constant(intValue = 160))
    private static int ascxgfvqa(int constant){
        return 80;
    }
}
