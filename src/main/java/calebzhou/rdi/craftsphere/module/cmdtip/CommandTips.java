package calebzhou.rdi.craftsphere.module.cmdtip;

import calebzhou.rdi.craftsphere.util.DialogUtils;
import calebzhou.rdi.craftsphere.util.NetworkReceivableS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

public class CommandTips implements NetworkReceivableS2C {
    public static final Identifier COMMAND_STATUS_NETWORK =new Identifier(MODID,"command_status");

    @Override
    public void registerNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(COMMAND_STATUS_NETWORK,(client, handler, buf, responseSender) -> {
            try {
                String msg = buf.readString();
                String msgType = msg.split(":")[0];
                String msgDisp = msg.split(":")[1];
                DialogUtils.showInfoIngame(msgDisp, MessageType.valueOf(msgType));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
