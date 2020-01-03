package com.hm.cxpay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import net.cb.cb.library.base.BaseBean;

import java.util.List;

/**
 * @author Liszt
 * @date 2019/12/2
 * Description 红包明细详情
 */
public class RedDetailsBean extends BaseBean implements Parcelable {

    List<RedEnvelopeItemBean> items;
    long sumAmt;
    long total;

    protected RedDetailsBean(Parcel in) {
        sumAmt = in.readLong();
        total = in.readLong();
    }

    public static final Creator<RedDetailsBean> CREATOR = new Creator<RedDetailsBean>() {
        @Override
        public RedDetailsBean createFromParcel(Parcel in) {
            return new RedDetailsBean(in);
        }

        @Override
        public RedDetailsBean[] newArray(int size) {
            return new RedDetailsBean[size];
        }
    };

    public List<RedEnvelopeItemBean> getItems() {
        return items;
    }

    public void setItems(List<RedEnvelopeItemBean> items) {
        this.items = items;
    }

    public long getSumAmt() {
        return sumAmt;
    }

    public void setSumAmt(long sumAmt) {
        this.sumAmt = sumAmt;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(sumAmt);
        dest.writeLong(total);
    }
}
