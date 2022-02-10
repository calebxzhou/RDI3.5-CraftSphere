package calebzhou.rdi.craftsphere.util;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.mixin.AccessSystemDetails;
import calebzhou.rdi.craftsphere.model.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.SystemDetails;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class Updater {
    public static void check(){
        ApiResponse<String> response = HttpUtils.sendRequest("GET", HttpUtils.UPDATE_ADDR);
        String data = response.getMessage();
        if(StringUtils.isEmpty(data)){
            DialogUtils.showError("无法连接至RDI更新服务器，可能是您的网络问题\n(也有可能是dav的土豆服务器炸了 逃\n按“确定”键继续启动客户端。");
            return;
        }
        if(!data.equals(String.valueOf(ExampleMod.VERSION))){
            if (DialogUtils.showYesNo("检测到了客户端有更新，请您去群文件下载新版。")) {
                System.exit(0);
            }
        }

    }
}
