package net.cb.cb.library.bean;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.event.BaseEvent;

/***
 * 刷新好友
 */
public class EventRefreshFriend extends BaseEvent {
    private boolean isLocal = false;//冲本地刷新好友列表

    @CoreEnum.ERosterAction
    private int rosterAction = CoreEnum.ERosterAction.DEFAULT;//roster操作指令

    private long uid;

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public int getRosterAction() {
        return rosterAction;
    }

    public void setRosterAction(int rosterAction) {
        this.rosterAction = rosterAction;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
}
