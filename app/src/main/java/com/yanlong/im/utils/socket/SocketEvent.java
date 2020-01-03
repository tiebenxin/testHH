package com.yanlong.im.utils.socket;

public interface SocketEvent {
    void onHeartbeat();
    void onACK(MsgBean.AckMessage bean);
    void onMsg(MsgBean.UniversalMessage bean);
    void onSendMsgFailure(MsgBean.UniversalMessage.Builder bean);
    void onLine(boolean state);
}
