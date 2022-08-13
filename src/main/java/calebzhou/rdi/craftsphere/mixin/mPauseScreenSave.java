package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.NetworkPackets;
import calebzhou.rdi.craftsphere.util.NetworkUtils;
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

    @Inject(method = "method_19836",at = @At(value = "HEAD"))
    public void saveOnExit(Button button, CallbackInfo ci){
        NetworkUtils.sendPacketToServer(NetworkPackets.SAVE_WORLD,1);

    }
    @ModifyConstant(method = "createPauseMenu",constant = @Constant(stringValue = "menu.disconnect"))
    private String save(String s){
        return "rdi.menu.disconnect";
    }
}
