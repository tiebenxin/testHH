package com.yanlong.im.chat.ui.forward;

/**
 * @author zgd
 * @date 2019/11/14
 * 单选还是多选
 */
public class SingleOrMoreEvent {
    public Boolean isSingleSelected=true;//默认单选

    public SingleOrMoreEvent(Boolean isSingleSelected){
        this.isSingleSelected=isSingleSelected;
    }
}
