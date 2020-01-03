package com.hm.cxpay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/11/30
 * Description 申请绑定银行卡返回信息
 */
public class BindBankInfo extends BaseBean implements Parcelable {
    String sign;//签名
    String tranceNum;//流水单号
    String transDate;//流水时间

    protected BindBankInfo(Parcel in) {
        sign = in.readString();
        tranceNum = in.readString();
        transDate = in.readString();
    }

    public static final Creator<BindBankInfo> CREATOR = new Creator<BindBankInfo>() {
        @Override
        public BindBankInfo createFromParcel(Parcel in) {
            return new BindBankInfo(in);
        }

        @Override
        public BindBankInfo[] newArray(int size) {
            return new BindBankInfo[size];
        }
    };

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTranceNum() {
        return tranceNum;
    }

    public void setTranceNum(String tranceNum) {
        this.tranceNum = tranceNum;
    }

    public String getTransDate() {
        return transDate;
    }

    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sign);
        dest.writeString(tranceNum);
        dest.writeString(transDate);
    }
}
