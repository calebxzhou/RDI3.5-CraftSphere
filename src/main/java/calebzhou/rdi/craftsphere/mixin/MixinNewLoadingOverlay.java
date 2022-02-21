package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.screen.LoadingOverlay;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.resource.ResourceReloadLogger;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(MinecraftClient.class)
public abstract class MixinNewLoadingOverlay {

    @Shadow @Final private ReloadableResourceManager resourceManager;

    @Shadow protected abstract void handleResourceReloadException(Throwable exception);

    @Shadow @Final private static CompletableFuture<Unit> COMPLETED_UNIT_FUTURE;

    @Shadow @Final private ResourceReloadLogger resourceReloadLogger;

    @Shadow protected abstract void checkGameData();

    private List<ResourcePack> inputStream2;
    @Inject(method = "Lnet/minecraft/client/MinecraftClient;<init>(Lnet/minecraft/client/RunArgs;)V",
    at=@At(value = "INVOKE",
            target = "Lnet/minecraft/client/resource/ResourceReloadLogger;" +
                    "reload(Lnet/minecraft/client/resource/ResourceReloadLogger$ReloadReason;" +
                    "Ljava/util/List;)V"),locals = LocalCapture.CAPTURE_FAILHARD)
    private void getResourcePackInputstream(RunArgs args, CallbackInfo ci, File file, String string,int i, List inputStream2){
        this.inputStream2=inputStream2;
    }
    //不显示mc本身的加载画面
    @Redirect(method = "Lnet/minecraft/client/MinecraftClient;<init>(Lnet/minecraft/client/RunArgs;)V",
    at=@At(value = "INVOKE",target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V"))
    private void notDisplayOverlay(MinecraftClient instance, Overlay overlay){
        instance.setOverlay(new LoadingOverlay(instance, resourceManager.reload(
                (Executor) Util.getMainWorkerExecutor(), (Executor)this,
                COMPLETED_UNIT_FUTURE, inputStream2),
                throwable -> Util.ifPresentOrElse(throwable, this::handleResourceReloadException,
                        () -> {
            if (SharedConstants.isDevelopment) {
                checkGameData();
            }
            resourceReloadLogger.finish();
        })));
    }
}
