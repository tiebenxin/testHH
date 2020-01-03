package com.yanlong.im.utils.socket;

public  class SendListBean {
    private int reSendNum = 0;
    private long firstTimeSent;//首次发送时间
    private MsgBean.UniversalMessage.Builder msg;
    private MsgBean.AckMessage.Builder msgAck;

    public int getReSendNum() {
        return reSendNum;
    }

    public void setReSendNum(int reSendNum) {
        this.reSendNum = reSendNum;
    }

    public long getFirstTimeSent() {
        return firstTimeSent;
    }

    public void setFirstTimeSent(long firstTimeSent) {
        this.firstTimeSent = firstTimeSent;
    }

    public MsgBean.UniversalMessage.Builder getMsg() {
        return msg;
    }

    public void setMsg(MsgBean.UniversalMessage.Builder msg) {
        this.msg = msg;
    }

    public MsgBean.AckMessage.Builder getMsgAck() {
        return msgAck;
    }

    public void setMsgAck(MsgBean.AckMessage.Builder msgAck) {
        this.msgAck = msgAck;
    }
}