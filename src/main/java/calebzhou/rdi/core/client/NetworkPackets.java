package calebzhou.rdi.core.client;

import net.minecraft.resources.ResourceLocation;

import static calebzhou.rdi.core.client.RdiCore.MODID;

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
    //右下角消息弹框
    public static final ResourceLocation POPUP=new ResourceLocation(MODID,"popup");
    //存档
    public static final ResourceLocation SAVE_WORLD = new ResourceLocation(MODID,"save_world");
    //快速繁殖
    public static final ResourceLocation ANIMAL_SEX=new ResourceLocation(MODID,"animal_sex");
    public static final ResourceLocation TELEPORT=new ResourceLocation(MODID,"teleport");
    //S2C 服务端->客户端

    //岛屿信息
    public static final ResourceLocation ISLAND_INFO =new ResourceLocation(MODID,"island_info");
    //对话框信息
    public static final ResourceLocation DIALOG_INFO = new ResourceLocation(MODID,"dialog_info");
}
