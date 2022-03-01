package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.util.IdentifierUtils;
import calebzhou.rdi.craftsphere.util.NetworkReceivableS2C;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class TeleportRequest implements NetworkReceivableS2C {
    @Override
    public void registerNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(IdentifierUtils.byClass(this.getClass()),(client, handler, buf, responseSender) -> {

        });
    }
}
