package com.yanlong.im.chat.bean;


import io.realm.RealmObject;

public class RequestFriendMessage extends RealmObject {


    private String say_hi; // 招呼语

    public String getSay_hi() {
        return this.say_hi;
    }
    public void setSay_hi(String say_hi) {
        this.say_hi = say_hi;
    }

}
