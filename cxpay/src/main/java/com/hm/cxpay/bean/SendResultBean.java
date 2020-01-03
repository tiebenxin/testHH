package com.hm.cxpay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/12/3
 * Description 发送红包结果bean
 */
public class SendResultBean extends BaseBean implements Parcelable {
    String actionId;
    int code;//1:成功 2:失败 99:处理中 客户端暂不考虑[1010:需要下一步验证
    long createTime;
    String errMsg;
    long tradeId;
    String sign;

    protected SendResultBean(Parcel in) {
        actionId = in.readString();
        code = in.readInt();
        createTime = in.readLong();
        errMsg = in.readString();
        tradeId = in.readLong();
        sign = in.readString();

    }

    public static final Creator<SendResultBean> CREATOR = new Creator<SendResultBean>() {
        @Override
        public SendResultBean createFromParcel(Parcel in) {
            return new SendResultBean(in);
        }

        @Override
        public SendResultBean[] newArray(int size) {
            return new SendResultBean[size];
        }
    };

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public long getTradeId() {
        return tradeId;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public String getSign() {
        return sign;
    }


    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(actionId);
        dest.writeInt(code);
        dest.writeLong(createTime);
        dest.writeString(errMsg);
        dest.writeLong(tradeId);
        dest.writeString(sign);
    }
}
