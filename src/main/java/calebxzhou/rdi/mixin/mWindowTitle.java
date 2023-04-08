package calebxzhou.rdi.mixin;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static calebxzhou.rdi.consts.RdiConsts.CORE_VERSION_DISPLAY;
import static calebxzhou.rdi.consts.RdiConsts.MODID_DISPLAY;

@Mixin(Minecraft.class)
public class mWindowTitle {

    /**
     * @author
     *  RDI 窗口标题
     */
    @Overwrite
    private String createTitle(){
        return MODID_DISPLAY+" "+ CORE_VERSION_DISPLAY;
    }
}
