package calebzhou.rdi.craftsphere.mixin;


import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ConnectScreen.class)
public class mConnectScreen {
    @Shadow
    @Mutable
    private Component status = Component.literal("载入模组数据...");

    //不在日志中显示connecting to的，防止看出来ip地址
    @ModifyConstant(
            method = "connect(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;)V",
            constant = @Constant(stringValue = "Connecting to {}, {}")
    )
    private String asdas(String asd){
        return "Connecting";
    }

}
