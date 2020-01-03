package com.yanlong.im.location;

import com.yanlong.im.chat.bean.LocationMessage;

/**
 * @anthor zgd
 * @data 2019/12/15
 */
public class LocationSendEvent {
    public LocationMessage message;

    public LocationSendEvent(LocationMessage message){
        this.message=message;
    }
}
