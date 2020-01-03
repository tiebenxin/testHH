package com.yanlong.im.user.bean;

import net.cb.cb.library.base.BaseBean;

public class LoginBean extends BaseBean {
    private String password;
    private Long phone;
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }

    public void setPhone(Long phone) {
        this.phone = phone;
    }
    public Long getPhone() {
        return phone;
    }
}
