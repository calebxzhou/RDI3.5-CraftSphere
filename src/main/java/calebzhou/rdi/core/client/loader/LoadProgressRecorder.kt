package calebzhou.rdi.core.client.loader

import calebzhou.rdi.core.client.RdiSharedConstants
import calebzhou.rdi.core.client.misc.MusicPlayer
import calebzhou.rdi.core.client.misc.RdiSystemTray.createTray
import calebzhou.rdi.core.client.util.DialogUtils.showPopup
import calebzhou.rdi.core.client.util.OggPlayer
import kotlinx.coroutines.*
import net.minecraft.Util
import java.awt.TrayIcon
import java.io.File

object LoadProgressRecorder {


    //载入进度条 -1开始 1结束
    var loadProgress = -1f
    @JvmField
	var loadStartTime: Long = 0
    private var loadEndTime: Long = 0
    lateinit var musicPlayJob : OggPlayer
	fun onFinish() {
        if (Util.getPlatform() !== Util.OS.WINDOWS) return
        loadEndTime = System.currentTimeMillis()
        val usedTime = (loadEndTime - loadStartTime) / 1000.0f
        val displayTime = String.format("%.2f", usedTime)
        //最快载入20秒
        val standardLoadTime = 40
        var beyondPlayerRatio = 1.0 / (usedTime / standardLoadTime)
        if (beyondPlayerRatio >= 1.0) beyondPlayerRatio = 0.999
        val beyondPerc = String.format("%.2f", beyondPlayerRatio * 100)
        showPopup(TrayIcon.MessageType.INFO, "您本次载入游戏用时" + displayTime + "秒", "超越了$beyondPerc%的玩家！")
               GlobalScope.launch {
                MusicPlayer.playOgg(File(RdiSharedConstants.RDI_SOUND_FOLDER, "startup.ogg"))
                delay(1500)
            musicPlayJob = MusicPlayer.playOgg(File(RdiSharedConstants.RDI_SOUND_FOLDER, "title.ogg"))
            }

    }
    fun onStart(){
        loadStartTime = System.currentTimeMillis()
        createTray()
        if (Util.getPlatform() === Util.OS.WINDOWS)
            showPopup(TrayIcon.MessageType.INFO, "RDI客户端已经开始载入了！请您耐心等待...")

    }
}
