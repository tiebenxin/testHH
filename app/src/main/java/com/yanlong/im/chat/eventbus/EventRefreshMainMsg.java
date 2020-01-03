package com.yanlong.im.chat.eventbus;

import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.Session;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.event.BaseEvent;

/***
 * 刷新首页的消息
 */
public class EventRefreshMainMsg extends BaseEvent {
    private int type;//聊天类型，单聊还是群聊
    private Long uid;//刷新用户session
    private String gid;//刷新群聊session
    private MsgAllBean msgAllBean;//需要刷新最后一条消息
    private Session session;//需要刷新整个session
    private boolean isRefreshTop;//是否刷新置顶

    @CoreEnum.ESessionRefreshTag
    private int refreshTag = CoreEnum.ESessionRefreshTag.ALL;//刷新类型，单个刷新还是全部刷新,默认刷新all


    public int getType() {
        return type;
    }

    public void setType(@CoreEnum.EChatType int type) {
        this.type = type;
    }

    public long getUid() {
        if (uid == null) {
            uid = -1L;
        }
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public int getRefreshTag() {
        return refreshTag;
    }

    public void setRefreshTag(@CoreEnum.ESessionRefreshTag int refreshTag) {
        this.refreshTag = refreshTag;
    }

    public MsgAllBean getMsgAllBean() {
        return msgAllBean;
    }

    public void setMsgAllBean(MsgAllBean msgAllBean) {
        this.msgAllBean = msgAllBean;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public boolean isRefreshTop() {
        return isRefreshTop;
    }

    public void setRefreshTop(boolean refreshTop) {
        isRefreshTop = refreshTop;
    }
}
