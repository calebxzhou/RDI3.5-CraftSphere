package calebzhou.rdi.craftsphere.misc;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.util.DialogUtils;

public class LoadFinishHandler {
    public static void handle(){
        if(ExampleMod.loadProgressFrame!=null){
            //停止载入界面
            ExampleMod.loadEndTime = System.currentTimeMillis();
            ExampleMod.loadProgressFrame.dispose();
            ExampleMod.loadProgressFrame=null;
            float usedTime = (ExampleMod.loadEndTime-ExampleMod.loadStartTime)/1000.0f;
            String displayTime = String.format("%.2f",usedTime);
            //最快载入20秒
            int standardLoadTime = 20;
            double beyondPlayerRatio = 1.0 / (usedTime / standardLoadTime);
            if(beyondPlayerRatio>=1.0)
                beyondPlayerRatio=0.999;
            String beyondPerc = String.format("%.2f",beyondPlayerRatio*100);
            DialogUtils.showPopup("info","您本次载入游戏用时"+displayTime+"秒","超越了"+beyondPerc+"%的玩家！");
        }
    }
}
