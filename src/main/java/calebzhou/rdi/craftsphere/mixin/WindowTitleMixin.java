package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Minecraft.class)
public class WindowTitleMixin {
    /**
     * @author
     *  RDI 窗口标题
     */
    @Overwrite
    private String getWindowTitle(){
        return ExampleMod.MODID_CHN+" "+ ExampleMod.VER_DISPLAY;
    }
}
