package com.hm.cxpay.bean;

import net.cb.cb.library.base.BaseBean;

import java.util.List;

/**
 * @类名：账单明细
 * @Date：2019/12/9
 * @by zjy
 * @备注：
 */
public class BillBean extends BaseBean {
    private long total;
    private List<CommonBean> items;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<CommonBean> getItems() {
        return items;
    }

    public void setItems(List<CommonBean> items) {
        this.items = items;
    }
}
