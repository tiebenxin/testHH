package com.yanlong.im.chat.ui.cell;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.VoiceMessage;
import com.yanlong.im.chat.ui.view.VoiceView;
import com.yanlong.im.utils.audio.AudioPlayManager;

/*
 * 语音
 * */
public class ChatCellVoice extends ChatCellBase {

    private VoiceView v_voice;
    private VoiceMessage voiceMessage;
    private Uri uri;

    protected ChatCellVoice(Context context, View view, ICellEventListener listener, MessageAdapter adapter) {
        super(context, view, listener, adapter);
    }

    @Override
    protected void initView() {
        super.initView();
        v_voice = getView().findViewById(R.id.v_voice);
    }

    @Override
    protected void showMessage(MsgAllBean message) {
        super.showMessage(message);
        voiceMessage = message.getVoiceMessage();
        if (voiceMessage != null) {
            uri = Uri.parse(voiceMessage.getUrl());
            v_voice.init(message.isMe(), voiceMessage.getTime(), message.isRead(), AudioPlayManager.getInstance().isPlay(uri), voiceMessage.getPlayStatus());
        }
    }

    @Override
    public void onBubbleClick() {
        super.onBubbleClick();
        if (mCellListener != null) {
            mCellListener.onEvent(ChatEnum.ECellEventType.VOICE_CLICK, model, voiceMessage, currentPosition);
        }

    }

    private void updateRead() {
        //设置为已读
        if (model.isRead() == false) {
            MsgAction action = new MsgAction();
            action.msgRead(model.getMsg_id(), true);
            model.setRead(true);
            v_voice.init(model.isMe(), voiceMessage.getTime(), model.isRead(), AudioPlayManager.getInstance().isPlay(uri), voiceMessage.getPlayStatus());
        }
    }

    void updateVoice() {
        voiceMessage = model.getVoiceMessage();
        if (voiceMessage != null) {
            uri = Uri.parse(voiceMessage.getUrl());
            v_voice.init(model.isMe(), voiceMessage.getTime(), model.isRead(), AudioPlayManager.getInstance().isPlay(uri), voiceMessage.getPlayStatus());
        }
    }
}
