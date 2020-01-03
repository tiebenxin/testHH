package com.yanlong.im.chat.ui.cell;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.MsgAllBean;

/*
 * 通知消息, 撤回消息
 * */
public class ChatCellLock extends ChatCellBase {

    private TextView tv_content;

    protected ChatCellLock(Context context, View view, ICellEventListener listener, MessageAdapter adapter) {
        super(context, view, listener, adapter);
    }

    @Override
    protected void initView() {
        super.initView();
        tv_content = getView().findViewById(R.id.tv_broadcast);
    }

    @Override
    protected void showMessage(MsgAllBean message) {
        super.showMessage(message);
        if (messageType == ChatEnum.EMessageType.NOTICE) {
            tv_content.setText(Html.fromHtml(message.getMsgNotice().getNote()));
        } else if (messageType == ChatEnum.EMessageType.MSG_CANCEL) {
            tv_content.setText(Html.fromHtml(message.getMsgCancel().getNote()));

        }
    }
}
