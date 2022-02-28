package calebzhou.rdi.craftsphere.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public interface WorldTickable {
    MinecraftClient client = MinecraftClient.getInstance();
    void tickWorld(ClientWorld world);
}
