package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.misc.ServerConnector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class mLoadComplete {
    @Redirect(method = "<init>(Lnet/minecraft/client/main/GameConfig;)V",
    at=@At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void directGoServer(Minecraft instance, Screen screen){
        //直接连接服务器
        ServerConnector.connect();
    }

}

