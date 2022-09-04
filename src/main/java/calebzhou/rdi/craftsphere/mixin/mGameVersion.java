package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.RdiCore;
import net.minecraft.WorldVersion;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//改变游戏版本
@Mixin(DebugScreenOverlay.class)
public class mGameVersion {
    @Redirect(method = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;getGameInformation()Ljava/util/List;",
    at = @At(value = "INVOKE",target = "Lnet/minecraft/WorldVersion;getName()Ljava/lang/String;"))
    private String version(WorldVersion instance){
        return RdiCore.GAME_VERSION;
    }
}
