package calebzhou.rdi.core.client;

import net.minecraft.resources.ResourceLocation;

import static calebzhou.rdi.core.client.RdiSharedConstants.MODID;


//网络包
public class NetworkPackets {
    /**
     *  C2S  客户端->服务端
     */
    //挂机检测
    public static final ResourceLocation AFK_DETECT = new ResourceLocation(MODID,"afk_detect");
    //跳舞树
    public static final ResourceLocation DANCE_TREE_GROW =new ResourceLocation(MODID,"dance_tree_grow");
    //硬件信息
    public static final ResourceLocation HW_SPEC=new ResourceLocation(MODID,"hw_spec");

    //存档
    public static final ResourceLocation SAVE_WORLD = new ResourceLocation(MODID,"save_world");
    //快速繁殖
    public static final ResourceLocation ANIMAL_SEX=new ResourceLocation(MODID,"animal_sex");
    /**
    *		S2C 服务端->客户端
     */
//玩家天气信息
	public static final ResourceLocation WEATHER = new ResourceLocation(MODID,"weather");
	//玩家地理定位
	public static final ResourceLocation GEO_LOCATION = new ResourceLocation(MODID,"geo_location");
    //对话框信息
    public static final ResourceLocation DIALOG_INFO = new ResourceLocation(MODID,"dialog_info");
	//右下角消息弹框
	public static final ResourceLocation POPUP=new ResourceLocation(MODID,"popup");
	//设定密码
	public static final ResourceLocation SET_PASSWORD = new ResourceLocation(MODID,"set_password");

}
