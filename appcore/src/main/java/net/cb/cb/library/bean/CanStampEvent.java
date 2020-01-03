package net.cb.cb.library.bean;

import net.cb.cb.library.event.BaseEvent;

/***
 *zgd
 * 20191223
 */
public class CanStampEvent extends BaseEvent {
    public Boolean canStamp=true;

    public CanStampEvent(Boolean canStamp) {
        this.canStamp = canStamp;
    }
}
