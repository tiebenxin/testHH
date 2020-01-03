package com.yanlong.im.chat.ui.forward;

/**
 * author : zgd
 * date   : 2019/11/1415:40
 * 转发多人保存对象
 */
public class MoreSessionBean {
    private long uid=-1L;
    private String gid;
    private  String avatar;
    private String nick;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
}
