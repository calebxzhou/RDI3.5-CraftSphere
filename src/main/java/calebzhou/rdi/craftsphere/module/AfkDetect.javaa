package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.util.NetworkUtils;
import calebzhou.rdi.craftsphere.util.WorldTickable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

public class AfkDetect implements WorldTickable {
    public static final ResourceLocation AFK_NETWORK = new ResourceLocation(MODID,"afk");
    public static final int TICKS_ON_AFK = 5*60*20;//5分钟不动就是挂机
    //总挂机时间
    private int totalAfkTicks =0;
    @Override
    public void tickWorld(ClientLevel world) {
        long handle = client.getWindow().getWindow();
        ++totalAfkTicks;
        //触碰键盘，告诉服务器停止挂机
        GLFW.glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
            totalAfkTicks =0;
            NetworkUtils.sendPacketC2S(AFK_NETWORK,client.player.getScoreboardName()+",noafk,"+totalAfkTicks);
        });
        //如果达到了挂机时间，告诉服务器已经挂机
        if(totalAfkTicks >=TICKS_ON_AFK){
            //三秒发送一次
            if(totalAfkTicks%60==0)
                NetworkUtils.sendPacketC2S(AFK_NETWORK,client.player.getScoreboardName()+",afk,"+totalAfkTicks);
        }
    }
}
