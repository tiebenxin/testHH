package com.yanlong.im.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.ReadDestroyBean;
import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.user.ui.UserInfoActivity;
import com.yanlong.im.utils.DaoUtil;
import com.yanlong.im.utils.DestroyTimeView;
import com.yanlong.im.utils.GlideOptionsUtil;
import com.yanlong.im.utils.ReadDestroyUtil;
import com.yanlong.im.utils.UserUtil;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.CloseActivityEvent;
import net.cb.cb.library.bean.EventExitChat;
import net.cb.cb.library.bean.EventIsShowRead;
import net.cb.cb.library.bean.EventRefreshChat;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.manager.Constants;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AlertYesNo;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 聊天详情
 */
public class ChatInfoActivity extends AppActivity {
    public static final String AGM_FUID = "fuid";
    private Long fuid;

    private HeadView headView;
    private ActionbarView actionbar;
    private RecyclerView topListView;
    private LinearLayout viewLog;
    private LinearLayout viewTop;
    private LinearLayout read_destroy_ll;
    private CheckBox ckTop;
    private LinearLayout viewDisturb;
    private CheckBox ckDisturb;
    private LinearLayout viewLogClean;
    //  private Session session;
    private UserInfo fUserInfo;
    boolean isSessionChange = false;

    private int destroyTime;

