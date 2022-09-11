package calebzhou.rdi.core.client.util;

import okhttp3.FormBody;

//封装HTTP请求
public class RdiHttpRequest {


    public RdiHttpRequest(Type type, String fullUrl, String... params) {
        this.type = type;
        this.fullUrl = fullUrl;
        this.params = params;
    }

    //http请求类型
    public Type type;
    //请求url
    public String fullUrl;
    public String[] params;

    public FormBody getParamBody(){
        if(params==null)
            return null;
        FormBody.Builder builder = new FormBody.Builder();
        for (String param : params) {
            String[] split = param.split("=");
            builder.add(split[0],split[1]);
        }
        return builder.build();
    }
    public enum Type{
        get,post,delete,put;
        public String getHttpType(){
            return this.toString().toUpperCase();
        }
    }
}
