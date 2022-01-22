package calebzhou.rdi.craftsphere.mixin;


import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.io.Serial;

@Mixin(ConnectScreen.class)
public class MixinConnectScreen {
    @Shadow
    @Mutable
    private Text status = new LiteralText("正在载入模组数据...");
}
