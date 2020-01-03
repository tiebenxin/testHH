package net.cb.cb.library.bean;

import net.cb.cb.library.event.BaseEvent;
import net.cb.cb.library.utils.StringUtil;

/***
 * 关闭界面 事件
 */
public class CloseActivityEvent  extends BaseEvent {
    public String type;

    public CloseActivityEvent(String type){
        if(StringUtil.isNotNull(type)){
            this.type=type;
        }else {
            this.type="";
        }
    }
}
