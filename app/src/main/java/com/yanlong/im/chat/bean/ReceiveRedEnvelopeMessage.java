package com.yanlong.im.chat.bean;


import io.realm.RealmObject;

public class ReceiveRedEnvelopeMessage extends RealmObject {


    private String id;

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }



}
