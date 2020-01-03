package com.hm.cxpay.net;

/**
 * date on 2018/2/27
 * author ll147996
 * describe
 */

public interface NetworkRequestListener {

    void noNetwork();
    void start(boolean isShowProgress);
    void end();
    void interruptedNetwork();
}
