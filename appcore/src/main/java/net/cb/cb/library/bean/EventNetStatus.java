package net.cb.cb.library.bean;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.event.BaseEvent;

/**
 * @author Liszt
 * @date 2019/9/19
 * Description
 */
public class EventNetStatus extends BaseEvent {
    @CoreEnum.ENetStatus
    private int status;

    public EventNetStatus(@CoreEnum.ENetStatus int value) {
        status = value;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
