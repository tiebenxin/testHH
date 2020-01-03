package com.yanlong.im.chat;

import com.yanlong.im.chat.bean.MsgAllBean;

import java.util.List;

/**
 * @创建人 shenxin
 * @创建时间 2019/10/22 0022 14:26
 */
public class EventSurvivalTimeAdd {
    public MsgAllBean msgAllBean;
    public List<MsgAllBean> list;

    public EventSurvivalTimeAdd(MsgAllBean msgAllBean, List<MsgAllBean> list){
        this.msgAllBean = msgAllBean;
        this.list = list;
    }

}
