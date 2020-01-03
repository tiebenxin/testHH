package com.hm.cxpay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/12/2
 * Description 红包 from User
 */
public class FromUserBean extends BaseBean implements Parcelable {
    String avatar="";
    String nickname="";
    long uid;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    protected FromUserBean(Parcel in) {
        avatar = in.readString();
        nickname = in.readString();
        uid = in.readLong();
    }

    public static final Creator<FromUserBean> CREATOR = new Creator<FromUserBean>() {
        @Override
        public FromUserBean createFromParcel(Parcel in) {
            return new FromUserBean(in);
        }

        @Override
        public FromUserBean[] newArray(int size) {
            return new FromUserBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(avatar);
        dest.writeString(nickname);
        dest.writeLong(uid);
    }
}
