package calebzhou.rdi.core.client.util;

import calebzhou.rdi.core.client.RdiCore;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HttpUtils {
    public static final String RDI_URL= RdiCore.debug?"https://localhost:26890/":"https://www.davisoft.cn:26890/";
    private static OkHttpClient client;
    private static final int CONNECTION_TIME_OUT = 10;

    static {
        try {
            client = new OkHttpClient.Builder()
                            .connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                            .readTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                            .writeTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                            .connectionPool(new ConnectionPool(32, 60, TimeUnit.SECONDS))
                            .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendRequestAsync(RdiHttpRequest request, Consumer<String> doOnSuccess, Consumer<Exception> doOnFailure){
        Request.Builder okreq = new Request.Builder();
        okreq.url(request.fullUrl);
        switch (request.type){
            case post -> okreq.post(request.getParamBody());
            case put -> okreq.put(request.getParamBody());
            case delete -> okreq.delete(request.getParamBody());
            default -> okreq.get();
        }
        client.newCall(okreq.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                doOnFailure.accept(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (ResponseBody body = response.body()){
                    if (!response.isSuccessful()) {
                        System.err.println(response);
                        return;
                    }
                    doOnSuccess.accept(body.string());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

}
