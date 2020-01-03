package com.hm.cxpay.eventbus;

import net.cb.cb.library.event.BaseEvent;

/**
 * @author Liszt
 * @date 2019/12/27
 * Description
 */
public class NoticeReceiveEvent extends BaseEvent {
    String tradeId;

    public NoticeReceiveEvent(String id){
        this.tradeId = id;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }
}
