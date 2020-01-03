package com.yanlong.im.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.AtMessage;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.ReadDestroyBean;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.user.ui.CommonSetingActivity;
import com.yanlong.im.user.ui.ComplaintActivity;
import com.yanlong.im.user.ui.GroupAddActivity;
import com.yanlong.im.user.ui.ImageHeadActivity;
import com.yanlong.im.user.ui.MyselfQRCodeActivity;
import com.yanlong.im.user.ui.UserInfoActivity;
import com.yanlong.im.utils.DestroyTimeView;
import com.yanlong.im.utils.GlideOptionsUtil;
import com.yanlong.im.utils.GroupHeadImageUtil;
import com.yanlong.im.utils.ReadDestroyUtil;
import com.yanlong.im.utils.socket.SocketData;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.CloseActivityEvent;
import net.cb.cb.library.bean.EventExitChat;
import net.cb.cb.library.bean.EventGroupChange;
import net.cb.cb.library.bean.EventRefreshChat;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.CallBack4Btn;
import net.cb.cb.library.utils.IntentUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.ViewUtils;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AlertYesNo;
import net.cb.cb.library.view.AppActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 群信息
 */
public class GroupInfoActivity extends AppActivity {
    public static final int GROUP_NAME = 1;
    public static final int GROUP_NICK = 1 << 1;
    public static final int GROUP_NOTE = 1 << 2;
    public static final int GROUP_MANAGER = 1 << 3;
    public static final String AGM_GID = "gid";
    private String gid;
    private static final int IMAGE_HEAD = 4000;
    private net.cb.cb.library.view.HeadView headView;
    private ActionbarView actionbar;
    private android.support.v7.widget.RecyclerView topListView;
    private LinearLayout viewGroupName, viewGroupImg;
    private LinearLayout viewGroupMore;
    private TextView txtGroupName;
    private LinearLayout viewGroupNick;
    private TextView txtGroupNick;
    private LinearLayout viewGroupQr;
    private LinearLayout viewGroupNote;
    private TextView txtGroupNote, txtNote;
    private LinearLayout viewLog;
    private LinearLayout viewTop;
    private CheckBox ckTop;
    private LinearLayout viewDisturb;
    private CheckBox ckDisturb;
    private LinearLayout viewGroupSave;
    private LinearLayout viewGroupManage;
    private CheckBox ckGroupSave;
    private LinearLayout viewGroupVerif;
    private LinearLayout viewClearChatRecord;
    private LinearLayout viewComplaint;
    private LinearLayout viewGroupAdd;
    private CheckBox ckGroupVerif;
    private Button btnDel;
    private Gson gson = new Gson();
    private Group ginfo;
    private boolean isSessionChange = false;
    public boolean isPercentage = true;// 大于等于400人显示增加群人数上限至1000人

