package calebzhou.rdi.craftsphere.misc;

import calebzhou.rdi.craftsphere.util.DialogUtils;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UsernameChecker {

    public static void check(String userType,String username){
        /*if(!userType.equalsIgnoreCase("Mojang")){
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
                        OsDialogUt.showError("无法启动客户端。\n您的昵称 "+username+" 被其他玩家正在使用中。\n请更换昵称。");
                        System.exit(0);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });

        }*/
    }
}
