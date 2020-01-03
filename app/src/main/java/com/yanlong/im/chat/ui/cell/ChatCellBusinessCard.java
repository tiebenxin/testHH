package com.yanlong.im.chat.ui.cell;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.BusinessCardMessage;
import com.yanlong.im.chat.bean.MsgAllBean;

public class ChatCellBusinessCard extends ChatCellBase {

    private TextView tv_title;
    private ImageView iv_avatar_card;
    private TextView tv_info;
    private BusinessCardMessage cardMessage;

    protected ChatCellBusinessCard(Context context, View view, ICellEventListener listener, MessageAdapter adapter) {
        super(context, view, listener, adapter);
    }

//    protected ChatCellBusinessCard(Context context, ChatEnum.EChatCellLayout cellLayout, ICellEventListener listener, MessageAdapter adapter, ViewGroup viewGroup) {
//        super(context, cellLayout, listener, adapter, viewGroup);
//    }

    @Override
    protected void initView() {
        super.initView();
        tv_title = getView().findViewById(R.id.tv_title);
        iv_avatar_card = getView().findViewById(R.id.iv_avatar_card);
        tv_info = getView().findViewById(R.id.tv_info);
    }

    @Override
    protected void showMessage(MsgAllBean message) {
        super.showMessage(message);
        cardMessage = message.getBusiness_card();
        showCard(cardMessage);
    }

    private void showCard(BusinessCardMessage cardMessage) {
        if (cardMessage == null) {
            return;
        }
        loadCardAvatar();
        tv_title.setText(cardMessage.getNickname());
        tv_info.setText(cardMessage.getComment());
    }

    /*
     * 加载发送者头像
     * */
    private void loadCardAvatar() {
        if (mContext == null || iv_avatar_card == null) {
            return;
        }
        RequestOptions options = new RequestOptions();
        options.centerCrop();
        Glide.with(mContext)
                .load(cardMessage.getAvatar())
                .apply(options)
                .into(iv_avatar_card);

    }

    @Override
    public void onBubbleClick() {
        super.onBubbleClick();
        if (mCellListener != null && model != null) {
            mCellListener.onEvent(ChatEnum.ECellEventType.CARD_CLICK, model, cardMessage);
        }
    }
}
