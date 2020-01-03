package com.hm.cxpay.rx.interceptor;

import android.text.TextUtils;


import com.hm.cxpay.global.PayEnvironment;

import net.cb.cb.library.manager.TokenManager;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 所有请求的公共参数
 * <p>
 * Created by Liszt on 2018/4/11
 */

public class CommonInterceptor implements Interceptor {
    public CommonInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request oldRequest = chain.request();
        /**
         * 公共参数
         */
        HttpUrl.Builder authorizedUrlBuilder = oldRequest.url()
                .newBuilder()
                .scheme(oldRequest.url().scheme())

                .host(oldRequest.url().host());

        /**
         * 新的请求
         */
        Request.Builder requestBuilder = oldRequest.newBuilder()
                .method(oldRequest.method(), oldRequest.body())
                .url(authorizedUrlBuilder.build());
        requestBuilder.header("Content-Type", "application/json;charset=utf-8");


        /**
         * 已经登陆的用户 带上X-Access-Token
         */
        if (!TextUtils.isEmpty(PayEnvironment.getInstance().getToken())) {
            requestBuilder.header(TokenManager.TOKEN_KEY, PayEnvironment.getInstance().getToken());
        }
        return chain.proceed(requestBuilder.build());
    }
}
