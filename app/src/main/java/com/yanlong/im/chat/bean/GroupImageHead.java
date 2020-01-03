package com.yanlong.im.chat.bean;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GroupImageHead extends RealmObject {
    @PrimaryKey
    private String gid;

    private String imgHeadUrl;

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getImgHeadUrl() {
        return imgHeadUrl;
    }

    public void setImgHeadUrl(String imgHeadUrl) {
        this.imgHeadUrl = imgHeadUrl;
    }

}
