package com.yanlong.im.chat.ui.cell;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.MsgAllBean;

public class ChatCellUnrecognized extends ChatCellBase {

    private TextView tv_content;

    protected ChatCellUnrecognized(Context context, View view, ICellEventListener listener, MessageAdapter adapter) {
        super(context, view, listener, adapter);
    }

    @Override
    protected void initView() {
        super.initView();
        tv_content = getView().findViewById(R.id.tv_content);
    }

    @Override
    protected void showMessage(MsgAllBean message) {
        super.showMessage(message);
        tv_content.setText("此版本不支持该功能");

    }

}
