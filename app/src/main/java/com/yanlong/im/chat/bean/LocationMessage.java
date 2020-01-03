package com.yanlong.im.chat.bean;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 位置消息
 */
public class LocationMessage extends RealmObject implements IMsgContent {
    @PrimaryKey
    private String msgId;
    private int latitude=-1;//纬度
    private int longitude=-1;//经度
    private String address;//地址
    private String addressDescribe;//地址描述
    private String img;//地图图片路径


    public LocationMessage() {

    }

    @Override
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressDescribe() {
        return addressDescribe;
    }

    public void setAddressDescribe(String addressDescribe) {
        this.addressDescribe = addressDescribe;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
