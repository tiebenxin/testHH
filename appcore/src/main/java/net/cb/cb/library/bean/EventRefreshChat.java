package net.cb.cb.library.bean;

import net.cb.cb.library.event.BaseEvent;

/**
 * @创建人 shenxin
 * @创建时间 2019/8/2 0002 14:52
 */
public class EventRefreshChat<T> extends BaseEvent {
    public boolean isScrollBottom =false;
    public T object;//需要刷新的消息对象

    public boolean isScrollBottom() {
        return isScrollBottom;
    }

    public void setScrollBottom(boolean scrollBottom) {
        isScrollBottom = scrollBottom;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
