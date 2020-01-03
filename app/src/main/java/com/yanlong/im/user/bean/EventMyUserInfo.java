package com.yanlong.im.user.bean;

import net.cb.cb.library.event.BaseEvent;

public class EventMyUserInfo extends BaseEvent {
    public static final int ALTER_HEAD = 1;
    public static final int ALTER_IMID = 2;
    public static final int ALTER_NAME = 3;
    public static final int ALTER_SEX = 3;

    public int type; //1 修改头像 2.修改常信号 3.修改昵称 4.修改性别

    private UserInfo userInfo;

    public EventMyUserInfo(UserInfo userInfo,int type){
        this.userInfo = userInfo;
        this.type = type;
    }


    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
