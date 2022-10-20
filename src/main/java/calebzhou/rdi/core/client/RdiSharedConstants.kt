package calebzhou.rdi.core.client

import java.io.File

/**
 * Created by calebzhou on 2022-09-10,15:09.
 */
object RdiSharedConstants {
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
    const val CORE_VERSION = "3.7k/221015"


    private val RDI_FOLDER = File("mods/rdi")
    @JvmField
	val RDI_SOUND_FOLDER = File(RDI_FOLDER, "sound")
    @JvmField
    val RDI_USERS_FOLDER = File(RDI_FOLDER, "users")
    @JvmField
	val RDI_FONT_FILE = File(RDI_FOLDER, "font/rdifont.ttf")
}
