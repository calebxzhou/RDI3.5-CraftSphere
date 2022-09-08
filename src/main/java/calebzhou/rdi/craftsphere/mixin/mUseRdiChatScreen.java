package calebzhou.rdi.craftsphere.mixin;


import calebzhou.rdi.craftsphere.screen.RdiChatScreen;
import calebzhou.rdi.craftsphere.screen.RdiInBedChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundChatPreviewPacket;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class mUseRdiChatScreen {
    @Shadow public abstract void setScreen(@Nullable Screen guiScreen);

    private String defaultText;

    @Inject(method = "openChatScreen",at=@At("HEAD"))
    private void getDefaultText(String defaultText, CallbackInfo ci){
        this.defaultText = defaultText;
    }

    @Redirect(method = "openChatScreen",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void r_setScreen(Minecraft instance, Screen guiScreen){
        setScreen(new RdiChatScreen(defaultText));
    }

    @Redirect(method = "tick",at = @At(value = "INVOKE",ordinal = 1,target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void r_setScreen2(Minecraft instance, Screen guiScreen){
        setScreen(new RdiInBedChatScreen());
    }/*
    @Inject(method = "tick",at=@At(value = "FIELD", opcode = Opcodes.GETFIELD,target = "net/minecraft/client/gui/screens/Screen;var2:Lnet/minecraft/client/gui/screens/Screen"))
    private void r_newChatInstanceof(){

    }*/
}
@Mixin(ClientPacketListener.class)
class mUseRdiChatScreen2{
    @Inject(method = "handleChatPreview",at=@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/components/ChatComponent;getFocusedChat()Lnet/minecraft/client/gui/screens/ChatScreen;"), cancellable = true)
    private void injf(ClientboundChatPreviewPacket packet, CallbackInfo ci){
        Screen var2 = Minecraft.getInstance().screen;
        if (var2 instanceof RdiChatScreen chatScreen) {
            if(var2!=null){
                chatScreen.getChatPreview().handleResponse(packet.queryId(), packet.preview());
            }
        }
        ci.cancel();
    }
}
@Mixin(ChatComponent.class)
class mUseRdiChatScreen3{
    @Overwrite
    private boolean isChatFocused() {
        Screen var2 = Minecraft.getInstance().screen;
        if (var2 instanceof RdiChatScreen chatScreen) {
            return true;
        } else {
            return false;
        }
    }

}
