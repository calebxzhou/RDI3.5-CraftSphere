package calebzhou.rdi.craftsphere.misc;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import net.minecraft.Util;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class LoadProgressDisplay {
    public static long loadStartTime;
    public static long loadEndTime;
    private static JTextArea loadProgressInfo  ;
    private static JProgressBar loadProgressBar  ;
    private static JFrame loadProgressFrame ;
    static {
        MusicPlayer.playStartupMusic();
        if(Util.getPlatform() == Util.OS.WINDOWS){
            loadProgressInfo = new JTextArea("RDI客户端正在启动....\n");
            loadProgressBar = new JProgressBar();
            loadProgressFrame= new JFrame("RDI客户端启动中");
            RdiSystemTray.createTray();

            loadProgressBar.setMaximum(7000);
            loadStartTime=System.currentTimeMillis();
            loadProgressFrame.setLayout(new BorderLayout());
            loadProgressFrame.setAlwaysOnTop (true);
            loadProgressFrame.setBounds(0,0,400,300);
            DefaultCaret caret = (DefaultCaret)loadProgressInfo.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            loadProgressInfo.setCaret(caret);

            JScrollPane scroll = new JScrollPane (loadProgressInfo,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

            loadProgressFrame.add(scroll,BorderLayout.CENTER);
            loadProgressFrame.add(loadProgressBar,BorderLayout.SOUTH);
            loadProgressFrame.setLocationRelativeTo(null);
            loadProgressFrame.setVisible(true);
        }
    }
    public static void appendLoadProgressInfo(String info){
        if(Util.getPlatform() != Util.OS.WINDOWS)
            return;
        loadProgressInfo.append(info);
        int barValue = loadProgressBar.getValue();

        if(info.startsWith("#")){
            ++barValue;
        }else{
            barValue+=50;
        }
        loadProgressInfo.append("\n");
        loadProgressBar.setValue(barValue);
        loadProgressInfo.setCaretPosition(loadProgressInfo.getDocument().getLength());

    }
    public static void onFinish(){
        if(Util.getPlatform() != Util.OS.WINDOWS)
            return;
        if(loadProgressFrame!=null){
            //停止载入界面
            loadEndTime = System.currentTimeMillis();
            loadProgressFrame.dispose();
            loadProgressFrame=null;
            float usedTime = (loadEndTime-loadStartTime)/1000.0f;
            String displayTime = String.format("%.2f",usedTime);
            //最快载入20秒
            int standardLoadTime = 20;
            double beyondPlayerRatio = 1.0 / (usedTime / standardLoadTime);
            if(beyondPlayerRatio>=1.0)
                beyondPlayerRatio=0.999;
            String beyondPerc = String.format("%.2f",beyondPlayerRatio*100);
            DialogUtils.showPopup(TrayIcon.MessageType.INFO,"您本次载入游戏用时"+displayTime+"秒","超越了"+beyondPerc+"%的玩家！");
        }
    }
}
