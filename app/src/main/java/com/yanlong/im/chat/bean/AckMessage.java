package com.yanlong.im.chat.bean;

import io.realm.RealmObject;

public class AckMessage extends RealmObject {

    private String request_id;
    private String msg_id; // 消息id

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }



}
