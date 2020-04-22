package com.javatop.cn.spider.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpClientUtils {
    private static final MediaType MEDIA_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType TEXT_JSON = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");

    private static OkHttpClient httpClient = new OkHttpClient()
            .newBuilder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
//            .proxy(proxy)     //设置代理
            .build();

    /**
     * Post请求方法
     *
     * @param url
     * @param requestParam
     * @return
     */
    public static String postResult(String url, String requestParam) {
        Request request = new Request.Builder()
                .url(url)
                .tag(url)
                .post(RequestBody.create(requestParam, TEXT_JSON))
//                .post(RequestBody.create(requestParam, MEDIA_JSON))
                .build();
        String result = "";
        try {
            Call call = httpClient.newCall(request);
            result = call.execute().body().string();
            closeByTag(call, request);//释放当前请求
        } catch (IOException e) {
            log.error("Fail to request {} with params {}，Exception： {}", url, requestParam, e.getStackTrace());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Get请求方法
     *
     * @param url
     * @return
     */
    public static String getResult(String url) {
        Request request = new Request.Builder()
                .url(url)
                .header("application/json", "charset=utf-8")
                .tag(url)
                .get()
                .build();
        String result = "";
        try {
            Call call = httpClient.newCall(request);
            result = call.execute().body().string();
            closeByTag(call, request);//释放当前请求
        } catch (IOException e) {
            log.error("Fail to request {}，Exception： {}", url, e.getStackTrace());
        }
        return result;
    }

    /**
     * 释放当前请求
     *
     * @param call
     * @param request
     */

    public static void closeByTag(Call call, Request request) {
        if (null == request || null == request.tag() || null == call) {
            return;
        }
        Object tag = request.tag();
        synchronized (call) {
            try {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            } catch (Exception e) {
                log.info("关闭当前请求时出现错误\n" + e.getStackTrace().toString());
            }
        }
    }

    /**
     * 下载文件
     *
     * @param url
     */
    public static void okHttpDownLoad(String url, String requestParam, String fileName) {
        Request.Builder builder = new Request
                .Builder()
                .url(url);
        RequestBody body = new FormBody
                .Builder()
                .add("id", requestParam)
                .add("key", "2")
                .build();
        builder.post(body);
        Request request = builder.build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) {

                Headers responseHeaders = response.headers();
                String headerName = responseHeaders.get("Content-disposition");
//                log.info("headerName-----" + headerName);
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream;
                try {

                    fileOutputStream = new FileOutputStream(new File(fileName + File.separator +
                            headerName.substring(headerName.indexOf("\"") + 1, headerName.lastIndexOf("\""))));
                    byte[] buffer = new byte[2048];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    inputStream.close();
                } catch (IOException e) {
                    log.info("IOException");
                    e.printStackTrace();
                }
//                log.info("文件下载成功");
            }
        });
    }
}
