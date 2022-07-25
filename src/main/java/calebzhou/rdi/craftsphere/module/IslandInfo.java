package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.util.DialogUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

public class IslandInfo implements NetworkReceivableS2C {
    public static final ResourceLocation ISLAND_INFO_NETWORK =new ResourceLocation(MODID,"island_info");
    public void registerNetworking(){
        //指令执行结果
        /**/
        //空岛信息
        ClientPlayNetworking.registerGlobalReceiver(ISLAND_INFO_NETWORK,(client, handler, buf, responseSender) -> {
            try {
                String msg = buf.readUtf();
                switch (msg){
                    case "404"->{
                        if (DialogUtils.showYesNo("您还没有岛屿呢~\n要立刻创建自己的岛屿吗？")) {
                            client.player.chat("/create");
                        }
                    }
                    case "confirm-delete"->{
                        if (DialogUtils.showYesNo("真的要删除岛屿吗？\n岛屿上的所有建筑都会被清空！\n本操作不可恢复！！")) {
                            client.player.chat("/confirm-delete");
                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
