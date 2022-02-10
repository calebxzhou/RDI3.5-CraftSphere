package calebzhou.rdi.craftsphere.mixin;


import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.io.Serial;

@Mixin(ConnectScreen.class)
public class MixinConnectScreen {
    @Shadow
    @Mutable
    private Text status = new LiteralText("载入模组数据...");
    @ModifyConstant(
            method = "Lnet/minecraft/client/gui/screen/ConnectScreen;connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;)V",
            constant = @Constant(stringValue = "Connecting to {}, {}")
    )
    private String asdas(String asd){
        return "";
    }

}
