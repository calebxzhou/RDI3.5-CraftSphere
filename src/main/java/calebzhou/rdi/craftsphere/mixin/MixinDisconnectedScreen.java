package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.util.DialogUtils;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public class MixinDisconnectedScreen {
    @Inject(method = "Lnet/minecraft/client/gui/screen/DisconnectedScreen;<init>(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;)V",
    at=@At("TAIL"))
    private void _123asd(Screen parent, Text title, Text reason, CallbackInfo ci){
        if(reason.getString().contains("contains IDs unknown"))
            DialogUtils.showError("客户端Mod与服务器不匹配\n请您到群文件下载新的模组包。");
    }
}
