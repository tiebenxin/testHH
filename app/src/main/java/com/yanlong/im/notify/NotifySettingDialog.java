package com.yanlong.im.notify;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.yanlong.im.R;

import net.cb.cb.library.base.BaseDialog;

/**
 * @author Liszt
 * @date 2019/8/29
 * Description 设置允许通知
 */
public class NotifySettingDialog extends BaseDialog {


    private TextView tv_content, tv_title;

    public NotifySettingDialog(Context context, int theme) {
        super(context, theme);
    }

    public NotifySettingDialog(Context context) {
        super(context);
    }


    @Override
    public void initView() {
        setContentView(R.layout.dialog_setting_notify);
        tv_content = findViewById(R.id.tv_content);
        tv_title = findViewById(R.id.tv_title);
    }

    public void setTitle(String t) {
        tv_title.setText(t);
    }

    public void setMessage(String msg) {
        tv_content.setText(msg);
    }

    @Override
    public void processClick(View view) {

    }
}
