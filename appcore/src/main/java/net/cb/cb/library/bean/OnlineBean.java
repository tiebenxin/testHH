package net.cb.cb.library.bean;

import net.cb.cb.library.base.BaseBean;

/**
 * @author Liszt
 * @date 2019/8/21
 * Description
 */
public class OnlineBean extends BaseBean {
    long uid;
    long lastonline;//最后在线时间
    int activeType;//永华活跃状态（0离线|1在线）

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getLastonline() {
        return lastonline;
    }

    public void setLastonline(long lastonline) {
        this.lastonline = lastonline;
    }

    public int getActiveType() {
        return activeType;
    }

    public void setActiveType(int activeType) {
        this.activeType = activeType;
    }
}
