package calebzhou.rdi.craftsphere.mixin.emoji;

import calebzhou.rdi.craftsphere.emojiful.EmojiClientProxy;
import calebzhou.rdi.craftsphere.emojiful.gui.EmojiSelectionGui;
import calebzhou.rdi.craftsphere.emojiful.gui.EmojiSuggestionHelper;
import calebzhou.rdi.craftsphere.emojiful.render.EmojiFontRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class mChatScreen extends Screen {
    @Inject(method = "init",at=@At("HEAD"))
    private void rdi_emoji_editbox(CallbackInfo ci){
        this.font = EmojiFontRenderer.getInstance();
    }
    @Inject(method = "init",at=@At("TAIL"))
    private void rdi_emoji_afterInit(CallbackInfo ci){
        EmojiClientProxy.emojiSuggestionHelper = new EmojiSuggestionHelper((ChatScreen) (Object)this);
        EmojiClientProxy.emojiSelectionGui = new EmojiSelectionGui((ChatScreen) (Object)this);
        Minecraft.getInstance().setOverlay(EmojiClientProxy.emojiSelectionGui);
    }

    @Inject(method = "render",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/screens/Screen;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"))
    private void rdi_emoji_render(PoseStack poseStack, int i, int j, float f, CallbackInfo ci){
        EmojiClientProxy.emojiSelectionGui.render(poseStack,i,j,f);
    }
/*    @Inject(method = "mouseClicked",at = @At("HEAD"))
    private void rdi_emoji_mouseClicked(double d, double e, CallbackInfoReturnable<Boolean> cir){
        EmojiClientProxy.emojiSelectionGui.mouseClicked(d,e);
    }
    @Inject(method = "mouseScrolled",at = @At("HEAD"))
    private void rdi_emoji_mouseScrolled(double d, double e, double f, CallbackInfoReturnable<Boolean> cir){
        EmojiClientProxy.emojiSelectionGui.mouseScrolled(d,e,f);
    }
    @Inject(method = "keyPressed",at = @At("HEAD"))
    private void rdi_emoji_keyPressed(int i, int j, int k, CallbackInfoReturnable<Boolean> cir){
        EmojiClientProxy.emojiSelectionGui.keyPressed(i,j,k);
    }*/



    private mChatScreen(Component component) {
        super(component);
    }
}
