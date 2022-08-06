package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.util.DialogUtils;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(ClientboundUpdateRecipesPacket.class)
public class mCheckModList {
    @Redirect(method = "fromNetwork",at = @At(value = "INVOKE",target = "Ljava/util/Optional;orElseThrow(Ljava/util/function/Supplier;)Ljava/lang/Object;"))
    private static Object checkML(Optional instance, Supplier<? > exceptionSupplier) throws Throwable {
        return instance.orElseThrow(()-> {
            DialogUtils.showPopup(TrayIcon.MessageType.ERROR,"Mod列表与服务器不匹配","请更新客户端");
            return exceptionSupplier;
        });
    }
}
