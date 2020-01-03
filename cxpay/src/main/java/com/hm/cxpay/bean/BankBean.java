package com.hm.cxpay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/11/30
 * Description  绑定银行卡基本数据
 */
public class BankBean extends BaseBean implements Parcelable {
    String bankName;//银行名
    String cardNo;//银行卡号
    long id;//id,传到后台需要此字段
    String logo;//银行logo
    int seqNo;//排序号，升序

    protected BankBean(Parcel in) {
        bankName = in.readString();
        cardNo = in.readString();
        id = in.readLong();
        logo = in.readString();
        seqNo = in.readInt();
    }

    public static final Creator<BankBean> CREATOR = new Creator<BankBean>() {
        @Override
        public BankBean createFromParcel(Parcel in) {
            return new BankBean(in);
        }

        @Override
        public BankBean[] newArray(int size) {
            return new BankBean[size];
        }
    };

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public Long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bankName);
        dest.writeString(cardNo);
        dest.writeLong(id);
        dest.writeString(logo);
        dest.writeInt(seqNo);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof BankBean) {
            if (((BankBean) obj).id == (this.id)) {
                return true;
            }
        }
        return false;
    }
}