    private ReadDestroyUtil readDestroyUtil = new ReadDestroyUtil();
    private LinearLayout viewDestroyTime;
    private TextView tvDestroyTime;
    private CheckBox ckSetRead;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_info);
        findViews();
        initEvent();
        initData();
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }



    //自动寻找控件
    private void findViews() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        topListView = findViewById(R.id.topListView);
        viewLog = findViewById(R.id.view_log);
        viewTop = findViewById(R.id.view_top);
        ckTop = findViewById(R.id.ck_top);
        viewDisturb = findViewById(R.id.view_disturb);
        ckDisturb = findViewById(R.id.ck_disturb);
        viewLogClean = findViewById(R.id.view_log_clean);
        viewDestroyTime = findViewById(R.id.view_destroy_time);
        tvDestroyTime = findViewById(R.id.tv_destroy_time);
        ckSetRead = findViewById(R.id.ck_set_read);
        read_destroy_ll = findViewById(R.id.read_destroy_ll);
    }


    //自动生成的控件事件
    private void initEvent() {
        fuid = getIntent().getLongExtra(AGM_FUID, 0);
        taskGetInfo();
        if (Constants.CX888_UID.equals(fuid)) {
            read_destroy_ll.setVisibility(View.GONE);
        }

        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });

        //顶部处理
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        topListView.setLayoutManager(linearLayoutManager);
        topListView.setAdapter(new RecyclerViewTopAdapter());

        ckTop.setChecked(fUserInfo.getIstop() == 1);
        ckTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fUserInfo.setIstop(isChecked ? 1 : 0);
                taskSaveInfo();
                taskUpSwitch(null, fUserInfo.getIstop());
            }
        });

        ckDisturb.setChecked(fUserInfo.getDisturb() == 1);
        ckDisturb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fUserInfo.setDisturb(isChecked ? 1 : 0);
                taskSaveInfo();
                taskUpSwitch(fUserInfo.getDisturb(), null);
            }
        });

        ckSetRead.setChecked(fUserInfo.getMyRead() == 1);
        ckSetRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fUserInfo.setMyRead(isChecked ? 1 : 0);
                taskSaveInfo();
                taskFriendsSetRead(fuid, isChecked ? 1 : 0);
            }
        });

        viewLogClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertYesNo alertYesNo = new AlertYesNo();
                alertYesNo.init(ChatInfoActivity.this, "提示", "确定清空聊天记录？", "确定", "取消", new AlertYesNo.Event() {
                    @Override
                    public void onON() {

                    }

                    @Override
                    public void onYes() {
                        taskDelMsg();
                    }
                });
                alertYesNo.show();

            }
        });

        viewLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SearchMsgActivity.class)
                        .putExtra(SearchMsgActivity.AGM_FUID, fuid)
                );
            }
        });

        viewDestroyTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DestroyTimeView destroyTimeView = new DestroyTimeView(ChatInfoActivity.this);
                destroyTimeView.initView();
                destroyTimeView.setPostion(destroyTime);
                destroyTimeView.setListener(new DestroyTimeView.OnClickItem() {
                    @Override
                    public void onClickItem(String content, int survivaltime) {
                        if (ChatInfoActivity.this.destroyTime != survivaltime) {
                            destroyTime = survivaltime;
                            tvDestroyTime.setText(content);
                            taskSurvivalTime(fuid, survivaltime);
                        }
                    }
                });
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        setResult(ChatActivity.REQ_REFRESH);
    }


    private void initData() {
        readDestroyUtil = new ReadDestroyUtil();
        UserInfo userInfo = userDao.findUserInfo(fuid);
        destroyTime = userInfo.getDestroy();
        String content = readDestroyUtil.getDestroyTimeContent(destroyTime);
        tvDestroyTime.setText(content);
    }


    //自动生成RecyclerViewAdapter
    class RecyclerViewTopAdapter extends RecyclerView.Adapter<RecyclerViewTopAdapter.RCViewTopHolder> {

        @Override
        public int getItemCount() {
            // return listDataTop == null ? 0 : listDataTop.size();
            if(UserUtil.isSystemUser(fuid)){
                return 1;
            }else{
                return 2;
            }
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(RCViewTopHolder holder, int position) {
            //listDataTop.get(position)
            UserInfo userInfo = null;
            switch (position) {
                case 0:
                    userInfo = fUserInfo;


                    // holder.imgHead.setImageURI(Uri.parse("" + userInfo.getHead()));
                    Glide.with(context).load(userInfo.getHead())
                            .apply(GlideOptionsUtil.headImageOptions()).into(holder.imgHead);

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getContext(), UserInfoActivity.class)
                                    .putExtra(UserInfoActivity.ID, fUserInfo.getUid()));
                        }
                    });
                    break;
                case 1:

                    holder.imgHead.setImageResource(R.mipmap.ic_group_a);

                    holder.imgHead.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (fUserInfo.getuType() == 2) {//是好友
                                finish();
                                EventBus.getDefault().post(new EventExitChat());

                                startActivity(new Intent(getContext(), GroupCreateActivity.class).putExtra(GroupCreateActivity.AGM_SELECT_UID, "" + fUserInfo.getUid()));
                            }
                        }
                    });
                    break;
            }

        }


        //自动寻找ViewHold
        @Override
        public RCViewTopHolder onCreateViewHolder(ViewGroup view, int i) {
            RCViewTopHolder holder = new RCViewTopHolder(inflater.inflate(R.layout.item_group_create_top, view, false));
            return holder;
        }


        //自动生成ViewHold
        public class RCViewTopHolder extends RecyclerView.ViewHolder {
            private ImageView imgHead;

            //自动寻找ViewHold
            public RCViewTopHolder(View convertView) {
                super(convertView);
                imgHead = convertView.findViewById(R.id.img_head);
            }

        }
    }

    private MsgDao msgDao = new MsgDao();
    private MsgAction msgAction = new MsgAction();
    private UserDao userDao = new UserDao();

    //获取会话和对方信息
    private void taskGetInfo() {
       /* session = DaoUtil.findOne(Session.class, "from_uid", fuid);
        if (session == null) {
            session = msgDao.sessionCreate(null, fuid);
        }*/
        fUserInfo = DaoUtil.findOne(UserInfo.class, "uid", fuid);
        if (fUserInfo != null && fUserInfo.getuType() != 2) {//非好友不能设置开关等
            ckDisturb.setEnabled(false);
            ckTop.setEnabled(false);

        } else {
            ckDisturb.setEnabled(true);
            ckTop.setEnabled(true);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        taskGetInfo();
    }

    //更新配置
    private void taskSaveInfo() {
        DaoUtil.update(fUserInfo);
    }

    private void taskDelMsg() {
        msgDao.msgDel(fuid, null);
        EventBus.getDefault().post(new EventRefreshChat());
        MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.PRIVATE, fuid, "", CoreEnum.ESessionRefreshTag.SINGLE, null);
        ToastUtil.show(ChatInfoActivity.this, "删除成功");
    }

    /*
     * 置顶和免打扰
     * */
    private void taskUpSwitch(Integer isMute, Integer istop) {
        msgAction.sessionSwitch(fuid, isMute, istop, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {
                    Session session = null;
                    if (isMute == null && istop != null) {
                        session = msgDao.updateUserSessionTop(fuid, istop);
//                        msgDao.updateUserTop(fuid, istop.intValue()); TODO 消息列表没数据时会报java.lang.IllegalStateException异常
                    } else if (isMute != null && istop == null) {
                        session = msgDao.updateUserSessionDisturb(fuid, isMute);
//                        msgDao.updateUserDisturb(fuid, isMute.intValue());TODO 消息列表没数据时会报java.lang.IllegalStateException异常
                    }
                    MessageManager.getInstance().setMessageChange(true);
                    MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.PRIVATE, fuid, "", CoreEnum.ESessionRefreshTag.SINGLE, session, true);
                    MessageManager.getInstance().notifySwitchDisturb();
                } else {
                    ToastUtil.show(getContext(), response.body().getMsg());

                }
            }
        });
    }

    private void taskSurvivalTime(long friend, int survivalTime) {
        msgAction.setSurvivalTime(friend, survivalTime, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                super.onResponse(call, response);
                if (response.body() == null) {
                    return;
                }
                if (response.body().isOk()) {
                    userDao.updateReadDestroy(fuid, survivalTime);
                    msgDao.noteMsgAddSurvivaltime(fuid, null);
                }
            }
        });
    }


    /**
     * 设置已读
     */
    private void taskFriendsSetRead(long uid, int read) {
        new UserAction().friendsSetRead(uid, read, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                super.onResponse(call, response);
                if (response.body() == null) {
                    return;
                }
                EventBus.getDefault().post(new EventIsShowRead());
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setingReadDestroy(ReadDestroyBean bean) {
        if (bean.uid == fuid) {
            destroyTime = bean.survivaltime;
            String content = readDestroyUtil.getDestroyTimeContent(destroyTime);
            tvDestroyTime.setText(content);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void closeActivityEvent(CloseActivityEvent event) {
        if(event.type.contains("ChatInfoActivity")){
            finish();
        }
    }

}
