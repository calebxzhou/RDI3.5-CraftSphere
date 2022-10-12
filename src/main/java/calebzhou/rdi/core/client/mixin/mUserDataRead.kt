package calebzhou.rdi.core.client.mixin

import calebzhou.rdi.core.client.constant.RdiFileConst
import calebzhou.rdi.core.client.logger
import calebzhou.rdi.core.client.model.RdiUser
import calebzhou.rdi.core.client.util.UuidUtils
import net.minecraft.client.User
import org.apache.commons.io.FileUtils
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Created by calebzhou on 2022-09-22,22:53.
 */
@Mixin(User::class)
class mUserDataRead {
    @Inject(method = ["<init>"], at = [At("TAIL")])
    private fun RDIReadDaata(
        name: String,
        uuid: String,
        accessToken: String,
        xuid: Optional<*>,
        clientId: Optional<*>,
        type: User.Type,
        ci: CallbackInfo
    ) {
        RdiUser.currentRdiUser= RdiUser.loadCurrentRdiUser(uuid, name, type)
    }
}
