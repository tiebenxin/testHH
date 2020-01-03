package com.yanlong.im.chat.bean;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * @创建人 shenxin
 * @创建时间 2019/9/3
 */
public class ContactNameBean extends RealmObject {

    @PrimaryKey
    private Long uid;

    private String contactName;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}
