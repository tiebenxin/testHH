package com.hm.cxpay.bean;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/12/25
 * Description 领取或者退还转账结果bean
 */
public class TransferResultBean extends BaseBean {
    private String sign;
    private int stat;// 1:成功 3:已过期 4:已领取过 5已拒绝过
    private long time;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
