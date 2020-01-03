package com.yanlong.im.chat.bean;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * @创建人 shenxin
 * @创建时间 2019/7/25 0025 10:45
 */
public class AtMessage extends RealmObject implements IMsgContent {
    @PrimaryKey
    private String msgId;

    private int at_type;

    private String msg;

    private RealmList<Long> uid = new RealmList<>();

    public int getAt_type() {
        return at_type;
    }

    public void setAt_type(int at_type) {
        this.at_type = at_type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Long> getUid() {
        return uid;
    }

    public void setUid(RealmList<Long> uid) {
        this.uid = uid;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
