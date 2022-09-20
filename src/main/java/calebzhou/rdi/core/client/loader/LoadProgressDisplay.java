package calebzhou.rdi.core.client.loader;

import calebzhou.rdi.core.client.misc.MusicPlayer;
import calebzhou.rdi.core.client.misc.RdiSystemTray;
import calebzhou.rdi.core.client.util.DialogUtils;
import net.minecraft.Util;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class LoadProgressDisplay extends Thread{
    public static final LoadProgressDisplay INSTANCE = new LoadProgressDisplay();
    private long loadStartTime;
    private long loadEndTime;
    private LoadProgressDisplay(){
		if(Util.getPlatform() != Util.OS.WINDOWS) return;

    }

    @Override
    public void run() {
        loadStartTime=System.currentTimeMillis();
    }


    public void onFinish(){
        if(Util.getPlatform() != Util.OS.WINDOWS)
			return;
            loadEndTime = System.currentTimeMillis();
			float usedTime = (loadEndTime-loadStartTime)/1000.0f;
            String displayTime = String.format("%.2f",usedTime);
            //最快载入20秒
            int standardLoadTime = 40;
            double beyondPlayerRatio = 1.0 / (usedTime / standardLoadTime);
            if(beyondPlayerRatio>=1.0)
                beyondPlayerRatio=0.999;
            String beyondPerc = String.format("%.2f",beyondPlayerRatio*100);
            DialogUtils.showPopup(TrayIcon.MessageType.INFO,"您本次载入游戏用时"+displayTime+"秒","超越了"+beyondPerc+"%的玩家！");
            MusicPlayer.playStartupMusic();
    }
}
