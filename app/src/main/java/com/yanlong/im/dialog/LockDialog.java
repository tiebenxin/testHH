package com.yanlong.im.dialog;

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
public class LockDialog extends BaseDialog {


    private TextView tv_content;

    public LockDialog(Context context, int theme) {
        super(context, theme);
    }

    public LockDialog(Context context) {
        super(context);
    }


    @Override
    public void initView() {
        setContentView(R.layout.dialog_lock_detail);
        tv_content = findViewById(R.id.tv_content);
    }

//    public void setTitle(String t) {
//        tv_title.setText(t);
//    }

    public void setMessage(String msg) {
        tv_content.setText(msg);
    }

    @Override
    public void processClick(View view) {

    }
}
