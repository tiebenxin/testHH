package com.yanlong.im.pay.bean;

import net.cb.cb.library.base.BaseBean;

public class SignatureBean extends BaseBean {

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    private String sign;

}
