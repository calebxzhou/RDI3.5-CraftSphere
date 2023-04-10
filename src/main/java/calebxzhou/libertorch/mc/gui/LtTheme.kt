package calebxzhou.libertorch.mc.gui

import calebxzhou.libertorch.ui.DefaultColors
import calebxzhou.libertorch.ui.LtColor

/**
 * Created  on 2023-04-09,10:34.
 */
data class LtTheme(
    //背景颜色
    val bgColor: LtColor,
    //字体颜色（启用/停用）
    val fontActiveColor: LtColor,
    val fontInactiveColor: LtColor,
    //组件外框颜色
    val widgetOutlineColor: LtColor,
    //滑块颜色
    val sliderHandleColor: LtColor,
    //提示框背景色
    val tooltipBgColor: LtColor,
    //提示框外框色
    val tooltipOutlineColor: LtColor
){
    companion object{
        var now: LtTheme = LtTheme(
            DefaultColors.PineGreen.color,
            DefaultColors.White.color,
            DefaultColors.Gray.color,
            DefaultColors.KleinBlue.color,
            DefaultColors.LightBlue.color,
            DefaultColors.OliveGreen.color,
            DefaultColors.LightBlue.color

            )
    }
}
