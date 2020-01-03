package com.yanlong.im.listener;

/**
 * Author: zgd
 * Date: 2016/6/17
 * 回调基本类
 */
public  class BaseListener {
    //成功
    public void onSuccess(){}
    //失败
    public void onError(){}
    //取消
    public void onCancel(){}

    //多功能
    public void onSuccess(String str){}
    public void onSuccess(int intOne){}

    public void onSuccess(Object object){}

}
