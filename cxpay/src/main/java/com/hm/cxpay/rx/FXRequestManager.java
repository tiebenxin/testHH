package com.hm.cxpay.rx;

/**
 * Created by zm on 2018/4/11
 */
public class FXRequestManager extends RequestManager {

    public static <T> T getRequest(Class<T> clazz) {
        T t = (T) sRequestManager.get(clazz);
        if (t == null) {
            t = FXRetrofitClient.createApi(clazz, PayHostUtils.getHttpsUrl());
            sRequestManager.put(clazz, t);
        }
        return t;
    }
}
