package calebxzhou.libertorch.ui

/**
 * Created  on 2023-04-09,10:55.
 */
//颜色 hex=16进制色值
data class LtColor(val hex: Int){
    //红色 0~255
    val red
        get() = hex shr 16 and 0xFF
    //绿 0~255
    val green
        get() = hex shr 8 and 0xFF
    //蓝 0~255
    val blue
        get() = hex and 0xFF
    //不透明hex色值
    val opaque
        get() = hex or 0xFF000000.toInt()

    //红 0.0~1.0F
    val redF
        get() = red / 255f
    //绿 0.0~1.0F
    val greenF
        get() = green / 255f
    //蓝 0.0~1.0F
    val blueF
        get() = blue / 255f

}
