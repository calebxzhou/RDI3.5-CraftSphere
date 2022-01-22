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
        return "仅支持 RDI账号/微软账号 登录，不支持盗版。\n若您已拥有账号，请尝试在启动器中 重新登录 账号。\n【%s】";
    }
}
