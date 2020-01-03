package com.yanlong.im.chat.bean;

import net.cb.cb.library.base.BaseBean;

public class GroupUserInfo extends BaseBean {

    private String uid;

    private String nickname;

    private String avatar;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String membername) {
        this.nickname = membername;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
