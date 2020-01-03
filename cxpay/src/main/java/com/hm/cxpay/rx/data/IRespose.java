package com.hm.cxpay.rx.data;

/**
 * 返回数据基类必须实现接口
 * 提供结果判断 和 通用消息返回
 */
public interface IRespose {

    boolean isSuccess() ;

    String getMessage();

    int getIStatus();
}
