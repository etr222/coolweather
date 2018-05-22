package com.example.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
//使用OkHttp
//    第一步：开启子线程。创建OkHttpClient对象。通过new OkHttpClient();
//    第二步：发送GET请求：创建Request对象。通过new Request.Builder().url("网址").build();默认为GET请求。
//            发送POST请求：先创建RequestBody对象。通过new FormBody.Builder().add().build();
//                          创建Request对象。通过new Request.Builder().url("网址").post(RequestBody对象).build();
//    第三步：创建Response对象。通过OkHttpClient对象.newCall(Request对象).execute();
//    第四步：获取服务器返回的数据。通过Response对象.body().string();

/*
* OkHttp的工具类。
* */
public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
//        OkHttp的使用：
//        第一步：创建OkHttpClient的对象。通过new OkHttpClient();
        OkHttpClient client=new OkHttpClient();
//    第二步：发送GET请求：创建Request对象。通过new Request.Builder().url("网址").build();默认为GET请求。
//            发送POST请求：先创建RequestBody对象。通过new FormBody.Builder().add().build();
//                          创建Request对象。通过new Request.Builder().url("网址").post(RequestBody对象).build();
        Request request=new Request.Builder().url(address).build();
//    第三步：创建Response对象。通过OkHttpClient对象.newCall(Request对象).execute();
        client.newCall(request).enqueue(callback);
    }
}
