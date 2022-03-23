package calebzhou.rdi.craftsphere.mixin;


import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyBindsList.KeyEntry.class)
public interface AccessKeyEntry {
    /*@Invoker("<init>")
    static KeyBindsList.KeyEntry callInit(KeyMapping keyMapping, Component component) {
        return null;
    }*/
}
