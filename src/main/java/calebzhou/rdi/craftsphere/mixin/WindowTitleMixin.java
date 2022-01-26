package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MinecraftClient.class)
public class WindowTitleMixin {
    /**
     * @author
     *  RDI 窗口标题
     */
    @Overwrite
    private String getWindowTitle(){
        return "RDI 天空科技 3.0Beta3  2022/01/26";
    }
}
