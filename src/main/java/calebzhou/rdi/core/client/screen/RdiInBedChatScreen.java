package calebzhou.rdi.core.client.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class RdiInBedChatScreen extends RdiChatScreen{

    public RdiInBedChatScreen() {
        super("");
    }
    protected void init() {
        super.init();
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 40, 200, 20, Component.translatable("multiplayer.stopSleeping"), (button) -> {
            this.sendWakeUp();
        }));
    }

    public void onClose() {
        this.sendWakeUp();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.sendWakeUp();
        } else if (keyCode == 257 || keyCode == 335) {
            this.handleChatInput(this.input.getValue(), true);
            this.input.setValue("");
            this.minecraft.gui.getChat().resetChatScroll();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void sendWakeUp() {
        ClientPacketListener clientPacketListener = this.minecraft.player.connection;
        clientPacketListener.send((Packet)(new ServerboundPlayerCommandPacket(this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING)));
    }

    public void onPlayerWokeUp() {
        if (this.input.getValue().isEmpty()) {
            this.minecraft.setScreen((Screen)null);
        } else {
            this.minecraft.setScreen(new RdiChatScreen(this.input.getValue()));
        }

    }
}
