package calebzhou.rdi.craftsphere.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public interface WorldTickable {
    Minecraft client = Minecraft.getInstance();
    void tickWorld(ClientLevel world);
}
