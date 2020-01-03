package com.hm.cxpay.rx.data;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BaseResponse<T> implements IRespose, Serializable{
    /**
     * 下线通知
     */
    public static final String OFFLINE = "";
    /**
     * 后台解锁
     */
    public static final String BACKSTAGECLERAR = "";
    /**
     * 建议更新
     */
    public static final String SUCCESS_CODE_UPGRADE = "";
    /**
     * 强制更新
     */
    public static final String SUCCESS_CODE_UPGRADE_FOUCE = "";

    @SerializedName("errCode")
    private int code;

    @SerializedName("errMsg")
    private String message;
    T data;

    @Override
    public boolean isSuccess() {
        return code ==0;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getIStatus() {
        return code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
