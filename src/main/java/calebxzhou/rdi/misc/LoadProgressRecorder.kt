package calebxzhou.rdi.misc

import calebxzhou.libertorch.sound.OggPlayer
import calebxzhou.libertorch.sound.SoundPlayer
import calebxzhou.rdi.consts.RdiSounds

object LoadProgressRecorder {
    @JvmField
    var loadStartTime: Long = 0
    var loadEndTime: Long = 0
    var musicPlayJob : OggPlayer?=null
    val standardLoadTime = 40
    @JvmStatic
	fun onFinish() {
        loadEndTime = System.currentTimeMillis()

        SoundPlayer.playOgg(RdiSounds.Startup.oggStream)
//showPopup(TrayIcon.MessageType.INFO, "您本次载入游戏用时" + displayTime + "秒", "超越了$beyondPerc%的玩家！")

    }
    //载入游戏用了多少秒
    /*fun getLoadTimeSeconds():Float{
        return (loadEndTime - loadStartTime) / 1000.0f
    }
    //载入时间超过了百分之多少的玩家
    fun getLoadTimePercentBeyondPlayers():Float{
        val usedTime = getLoadTimeSeconds()
        var beyondPlayerRatio = 1.0 / (usedTime / standardLoadTime)
        if (beyondPlayerRatio >= 1.0)
            beyondPlayerRatio = 0.999
        return (beyondPlayerRatio * 100).toFloat()
    }*/

}
