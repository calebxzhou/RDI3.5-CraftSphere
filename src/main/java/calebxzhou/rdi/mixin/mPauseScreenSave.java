package calebxzhou.rdi.mixin;

import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PauseScreen.class)
public class mPauseScreenSave {

    @ModifyConstant(method = "createPauseMenu",constant = @Constant(stringValue = "menu.disconnect"))
    private String save(String s){
        return "rdi.menu.disconnect";
    }
}
