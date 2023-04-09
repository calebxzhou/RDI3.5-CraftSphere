package calebxzhou.libertorch.util

import calebxzhou.libertorch.ui.DefaultColors
import calebxzhou.libertorch.ui.LtColor
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft

/**
 * Created  on 2023-04-08,20:31.
 */
object Gl {
    @JvmStatic
    fun clearColor(color : LtColor){
        clearColor(color.redF, color.greenF  , color.blueF  ,1f)
    }
    @JvmStatic
    fun clearColor(red:Float,green:Float,blue:Float,alpha:Float){
        GlStateManager._clearColor(red,green,blue,alpha)
        GlStateManager._clear(16384, Minecraft.ON_OSX)
        RenderSystem.enableBlend ()
    }
}
