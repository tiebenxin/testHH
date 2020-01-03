package com.hm.cxpay.rx;

import java.util.HashMap;

public class RequestManager {

    protected static HashMap<Class, Object> sRequestManager = new HashMap<>();

    public static <T> T getRequest(Class<T> clazz) {
        T t = (T) sRequestManager.get(clazz);
        if (t == null) {
            t = RetrofitClient.createApi(clazz, PayHostUtils.getHttpsUrl());
            sRequestManager.put(clazz, t);
        }
        return t;
    }
}
