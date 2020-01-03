package com.yanlong.im.chat.bean;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * @author Liszt
 * @date 2019/12/27
 * Description 红包信息类,适用场景：红包支付成功后，未发送IM消息
 */
public class EnvelopeInfo extends RealmObject {
    @PrimaryKey
    String rid;
    String comment;//红包内容，恭喜发财，大吉大利
    int reType;//红包类型0，魔方, 1系统红包
    int envelopeStyle;// 0 普通, 1 拼手气
    int sendStatus;//发送状态 0 未发送， 1 已发送
    String sign;//签名
    long createTime;//创建时间
    String gid;//群聊
    long uid;//单聊 红包接受者
    long amount;//发送金额

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getReType() {
        return reType;
    }

    public void setReType(int reType) {
        this.reType = reType;
    }

    public int getEnvelopeStyle() {
        return envelopeStyle;
    }

    public void setEnvelopeStyle(int envelopeStyle) {
        this.envelopeStyle = envelopeStyle;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
