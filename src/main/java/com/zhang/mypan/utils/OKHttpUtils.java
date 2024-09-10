package com.zhang.mypan.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OKHttpUtils {
    public static final int TIME_OUT_SECONDS = 6; //  超时时间 6s

    public static OkHttpClient.Builder getClientBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().followRedirects(false).retryOnConnectionFailure(false);
        builder.connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS).readTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS);
        return builder;
    }

    public static Request.Builder getRequestBuilder(Map<String, String> header) {
        Request.Builder requestBuilder = new Request.Builder();
        if (null != header) {
            for (Map.Entry<String, String> map : header.entrySet()) {
                String key = map.getKey();
                String value;
                if (map.getValue() == null) {
                    value = "";
                } else {
                    value = map.getValue();
                }
                requestBuilder.addHeader(key, value);
            }
        }
        return requestBuilder;
    }

    public static String getRequest(String url) {
        ResponseBody responseBody = null;
        try {
            OkHttpClient.Builder builder = getClientBuilder();
            Request.Builder requestBuilder = getRequestBuilder(null);
            OkHttpClient client = builder.build();
            Request request = requestBuilder.url(url).build();
            Response response = client.newCall(request).execute();
            responseBody = response.body();
            String responseStr = responseBody.string();
            log.info("postRestRequest请求地址:{}，返回地址{}", url, responseStr);
            return responseStr;
        } catch (IOException e) {
            log.error("okhttp post 请求异常", e);
            throw new RuntimeException(e);
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }

    }

}
