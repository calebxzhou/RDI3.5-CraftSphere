package calebzhou.rdi.craftsphere.mixin;

import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfileKeyPair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public class mFastCreateMojangService {
    @Overwrite
    private UserApiService createUserApiService(YggdrasilAuthenticationService yggdrasilAuthenticationService, GameConfig gameConfig) {
            return UserApiService.OFFLINE;
    }
}
@Mixin(ProfileKeyPairManager.class)
abstract
class mFastCreateMojangService2 {
    @Shadow protected abstract void writeProfileKeyPair(@Nullable ProfileKeyPair profileKeyPair);

    @Shadow protected abstract Optional<ProfileKeyPair> readProfileKeyPair();

    @Overwrite
    private CompletableFuture<Optional<ProfileKeyPair>> readOrFetchProfileKeyPair(UserApiService userApiService) {
        return CompletableFuture.supplyAsync(() -> {
            writeProfileKeyPair(null);
            return readProfileKeyPair();

        }, Util.backgroundExecutor());

    }
}

