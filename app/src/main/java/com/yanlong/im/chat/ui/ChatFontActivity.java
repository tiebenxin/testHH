package com.yanlong.im.chat.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import com.yanlong.im.R;
import net.cb.cb.library.utils.SharedPreferencesUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.FontSizeView;

/***
 * 聊天字体设置
 */
public class ChatFontActivity extends AppActivity {
    private net.cb.cb.library.view.HeadView headView;
    private ActionbarView actionbar;
    private LinearLayout viewMe;
    private LinearLayout viewMe1;
    private android.support.v7.widget.AppCompatTextView txtMe1;
    private android.support.v7.widget.AppCompatTextView txtOt1;
    private FontSizeView seekBar;


    //自动寻找控件
    private void findViews() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        viewMe = findViewById(R.id.view_me);
        viewMe1 = findViewById(R.id.view_me_1);
        txtMe1 = findViewById(R.id.txt_me_1);
        txtOt1 = findViewById(R.id.txt_ot_1);
        seekBar = findViewById(R.id.seekBar);
    }

private Integer size;

    //自动生成的控件事件
    private void initEvent() {
        size = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.FONT_CHAT).get4Json(Integer.class);
        actionbar.setTxtRight("完成");
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                if(size!=null){
                    SharedPreferencesUtil util = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.FONT_CHAT);

                    util.save2Json(size.intValue());
                }
                onBackPressed();

            }
        });

        seekBar.setChangeCallbackListener(new FontSizeView.OnChangeCallbackListener() {
            @Override
            public void onChangeListener(int position) {

                 size = ((Float)(12 + (position * 3f))).intValue();
                setTextSize(size.intValue());
            }
        });


        if (size != null) {
            int p=(size-12)/3;
            seekBar.setDefaultPosition(p);
        }

    }


    private void setTextSize(int size) {
        txtMe1.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        txtOt1.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_font);
        findViews();
        initEvent();
    }
}
