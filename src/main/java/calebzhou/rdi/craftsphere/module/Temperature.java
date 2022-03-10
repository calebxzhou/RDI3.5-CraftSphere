package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.util.WorldTickable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;

public class Temperature implements WorldTickable {
    public static int temp;
    public static final int MAX_TEMP = 4500;
    public static final int MIN_TEMP = 3200;
    public static final int STD_TEMP = 3650;
    public static final float COLD_TEMP = 0.4f;
    public static final float HEAT_TEMP = 1.2f;
    public Temperature() {
        ClientTickEvents.END_WORLD_TICK.register(this::tickWorld);
        temp = STD_TEMP;
    }

    private int ticks=0;
    @Override
    public void tickWorld(ClientWorld world) {
        //一秒计算一次体温
        if(ticks<20){
            ++ticks;
            return;
        }

        calculatePlayerTemperature(MinecraftClient.getInstance().player);
        ticks=0;
    }
    private void calculatePlayerTemperature(ClientPlayerEntity player){
        //限制温度在32到45之间
        temp = MathHelper.clamp(temp,MIN_TEMP,MAX_TEMP);

        //护甲保暖值，0都不穿，头盔1，胸甲5，裤子3，鞋2
        int armorVal = wearsArmorPartsValue(player);
        //生物群系温度
        float bioTemp = player.world.getBiome(player.getBlockPos()).getTemperature();
        //生物群系的真实温度，摄氏度，https://www.reddit.com/r/Minecraft/comments/3eh7yu/the_rl_temperature_of_minecraft_biomes_revealed/
        double realTemp = (13.6484805403*bioTemp)+7.0879687222;
        //头部位置的光照等级 0~15
        int headLightLevel = player.world.getLightLevel(LightType.SKY, player.getCameraBlockPos());
        //是否在水中
        boolean isInWater = player.isSwimming() || player.getBlockStateAtPos().getBlock() == Blocks.WATER;
        //是否跑步
        boolean isRunning = player.isSprinting();
        //所处高度
        double height = player.getY();
        //高度对应环境温度


        //player.sendMessage(new LiteralText(temp+""),true);
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
            returnValue+=5;
        }
        if (!legStack.isEmpty()) {
            returnValue+=3;
        }
        if (!feetStack.isEmpty()) {
            returnValue+=2;
        }
        return returnValue;

    }



}
