package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerTabOverlay.class)
public class MixinTabPlayerHud {

    //旁观模式不显示灰色昵称
    @Overwrite
    private Component decorateName(PlayerInfo entry, MutableComponent name) {
        return name;
    }
    @ModifyArg(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V",
            at =@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/network/chat/Component;FFI)I"),
    index = 4)
    private int noSpecGray(int constant) {
        return -1;
    }

    //离线登录也显示头像
    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V",
    at = @At(value = "INVOKE",target = "Lnet/minecraft/network/Connection;isEncrypted()Z"))
    private boolean alwaysDisplayAvatar(Connection instance){
        return true;
    }

    //延迟图标永远是绿的 满格
    @Redirect(method = "renderPingIcon(Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V",
    at=@At(value = "INVOKE",target = "Lnet/minecraft/client/multiplayer/PlayerInfo;getLatency()I"))
    private int getLatency(PlayerInfo instance){
        return 20;
    }
}
