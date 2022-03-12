package calebzhou.rdi.craftsphere.module.cmdtip;

import calebzhou.rdi.craftsphere.util.DialogUtils;
import calebzhou.rdi.craftsphere.util.NetworkReceivableS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

public class CommandTips implements NetworkReceivableS2C {
    public static final ResourceLocation COMMAND_STATUS_NETWORK =new ResourceLocation(MODID,"command_status");

    @Override
    public void registerNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(COMMAND_STATUS_NETWORK,(client, handler, buf, responseSender) -> {
            try {
                String msg = buf.readUtf();
                String msgType = msg.split(":")[0];
                String msgDisp = msg.split(":")[1];
                DialogUtils.showInfoIngame(msgDisp, MessageType.valueOf(msgType));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
