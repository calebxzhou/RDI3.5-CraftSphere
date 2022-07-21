package calebzhou.rdi.craftsphere.module.area;

import calebzhou.rdi.craftsphere.module.cmdtip.MessageType;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import java.util.Optional;
import net.minecraft.core.BlockPos;

public class ModelAreaSelection {
    public static final ModelAreaSelection INSTANCE = new ModelAreaSelection();
    public static boolean isAreaSelectionMode = false;

    private BlockPos p1;
    private BlockPos p2;

    public Optional<BlockPos> getP1() {
        return Optional.ofNullable(p1);
    }

    public void setP1(BlockPos p1) {
        DialogUtils.showInfoIngame("成功设置区域点1："+p1.toShortString(), MessageType.SUCCESS);
        this.p1 = p1;
    }

    public  Optional<BlockPos> getP2() {
        return Optional.ofNullable(p2);
    }

    public  void setP2(BlockPos p2) {
        DialogUtils.showInfoIngame("成功设置区域点2："+p2.toShortString(),MessageType.SUCCESS);
        this.p2 = p2;
    }
    public void clear(){
        this.p1=null;
        this.p2=null;
    }

}
