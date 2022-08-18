package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.FileConst;
import calebzhou.rdi.craftsphere.misc.MusicPlayer;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(ClientPacketListener.class)
public class mPlayMusic {
    @Inject(method="handleLogin",at=@At("TAIL"))
    private void play(ClientboundLoginPacket clientboundLoginPacket, CallbackInfo ci){
        MusicPlayer.needToPlayEnterGameSound=true;
        //MusicPlayer.playOgg(new File(FileConst.RDI_SOUND_FOLDER,"enter_game.ogg"));
    }
}
@Mixin(ReceivingLevelScreen.class)
class mPlayMusic2{
   @Inject(method = "tick",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/screens/ReceivingLevelScreen;onClose()V"))
   private void plauy(CallbackInfo ci){
       if(MusicPlayer.needToPlayEnterGameSound){
           MusicPlayer.playOgg(new File(FileConst.RDI_SOUND_FOLDER,"enter_game.ogg"));
           MusicPlayer.needToPlayEnterGameSound=false;
       }

   }
}
