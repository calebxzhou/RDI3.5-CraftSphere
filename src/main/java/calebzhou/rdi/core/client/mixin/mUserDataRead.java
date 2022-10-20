package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.RdiLoader;
import calebzhou.rdi.core.client.model.RdiUser;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Created by calebzhou on 2022-09-22,22:53.
 */
@Mixin(User.class)
class mUserDataRead {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void RDIReadDaata(String name, String uuid, String string3, Optional optional, Optional optional2, User.Type type, CallbackInfo ci) {
		RdiLoader.INSTANCE.loadCurrentRdiUser(uuid, name, type);
    }
}
