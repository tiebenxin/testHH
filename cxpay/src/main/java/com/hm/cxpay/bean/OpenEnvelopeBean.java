package com.hm.cxpay.bean;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/12/10
 * Description 拆红包bean
 */
public class OpenEnvelopeBean extends BaseBean {
    long amt;//抢到金额，只有灵气状态1或者4才有值
    int stat;//1:正常-代表已抢到 2:已领完 3:已过期 4:已领取过

    public long getAmt() {
        return amt;
    }

    public void setAmt(long amt) {
        this.amt = amt;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }
}
