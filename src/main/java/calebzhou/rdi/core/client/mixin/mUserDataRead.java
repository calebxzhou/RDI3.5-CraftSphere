package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.RdiCore;
import calebzhou.rdi.core.client.constant.RdiFileConst;
import calebzhou.rdi.core.client.model.RdiUser;
import calebzhou.rdi.core.client.util.UuidUtils;
import net.minecraft.client.User;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Created by calebzhou on 2022-09-22,22:53.
 */
@Mixin(User.class)
public class mUserDataRead {
	@Inject(method = "<init>",at = @At("TAIL"))
	private void RDIReadDaata(String name, String uuid, String accessToken, Optional xuid, Optional clientId, User.Type type, CallbackInfo ci){
		if(uuid.startsWith("00000000")){
			uuid = UuidUtils.createUuidByName(name);
		}
		String userTypeName = type.getName();
		File passwordFile = RdiFileConst.getUserPasswordFile(uuid);

		String pwd = null;
		try {
			pwd = FileUtils.readFileToString(passwordFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			RdiCore.LOGGER.warn("此账户没有注册过 {}",e.getMessage());
		}
//mojang登录的uuid不带横线，要通过正则表达式转换成带横线的
		RdiUser rdiUser = new RdiUser(
				 UuidUtils.uuidAddDash(uuid),
				name,
				pwd,
				userTypeName
		);
		RdiUser.setCurrentUser(rdiUser);
	}
}
