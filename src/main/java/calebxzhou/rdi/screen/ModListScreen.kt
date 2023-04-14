package calebxzhou.rdi.screen

import calebxzhou.libertorch.mc.gui.LtScreen
import org.quiltmc.loader.api.ModMetadata
import org.quiltmc.loader.api.QuiltLoader

/**
 * Created  on 2023-04-11,12:36.
 */
class ModListScreen: LtScreen("mod管理") {
    val modMap = hashMapOf<String,ModMetadata>()
    init {
        QuiltLoader.getAllMods().forEach { modContainer ->
            modContainer.metadata()
        }
    }
    override fun init() {

    }
}
