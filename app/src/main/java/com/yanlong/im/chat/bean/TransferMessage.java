package com.yanlong.im.chat.bean;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

//转账消息
public class TransferMessage extends RealmObject implements IMsgContent {
    @PrimaryKey
    private String msgid;
    private String id; // 转账流水号
    private String transaction_amount; // 转账金额
    private String comment; // 备注信息
    private String sign; // 签名信息
    int opType;//操作类型, 红包状态

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransaction_amount() {
        return this.transaction_amount;
    }

    public void setTransaction_amount(String transaction_amount) {
        this.transaction_amount = transaction_amount;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
    public String getMsgId() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }
}
