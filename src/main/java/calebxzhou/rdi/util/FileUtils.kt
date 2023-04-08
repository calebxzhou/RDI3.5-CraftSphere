package calebxzhou.rdi.util

import calebxzhou.rdi.RdiCore
import calebxzhou.rdi.consts.RdiConsts.MODID
import java.io.File
import java.io.InputStream

object FileUtils {
    val GAME_FOLDER = File(".")
    val MOD_FOLDER = File(GAME_FOLDER, "mods")
    @JvmStatic
    fun getJarAsset(fileInJar: String): InputStream {
        return RdiCore::class.java.getResourceAsStream("/assets/$MODID/$fileInJar")!!
    }
}
