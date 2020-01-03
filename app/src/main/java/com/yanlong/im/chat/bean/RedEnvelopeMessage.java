package com.yanlong.im.chat.bean;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RedEnvelopeMessage extends RealmObject implements IMsgContent {
    @PrimaryKey
    private String msgid;
    private String id;//红包id,MF
    private Integer re_type;    // MFPAY = 0; SYSTEM =1
    private String comment;

    //红包的状态0:没拆,1拆了
    private int isInvalid = 0;
    //红包玩法种类:0普通1拼手气
    private int style = 0;

    private long traceId;//交易订单号
    private String actionId;
    private String accessToken;//查看系统红包token
    private int envelopStatus;// 红包状态：0-正常， 1-已领取  2-已过期
    String sign;//签名信息

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public int getIsInvalid() {
        return isInvalid;
    }

    public void setIsInvalid(int isInvalid) {
        this.isInvalid = isInvalid;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRe_type() {
        return this.re_type;
    }

    public void setRe_type(Integer re_type) {
        this.re_type = re_type;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getMsgId() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public long getTraceId() {
        return traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getEnvelopStatus() {
        return envelopStatus;
    }

    public void setEnvelopStatus(int envelopStatus) {
        this.envelopStatus = envelopStatus;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
