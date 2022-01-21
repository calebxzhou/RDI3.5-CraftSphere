package calebzhou.rdi.craftsphere.util;

import calebzhou.rdi.craftsphere.model.ApiResponse;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpUtils {
    private static final String ADDR="http://localhost:26888/api_v1_public/";
    public static URL getFullUrl(String shortUrl){
        try {
            return new URL(ADDR + shortUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static ApiResponse sendRequest(String type, String shortUrl, String... params){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .method(type, concatParams(params))
                .uri(URI.create(ADDR + shortUrl))
                .setHeader("User-Agent", "RDI-MC-Client")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(),ApiResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
       /* URL url=getFullUrl(shortUrl);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        connection.setConnectTimeout(15000);
        connection.setReadTimeout(60000);
        // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
        connection.setDoOutput(true);
        // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
        connection.setDoInput(true);
        // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

        String result = null;
        try(OutputStream os = connection.getOutputStream();
            InputStream is=connection.getInputStream();
            BufferedReader br=new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
        ){
            os.write(param.getBytes(StandardCharsets.UTF_8));
            if (connection.getResponseCode() == 200) {
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                }
                result = sbf.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.disconnect();*/

    }
    //类名= url POST
    public static <T extends Serializable> void asyncSendObject(T object){
        asyncSendObject(object.getClass().getSimpleName(),object,"");
    }
    //param &开头 POST
    public static <T extends Serializable> void asyncSendObject(String shortUrl, T object, String params){
        asyncSendObject("POST",shortUrl,object,params);

    }
    //param &开头
    public static <T extends Serializable> void asyncSendObject(String type,String shortUrl, T object, String params){
        ThreadPool.newThread(()-> sendRequest(type,shortUrl, "obj="+new Gson().toJson(object),params));
    }
    private static HttpRequest.BodyPublisher concatParams(String ... params){
        StringBuilder sb = new StringBuilder();
        Arrays.stream(params).forEach((param)->{
            sb.append(param);
            sb.append("&");
        });
        return HttpRequest.BodyPublishers.ofString(sb.toString());
    }
    /*public static String post(String shortUrl,String... params){
        return doPost(ADDR+shortUrl,concatParams(params));
    }
    public static String get(String shortUrl,String... params){
        return doGet(ADDR+shortUrl+"?"+concatParams(params));
    }
    public static String doGet(String fullUrl) {
        long t1= System.currentTimeMillis();
        HttpURLConnection connection = null;
        InputStream is;
        BufferedReader br;
        String result = null;
        try {
            URL url = new URL(fullUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                // 存放数据
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    //sbf.append("\r\n");
                }
                result = sbf.toString();
            }else{
                return "微服务错误："+connection.getResponseCode();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            return "微服务错误："+e.getMessage();
        } finally {
            connection.disconnect();// 关闭远程连接
        }
        long t2= System.currentTimeMillis();
        ServerUtils.recordHttpReqDelay(t1,t2);
        return result;
    }
    public static String doPost(String httpUrl, String param) {
        long startTime = System.currentTimeMillis();
        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
        try {
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            // 设置鉴权信息：Authorization: Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0
            //connection.setRequestProperty("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
            // 通过连接对象获取一个输出流
            os = connection.getOutputStream();
            // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的

            os.write(param.getBytes(StandardCharsets.UTF_8));

            // 通过连接对象获取一个输入流，向远程读取
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                // 循环遍历一行一行读取数据
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    //sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 断开与远程地址url的连接
            connection.disconnect();
        }
        long endTime = System.currentTimeMillis();
        ServerUtils.recordHttpReqDelay(startTime,endTime);
        return result;
    }*/
}