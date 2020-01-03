package com.yanlong.im.chat.ui.cell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.MsgAllBean;

import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.view.WebPageActivity;

import java.util.regex.Matcher;

/*
 * @author Liszt
 * Description  戳一下消息
 * */
public class ChatCellStamp extends ChatCellBase {

    private TextView tv_content;

    protected ChatCellStamp(Context context, View view, ICellEventListener listener, MessageAdapter adapter) {
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
        setText(message.getStamp().getComment());
    }

    private void setText(String msg) {
        if (!StringUtil.isNotNull(msg)) {
            return;
        }
        String textSource = "<font color='#079892'>戳一下　</font>" + msg;
        tv_content.setText(Html.fromHtml(textSource));
    }


}
