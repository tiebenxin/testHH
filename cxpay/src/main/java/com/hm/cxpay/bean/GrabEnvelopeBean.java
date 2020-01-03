package com.hm.cxpay.bean;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/12/9
 * Description 抢红包接口bean
 */
public class GrabEnvelopeBean extends BaseBean {
    String accessToken;//用户授权访问凭证,拆红包、详情等接口需上送
    int stat;//1:正常 2:已领完 3:已过期 4:其他(bug?已领取过...)

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }
}
