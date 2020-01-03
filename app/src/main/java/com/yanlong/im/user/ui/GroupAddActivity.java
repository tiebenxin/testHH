package com.yanlong.im.user.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.user.action.UserAction;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

import retrofit2.Call;
import retrofit2.Response;

public class GroupAddActivity extends AppActivity {
    private Button btn_commit_groupadd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_add);
        initView();
        initEvent();
    }

    private HeadView headView_groupadd;
    private EditText ed_content_groupadd;

    private void initView() {
        headView_groupadd = findViewById(R.id.headView_groupadd);
        btn_commit_groupadd = findViewById(R.id.btn_commit_groupadd);
        ed_content_groupadd = findViewById(R.id.ed_content_groupadd);
    }


    private void initEvent() {
        headView_groupadd.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        btn_commit_groupadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textContetn = ed_content_groupadd.getText().toString();
                if (TextUtils.isEmpty(textContetn) || textContetn.length() < 8) {
                    ToastUtil.show(getApplicationContext(), "请输入申请描述不少于8个字");
                    return;
                }
                alert.show();
                LogUtil.getLog().e("TAG", getIntent().getStringExtra("gid") + "---------------" + UserAction.getMyInfo().getPhone());
                new MsgAction().changeGroupLimit(getIntent().getStringExtra("gid"), textContetn, UserAction.getMyInfo().getPhone(), new CallBack<ReturnBean>() {
                    @Override
                    public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                        alert.dismiss();
                        if (response.body() != null) {
                            if (response.body().getCode() == 0) {
                                ToastUtil.show(GroupAddActivity.this, "申请成功!请等待审核");
                                finish();
                            } else {
                                ToastUtil.show(GroupAddActivity.this, response.body().getMsg());
                            }
                        }

                    }

                    @Override
                    public void onFailure(Call<ReturnBean> call, Throwable t) {
                        alert.dismiss();
                        LogUtil.getLog().e("TAG", t.getMessage());
                    }
                });
            }
        });

    }
}
