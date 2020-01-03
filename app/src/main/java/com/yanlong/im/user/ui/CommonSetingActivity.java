package com.yanlong.im.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.utils.PasswordTextWather;

import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

public class CommonSetingActivity extends AppActivity {
    public final static String TITLE = "title"; //标题栏
    public final static String HINT = "hint"; //提醒
    public final static String CONTENT = "content";//传回内容
    public final static String REMMARK = "remark";
    public final static String REMMARK1 = "remark1";
    public final static String SETING = "seting"; //设置输入栏的内容
    public final static String TYPE_LINE = "type"; //默认0 单行 1 多行
    public final static String SIZE = "SIZE";//限制字数长度
    public final static String SPECIAL = "special";//特殊处理 1.常信号

    private HeadView mHeadView;
    private TextView mTvTitle;
    private EditText mEdContent;
    private TextView mTvContent;
    private int special;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int type = intent.getIntExtra(TYPE_LINE, 0);
        if (type == 0) {
            setContentView(R.layout.activity_common_seting);
        } else {
            setContentView(R.layout.activity_common_seting_multi);
        }
        initView();
        initEvent();

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void initView() {
        mHeadView = findViewById(R.id.headView);
        mTvTitle = findViewById(R.id.tv_title);
        mEdContent = findViewById(R.id.ed_content);
        mTvContent = findViewById(R.id.tv_content);
        mHeadView.getActionbar().setTxtRight("完成");
        Intent intent = getIntent();

        String title = intent.getStringExtra(TITLE);
        if (!TextUtils.isEmpty(title)) {
            mHeadView.getActionbar().setTitle(title);
        }

        String hint = intent.getStringExtra(HINT);
        if (!TextUtils.isEmpty(hint)) {
            mEdContent.setHint(hint);
        }

        String remark = intent.getStringExtra(REMMARK);
        if (!TextUtils.isEmpty(remark)) {
            mTvTitle.setText(remark);
        }

        String remark1 = intent.getStringExtra(REMMARK1);
        if (!TextUtils.isEmpty(remark1)) {
            mTvContent.setVisibility(View.VISIBLE);
            mTvContent.setText(remark1);
        } else {
            mTvContent.setVisibility(View.GONE);
            mTvContent.setText(remark1);
        }

        String seting = intent.getStringExtra(SETING);
        if (!TextUtils.isEmpty(seting)) {
            mEdContent.setText(seting);
        }

        int size = intent.getIntExtra(SIZE, 70);
        mEdContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(size)});

        special = intent.getIntExtra(SPECIAL, 0);
        switch (special) {
            case 1:
                mEdContent.addTextChangedListener(new PasswordTextWather(mEdContent,this));
                break;
        }
    }


    private void initEvent() {
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                String content = mEdContent.getText().toString();
                //群昵称可以设置空字符串(取默认名)、用户名设置和备注不可以用空字符串
                if(!TextUtils.isEmpty(mHeadView.getActionbar().getTitle())){
                    if(!mHeadView.getActionbar().getTitle().equals("我在本群的信息")){
                        if (!TextUtils.isEmpty(content) && TextUtils.isEmpty(content.trim())) {
                            ToastUtil.show(CommonSetingActivity.this, "不能用空字符");
                            return;
                        }
                    }else {
                        content = content.trim();//群昵称可以为空格，过滤空格并传""则取原来昵称
                        //截取前两位判断开头是否为emoji
                        if(content.length()>=2){
                            String emoji = content.substring(0,2);
                            if(StringUtil.ifContainEmoji(emoji)){
                                content = " "+content;
                            }
                        }
                    }
                }

                if(special == 1){
                    if(checkProduct()){
                        return;
                    }
                }
                Intent intent = new Intent();
                intent.putExtra(CONTENT, content);
                setResult(RESULT_OK, intent);
                onBackPressed();

            }
        });
    }

    private boolean checkProduct() {
        boolean isCheck = false;

        if (special == 1) {
            int size = mEdContent.getText().toString().length();
            String content = mEdContent.getText().toString();
            if (!TextUtils.isEmpty(content)) {
                String initial = content.substring(0, 1);
                char initialChat = initial.charAt(0);
                if ((initialChat >= 'A' && initialChat <= 'Z') || (initialChat >= 'a' && initialChat <= 'z')) {
                    isCheck = false;
                }else{
                    ToastUtil.show(context,"首位必须用英文字母");
                    isCheck = true;
                    return isCheck;
                }
            }

            if (size < 6) {
                ToastUtil.show(context, "不能少于六个字符");
                isCheck = true;
            }
        }
        return isCheck;
    }

}
