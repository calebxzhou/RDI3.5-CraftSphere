package calebxzhou.rdi.screen

import calebxzhou.rdi.logger
import net.minecraft.client.gui.screens.Screen

/**
 * Created  on 2023-04-14,20:39.
 */
object ScreenChangeFilter {
    //不进入better end的欢迎画面
    private val noEnterScreenList = listOf("org.betterx.bclib.client.gui.screens.WelcomeScreen")

    @JvmStatic
    fun doFilter(screenClass: Screen) : Boolean{
        val name = screenClass.javaClass.name
        return if(noEnterScreenList.contains(name)){
            logger.info("不进入画面$name")
            false
        }else true
    }
}
