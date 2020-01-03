package com.hm.cxpay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.hm.cxpay.global.PayEnum;

import net.cb.cb.library.base.BaseBean;

import java.util.List;

/**
 * @author Liszt
 * @date 2019/12/10
 * Description 查看红包详情
 */
public class EnvelopeDetailBean extends BaseBean implements Parcelable {
    long amt;//红包总金额
    int cnt; //红包个数
    long finishTime;//红包全部领完时间
    FromUserBean imUserInfo;//发红包者用户信息
    String note;//红包备注：恭喜发财，大吉大利
    long remainAmt;//剩余金额
    int remainCnt;//红包个数
    long time;//红包发送时间
    int type;//红包类型：0 普通红包，1拼手气红包
    List<EnvelopeReceiverBean> recvList;//领取记录
    @SerializedName("tmpToGroup")
    int chatType;//单聊=0，还是群聊=1
    @PayEnum.EEnvelopeStatus
    int envelopeStatus;//红包状态


    protected EnvelopeDetailBean(Parcel in) {
        amt = in.readLong();
        cnt = in.readInt();
        finishTime = in.readLong();
        imUserInfo = in.readParcelable(FromUserBean.class.getClassLoader());
        note = in.readString();
        remainAmt = in.readLong();
        remainCnt = in.readInt();
        time = in.readLong();
        type = in.readInt();
        recvList = in.createTypedArrayList(EnvelopeReceiverBean.CREATOR);
        chatType = in.readInt();
        envelopeStatus = in.readInt();

    }

    public static final Creator<EnvelopeDetailBean> CREATOR = new Creator<EnvelopeDetailBean>() {
        @Override
        public EnvelopeDetailBean createFromParcel(Parcel in) {
            return new EnvelopeDetailBean(in);
        }

        @Override
        public EnvelopeDetailBean[] newArray(int size) {
            return new EnvelopeDetailBean[size];
        }
    };

    public long getAmt() {
        return amt;
    }

    public void setAmt(long amt) {
        this.amt = amt;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public FromUserBean getImUserInfo() {
        return imUserInfo;
    }

    public void setImUserInfo(FromUserBean imUserInfo) {
        this.imUserInfo = imUserInfo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getRemainAmt() {
        return remainAmt;
    }

    public void setRemainAmt(long remainAmt) {
        this.remainAmt = remainAmt;
    }

    public int getRemainCnt() {
        return remainCnt;
    }

    public void setRemainCnt(int remainCnt) {
        this.remainCnt = remainCnt;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<EnvelopeReceiverBean> getRecvList() {
        return recvList;
    }

    public void setRecvList(List<EnvelopeReceiverBean> recvList) {
        this.recvList = recvList;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public int getEnvelopeStatus() {
        return envelopeStatus;
    }

    public void setEnvelopeStatus(int envelopeStatus) {
        this.envelopeStatus = envelopeStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(amt);
        dest.writeInt(cnt);
        dest.writeLong(finishTime);
        dest.writeParcelable(imUserInfo, flags);
        dest.writeString(note);
        dest.writeLong(remainAmt);
        dest.writeInt(remainCnt);
        dest.writeLong(time);
        dest.writeInt(type);
        dest.writeTypedList(recvList);
        dest.writeInt(chatType);
        dest.writeInt(envelopeStatus);
    }
}
