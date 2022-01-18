package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.util.FileUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.InputStream;

@Mixin(MinecraftClient.class)
public class WindowIconMixin {

    @Redirect(method = "Lnet/minecraft/client/MinecraftClient;<init>(Lnet/minecraft/client/RunArgs;)V",
    at=@At(value="INVOKE",target="Lnet/minecraft/client/util/Window;setIcon(Ljava/io/InputStream;Ljava/io/InputStream;)V"))
    private void setIcon(Window instance, InputStream icon16, InputStream icon32){
        InputStream stream = FileUtils.getJarResourceAsStream("icon.png");
        instance.setIcon(stream,stream);
    }

}
