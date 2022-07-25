package calebzhou.rdi.craftsphere.mixin;

import com.google.common.collect.Ordering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;
import java.util.List;

@Mixin(PlayerTabOverlay.class)
public class MixinNovelNameTabOverlay {
    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V",
    at=@At(value = "INVOKE",target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;"))
    private List acx(Ordering instance, Iterable elements){
        return Arrays.asList(Minecraft.getInstance().player.connection.getOnlinePlayers().toArray());
    }
}
