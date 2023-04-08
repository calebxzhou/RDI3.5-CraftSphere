package calebxzhou.rdi.consts

import java.io.File

/**
 * Created by calebzhou on 2022-09-10,15:09.
 */
object RdiConsts {
    const val SEED = 1145141919810
    const val SEA_LEVEL = 96

    //本地端口
    const val GAMEPLAY_PORT = 19198
    //mod id
    const val MODID = "rdict3"

    //mod id中文名
    const val MODID_DISPLAY = "RDI CeleTech"

    //是否为调试模式,本地用
    const val DEBUG = true

    //版本号与协议号
    const val PROTOCOL_VERSION = 0x37
    const val GAME_VERSION = "1.19.2"

    //显示版本
    const val CORE_VERSION_DISPLAY = "3.9/230408"
    //核心版本
    const val CORE_VERSION = 0x380


    private val RDI_FOLDER = File("mods/rdi")
    private val RDI_ASSETS_FOLDER = File(RDI_FOLDER,"assets")
    private val RDI_PROFILES_FOLDER = File(RDI_FOLDER,"profiles")
    @JvmField
	val RDI_SOUND_FOLDER = File(RDI_ASSETS_FOLDER, "sound")
    @JvmField
    val RDI_FONT_FILE = File(RDI_ASSETS_FOLDER, "font/rdifont.ttf")
    @JvmField
    val RDI_VIDEO_FOLDER = File(RDI_ASSETS_FOLDER, "video")

    @JvmField
    val RDI_USERS_FOLDER = File(RDI_PROFILES_FOLDER, "users")
    val RDI_LEVEL_FOLDER = File(RDI_PROFILES_FOLDER, "levels")
    val RDI_LEVEL_BKUP_FOLDER = File(RDI_PROFILES_FOLDER, "levels_backup")

}
