package com.hm.cxpay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/12/25
 * Description
 */
public class TransferDetailBean extends BaseBean implements Parcelable {
    private long amt;//金额
    private String bankCardInfo = "";//银行卡信息，转账发起人才能返回
    private int income = 0;//1 收入 其他支出
    private String note = "";
    private FromUserBean payUser;//支付者用户信息
    private long recvTime;//领取时间
    private FromUserBean recvUser;//接受者用户信息
    private int refundWay;
    private long rejectTime;//退还时间
    private int stat;//1未领取 2已领取 3已拒收 4已过期
    private long transTime;//转账时间

    protected TransferDetailBean(Parcel in) {
        amt = in.readLong();
        bankCardInfo = in.readString();
        income = in.readInt();
        note = in.readString();
        payUser = in.readParcelable(FromUserBean.class.getClassLoader());
        recvTime = in.readLong();
        recvUser = in.readParcelable(FromUserBean.class.getClassLoader());
        refundWay = in.readInt();
        rejectTime = in.readLong();
        stat = in.readInt();
        transTime = in.readLong();
    }

    public static final Creator<TransferDetailBean> CREATOR = new Creator<TransferDetailBean>() {
        @Override
        public TransferDetailBean createFromParcel(Parcel in) {
            return new TransferDetailBean(in);
        }

        @Override
        public TransferDetailBean[] newArray(int size) {
            return new TransferDetailBean[size];
        }
    };

    public long getAmt() {
        return amt;
    }

    public void setAmt(long amt) {
        this.amt = amt;
    }

    public String getBankCardInfo() {
        return bankCardInfo;
    }

    public void setBankCardInfo(String bankCardInfo) {
        this.bankCardInfo = bankCardInfo;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public FromUserBean getPayUser() {
        return payUser;
    }

    public void setPayUser(FromUserBean payUser) {
        this.payUser = payUser;
    }

    public long getRecvTime() {
        return recvTime;
    }

    public void setRecvTime(long recvTime) {
        this.recvTime = recvTime;
    }

    public FromUserBean getRecvUser() {
        return recvUser;
    }

    public void setRecvUser(FromUserBean recvUser) {
        this.recvUser = recvUser;
    }

    public int getRefundWay() {
        return refundWay;
    }

    public void setRefundWay(int refundWay) {
        this.refundWay = refundWay;
    }

    public long getRejectTime() {
        return rejectTime;
    }

    public void setRejectTime(long rejectTime) {
        this.rejectTime = rejectTime;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public long getTransTime() {
        return transTime;
    }

    public void setTransTime(long transTime) {
        this.transTime = transTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(amt);
        dest.writeString(bankCardInfo);
        dest.writeInt(income);
        dest.writeString(note);
        dest.writeParcelable(payUser, flags);
        dest.writeLong(recvTime);
        dest.writeParcelable(recvUser, flags);
        dest.writeInt(refundWay);
        dest.writeLong(rejectTime);
        dest.writeInt(stat);
        dest.writeLong(transTime);
    }
}
