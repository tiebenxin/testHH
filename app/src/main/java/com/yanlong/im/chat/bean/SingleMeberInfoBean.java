package com.yanlong.im.chat.bean;

import net.cb.cb.library.base.BaseBean;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-26
 * @updateAuthor
 * @updateDate
 * @description
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class SingleMeberInfoBean extends BaseBean {

    /**
     * membername :
     * disturb : false
     * cantOpenUpRedEnv : false
     * shutUpDuration : 259200
     */

    private String membername;
    private boolean disturb;
    private boolean cantOpenUpRedEnv;
    private int shutUpDuration;

    public String getMembername() {
        return membername;
    }

    public void setMembername(String membername) {
        this.membername = membername;
    }

    public boolean isDisturb() {
        return disturb;
    }

    public void setDisturb(boolean disturb) {
        this.disturb = disturb;
    }

    public boolean isCantOpenUpRedEnv() {
        return cantOpenUpRedEnv;
    }

    public void setCantOpenUpRedEnv(boolean cantOpenUpRedEnv) {
        this.cantOpenUpRedEnv = cantOpenUpRedEnv;
    }

    public int getShutUpDuration() {
        return shutUpDuration;
    }

    public void setShutUpDuration(int shutUpDuration) {
        this.shutUpDuration = shutUpDuration;
    }
}
