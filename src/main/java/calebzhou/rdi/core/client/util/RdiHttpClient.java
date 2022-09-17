package calebzhou.rdi.core.client.util;

import calebzhou.rdi.core.client.RdiCore;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

//封装HTTP请求
public class RdiHttpClient {
	//public static final String RDI_URL= RdiCore.debug?"https://localhost:26890/":"https://www.davisoft.cn:26890/";
    private static final int CONNECTION_TIME_OUT = 10;
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(32, 60, TimeUnit.SECONDS))
            .build();

    public static String sendRequest(Request request){
        Response response = null;
        try {
            response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                    return body.string();
            }else{
                throw new RdiHttpRequestException(response.code(),response.message());
            }
        } catch (IOException e) {
            throw new RdiHttpRequestException(e);
        }
    }

    public static void sendRequestAsync(Request request, BiConsumer<Call,Response> doOnSuccess, BiConsumer<Call,IOException> doOnFailure){
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException exception) {
                doOnFailure.accept(call,exception);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
               doOnSuccess.accept(call,response);
            }
        });
    }
    public static void sendRequestAsync(Request request, Consumer<String> handleResponseBody){
        sendRequestAsync(request,
                (call, response) -> {
                    try {
                        handleResponseBody.accept(response.body().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RdiHttpRequestException(response.code(),e);
                    }
                },
                (call, ex) -> {
                    throw new RdiHttpRequestException(ex);
                }
        );
    }

    public static class RequestBuilder {

        private final Request.Builder okhttpRequestBuilder = new Request.Builder();
        private final FormBody.Builder formBodyBuilder = new FormBody.Builder();
        private HttpUrl.Builder urlBuilder;
        private RequestType requestType;

        public RequestBuilder type(RequestType type) {
            this.requestType = type;
            return this;
        }

        public RequestBuilder url(String url) {
            urlBuilder=HttpUrl.parse(url).newBuilder();
            return this;
        }
        public RequestBuilder param(String key, String value){
            //对于GET请求，参数加在URL上，其他类型参数在body里
            if(this.requestType== RequestType.GET){
                urlBuilder.addQueryParameter(key, value);
            }else{
                formBodyBuilder.add(key, value);
            }

            return this;
        }
        public RequestBuilder header(String key, String value){
            okhttpRequestBuilder.header(key, value);
            return this;
        }

        public Request build(){
            return okhttpRequestBuilder
                    .url(urlBuilder.build())
                    .method(requestType.name(),
                            this.requestType != RequestType.GET ?
                                    formBodyBuilder.build() : null)
                    .build();

        }


    }
    public static class RdiHttpRequestException extends RuntimeException {
        public RdiHttpRequestException(Exception ioe){
            super(ioe);
        }
        public RdiHttpRequestException(String msg){
            super(msg);
        }
        public RdiHttpRequestException(int errCode,String msg){
            super(errCode+": "+msg);
        }
        public RdiHttpRequestException(int errCode,Exception msg){
            super(errCode+": "+msg.getMessage()+","+msg.getCause());
        }
    }

    public enum RequestType {
        GET,POST,DELETE,PUT;
    }
}
