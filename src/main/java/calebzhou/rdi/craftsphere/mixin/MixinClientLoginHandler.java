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
        return "登录失败。\n" +
                "如果您使用微软/Mojang账号，请在启动器中重新登录。\n如果您没有使用微软/Mojang账号，那么您的昵称可能已被他人占用，请更换其他昵称。\n【%s】";
    }
}
