package com.hm.cxpay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/12/24
 * Description
 */
public class CxTransferBean extends BaseBean implements Parcelable {
    long tradeId;//交易id
    long amount;//转账金额
    String info = "";//转账说明
    String sign = "";//签名’
    int opType;//操作类型
    long uid;
    String msgJson;//发起转账消息json

    public CxTransferBean() {
    }

    protected CxTransferBean(Parcel in) {
        tradeId = in.readLong();
        amount = in.readLong();
        info = in.readString();
        sign = in.readString();
        opType = in.readInt();
        uid = in.readLong();
        msgJson = in.readString();
    }

    public static final Creator<CxTransferBean> CREATOR = new Creator<CxTransferBean>() {
        @Override
        public CxTransferBean createFromParcel(Parcel in) {
            return new CxTransferBean(in);
        }

        @Override
        public CxTransferBean[] newArray(int size) {
            return new CxTransferBean[size];
        }
    };

    public long getTradeId() {
        return tradeId;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public int getOpType() {
        return opType;
    }

    public void setOpType(int opType) {
        this.opType = opType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getMsgJson() {
        return msgJson;
    }

    public void setMsgJson(String msgJson) {
        this.msgJson = msgJson;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(tradeId);
        dest.writeLong(amount);
        dest.writeString(info);
        dest.writeString(sign);
        dest.writeInt(opType);
        dest.writeLong(uid);
        dest.writeString(msgJson);
    }
}
