package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerListHud.class)
public class MixinPlayerHud {
    /**
     * @author
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
    private int noSpecGray(int constant)
    {
        return -1;
    }
}
