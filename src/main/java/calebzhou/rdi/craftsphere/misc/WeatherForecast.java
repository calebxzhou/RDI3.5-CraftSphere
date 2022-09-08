package calebzhou.rdi.craftsphere.misc;

import calebzhou.rdi.craftsphere.util.ChatUtils;
import calebzhou.rdi.craftsphere.util.HttpUtils;
import calebzhou.rdi.craftsphere.util.RdiHttpRequest;

public class WeatherForecast implements Runnable {
    @Override
    public void run() {
        //先查国内的
        HttpUtils.sendRequestAsync(
                new RdiHttpRequest(RdiHttpRequest.Type.get,HttpUtils.RDI_URL+"public/china_ip2loca"),
                response->{

                },
                exception -> {
                    ChatUtils.addMessage("无法获取天气预报，这可能是一个bug，请咨询服主并上传您的客户端日志！"+exception.getMessage()+ exception.getCause());
                    exception.printStackTrace();
                }
                );
    }
}
