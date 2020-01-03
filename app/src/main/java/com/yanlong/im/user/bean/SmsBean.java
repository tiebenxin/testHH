package com.yanlong.im.user.bean;

import net.cb.cb.library.base.BaseBean;

public class SmsBean extends BaseBean {

    private String captcha;

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
