package net.cb.cb.library.bean;

import net.cb.cb.library.event.BaseEvent;

/**
 * @author Liszt
 * @date 2019/10/24
 * Description
 */
public class EventGroupChange extends BaseEvent {
    private boolean isNeedLoad = false;//是否需要加载新的群数据

    public boolean isNeedLoad() {
        return isNeedLoad;
    }

    public void setNeedLoad(boolean needLoad) {
        isNeedLoad = needLoad;
    }
}
