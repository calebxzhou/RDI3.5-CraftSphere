package calebzhou.rdi.craftsphere;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

public class PlayerMotionDetect {
    private static int ticks = 0;
    public static void detect(ClientWorld world) {
        MinecraftClient client = MinecraftClient.getInstance();
            double y1 = client.player.getY();
            ++ticks;
            if(ticks>20){
                double y2 = client.player.getY();
                double deltaY = y2-y1;
                if(deltaY > RdiConfigure.getConfig().autoSlowfallSpeed && RdiConfigure.getConfig().autoSlowfallSpeed>0){
                    client.player.sendChatMessage("/slowfall 1");
                }
                if(y2<-80)
                    client.player.sendChatMessage("/spawn");
                ticks=0;
            }


    }
}
