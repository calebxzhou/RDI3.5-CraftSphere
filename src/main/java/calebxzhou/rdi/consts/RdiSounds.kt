package calebxzhou.rdi.consts

import calebxzhou.rdi.util.FileUtils
import java.io.InputStream

/**
 * Created  on 2023-02-24,23:30.
 */
enum class RdiSounds {

    Connect,
    Disconnect,
    Settings,
    Startup,
    Launch,;


    private val dir = "sounds/"
    val oggStream: InputStream
        get() {
            return FileUtils.getJarAsset("${dir}${this.name.lowercase()}.ogg")
        }

}
