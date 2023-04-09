package calebxzhou.rdi.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static calebxzhou.rdi.consts.RdiConsts.CoreVersion;
import static calebxzhou.rdi.consts.RdiConsts.MODID_DISPLAY;

@Mixin(Minecraft.class)
public class mWindowTitle {

    /**
     * @author
     *  RDI 窗口标题
     */
    @Overwrite
    private String createTitle(){
        return MODID_DISPLAY+" "+ CoreVersion;
    }
}
