package calebzhou.rdi.craftsphere;

import net.minecraft.resources.ResourceLocation;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

//网络包
public class NetworkPackets {
    //C2S 客户端->服务端
    //隔空跳跃
    public static final ResourceLocation LEAP=new ResourceLocation(MODID,"leap");
    //挂机检测
    public static final ResourceLocation AFK_DETECT = new ResourceLocation(MODID,"afk_detect");
    //跳舞树
    public static final ResourceLocation DANCE_TREE_GROW =new ResourceLocation(MODID,"dance_tree_grow");
    //硬件信息
    public static final ResourceLocation HW_SPEC=new ResourceLocation(MODID,"hw_spec");

    //S2C 服务端->客户端

    //岛屿信息
    public static final ResourceLocation ISLAND_INFO =new ResourceLocation(MODID,"island_info");
    //对话框信息
    public static final ResourceLocation DIALOG_INFO = new ResourceLocation(MODID,"dialog_info");
}
