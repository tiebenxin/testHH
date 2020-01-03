package com.yanlong.im.chat.bean;

import net.cb.cb.library.base.BaseBean;

public class MsgBean extends BaseBean {

    private int type;
    private boolean isMe;
    private String context;

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
