package com.hm.cxpay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/11/29
 * Description  银行卡信息
 */
public class BankInfo extends BaseBean implements Parcelable {
    String bankCode;//银行编码
    String bankName;//银行名称
    int cardType;//银行卡类别，目前只支持储蓄卡，不支持信用卡
    String cardTypeLabel;//银行卡类别标签，目前只支持储蓄卡，不支持信用卡
    String ownerName;//持卡人姓名
    String ownerId;//持卡人身份证号
    String bankNumber;//卡号
    String phone;//手机号

    protected BankInfo(Parcel in) {
        bankCode = in.readString();
        bankName = in.readString();
        cardType = in.readInt();
        cardTypeLabel = in.readString();
        ownerName = in.readString();
        ownerId = in.readString();
        bankNumber = in.readString();
        phone = in.readString();
    }

    public static final Creator<BankInfo> CREATOR = new Creator<BankInfo>() {
        @Override
        public BankInfo createFromParcel(Parcel in) {
            return new BankInfo(in);
        }

        @Override
        public BankInfo[] newArray(int size) {
            return new BankInfo[size];
        }
    };

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public int getCardType() {
        return cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }

    public String getCardTypeLabel() {
        return cardTypeLabel;
    }

    public void setCardTypeLabel(String cardTypeLabel) {
        this.cardTypeLabel = cardTypeLabel;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public void setBankNumber(String bankNumber) {
        this.bankNumber = bankNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bankCode);
        dest.writeString(bankName);
        dest.writeInt(cardType);
        dest.writeString(cardTypeLabel);
        dest.writeString(ownerName);
        dest.writeString(ownerId);
        dest.writeString(bankNumber);
        dest.writeString(phone);
    }
}
