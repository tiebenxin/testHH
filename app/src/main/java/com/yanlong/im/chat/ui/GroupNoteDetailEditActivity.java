package com.yanlong.im.chat.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;

import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.AtMessage;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.databinding.ActivityGroupNoteDetailBinding;
import com.yanlong.im.databinding.ActivityGroupNoteDetailEditBinding;
import com.yanlong.im.utils.socket.SocketData;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.EventRefreshChat;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AlertYesNo;
import net.cb.cb.library.view.AppActivity;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @author Liszt
 * @date 2019/9/6
 * Description 群公告查看界面
 */
public class GroupNoteDetailEditActivity extends AppActivity {
    public static final String IS_OWNER = "is_owner";//是否是群主
    public static final String NOTE = "note";//群公告
    public final static String CONTENT = "content";//传回内容
    public final static String GID = "gid";//传回内容
    public final static String GROUP_NICK = "group_nick";//群主的群昵称


    private ActivityGroupNoteDetailEditBinding ui;
    private String note;
    private String gid;
    private String groupNick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_group_note_detail_edit);
        Intent intent = getIntent();
        boolean isOwner = true;
        groupNick = intent.getStringExtra(GROUP_NICK);
        gid = intent.getStringExtra(GID);
        note = intent.getStringExtra(NOTE);
        if (isOwner) {
            ui.etTxt.setVisibility(View.VISIBLE);
            ui.tvContent.setVisibility(View.GONE);
            ui.etTxt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});
            if (!TextUtils.isEmpty(note)) {
                ui.etTxt.setText(note);
            } else {
                ui.etTxt.setHint("修改群公告");
            }
            ui.headView.getActionbar().setTxtRight("完成");
        } else {
            ui.etTxt.setVisibility(View.GONE);
            ui.tvContent.setVisibility(View.VISIBLE);
            ui.tvContent.setText(note);
        }
        initEvent();
    }

    private void initEvent() {
        ui.headView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                String content = ui.etTxt.getText().toString();
                if (!TextUtils.isEmpty(content) && TextUtils.isEmpty(content.trim())) {
                    ToastUtil.show(GroupNoteDetailEditActivity.this, "不能用空字符");
                    return;
                }
                content = content.trim();
                if (note.equals(content)) {//未修改，不返回
                    Intent intent = new Intent();
                    intent.putExtra(NOTE, content.trim());
                    setResult(RESULT_CANCELED, intent);
                    onBackPressed();

                } else {
//                    Intent intent = new Intent();
//                    intent.putExtra(CONTENT, content.trim());
//                    setResult(RESULT_OK, intent);
                    if (groupNick == null) {
                        groupNick = "";
                    }
                    changeGroupAnnouncement(gid, content, groupNick);
                }

            }
        });
    }

    private void changeGroupAnnouncement(final String gid, final String announcement, String masterName) {
        if (TextUtils.isEmpty(gid)) {
            ToastUtil.show(this, "群信息为空");
            return;
        }

        AlertYesNo alertYesNo = new AlertYesNo();
        alertYesNo.init(GroupNoteDetailEditActivity.this, "提示", "该公告会通知全部成员，是否发布？", "发布", "取消", new AlertYesNo.Event() {
            @Override
            public void onON() {

            }

            @Override
            public void onYes() {
                new MsgAction().changeGroupAnnouncement(gid, announcement, masterName, new CallBack<ReturnBean>() {
                    @Override
                    public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                        if (response.body() == null) {
                            return;
                        }
                        ToastUtil.show(getContext(), response.body().getMsg());
                        if (response.body().isOk()) {
                            updateAndGetGroup(announcement);
                            Intent intent = new Intent();
                            intent.putExtra(CONTENT, announcement);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });
            }
        });
        alertYesNo.show();


    }

    private void updateAndGetGroup(String note) {
        if (!TextUtils.isEmpty(gid)) {
            MsgDao dao = new MsgDao();
            Group group = dao.groupNumberGet(gid);
            group.setAnnouncement(note);
            dao.groupNumberSave(group);
        }
    }

    private void createAndSaveMsg() {
        if (TextUtils.isEmpty(gid)) {
            return;
        }
        AtMessage atMessage = SocketData.
                createAtMessage(SocketData.getUUID(), "@所有人 \r\n" + note, ChatEnum.EAtType.ALL, null);
        MsgAllBean bean = SocketData.createMessageBean(null, gid, ChatEnum.EMessageType.AT, ChatEnum.ESendStatus.NORMAL, -1L, atMessage);
        if (bean != null) {
            SocketData.saveMessage(bean);
        }
    }
}
