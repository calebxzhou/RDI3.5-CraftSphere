package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(PlayerListHud.class)
public class MixinTabPlayerHud {

    /**
     * @author 旁观模式不显示灰色昵称
     */
    @Overwrite
    private Text applyGameModeFormatting(PlayerListEntry entry, MutableText name) {
        return name;
    }
    @ModifyArg(
            method = "Lnet/minecraft/client/gui/hud/PlayerListHud;" +
                    "render(Lnet/minecraft/client/util/math/MatrixStack;" +
                    "ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
    at =@At(value = "INVOKE",target = "Lnet/minecraft/client/font/TextRenderer;" +
            "drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I"),
    index = 4)
    private int noSpecGray(int constant) {
        return -1;
    }

    //离线登录也显示头像
    @Redirect(method = "Lnet/minecraft/client/gui/hud/PlayerListHud;render(Lnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
    at = @At(value = "INVOKE",target = "Lnet/minecraft/network/ClientConnection;isEncrypted()Z"))
    private boolean alwaysDisplayAvatar(ClientConnection instance){
        return true;
    }

    //延迟图标永远是绿的
    @Redirect(method = "Lnet/minecraft/client/gui/hud/PlayerListHud;renderLatencyIcon(Lnet/minecraft/client/util/math/MatrixStack;IIILnet/minecraft/client/network/PlayerListEntry;)V",
    at=@At(value = "INVOKE",target = "Lnet/minecraft/client/network/PlayerListEntry;getLatency()I"))
    private int getLatency(PlayerListEntry instance){
        return 20;
    }
}
