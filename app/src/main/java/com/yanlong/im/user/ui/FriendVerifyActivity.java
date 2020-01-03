package com.yanlong.im.user.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;

import com.yanlong.im.R;
import com.yanlong.im.databinding.ActivityFriendVerifyBinding;
import com.yanlong.im.user.action.UserAction;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @author Liszt
 * @date 2019/9/29
 * Description 好友验证页面
 */
public class FriendVerifyActivity extends AppActivity {
    public final static String CONTENT = "content";
    public final static String USER_ID = "user_id";

    private ActivityFriendVerifyBinding ui;
    private Long userId;
    private String content;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_friend_verify);
        Intent intent = getIntent();
        content = intent.getStringExtra(CONTENT);
        userId = intent.getLongExtra(USER_ID, -1L);
        if (!TextUtils.isEmpty(content)) {
            ui.etTxt.setText(content);
        }

        ui.headView.getActionbar().setTxtRight("发送");
        ui.headView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                setResult(RESULT_CANCELED);
                finish();
            }

            @Override
            public void onRight() {
                String t = ui.etTxt.getText().toString();
                if (!TextUtils.isEmpty(t)) {
                    content = t;
                }
                taskAddFriend(userId, content);
            }
        });
    }

    private void taskAddFriend(Long userId, String sayHi) {
        if (userId <= 0) {
            ToastUtil.show(this, "无效用户");
        }
        new UserAction().friendApply(userId, sayHi, null, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                ToastUtil.show(FriendVerifyActivity.this, response.body().getMsg());
                if (response.body().isOk()) {
                    ToastUtil.show(context,"好友请求发送成功");
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }
}
