package com.hm.cxpay.eventbus;

import com.hm.cxpay.bean.CxTransferBean;

import net.cb.cb.library.event.BaseEvent;

/**
 * @author Liszt
 * @date 2019/12/24
 * Description
 */
public class TransferSuccessEvent extends BaseEvent {
    private CxTransferBean bean;

    public TransferSuccessEvent(CxTransferBean b) {
        bean = b;
    }

    public CxTransferBean getBean() {
        return bean;
    }

    public void setBean(CxTransferBean bean) {
        this.bean = bean;
    }
}
