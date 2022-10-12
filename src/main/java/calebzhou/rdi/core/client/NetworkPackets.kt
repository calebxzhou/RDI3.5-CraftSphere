package calebzhou.rdi.core.client

import net.minecraft.resources.ResourceLocation

//网络包
public object NetworkPackets {
    /**
     * C2S  客户端->服务端
     */@JvmField
    //挂机检测
    public val AFK_DETECT = ResourceLocation(RdiSharedConstants.MODID, "afk_detect")
    @JvmField
    //跳舞树
    val DANCE_TREE_GROW = ResourceLocation(RdiSharedConstants.MODID, "dance_tree_grow")
    @JvmField
    //硬件信息
    val HW_SPEC = ResourceLocation(RdiSharedConstants.MODID, "hw_spec")

    //存档
@JvmField
	val SAVE_WORLD = ResourceLocation(RdiSharedConstants.MODID, "save_world")
    @JvmField
    //快速繁殖
    val ANIMAL_SEX = ResourceLocation(RdiSharedConstants.MODID, "animal_sex")

    /**
     * S2C 服务端->客户端
     */
    //玩家天气信息
    @JvmField
	val WEATHER = ResourceLocation(RdiSharedConstants.MODID, "weather")

    //玩家地理定位
    @JvmField
	val GEO_LOCATION = ResourceLocation(RdiSharedConstants.MODID, "geo_location")

    //对话框信息
    @JvmField
	val DIALOG_INFO = ResourceLocation(RdiSharedConstants.MODID, "dialog_info")

    //右下角消息弹框
    @JvmField
	val POPUP = ResourceLocation(RdiSharedConstants.MODID, "popup")

    //设定密码
    @JvmField
	val SET_PASSWORD = ResourceLocation(RdiSharedConstants.MODID, "set_password")
}