    private int destroyTime;
    private LinearLayout viewDestroyTime;
    private TextView tvDestroyTime;
    private ReadDestroyUtil readDestroyUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        findViews();
    }

    //自动寻找控件
    private void findViews() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        topListView = findViewById(R.id.topListView);
        viewGroupName = findViewById(R.id.view_group_name);
        viewGroupMore = findViewById(R.id.view_group_more);
        txtGroupName = findViewById(R.id.txt_group_name);
        viewGroupNick = findViewById(R.id.view_group_nick);
        txtGroupNick = findViewById(R.id.txt_group_nick);
        viewGroupQr = findViewById(R.id.view_group_qr);
        viewGroupNote = findViewById(R.id.view_group_note);
        txtGroupNote = findViewById(R.id.txt_group_note);
        txtNote = findViewById(R.id.txt_note);
        viewLog = findViewById(R.id.view_log);
        viewTop = findViewById(R.id.view_top);
        ckTop = findViewById(R.id.ck_top);
        viewDisturb = findViewById(R.id.view_disturb);
        ckDisturb = findViewById(R.id.ck_disturb);
        viewGroupSave = findViewById(R.id.view_group_save);
        viewGroupManage = findViewById(R.id.view_group_manage);
        viewGroupImg = findViewById(R.id.view_group_img);
        viewGroupAdd = findViewById(R.id.view_group_add);

        ckGroupVerif = findViewById(R.id.ck_group_verif);
        viewGroupVerif = findViewById(R.id.view_group_verif);
        viewComplaint = findViewById(R.id.view_complaint);
        ckGroupSave = findViewById(R.id.ck_group_save);
        btnDel = findViewById(R.id.btn_del);
        viewClearChatRecord = findViewById(R.id.view_clear_chat_record);

        viewDestroyTime = findViewById(R.id.view_destroy_time);
        tvDestroyTime = findViewById(R.id.tv_destroy_time);
        readDestroyUtil = new ReadDestroyUtil();

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    //自动生成的控件事件
    private void initEvent() {
        gid = getIntent().getStringExtra(AGM_GID);
        taskGetInfo();
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });

        btnDel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertYesNo alertYesNo = new AlertYesNo();
                alertYesNo.init(GroupInfoActivity.this, "提示", "删除群后会删除群会话与群聊数据", "确定", "取消",
                        new AlertYesNo.Event() {
                            @Override
                            public void onON() {

                            }

                            @Override
                            public void onYes() {
                                taskExitGroup();
                            }
                        });
                alertYesNo.show();
            }
        });


        viewGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAdmin()) {
                    ToastUtil.show(getContext(), "非群主无法修改");
                    return;
                }
                Intent intent = new Intent(GroupInfoActivity.this, CommonSetingActivity.class);
                intent.putExtra(CommonSetingActivity.TITLE, "群聊名称");
                intent.putExtra(CommonSetingActivity.REMMARK, "群聊名称");
                intent.putExtra(CommonSetingActivity.HINT, "群聊名称");
                intent.putExtra(CommonSetingActivity.SIZE, 16);
                intent.putExtra(CommonSetingActivity.SETING, ginfo.getName());
                startActivityForResult(intent, GROUP_NAME);
            }
        });

        viewGroupNick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, CommonSetingActivity.class);
                intent.putExtra(CommonSetingActivity.TITLE, "我在本群的信息");
                intent.putExtra(CommonSetingActivity.REMMARK, "设置我在这个群里面的昵称");
                intent.putExtra(CommonSetingActivity.HINT, "群昵称");
                intent.putExtra(CommonSetingActivity.SIZE, 16);
                intent.putExtra(CommonSetingActivity.SETING, txtGroupNick.getText().toString());

                startActivityForResult(intent, GROUP_NICK);
            }
        });

        viewGroupNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ginfo.getMaster();
                if (isAdmin()) {
                    Intent intent = new Intent(GroupInfoActivity.this, GroupNoteDetailActivity.class);
                    intent.putExtra(GroupNoteDetailActivity.GID, gid);
                    intent.putExtra(GroupNoteDetailActivity.NOTE, ginfo.getAnnouncement());
                    intent.putExtra(GroupNoteDetailActivity.IS_OWNER, true);
                    intent.putExtra(GroupNoteDetailActivity.GROUP_NICK, ginfo.getMygroupName());
                    startActivityForResult(intent, GROUP_NOTE);
                } else {
                    String note = ginfo.getAnnouncement();
                    if (!TextUtils.isEmpty(note)) {
                        Intent intent = new Intent(GroupInfoActivity.this, GroupNoteDetailActivity.class);
                        intent.putExtra(GroupNoteDetailActivity.NOTE, ginfo.getAnnouncement());
                        intent.putExtra(GroupNoteDetailActivity.GID, gid);
                        intent.putExtra(GroupNoteDetailActivity.IS_OWNER, false);
                        startActivity(intent);
                    }
                }
            }
        });
        viewLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SearchMsgActivity.class)
                        .putExtra(SearchMsgActivity.AGM_GID, gid)
                );
            }
        });

        viewGroupMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  ToastUtil.show(getContext(),"更多");
                startActivity(new Intent(getContext(), GroupInfoMumberActivity.class).putExtra(AGM_GID, gid));
            }
        });

        viewGroupManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ViewUtils.isFastDoubleClick()) {
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString(GroupManageActivity.AGM_GID, gid);
                bundle.putBoolean(GroupManageActivity.IS_ADMIN, isAdmin());
                IntentUtil.gotoActivityForResult(GroupInfoActivity.this, GroupManageActivity.class, bundle, GROUP_MANAGER);
            }
        });

        viewGroupImg.setVisibility(View.VISIBLE);
        viewGroupImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent headIntent = new Intent(GroupInfoActivity.this, ImageHeadActivity.class);
                //todo 头像修改
                headIntent.putExtra(ImageHeadActivity.IMAGE_HEAD, ginfo.getAvatar());
                headIntent.putExtra("admin", isAdmin());
                headIntent.putExtra("groupSigle", true);
                headIntent.putExtra("gid", gid);
                startActivityForResult(headIntent, IMAGE_HEAD);
            }
        });

        viewClearChatRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertYesNo alertYesNo = new AlertYesNo();
                alertYesNo.init(GroupInfoActivity.this, "提示", "确定清空聊天记录？", "确定", "取消", new AlertYesNo.Event() {
                    @Override
                    public void onON() {

                    }

                    @Override
                    public void onYes() {
                        MsgDao msgDao = new MsgDao();
                        msgDao.msgDel(null, gid);
                        EventBus.getDefault().post(new EventRefreshChat());
                        MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, -1L, gid, CoreEnum.ESessionRefreshTag.SINGLE, null);
                        ToastUtil.show(GroupInfoActivity.this, "删除成功");
                    }
                });
                alertYesNo.show();

            }
        });

        viewComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, ComplaintActivity.class);
                intent.putExtra(ComplaintActivity.GID, gid);
                startActivity(intent);
            }
        });

        viewDestroyTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAdmin()) {
                    ToastUtil.show(context, "只有群主才能修改该选项");
                    return;
                }
                DestroyTimeView destroyTimeView = new DestroyTimeView(GroupInfoActivity.this);
                destroyTimeView.initView();
                destroyTimeView.setPostion(destroyTime);
                destroyTimeView.setListener(new DestroyTimeView.OnClickItem() {
                    @Override
                    public void onClickItem(String content, int survivaltime) {
                        if (destroyTime != survivaltime) {
                            destroyTime = survivaltime;
                            tvDestroyTime.setText(content);
                            changeSurvivalTime(gid, survivaltime);
                        }
                    }
                });
            }
        });

        viewGroupAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ViewUtils.isFastDoubleClick()) {
                    return;
                }
                Intent intent = new Intent(GroupInfoActivity.this, GroupAddActivity.class).putExtra("gid", gid);
                startActivity(intent);
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setingReadDestroy(ReadDestroyBean bean) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!isBackValue) {
            initEvent();
            isBackValue = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void initData() {
        //顶部处理
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        ckTop.setOnCheckedChangeListener(null);
        ckDisturb.setOnCheckedChangeListener(null);
        ckGroupSave.setOnCheckedChangeListener(null);
        ckGroupVerif.setOnCheckedChangeListener(null);

        topListView.setLayoutManager(gridLayoutManager);
        topListView.setAdapter(new RecyclerViewTopAdapter());
        viewGroupVerif.setVisibility(View.GONE);
        txtGroupName.setText(TextUtils.isEmpty(ginfo.getName()) ? "未设置" : ginfo.getName());
        if (StringUtil.isNotNull(ginfo.getMygroupName())) {
            txtGroupNick.setText(ginfo.getMygroupName());
        } else {
            if (UserAction.getMyInfo() != null) {
                txtGroupNick.setText(UserAction.getMyInfo().getName());
            }
        }
        if (!isPercentage) {
            viewGroupAdd.setVisibility(View.GONE);
        }
        ckDisturb.setChecked(ginfo.getNotNotify() == 1);
        ckGroupSave.setChecked(ginfo.getSaved() == 1);
        ckTop.setChecked(ginfo.getIsTop() == 1);


        ckTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                taskSetState(gid, isChecked ? 1 : 0, null, null, null);
            }
        });
        ckDisturb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                taskSetStateDisturb(gid, null, isChecked ? 1 : 0, null, null);
            }
        });
        ckGroupSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                taskSetStateGroupSave(gid, null, null, isChecked ? 1 : 0, null);
            }
        });

        //开启群验证
        ckGroupVerif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                taskSetStateGroupVerif(gid, null, null, null, isChecked ? 1 : 0);
            }
        });

        viewGroupQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MyselfQRCodeActivity.class)
                        .putExtra(MyselfQRCodeActivity.TYPE, 1)
                        .putExtra(MyselfQRCodeActivity.GROUP_NAME, /*ginfo.getName()*/msgDao.getGroupName(gid))
                        .putExtra(MyselfQRCodeActivity.GROUP_HEAD, ginfo.getAvatar())
                        .putExtra(MyselfQRCodeActivity.GROUP_ID, ginfo.getGid())
                );
            }
        });


        destroyTime = ginfo.getSurvivaltime();
        String content = readDestroyUtil.getDestroyTimeContent(destroyTime);
        tvDestroyTime.setText(content);
    }

    private List<MemberUser> listDataTop = new ArrayList<>();

    //自动生成RecyclerViewAdapter
    class RecyclerViewTopAdapter extends RecyclerView.Adapter<RecyclerViewTopAdapter.RCViewTopHolder> {

        @Override
        public int getItemCount() {

            return listDataTop == null ? 0 : listDataTop.size();
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(final RCViewTopHolder holder, int position) {

            //6.15加标识
            final MemberUser number = listDataTop.get(position);
            if (number != null) {
                //holder.imgHead.setImageURI(Uri.parse("" + number.getHead()));
                Glide.with(context).load(number.getHead())
                        .apply(GlideOptionsUtil.headImageOptions()).into(holder.imgHead);

                holder.txtName.setText("" + number.getShowName());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (number.getUid() == UserAction.getMyId().longValue()) {
                            return;
                        }
                        boolean value = false;
                        if (isAdmin()) {
                            value = true;
                        } else {
                            if (isAdministrators(UserAction.getMyId())) {
                                value = !isAdministrators(number.getUid());
                            }
                        }
                        startActivity(new Intent(getContext(), UserInfoActivity.class)
                                .putExtra(UserInfoActivity.ID, number.getUid())
                                .putExtra(UserInfoActivity.JION_TYPE_SHOW, 1)
                                .putExtra(UserInfoActivity.GID, gid)
                                .putExtra(UserInfoActivity.IS_GROUP, true)
                                .putExtra(UserInfoActivity.IS_ADMINS, value)
                                .putExtra(UserInfoActivity.MUC_NICK, number.getMembername()));

                    }
                });
                if (ginfo.getMaster().equals("" + number.getUid())) {
                    holder.txtMain.setVisibility(View.VISIBLE);
                    holder.txtMain.setBackgroundResource(R.drawable.shape_circle_head_yellow);
                    holder.txtMain.setText("群主");
                } else {
                    if (isAdministrators(number.getUid())) {
                        holder.txtMain.setVisibility(View.VISIBLE);
                        holder.txtMain.setBackgroundResource(R.drawable.shape_circle_head_blue);
                        holder.txtMain.setText("管理员");
                    } else {
                        holder.txtMain.setVisibility(View.GONE);
                    }
                }
            } else {
                boolean value = isAdmin() || isAdministrators();
                if (value && position == listDataTop.size() - 1) {
                    // holder.imgHead.setImageURI((new Uri.Builder()).scheme("res").path(String.valueOf(R.mipmap.ic_group_c)).build());
                    holder.imgHead.setImageResource(R.mipmap.ic_group_c);
                    holder.txtName.setText("");
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            taskDel();
                        }
                    });
                } else {
                    //holder.imgHead.setImageURI((new Uri.Builder()).scheme("res").path(String.valueOf(R.mipmap.ic_group_a)).build());
                    holder.imgHead.setImageResource(R.mipmap.ic_group_a);
                    holder.txtName.setText("");
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            taskAdd();
                        }
                    });
                }
            }
        }


        //自动寻找ViewHold
        @Override
        public RCViewTopHolder onCreateViewHolder(ViewGroup view, int i) {
            RCViewTopHolder holder = new RCViewTopHolder(inflater.inflate(R.layout.item_group_create_top2, view, false));
            return holder;
        }


        //自动生成ViewHold
        public class RCViewTopHolder extends RecyclerView.ViewHolder {
            private ImageView imgHead;
            private TextView txtName;
            private TextView txtMain;

            //自动寻找ViewHold
            public RCViewTopHolder(View convertView) {
                super(convertView);
                imgHead = convertView.findViewById(R.id.img_head);
                txtMain = convertView.findViewById(R.id.txt_main);
                txtName = convertView.findViewById(R.id.txt_name);
            }
        }
    }

    /**
     * 管理员排序 放到群主后面
     */
    private void listSort() {
        if (listDataTop != null && listDataTop.size() > 0) {
            List<MemberUser> listManager = new ArrayList<>();
            List<MemberUser> listUser = new ArrayList<>();
            listManager.add(listDataTop.get(0));
            for (int i = 1; i < listDataTop.size(); i++) {
                MemberUser memberUser = listDataTop.get(i);
                if (isAdministrators(memberUser.getUid())) {
                    listManager.add(memberUser);
                } else {
                    listUser.add(memberUser);
                }
            }
            listDataTop.clear();
            listDataTop.addAll(0, listManager);
            listDataTop.addAll(listUser);
        }
    }

    private boolean isBackValue = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String content = "";
            if (data != null) {
                content = data.getStringExtra(CommonSetingActivity.CONTENT);
            }
            switch (requestCode) {
                case GROUP_NAME:
                    if (TextUtils.isEmpty(content)) {
                        return;
                    }
                    taskChangeGroupName(gid, content);
                    break;
                case GROUP_NICK:
                    // TODO 为空时，跟双武这边统一，传原来的昵称
                    if (TextUtils.isEmpty(content)) {
                        content = txtGroupNick.getText().toString();
                    }
                    taskChangeMemberName(gid, content);
                    break;
                case GROUP_NOTE:
                    String note = data.getStringExtra(GroupNoteDetailActivity.CONTENT);
                    ginfo.setAnnouncement(note);
//                    updateAndGetGroup();
                    setGroupNote(ginfo.getAnnouncement());
                    createAndSaveMsg();
                    MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, null, gid, CoreEnum.ESessionRefreshTag.SINGLE, null);
                    isBackValue = true;
                    break;
                case GROUP_MANAGER:
                    finish();
                    break;
            }
        }
    }

    private UserDao userDao = new UserDao();
    private MsgAction msgAction = new MsgAction();
    private MsgDao msgDao = new MsgDao();

    /***
     * 获取群成员
     * @return
     */
    private List<MemberUser> taskGetNumbers() {
        //进入这个信息的时候会统一给的
        List<MemberUser> userInfos = ginfo.getUsers();
        userInfos = userInfos == null ? new ArrayList() : userInfos;
        return userInfos;
    }

    /***
     * 获取通讯录
     * @return
     */
    private List<UserInfo> taskGetFriends() {
        List<UserInfo> userInfos = userDao.friendGetAll(false);
        userInfos = userInfos == null ? new ArrayList() : userInfos;
        return userInfos;
    }

    /***
     * 退出群
     */
    private void taskExitGroup() {
        CallBack callBack = new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body().isOk()) {
                    EventBus.getDefault().post(new EventExitChat());
//                    MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, -1L, gid, CoreEnum.ESessionRefreshTag.DELETE, null);
                    finish();
                } else {
                    ToastUtil.show(getContext(), response.body().getMsg());
                }
            }
        };
        msgAction.groupQuit(gid, UserAction.getMyInfo().getName(), callBack);
    }

    private boolean isAdmin() {
        if (!StringUtil.isNotNull(ginfo.getMaster()))
            return false;
        return ginfo.getMaster().equals("" + UserAction.getMyId());
    }

    /**
     * 判断是否是管理员
     *
     * @return
     */
    private boolean isAdministrators() {
        boolean isManager = false;
        if (ginfo.getViceAdmins() != null && ginfo.getViceAdmins().size() > 0) {
            for (Long user : ginfo.getViceAdmins()) {
                if (user.equals(UserAction.getMyId())) {
                    isManager = true;
                    break;
                }
            }
        }
        return isManager;
    }

    private boolean isAdministrators(Long uid) {
        boolean isManager = false;
        if (ginfo.getViceAdmins() != null && ginfo.getViceAdmins().size() > 0) {
            for (Long user : ginfo.getViceAdmins()) {
                if (user.equals(uid)) {
                    isManager = true;
                    break;
                }
            }
        }
        return isManager;
    }

    private void taskGetInfo() {
        CallBack callBack = new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                if (response.body().isOk()) {
                    ginfo = response.body().getData();
                    if (ginfo == null) {
                        return;
                    }
                    actionbar.setTitle("群聊信息(" + ginfo.getUsers().size() + ")");
                    setGroupNote(ginfo.getAnnouncement());
                    listDataTop.clear();

                    listDataTop.addAll(ginfo.getUsers());
                    // 保证管理员在前面
                    listSort();
                    filterData();
                    final RealmList<MemberUser> list = ginfo.getUsers();
                    if (list.size() < 400) {
                        isPercentage = false;
                    }
                    initData();
                }
            }
        };
        msgAction.groupInfo4Db(gid, callBack);
    }

    //设置群公告
    private void setGroupNote(String note) {
        if (!TextUtils.isEmpty(note)) {
            txtGroupNote.setVisibility(View.VISIBLE);
            txtGroupNote.setText(note);
            txtNote.setVisibility(View.GONE);
        } else {
            txtGroupNote.setVisibility(View.GONE);
            txtNote.setVisibility(View.VISIBLE);
        }
    }

    private void taskGetInfoNetwork(boolean isMemberChange) {
        CallBack callBack = new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                if (response.body().isOk()) {
                    ginfo = response.body().getData();
                    //8.8 如果是有群昵称显示自己群昵称
//                    for (MemberUser number : ginfo.getUsers()) {
//                        if (StringUtil.isNotNull(number.getMembername())) {
//                            number.setName(number.getMembername());
//                        }
//                    }
                    if (isMemberChange) {
                        Group goldinfo = msgDao.getGroup4Id(gid);
                        if (!isChange(goldinfo, ginfo)) {
                            doImgHeadChange(gid, ginfo);
                        }
                        MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, -1L, gid, CoreEnum.ESessionRefreshTag.SINGLE, null);
                    }
                    actionbar.setTitle("群聊信息(" + ginfo.getUsers().size() + ")");
                    setGroupNote(ginfo.getAnnouncement());
                    listDataTop.clear();
                    listDataTop.addAll(ginfo.getUsers());
                    // 保证管理员在前面
                    listSort();
                    filterData();
                    initData();
                }
            }
        };
        msgAction.groupInfo(gid, callBack);
    }

    private void filterData() {
        if (isAdmin() || isAdministrators()) {// 群主、管理员
            if (ginfo.getUsers().size() > 18) {
                viewGroupMore.setVisibility(View.VISIBLE);
                for (int i = listDataTop.size() - 1; i >= 18; i--) {
                    listDataTop.remove(i);
                }
            } else {
                viewGroupMore.setVisibility(View.GONE);
            }
            listDataTop.add(null);
            listDataTop.add(null);
            viewGroupManage.setVisibility(View.VISIBLE);
        } else {// 普通用户
            if (ginfo.getUsers().size() > 19) {
                viewGroupMore.setVisibility(View.VISIBLE);
                for (int i = listDataTop.size() - 1; i >= 19; i--) {
                    listDataTop.remove(i);
                }
            } else {
                viewGroupMore.setVisibility(View.GONE);
            }
            listDataTop.add(null);
            if (isAdministrators()) {
                viewGroupManage.setVisibility(View.VISIBLE);
            } else {
                viewGroupManage.setVisibility(View.GONE);
            }
        }
    }

    /*
     * 置顶
     * */
    private void taskSetState(String gid, Integer isTop, Integer notNotify, Integer saved, Integer needVerification) {

        msgAction.groupSwitch(gid, isTop, notNotify, saved, needVerification, new CallBack4Btn<ReturnBean>(ckTop) {
            @Override
            public void onResp(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                ToastUtil.show(getContext(), response.body().getMsg());
                if (response.body().isOk()) {
                    taskGetInfoNetwork(false);
                }

            }
        });
    }

    /*
     * 免打扰
     * */
    private void taskSetStateDisturb(String gid, Integer isTop, Integer notNotify, Integer saved, Integer needVerification) {

        msgAction.groupSwitch(gid, isTop, notNotify, saved, needVerification, new CallBack4Btn<ReturnBean>(ckDisturb) {
            @Override
            public void onResp(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                ToastUtil.show(getContext(), response.body().getMsg());
                if (response.body().isOk()) {
                    taskGetInfoNetwork(false);
//                    if (notNotify != null) {//免打扰通知变化
                    isSessionChange = true;
//                    }
                }

            }
        });
    }

    /*
     * 群保存
     * */
    private void taskSetStateGroupSave(String gid, Integer isTop, Integer notNotify, Integer saved, Integer needVerification) {

        msgAction.groupSwitch(gid, isTop, notNotify, saved, needVerification, new CallBack4Btn<ReturnBean>(ckGroupSave) {
            @Override
            public void onResp(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                ToastUtil.show(getContext(), response.body().getMsg());
                if (response.body().isOk()) {
                    msgDao.setSavedGroup(gid, saved);
                    //  taskGetInfoNetwork();
                }
            }
        });
    }

    private void taskSetStateGroupVerif(String gid, Integer isTop, Integer notNotify, Integer saved, Integer needVerification) {

        msgAction.groupSwitch(gid, isTop, notNotify, saved, needVerification, new CallBack4Btn<ReturnBean>(ckGroupVerif) {
            @Override
            public void onResp(Call<ReturnBean> call, Response<ReturnBean> response) {

                if (response.body() == null)
                    return;
                ToastUtil.show(getContext(), response.body().getMsg());
                if (response.body().isOk()) {
                    taskGetInfoNetwork(false);
                }
            }
        });
    }


    private void taskAdd() {
        List<MemberUser> userInfos = taskGetNumbers();
        List<UserInfo> friendsUser = taskGetFriends();
        List<UserInfo> temp = new ArrayList<>();
        for (UserInfo a : friendsUser) {
            boolean isEx = false;
            for (MemberUser u : userInfos) {
                if (u.getUid() == a.getUid().longValue()) {
                    isEx = true;
                }
            }
            if (!isEx) {
                temp.add(a);
            }

        }
        String json = gson.toJson(temp);
        startActivity(new Intent(getContext(), GroupNumbersActivity.class)
                .putExtra(GroupNumbersActivity.AGM_GID, gid)
                .putExtra(GroupNumbersActivity.AGM_TYPE, GroupNumbersActivity.TYPE_ADD)
                .putExtra(GroupNumbersActivity.AGM_NUMBERS_JSON, json)
        );
    }

    private void taskDel() {
        List<MemberUser> userInfos = taskGetNumbers();
        if (isAdmin()) {
            for (MemberUser u : userInfos) {
                if (u.getUid() == UserAction.getMyId().longValue()) {
                    userInfos.remove(u);
                    break;
                }
            }
        } else {
            if (userInfos != null && ginfo != null) {
                for (int i = userInfos.size() - 1; i >= 0; i--) {
                    MemberUser u = userInfos.get(i);
                    // TODO 去掉自己、管理员、群主
                    if (u.getUid() == UserAction.getMyId().longValue() || (u.getUid() + "").equals(ginfo.getMaster())) {
                        userInfos.remove(u);
                    } else if (ginfo.getViceAdmins() != null && ginfo.getViceAdmins().size() > 0) {
                        for (Long user : ginfo.getViceAdmins()) {
                            if (user.equals(u.getUid())) {
                                userInfos.remove(u);
                                break;
                            }
                        }
                    }
                }
            }
        }

        String json = gson.toJson(userInfos);
        startActivity(new Intent(getContext(), GroupNumbersActivity.class)
                .putExtra(GroupNumbersActivity.AGM_GID, gid)
                .putExtra(GroupNumbersActivity.AGM_TYPE, GroupNumbersActivity.TYPE_DEL)
                .putExtra(GroupNumbersActivity.AGM_NUMBERS_JSON, json)
        );
    }


    private void taskChangeGroupName(String gid, final String name) {
        msgAction.changeGroupName(gid, name, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                ToastUtil.show(getContext(), response.body().getMsg());
                if (response.body().isOk()) {
                    txtGroupName.setText(name);
                    msgDao.updateGroupName(gid, name);
                    isSessionChange = true;
                    initEvent();
                }
            }
        });
    }

    private void taskChangeMemberName(String gid, final String name) {
        msgAction.changeMemberName(gid, name, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                ToastUtil.show(getContext(), response.body().getMsg());
                if (response.body().isOk()) {
                    txtGroupNick.setText(name);
                    msgDao.updateMyGroupName(gid, name);
                    taskGetInfo();
                    initEvent();
                }
            }
        });
    }

    private void changeSurvivalTime(String gid, int survivalTime) {
        msgAction.changeSurvivalTime(gid, survivalTime, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                super.onResponse(call, response);
                if (response.body() == null) {
                    return;
                }
                if (response.body().isOk()) {
                    userDao.updateGroupReadDestroy(gid, survivalTime);
                    msgDao.noteMsgAddSurvivaltime(ginfo.getUsers().get(0).getUid(), gid);
                }
            }
        });
    }


    private void changeGroupAnnouncement(final String gid, final String announcement, String nick) {
        msgAction.changeGroupAnnouncement(gid, announcement, nick, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                ToastUtil.show(getContext(), response.body().getMsg());
                if (response.body().isOk()) {
                    setGroupNote(announcement);
                    ginfo.setAnnouncement(announcement);
                    updateAndGetGroup();
                }
            }
        });
    }

    private void updateAndGetGroup() {
        if (ginfo != null && !TextUtils.isEmpty(gid)) {
            MsgDao dao = new MsgDao();
            dao.groupNumberSave(ginfo);
            ginfo = dao.groupNumberGet(gid);
        }
    }

    private boolean isChange(Group goldinfo, Group ginfo) {
        int a = ginfo.getUsers().size();
        if (goldinfo == null || goldinfo.getUsers() == null) {
            return true;
        }
        int b = goldinfo.getUsers().size();
        if (a != b) {
            return true;
        }
        int c = a > 9 ? 9 : a;
        for (int i = 0; i < a; i++) {
            if (StringUtil.isNotNull(goldinfo.getUsers().get(i).getHead()) && StringUtil.isNotNull(ginfo.getUsers().get(i).getHead())) {
                if (!goldinfo.getUsers().get(i).getHead().equals(ginfo.getUsers().get(i).getHead())) {
                    return true;
                }
            }

        }
        return false;
    }

    private void doImgHeadChange(String gid, Group ginfo) {

        int i = ginfo.getUsers().size();
        i = i > 9 ? 9 : i;
        //头像地址
        String url[] = new String[i];
        for (int j = 0; j < i; j++) {
            MemberUser userInfo = ginfo.getUsers().get(j);
            url[j] = userInfo.getHead();
        }
        File file = GroupHeadImageUtil.synthesis(getContext(), url);
        MsgDao msgDao = new MsgDao();
        msgDao.groupHeadImgUpdate(gid, file.getAbsolutePath());
//                        msgDao.groupSave(ginfo);
    }

    private void createAndSaveMsg() {
        if (ginfo == null || TextUtils.isEmpty(gid)) {
            return;
        }
//        MsgAllBean bean = SocketData.createMessageBean(gid, "@所有人 \r\n" + ginfo.getAnnouncement(), ginfo);
        AtMessage atMessage = SocketData.createAtMessage(SocketData.getUUID(), "@所有人 \r\n" + ginfo.getAnnouncement(), ChatEnum.EAtType.ALL, null);
        MsgAllBean bean = SocketData.createMessageBean(null, gid, ChatEnum.EMessageType.AT, ChatEnum.ESendStatus.NORMAL, -1L, atMessage);
        if (bean != null) {
            SocketData.saveMessage(bean);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventRefreshChat(EventGroupChange event) {
        if (event.isNeedLoad()) {
            taskGetInfoNetwork(true);
        } else {
            taskGetInfo();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isSessionChange) {//免打扰，群名变化
            MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, -1L, gid, CoreEnum.ESessionRefreshTag.SINGLE, null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void closeActivityEvent(CloseActivityEvent event) {
        if (event.type.contains("GroupInfoActivity")) {
            finish();
        }
    }
}
