package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.util.DialogUtils;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.client.main.Main;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

//如果是盗版登录就检查这个人的昵称，有没有被正版玩家占用，如果占用了就禁止启动游戏
@Mixin(Main.class)
public class MixinNameChk4Offline {
    @Inject(method = "main([Ljava/lang/String;)V",
    at = @At(value = "INVOKE",target = "Lnet/minecraft/client/main/Main;parseArgument(Ljoptsimple/OptionSet;Ljoptsimple/OptionSpec;)Ljava/lang/Object;",ordinal = 0),
    locals = LocalCapture.CAPTURE_FAILSOFT,
    remap = false)
    private static void checkName(String[] args, CallbackInfo ci, OptionParser optionParser, OptionSpec optionSpec, OptionSpec optionSpec2, OptionSpec optionSpec3, OptionSpec optionSpec4, OptionSpec optionSpec5, OptionSpec optionSpec6, OptionSpec optionSpec7, OptionSpec optionSpec8, OptionSpec optionSpec9, OptionSpec optionSpec10, OptionSpec optionSpec11, OptionSpec optionSpec12, OptionSpec optionSpec13, OptionSpec optionSpec14, OptionSpec optionSpec15, OptionSpec optionSpec16, OptionSpec optionSpec17, OptionSpec optionSpec18, OptionSpec optionSpec19, OptionSpec optionSpec20, OptionSpec optionSpec21, OptionSpec optionSpec22, OptionSpec optionSpec23, OptionSpec optionSpec24, OptionSpec optionSpec25, OptionSpec optionSpec26, OptionSet optionSet){
        String userType = (String) optionSpec24.value(optionSet);
        String username = (String) optionSpec11.value(optionSet);
        UsernameChecker.check(userType,username);
    }

}
class UsernameChecker {
    public static void check(String userType,String username){
        if(!userType.equalsIgnoreCase("Mojang")){
            //离线模式
            ThreadPool.newThread(()->{
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/"+username))
                        .setHeader("User-Agent", "RDI-MC-Client")
                        .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                        .build();
                try {
                    HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if(!StringUtils.isEmpty(send.body())){
                        DialogUtils.showError("无法启动客户端。\n您的昵称 "+username+" 被其他玩家正在使用中。\n请更换昵称。");
                        System.exit(0);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });

        }
    }
}
