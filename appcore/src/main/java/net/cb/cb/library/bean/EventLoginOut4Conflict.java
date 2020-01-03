package net.cb.cb.library.bean;

import net.cb.cb.library.event.BaseEvent;

/***
 *
 *踢下线
 *  * @author jyj
 * @date 2018/1/29
 */
public class EventLoginOut4Conflict extends BaseEvent {
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
