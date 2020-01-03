package com.yanlong.im.chat.ui.cell;

import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.MsgAllBean;

public interface ICellEventListener {
    void onEvent(@ChatEnum.ECellEventType int type, MsgAllBean message, Object...args);
}
