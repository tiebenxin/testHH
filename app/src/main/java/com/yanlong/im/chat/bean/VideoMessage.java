package com.yanlong.im.chat.bean;

import android.text.TextUtils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class VideoMessage extends RealmObject implements IMsgContent {
    @PrimaryKey
    private String msgId;
    private long duration;
    private String bg_url;
    private long width;
    private long height;
    private String url;
    private boolean isReadOrigin = false;
    private String localUrl;


    public long getDuration() {
        return duration;
    }


    public String getLocalUrl() {
        return !TextUtils.isEmpty(localUrl) ? localUrl : "";
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    @Override
    public String toString() {
        return "VideoMessage{" +
                "msgId='" + msgId + '\'' +
                ", duration=" + duration +
                ", bg_url='" + bg_url + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", url='" + url + '\'' +
                ", isReadOrigin=" + isReadOrigin +
                ", localUrl='" + localUrl + '\'' +
                '}';
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getBg_url() {
        return !TextUtils.isEmpty(bg_url) ? bg_url : "";
    }

    public void setBg_url(String bg_url) {
        this.bg_url = bg_url;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String getUrl() {
        return !TextUtils.isEmpty(url) ? url : "";
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isReadOrigin() {
        return isReadOrigin;
    }

    public void setReadOrigin(boolean readOrigin) {
        isReadOrigin = readOrigin;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    @Override
    public String getMsgId() {
        return msgId;
    }
}
