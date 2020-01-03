package net.cb.cb.library.bean;

import net.cb.cb.library.event.BaseEvent;

/***
 *
 *运行状态
 * @author jyj
 * @date 2018/1/29
 */
public class EventRunState extends BaseEvent {
    private Boolean isRun;

    public Boolean getRun() {
        return isRun;
    }

    public void setRun(Boolean run) {
        isRun = run;
    }
}
