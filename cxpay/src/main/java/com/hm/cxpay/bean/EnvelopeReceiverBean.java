package com.hm.cxpay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/12/10
 * Description 红包领取记录
 */
public class EnvelopeReceiverBean extends BaseBean implements Parcelable {
    long amt;//领取金额
    int bestLuck;//是否手气最佳：1是0否
    FromUserBean imUserInfo;//领取用户信息
    long time;//领取时间

    protected EnvelopeReceiverBean(Parcel in) {
        amt = in.readLong();
        bestLuck = in.readInt();
        imUserInfo = in.readParcelable(FromUserBean.class.getClassLoader());
        time = in.readLong();
    }

    public static final Creator<EnvelopeReceiverBean> CREATOR = new Creator<EnvelopeReceiverBean>() {
        @Override
        public EnvelopeReceiverBean createFromParcel(Parcel in) {
            return new EnvelopeReceiverBean(in);
        }

        @Override
        public EnvelopeReceiverBean[] newArray(int size) {
            return new EnvelopeReceiverBean[size];
        }
    };

    public long getAmt() {
        return amt;
    }

    public void setAmt(long amt) {
        this.amt = amt;
    }

    public int getBestLuck() {
        return bestLuck;
    }

    public void setBestLuck(int bestLuck) {
        this.bestLuck = bestLuck;
    }

    public FromUserBean getImUserInfo() {
        return imUserInfo;
    }

    public void setImUserInfo(FromUserBean imUserInfo) {
        this.imUserInfo = imUserInfo;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(amt);
        dest.writeInt(bestLuck);
        dest.writeParcelable(imUserInfo, flags);
        dest.writeLong(time);
    }
}
