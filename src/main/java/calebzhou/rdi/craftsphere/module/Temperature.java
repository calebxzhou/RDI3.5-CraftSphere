package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.util.WorldTickable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class Temperature implements WorldTickable {
    public Temperature() {
        ClientTickEvents.END_WORLD_TICK.register(this::tickWorld);
    }

    private int ticks=0;
    @Override
    public void tickWorld(ClientWorld world) {
        if(ticks<20){
            ++ticks;
            return;
        }

        calculatePlayerTemperature(MinecraftClient.getInstance().player);
        ticks=0;
    }
    private void calculatePlayerTemperature(ClientPlayerEntity player){
        //群系温度
        float biomeTemperature = player.world.getBiome(player.getBlockPos()).value().getTemperature();
        //冷
        if(biomeTemperature <= 0.4f){

        }else if(biomeTemperature>=1.2f){
            //热
        }
    }
    //玩家穿的各种装备，也计算体温
    public static int wearsArmorPartsValue(PlayerEntity playerEntity) {
        ItemStack headStack = playerEntity.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chestStack = playerEntity.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack legStack = playerEntity.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack feetStack = playerEntity.getEquippedStack(EquipmentSlot.FEET);

        int returnValue = 0;
        if (!headStack.isEmpty()) {
            returnValue++;
        }
        if (!chestStack.isEmpty()) {
            returnValue++;
        }
        if (!legStack.isEmpty()) {
            returnValue++;
        }
        if (!feetStack.isEmpty()) {
            returnValue++;
        }
        return returnValue;

    }



}
