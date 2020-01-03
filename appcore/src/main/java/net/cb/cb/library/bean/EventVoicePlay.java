package net.cb.cb.library.bean;

import net.cb.cb.library.event.BaseEvent;

public class EventVoicePlay<T> extends BaseEvent {
    int position;
    T t;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setBean(T t) {
        this.t = t;
    }

    public T getBean() {
        return t;
    }
}
