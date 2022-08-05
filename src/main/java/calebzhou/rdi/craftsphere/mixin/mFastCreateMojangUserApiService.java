package calebzhou.rdi.craftsphere.mixin;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Minecraft.class)
public class mFastCreateMojangUserApiService {
    @Overwrite
    private UserApiService createUserApiService(YggdrasilAuthenticationService yggdrasilAuthenticationService, GameConfig gameConfig) {
            return UserApiService.OFFLINE;
    }
}
