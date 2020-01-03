package net.cb.cb.library.bean;

import net.cb.cb.library.event.BaseEvent;

/***
 *
 *
 * @author jyj
 * @date 2018/1/29
 */
public class EventLoginOut extends BaseEvent {

    public int loginType ;

    public EventLoginOut(){
    }

    public EventLoginOut(int loginType){
        this.loginType = loginType;
    }

}
