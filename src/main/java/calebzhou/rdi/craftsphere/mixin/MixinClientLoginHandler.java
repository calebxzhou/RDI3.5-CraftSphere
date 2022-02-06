package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.client.network.ClientLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientLoginNetworkHandler.class)
public class MixinClientLoginHandler {

    @ModifyConstant(method = "Lnet/minecraft/client/network/ClientLoginNetworkHandler;joinServerSession(Ljava/lang/String;)Lnet/minecraft/text/Text;",
    constant = @Constant(stringValue = "disconnect.loginFailedInfo"))
    private String joinServer(String s){
        return "请尝试重新登录账号。\n【%s】";
    }
}
