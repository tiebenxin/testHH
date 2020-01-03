package com.yanlong.im.chat.bean;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ChatMessage extends RealmObject implements IMsgContent {
    @PrimaryKey
    private String msgid;

    private String msg; // 消息内容

    public ChatMessage() {

    }

    @Override
    public String getMsgId() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
