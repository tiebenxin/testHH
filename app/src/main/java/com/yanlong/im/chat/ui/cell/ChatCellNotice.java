package com.yanlong.im.chat.ui.cell;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.MsgNotice;
import com.yanlong.im.utils.HtmlTransitonUtils;

/*
 * 通知消息, 撤回消息
 * */
public class ChatCellNotice extends ChatCellBase {

    private TextView tv_content;
    private ImageView iv_icon;

    protected ChatCellNotice(Context context, View view, ICellEventListener listener, MessageAdapter adapter) {
        super(context, view, listener, adapter);
    }

    @Override
    protected void initView() {
        super.initView();
        tv_content = getView().findViewById(R.id.tv_broadcast);
        iv_icon = getView().findViewById(R.id.iv_broadcast);
    }

    @Override
    protected void showMessage(MsgAllBean message) {
        super.showMessage(message);
        if (messageType == ChatEnum.EMessageType.NOTICE) {
            if (message.getMsgNotice().getMsgType() == MsgNotice.MSG_TYPE_DEFAULT || message.getMsgNotice().getMsgType() == 17) {
                tv_content.setText(Html.fromHtml(message.getMsgNotice().getNote()));
            } else {
                tv_content.setText(new HtmlTransitonUtils().getSpannableString(mContext, message.getMsgNotice().getNote(), message.getMsgNotice().getMsgType()));
            }

            //8.22 如果是红包消息类型则显示红包图
            if (message.getMsgNotice().getMsgType() != null && (message.getMsgNotice().getMsgType() == 7 || message.getMsgNotice().getMsgType() == 8 || message.getMsgNotice().getMsgType() == 17)) {
                iv_icon.setVisibility(View.VISIBLE);
            } else {
                iv_icon.setVisibility(View.GONE);
            }
        } else if (messageType == ChatEnum.EMessageType.MSG_CANCEL) {
            tv_content.setText(Html.fromHtml(message.getMsgCancel().getNote()));
            if (message.getMsgCancel().getMsgType() == MsgNotice.MSG_TYPE_DEFAULT) {
                tv_content.setText(Html.fromHtml(message.getMsgCancel().getNote()));
            } else {
                tv_content.setText(new HtmlTransitonUtils().getSpannableString(mContext, message.getMsgCancel().getNote(), message.getMsgCancel().getMsgType()));
            }
            iv_icon.setVisibility(View.GONE);
        }
    }
}
