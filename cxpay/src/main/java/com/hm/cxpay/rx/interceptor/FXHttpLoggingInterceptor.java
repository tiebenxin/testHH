package com.hm.cxpay.rx.interceptor;


import com.hm.cxpay.rx.JHttpLoggingInterceptor;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

/**
 * 如果是上传文件格式的，不需要添加过滤
 * Created by Liszt on 2018/4/11
 */

public class FXHttpLoggingInterceptor extends JHttpLoggingInterceptor {

    /**
     * 需要被过滤掉log的url地址
     */
    private static final String[] FILTER_URLS = {"/DFS/ImageSave","/DFS/VideoSave"};

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        try {
            final String url = request.url().url().toString();
            for (String filter_url : FILTER_URLS) {
                if (url.contains(filter_url)) {
                    Response response = chain.proceed(request);
                    return response;
                }
            }
        } catch (Exception e) {
            return super.intercept(chain);
        }
        Response response = super.intercept(chain);
        return response;
    }
}
