package calebxzhou.libertorch

/**
 * 默认调色盘
 * Created  on 2023-03-12,13:12.
 */
enum class DefaultColorPalette(val color: Int) {

    OLIVE_GREEN(0x052e05),
    PINE_GREEN( 0x223a22),
    WHITE(0xffffff),


    ;

    //红
    fun red():Int{
        return color shr 16 and 0xFF
    }
    //绿
    fun green(): Int{
        return color shr 8 and 0xFF
    }
    //蓝
    fun blue(): Int{
        return color and 0xFF
    }
    //不透明色值 alpha=FF
    fun opaque() : Int{
        return color or 0xFF000000.toInt()
    }

}
