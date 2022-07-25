package calebzhou.rdi.craftsphere;

import calebzhou.rdi.craftsphere.misc.KeyBinds;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import calebzhou.rdi.craftsphere.util.NetworkUtils;
import calebzhou.rdi.craftsphere.util.PlayerUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

//事件注册
public class EventRegister {
    private ClientLevel world;
    public EventRegister(){
        //初始化按键事件
        KeyBinds.init();
        //客户端世界tick事件
        ClientTickEvents.END_WORLD_TICK.register(world->{
            this.world=world;
            //虚空防止掉落
            preventDroppingVoid();
            //隔空跳跃
            quickLeap();
            //检测挂机
            afkDetect();
            //跳舞树
            danceTree();
            //检查按键事件
            KeyBinds.handleKeyActions(world);
        });
        //接收服务器的空岛信息
        ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.ISLAND_INFO,(client, handler, buf, responseSender) -> {
            int i = buf.readInt();
            //接收到了0就提示创建岛屿
            if(i == 0){
                if (DialogUtils.showYesNo("没有找到您的岛屿。\n是：立刻创建自己的岛屿\n否：加入朋友的岛屿")) {
                    client.player.chat("/create");
                }
            }
        });
    }
    private int treeScore = 0;
    public void danceTree(){
        if(world.dimension() != ClientLevel.OVERWORLD)
            return;
        LocalPlayer player = Minecraft.getInstance().player;
        BlockPos onPos = player.getOnPos();
        Optional<BlockPos> nearestSapling = BlockPos.betweenClosedStream(
                onPos.offset(-5, -2, -5),
                        onPos.offset(5, 2, 5))
                .filter(blockPos -> world.getBlockState(blockPos).getBlock() instanceof SaplingBlock)
                .findFirst()
                .map(BlockPos::immutable);

        if(nearestSapling.isEmpty())
            return;
        int scoreToAdd = 0;
        //500分长一棵树
        final int requireScore = 500;
        //跑步 1tick+3
        if(player.isSprinting()) {
            scoreToAdd=3;
        }
        //跳跃+2
        else if(player.isFallFlying()) {
            scoreToAdd=2;
        }
        //下蹲+1
        else if(player.isShiftKeyDown()) {
            scoreToAdd=1;
        } else
            //只走路，不加分
            return;
        if(scoreToAdd>0){
            treeScore+=scoreToAdd;
            player.displayClientMessage(Component.literal("树苗生长进度"+ treeScore*5 +"/"+requireScore*5),true);
            final int finalScoreToAdd = scoreToAdd;
            BoneMealItem.addGrowthParticles(world,nearestSapling.get(), finalScoreToAdd *5);

        }
        if(treeScore>requireScore){
            NetworkUtils.sendPacketToServer(NetworkPackets.DANCE_TREE_GROW,nearestSapling.get().asLong());
            treeScore=0;
        }
    }
    public void preventDroppingVoid(){
        Minecraft client = Minecraft.getInstance();
        if(client.player != null && client.player.getY()<-80){
            client.player.chat("/spawn");
        }
    }
    public void quickLeap(){
        Minecraft client = Minecraft.getInstance();
        if (KeyBinds.LEAP_KEY.consumeClick()){
            BlockPos lookingAtBlock = PlayerUtils.getPlayerLookingAtBlock(client.player,false);
            if(lookingAtBlock==null){
                return;
            }
            if(client.player.getLevel().getBlockState(lookingAtBlock).getBlock() == Blocks.AIR){
                return;
            }
            NetworkUtils.sendPacketToServer(NetworkPackets.LEAP,lookingAtBlock.asLong());
        }
    }

    private int totalAfkTicks =0;
    public void afkDetect(){
        Minecraft client = Minecraft.getInstance();
        long handle = client.getWindow().getWindow();
        ++totalAfkTicks;
        //触碰键盘，告诉服务器停止挂机
        GLFW.glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
            totalAfkTicks =0;
            NetworkUtils.sendPacketToServer(NetworkPackets.AFK_DETECT,client.player.getStringUUID()+",noafk,"+totalAfkTicks);
        });
        //如果达到了挂机时间（5分钟），告诉服务器已经挂机
        int ticksOnAfk = 20 * 60 * 5;
        //三秒发送一次挂机时间
        int sendTicks = 20 * 3;
        if(totalAfkTicks >= ticksOnAfk){
            //三秒发送一次
            if(totalAfkTicks % sendTicks == 0)
                NetworkUtils.sendPacketToServer(NetworkPackets.AFK_DETECT,client.player.getStringUUID()+",afk,"+totalAfkTicks);
        }
    }
}
