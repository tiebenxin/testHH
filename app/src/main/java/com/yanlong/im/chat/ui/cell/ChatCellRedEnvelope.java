package com.yanlong.im.chat.ui.cell;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.RedEnvelopeMessage;
import com.yanlong.im.chat.bean.TransferMessage;
import com.yanlong.im.utils.socket.MsgBean;

/*
 * 红包消息及转账消息
 * */
public class ChatCellRedEnvelope extends ChatCellBase {

    private TextView tv_rb_title, tv_rb_info, tv_rb_type;
    private ImageView iv_rb_state, iv_rb_icon;
    private RedEnvelopeMessage redEnvelopeMessage;
    private TransferMessage transfer;

    protected ChatCellRedEnvelope(Context context, View view, ICellEventListener listener, MessageAdapter adapter) {
        super(context, view, listener, adapter);
    }

    @Override
    protected void initView() {
        super.initView();
        tv_rb_title = getView().findViewById(R.id.tv_rb_title);
        tv_rb_info = getView().findViewById(R.id.tv_rb_info);
        tv_rb_type = getView().findViewById(R.id.tv_rb_type);
        iv_rb_state = getView().findViewById(R.id.iv_rb_state);
        iv_rb_icon = getView().findViewById(R.id.iv_rb_icon);
    }

    @Override
    protected void showMessage(MsgAllBean message) {
        super.showMessage(message);
        boolean invalid = false;
        String title = "";
        String info = "";
        String typeName = "";
        int typeIcon = R.color.transparent;

        if (message.getMsg_type() == ChatEnum.EMessageType.RED_ENVELOPE) {
            redEnvelopeMessage = message.getRed_envelope();
            invalid = redEnvelopeMessage.getIsInvalid() == 0 ? false : true;
            title = redEnvelopeMessage.getComment();
            if (invalid) {
                info = "已领取";
            } else {
                info = "领取红包";
            }
            if (redEnvelopeMessage.getRe_type().intValue() == MsgBean.RedEnvelopeType.MFPAY_VALUE) {
                typeName = "云红包";
            } else {
                typeName = "支付宝";
            }
        } else if (message.getMsg_type() == ChatEnum.EMessageType.TRANSFER) {
            transfer = message.getTransfer();
            invalid = false;
            title = transfer.getTransaction_amount() + "元";
            info = transfer.getComment();
            typeName = "好友转账";

        }
        setMessage(invalid, title, info, typeName, typeIcon);
    }

    @Override
    public void onBubbleClick() {
        super.onBubbleClick();
        if (mCellListener != null) {
            if (messageType == ChatEnum.EMessageType.RED_ENVELOPE) {
                mCellListener.onEvent(ChatEnum.ECellEventType.RED_ENVELOPE_CLICK, model, redEnvelopeMessage);
            } else if (messageType == ChatEnum.EMessageType.TRANSFER) {
                mCellListener.onEvent(ChatEnum.ECellEventType.TRANSFER_CLICK, model, transfer);
            }
        }
    }

    private void setMessage(boolean invalid, String title, String info, String typeName, int typeIcon) {
        if (invalid) {//失效
            iv_rb_state.setImageResource(R.mipmap.ic_rb_zfb_n);
//            if (model.isMe()) {
//                bubbleLayout.setBackgroundResource(R.drawable.bg_chat_me_rp_h);
//            } else {
//                bubbleLayout.setBackgroundResource(R.drawable.bg_chat_other_rp_h);
//            }
            bubbleLayout.setBackgroundResource(model.isMe() ? R.drawable.selector_rp_h_me_touch : R.drawable.selector_rp_h_other_touch);


        } else {
            iv_rb_state.setImageResource(R.mipmap.ic_rb_zfb_un);
//            if (model.isMe()) {
//                bubbleLayout.setBackgroundResource(R.drawable.bg_chat_me_rp);
//            } else {
//                bubbleLayout.setBackgroundResource(R.drawable.bg_chat_other_rp);
//            }
            bubbleLayout.setBackgroundResource(model.isMe() ? R.drawable.selector_rp_me_touch : R.drawable.selector_rp_other_touch);

        }
        tv_rb_title.setText(title);
        tv_rb_info.setText(info);
        tv_rb_type.setText(typeName);
        iv_rb_icon.setImageResource(typeIcon);

    }
}
