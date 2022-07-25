package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//去掉说话的尖括号
@Mixin(TranslatableContents.class)
public class MixinNoChatBrackets {
    @Mutable
    @Shadow @Final private String key;

    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("TAIL"))
    public void modify(String key, CallbackInfo ci) {
        fixKey();
    }

    @Inject(method = "<init>(Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("TAIL"))
    public void modifyWithArgs(String key, Object[] args, CallbackInfo ci) {
        fixKey();
    }
    public void fixKey() {
        switch(key) {
            case "chat.type.text":
            case "chat.type.emote":
            case "chat.type.announcement":
            case "chat.type.admin":
            case "chat.type.team.text":
            case "chat.type.team.sent":
                key = "rdi." + key;
                break;
        }

    }
}
