package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.misc.RdiSystemTray;
import calebzhou.rdi.craftsphere.util.FileUtils;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

@Mixin(Main.class)
public class mCreateTray {

    @Inject(method = "main",remap = false,at = @At("HEAD"))
    private static void createTray(String[] strings, CallbackInfo ci){
        RdiSystemTray.createTray();
    }
}
