package calebzhou.rdi.craftsphere;

import calebzhou.rdi.craftsphere.misc.HwSpec;
import calebzhou.rdi.craftsphere.misc.KeyBinds;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import calebzhou.rdi.craftsphere.util.NetworkUtils;
import calebzhou.rdi.craftsphere.util.PlayerUtils;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import com.google.gson.Gson;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.level.block.SaplingBlock;

import java.awt.*;
import java.util.List;
import java.util.Optional;

//事件注册
public class EventRegister {
    private int danceTreeCurrentScore = 0;

    public EventRegister(){
        //初始化按键事件
        KeyBinds.init();
        //进入服务器发送硬件数据
        ClientPlayConnectionEvents.JOIN.register(this::onJoinServer);
        //客户端世界tick事件
        ClientTickEvents.END_WORLD_TICK.register(this::onClientWorldTick);
        ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.DIALOG_INFO,this::onReceiveDialogInfo);
        ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.POPUP,this::onReceivePopup);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnectServer);
    }

    private void onClientWorldTick(ClientLevel level) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player ==null) return;

        afkDetect(player);
        danceTree(player,level);
        animalSex(player);
        KeyBinds.handleKeyActions(level);
    }

    private int sexTickAmount =0;
    //繁殖成功所需要的tick数
    private static final int sexTickAmountNeedToAdult =200;
    private static final List<EntityType<?>> sexableEntityType=new ObjectArrayList<>();
    static{
        sexableEntityType.add(EntityType.PIG);
        sexableEntityType.add(EntityType.COW);
        sexableEntityType.add(EntityType.SHEEP);
        sexableEntityType.add(EntityType.CHICKEN);
        sexableEntityType.add(EntityType.VILLAGER);
    }
    //动物快速繁殖
    private void animalSex(LocalPlayer player) {
        //玩家下蹲
        if(player.isCrouching()) {
            //获取所面对的生物
            Entity lookingEntity = PlayerUtils.getPlayerLookingEntity();
            if(lookingEntity == null) return;
            EntityType<?> entityType = lookingEntity.getType();
            if(!sexableEntityType.contains(entityType)) return;

            PlayerUtils.displayClientMessage(player,String.format("动物繁殖进度 %d/%d",++sexTickAmount,sexTickAmountNeedToAdult));

            if(sexTickAmount>=sexTickAmountNeedToAdult){
                String entityStringUUID = lookingEntity.getStringUUID();
                NetworkUtils.sendPacketToServer(NetworkPackets.ANIMAL_SEX,entityStringUUID);
                sexTickAmount=0;
            }

        }


    }

    //建立服务器连接
    private void onJoinServer(ClientPacketListener listener, PacketSender sender, Minecraft minecraft) {
        ThreadPool.newThread(()->{
            String info = new Gson().newBuilder().setPrettyPrinting().create().toJson(HwSpec.getSystemSpec());
            NetworkUtils.sendPacketToServer(NetworkPackets.HW_SPEC,info);
        });
    }


    //断开服务器连接
    private void onDisconnectServer(ClientPacketListener listener, Minecraft minecraft) {
        //清零挂机时间和跳舞树积分
        danceTreeCurrentScore =0;
        totalAfkTicks=0;
    }

    //接收服务器的弹框信息
    private void onReceivePopup(Minecraft minecraft, ClientPacketListener listener, FriendlyByteBuf buf, PacketSender sender) {
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

    }

    //接收服务器的对话框信息
    private void onReceiveDialogInfo(Minecraft minecraft, ClientPacketListener listener, FriendlyByteBuf buf, PacketSender sender) {
         String info = buf.readUtf();
         String[] split = info.split("@");
         String type= split[0];
         String title= split[1];
         String content= split[2];
         DialogUtils.showMessageBox(type,title,content);
    }

    //跳舞树

    public void danceTree(LocalPlayer player,ClientLevel world){

        if(world.dimension() != ClientLevel.OVERWORLD)
            return;
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
            danceTreeCurrentScore +=scoreToAdd;
            player.displayClientMessage(Component.literal("树苗生长进度"+ danceTreeCurrentScore *5 +"/"+requireScore*5),true);
            final int finalScoreToAdd = scoreToAdd;
            BoneMealItem.addGrowthParticles(world,nearestSapling.get(), finalScoreToAdd *5);

        }
        if(danceTreeCurrentScore >requireScore){
            NetworkUtils.sendPacketToServer(NetworkPackets.DANCE_TREE_GROW,nearestSapling.get().toShortString().replace(" ",""));
            danceTreeCurrentScore =0;
        }
    }

    //多长tick检测一次是否挂机 3秒
    private final int checkAfkTickTime = 20*3;
    //两次检测之间的tick
    private int checkAfkInterval = 0;
    //总共挂机了多长时间
    private int totalAfkTicks =0;
    //如果达到了挂机时间（5分钟），告诉服务器已经挂机
    final int ticksOnAfk = 20 * 5 * 60;
    //检测挂机
    public void afkDetect(LocalPlayer player){

        BlockPos pos1 = player.getOnPos();
        //如果没达到检测挂机的时间 就先不检测
        if(checkAfkInterval < checkAfkTickTime){
            ++checkAfkInterval;
            return;
        }

        //达到检测挂机时间---
        BlockPos pos2 = player.getOnPos();
        //如果3秒之内没有动 就累计挂机tick
        if(pos2.compareTo(pos1)==0){
            totalAfkTicks += checkAfkInterval;
            checkAfkInterval=0;
        }

        //累计挂机tick达到了规定时间 开始向服务器发送挂机时长
        if(totalAfkTicks >= ticksOnAfk){
            NetworkUtils.sendPacketToServer(NetworkPackets.AFK_DETECT,totalAfkTicks);
            //如果玩家动了 就清除挂机时间
            if(pos2.compareTo(pos1)>0){
                totalAfkTicks=0;
                NetworkUtils.sendPacketToServer(NetworkPackets.AFK_DETECT,0);
            }
        }


    }

}
/*public void preventDroppingVoid(){
        Minecraft client = Minecraft.getInstance();
        if(client.player != null && client.player.getY()<-80){
            client.player.chat("/spawn");
        }
    }*/
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
