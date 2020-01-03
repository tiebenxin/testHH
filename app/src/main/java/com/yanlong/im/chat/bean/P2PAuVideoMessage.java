package com.yanlong.im.chat.bean;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-10-22
 * @updateAuthor
 * @updateDate
 * @description 音视频通话对象
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class P2PAuVideoMessage extends RealmObject implements IMsgContent{

    @PrimaryKey
    private String msgId;
   private int av_type; // Audio = 0; // 语音  Vedio = 1; // 视频
    private String operation; //操作(cancel|hangup|reject)
    private String desc; //操作描述

    public int getAv_type() {
        return av_type;
    }

    public void setAv_type(int av_type) {
        this.av_type = av_type;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgId() {
        return msgId;
    }
}
