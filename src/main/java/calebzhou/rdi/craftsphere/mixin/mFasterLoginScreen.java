package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public class mFasterLoginScreen {
    @Redirect(method = "handleLogin",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void noSetDownloadTerrainScreen(Minecraft instance, Screen screen){
        instance.setScreen(null);
    }
}
