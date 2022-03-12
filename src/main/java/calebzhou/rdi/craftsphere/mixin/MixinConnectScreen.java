package calebzhou.rdi.craftsphere.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.Serial;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

@Mixin(ConnectScreen.class)
public class MixinConnectScreen {
    @Shadow
    @Mutable
    private Component status = new TextComponent("载入模组数据...");

    //不在日志中显示connecting to
    @ModifyConstant(
            method = "Lnet/minecraft/client/gui/screens/ConnectScreen;connect(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;)V",
            constant = @Constant(stringValue = "Connecting to {}, {}")
    )
    private String asdas(String asd){
        return "";
    }

}
