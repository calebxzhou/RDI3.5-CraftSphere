package calebzhou.rdi.craftsphere;

import calebzhou.rdi.craftsphere.misc.HwSpec;
import calebzhou.rdi.craftsphere.misc.KeyBinds;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import calebzhou.rdi.craftsphere.util.NetworkUtils;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import com.google.gson.Gson;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.level.block.SaplingBlock;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Optional;

//事件注册
public class EventRegister {
    private ClientLevel world;
    public EventRegister(){
        //初始化按键事件
        KeyBinds.init();
        //进入服务器发送硬件数据
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ThreadPool.newThread(()->{
                String info = new Gson().newBuilder().setPrettyPrinting().create().toJson(HwSpec.getSystemSpec());
                NetworkUtils.sendPacketToServer(NetworkPackets.HW_SPEC,info);
            });
        });
        //客户端世界tick事件

        ClientTickEvents.END_WORLD_TICK.register(world->{
            this.world=world;
            if(Minecraft.getInstance().player!=null){
                //虚空防止掉落
                //preventDroppingVoid();
                //隔空跳跃
                //quickLeap();
                //检测挂机
                afkDetect();
                //跳舞树
                danceTree();
                //检查按键事件
                KeyBinds.handleKeyActions(world);
            }



        });
        //接收服务器的空岛信息
        /*ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.ISLAND_INFO,(client, handler, buf, responseSender) -> {
            int i = buf.readInt();
            //接收到了0就提示创建岛屿
            if(i == 0){
                if (DialogUtils.showYesNo("没有找到您的岛屿。\n是：立刻创建自己的岛屿\n否：加入朋友的岛屿")) {
                    client.player.chat("/create");
                }
            }
        });*/
        //接收服务器的对话框信息
        ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.DIALOG_INFO,(client, handler, buf, responseSender) -> {
            String info = buf.readUtf();
            String[] split = info.split("@");
            String type= split[0];
            String title= split[1];
            String content= split[2];
            DialogUtils.showMessageBox(type,title,content);
        });
        //接收服务器的弹框信息
        ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.POPUP,(client, handler, buf, responseSender) -> {
            String info = buf.readUtf();
            String[] split = info.split("@");
            String type= split[0];
            String title= split[1];
            String content= split[2];
            TrayIcon.MessageType realType;
            switch (type){
                case "info"->realType= TrayIcon.MessageType.INFO;
                case "warning"->realType= TrayIcon.MessageType.WARNING;
                case "error"->realType= TrayIcon.MessageType.ERROR;
                default -> realType= TrayIcon.MessageType.NONE;
            }
            DialogUtils.showPopup(realType,title,content);
        });
        //断开客户端清零挂机时间和跳舞树积分
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            treeScore=0;
            totalAfkTicks=0;
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
        //跳跃+1
        else if(!player.isOnGround()) {
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
   /* public void quickLeap(){
         return;
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
    }*/

    private int totalAfkTicks =0;
    public void afkDetect(){
        Minecraft client = Minecraft.getInstance();
        if(client.player==null) return;
        BlockPos onPos1 = client.player.getOnPos();
        ++totalAfkTicks;

        //如果达到了挂机时间（5分钟），告诉服务器已经挂机
        int ticksOnAfk = 20 *10/* 5 * 60*/;
        //三秒发送一次挂机时间
        int sendTicks = 20 * 3;
        if(totalAfkTicks >= ticksOnAfk){
            //三秒发送一次
            if(totalAfkTicks % sendTicks == 0){
                NetworkUtils.sendPacketToServer(NetworkPackets.AFK_DETECT,totalAfkTicks);
                BlockPos onPos2 = client.player.getOnPos();
                //触碰键盘，告诉服务器停止挂机
                if(onPos2.compareTo(onPos1) > 0){
                    totalAfkTicks =0;
                    NetworkUtils.sendPacketToServer(NetworkPackets.AFK_DETECT,0);
                }

            }
        }
    }
}
