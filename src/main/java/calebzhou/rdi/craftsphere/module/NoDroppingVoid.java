package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.RdiConfigure;
import calebzhou.rdi.craftsphere.util.WorldTickable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public class NoDroppingVoid implements WorldTickable {
    public NoDroppingVoid() {
        ClientTickEvents.END_WORLD_TICK.register(this::tickWorld);
    }

   // private static int ticks = 0;
    @Override
    public void tickWorld(ClientWorld world) {
        MinecraftClient client = MinecraftClient.getInstance();
        double y1 = client.player.getY();
        /*++ticks;
        if(ticks>20){
            double y2 = client.player.getY();
            double deltaY = y2-y1;
            if(deltaY > RdiConfigure.getConfig().autoSlowfallSpeed && RdiConfigure.getConfig().autoSlowfallSpeed>0){
                client.player.sendChatMessage("/slowfall 1");
            }*/
            if(y1<-80)
                client.player.sendChatMessage("/spawn");
           /* ticks=0;
        }*/
    }

}
