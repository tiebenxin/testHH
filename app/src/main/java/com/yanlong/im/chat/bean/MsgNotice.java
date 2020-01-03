package com.yanlong.im.chat.bean;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/***
 * 类型为0的
 * 通知消息
 */
public class MsgNotice extends RealmObject implements IMsgContent {
    public static final int MSG_TYPE_DEFAULT = 7897;

    @PrimaryKey
    private String msgid;
    private Long uid;
    private String note;
    private Integer msgType = MSG_TYPE_DEFAULT;

    public Integer getMsgType() {
        return msgType;
    }

    //7,8,17为红包消息类型, 通知消息类型
    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String getMsgId() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }


}
