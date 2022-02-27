package calebzhou.rdi.craftsphere.util;

import calebzhou.rdi.craftsphere.model.MessageType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

public class NetworkUtils {
    public NetworkUtils() {
        //注册网络监听器
        registerNetworkReceiver();
    }
    public static void sendPacketC2S(Identifier packType, String content){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(content);
        ClientPlayNetworking.send(packType,buf);
    }
    public static final Identifier COMMAND_STATUS =new Identifier(MODID,"command_status");

    public static final Identifier ISLAND_INFO =new Identifier(MODID,"island_info");
    private void registerNetworkReceiver(){
        //指令执行结果
        ClientPlayNetworking.registerGlobalReceiver(COMMAND_STATUS,(client, handler, buf, responseSender) -> {
            try {
                String msg = buf.readString();
                String msgType = msg.split(":")[0];
                String msgDisp = msg.split(":")[1];
                DialogUtils.showInfoIngame(msgDisp, MessageType.valueOf(msgType));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //空岛信息
        ClientPlayNetworking.registerGlobalReceiver(ISLAND_INFO,(client, handler, buf, responseSender) -> {
            try {
                String msg = buf.readString();
                switch (msg){
                    case "404"->{
                        if (DialogUtils.showYesNo("您还没有岛屿呢~\n要立刻创建自己的岛屿吗？")) {
                            client.player.sendChatMessage("/create");
                        }
                    }
                    case "confirm-delete"->{
                        if (DialogUtils.showYesNo("真的要删除岛屿吗？\n岛屿上的所有建筑都会被清空！\n本操作不可恢复！！")) {
                            client.player.sendChatMessage("/confirm-delete");
                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
