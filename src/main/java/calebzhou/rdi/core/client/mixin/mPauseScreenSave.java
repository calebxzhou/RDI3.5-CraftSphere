package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.NetworkPackets;
import calebzhou.rdi.core.client.util.NetworkUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class mPauseScreenSave {

    @ModifyConstant(method = "createPauseMenu",constant = @Constant(stringValue = "menu.disconnect"))
    private String save(String s){
        return "rdi.menu.disconnect";
    }
}
