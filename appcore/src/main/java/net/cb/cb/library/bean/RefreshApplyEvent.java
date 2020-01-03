package net.cb.cb.library.bean;

import net.cb.cb.library.event.BaseEvent;

/***
 *zgd
 * 20191223
 */
public class RefreshApplyEvent extends BaseEvent {
    public long uid;
    public int chatType = 0;//申请类似 个人 进群
    public int stat = 1; //好友状态 或 群状态 1申请 2同意 3拒绝 即隐藏删除

    public RefreshApplyEvent(long uid, int chatType, int stat) {
        this.uid = uid;
        this.chatType = chatType;
        this.stat = stat;
    }
}
