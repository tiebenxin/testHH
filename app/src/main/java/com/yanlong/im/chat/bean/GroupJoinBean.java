package com.yanlong.im.chat.bean;

import net.cb.cb.library.base.BaseBean;

public class GroupJoinBean extends BaseBean {

    private boolean pending;//true|false，是否审核中"

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }
}
