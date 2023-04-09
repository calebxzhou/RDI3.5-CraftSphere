package calebxzhou.libertorch.util

import java.time.LocalDateTime

/**
 * Created  on 2023-04-09,11:31.
 */
object TimeUt {
    @JvmStatic
    fun periodOfDay(): String{
        return when (LocalDateTime.now().hour) {
            in 0..5 -> "凌晨"
            in 6..8 -> "早上"
            in 9..10 -> "上午"
            in 11..12 -> "中午"
            in 13..17 -> "下午"
            in 18..23 -> "晚上"
            else -> {"您好"}
        }
    }
}
