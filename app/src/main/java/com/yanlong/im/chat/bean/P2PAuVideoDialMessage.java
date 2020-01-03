package com.yanlong.im.chat.bean;

import io.realm.RealmObject;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-11-04
 * @updateAuthor
 * @updateDate
 * @description
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class P2PAuVideoDialMessage extends RealmObject {
    private int av_type; // Audio = 0; // 语音  Vedio = 1; // 视频

    public int getAv_type() {
        return av_type;
    }

    public void setAv_type(int av_type) {
        this.av_type = av_type;
    }
}
