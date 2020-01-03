package com.hm.cxpay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/12/2
 * Description 红包明细item bean
 */
public class RedEnvelopeItemBean extends BaseBean implements Parcelable {
    long amt;//交易金额，单位：分
    FromUserBean fromUser;
    long time;
    long tradeId;
    int type;

    protected RedEnvelopeItemBean(Parcel in) {
        amt = in.readLong();
        fromUser = in.readParcelable(FromUserBean.class.getClassLoader());
        time = in.readLong();
        tradeId = in.readLong();
        type = in.readInt();
    }

    public static final Creator<RedEnvelopeItemBean> CREATOR = new Creator<RedEnvelopeItemBean>() {
        @Override
        public RedEnvelopeItemBean createFromParcel(Parcel in) {
            return new RedEnvelopeItemBean(in);
        }

        @Override
        public RedEnvelopeItemBean[] newArray(int size) {
            return new RedEnvelopeItemBean[size];
        }
    };

    public long getAmt() {
        return amt;
    }

    public void setAmt(long amt) {
        this.amt = amt;
    }

    public FromUserBean getFromUser() {
        return fromUser;
    }

    public void setFromUser(FromUserBean fromUser) {
        this.fromUser = fromUser;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTradeId() {
        return tradeId;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(amt);
        dest.writeParcelable(fromUser, flags);
        dest.writeLong(time);
        dest.writeLong(tradeId);
        dest.writeInt(type);
    }
}
