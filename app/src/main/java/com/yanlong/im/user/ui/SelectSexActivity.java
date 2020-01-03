package com.yanlong.im.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yanlong.im.R;

import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

public class SelectSexActivity extends AppActivity implements View.OnClickListener {
    public static final String SEX = "sex";
    private HeadView mHeadView;
    private LinearLayout mLlMan;
    private LinearLayout mLlWoman;
    private String sex = "";
    private ImageView mIvMan;
    private ImageView mIvWoman;
    private int type; //1 男 2女

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sex);
        initView();
        initEvent();
    }

    private void initView() {
        mHeadView = findViewById(R.id.headView);
        mLlMan = findViewById(R.id.ll_man);
        mLlWoman = findViewById(R.id.ll_woman);
        mIvMan = findViewById(R.id.iv_man);
        mIvWoman = findViewById(R.id.iv_woman);
        mHeadView.getActionbar().setTxtRight("完成");

        type = getIntent().getIntExtra(SEX,0);
        selectSex(type);
    }

    private void initEvent() {
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                Intent intent = new Intent();
                intent.putExtra("content", sex);
                setResult(RESULT_OK, intent);
                onBackPressed();
            }
        });
        mLlMan.setOnClickListener(this);
        mLlWoman.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_man:
                selectSex(1);
                break;
            case R.id.ll_woman:
                selectSex(2);
                break;
        }
    }


    private void selectSex(int sexType) {
        switch (sexType) {
            case 1:
                mIvMan.setImageResource(R.drawable.bg_cheack_green_s);
                mIvWoman.setImageResource(R.drawable.bg_cheack_green_e);
                sex = "男";
                break;
            case 2:
                mIvWoman.setImageResource(R.drawable.bg_cheack_green_s);
                mIvMan.setImageResource(R.drawable.bg_cheack_green_e);
                sex = "女";
                break;
            default:
                mIvWoman.setImageResource(R.drawable.bg_cheack_green_e);
                mIvMan.setImageResource(R.drawable.bg_cheack_green_e);
                sex="";
                break;
        }
    }

}
