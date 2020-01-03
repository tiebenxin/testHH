package com.yanlong.im.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.user.ui.UserInfoActivity;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.InputUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Response;

public class GroupInfoMumberActivity extends AppActivity {

    private String gid;
    private net.cb.cb.library.view.HeadView headView;
    private ActionbarView actionbar;
    private net.cb.cb.library.view.ClearEditText edtSearch;
    private net.cb.cb.library.view.MultiListView mtListView;

    private Gson gson = new Gson();
    private Group ginfo;
    private RealmList<MemberUser> listDataTop = new RealmList<>();

    /***
     * 搜索模式
     */
    private boolean isSearchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info_number);
        findViews();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initEvent();
    }


    //自动寻找控件
    private void findViews() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        edtSearch = findViewById(R.id.edt_search);
        mtListView = findViewById(R.id.mtListView);
    }


    //自动生成的控件事件
    private void initEvent() {
        gid = getIntent().getStringExtra(GroupInfoActivity.AGM_GID);
        taskGetInfo();

        mtListView.getLoadView().setStateNormal();

        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });


        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    taskSearch();
                }
                return false;
            }
        });
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtSearch.getText().toString().length() == 0) {
                    isSearchMode = false;
                    taskGetInfo();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void initData() {
        //顶部处理
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);

        mtListView.init(new RecyclerViewTopAdapter());
        mtListView.getListView().setLayoutManager(gridLayoutManager);
        mtListView.notifyDataSetChange();
    }


    /**
     * 管理员排序 放到群主后面
     */
    private void listSort() {
        listDataTop.clear();
        listDataTop.addAll(ginfo.getUsers());
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
            ginfo.setUsers(listDataTop);
        }
    }

    //自动生成RecyclerViewAdapter
    class RecyclerViewTopAdapter extends RecyclerView.Adapter<RecyclerViewTopAdapter.RCViewTopHolder> {

        @Override
        public int getItemCount() {

            return ginfo.getUsers() == null ? 0 : ginfo.getUsers().size();
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(RCViewTopHolder holder, int position) {


            final MemberUser number = ginfo.getUsers().get(position);
            if (number != null) {

                Glide.with(context).load(number.getHead())
                        .apply(GlideOptionsUtil.headImageOptions()).into(holder.imgHead);

                holder.txtName.setText("" + number.getShowName());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (number.getUid() == UserAction.getMyId().longValue()) {
                            return;
                        }
                        startActivity(new Intent(getContext(), UserInfoActivity.class)
                                .putExtra(UserInfoActivity.ID, number.getUid())
                                .putExtra(UserInfoActivity.JION_TYPE_SHOW, 1)
                                .putExtra(UserInfoActivity.GID, gid)
                                .putExtra(UserInfoActivity.MUC_NICK, number.getMembername())

                        );
                    }
                });
                if (ginfo.getMaster().equals("" + number.getUid())) {
                    holder.txtMain.setVisibility(View.VISIBLE);
                    holder.txtMain.setBackgroundResource(R.drawable.shape_circle_head_yellow);
                    holder.txtMain.setText("群主");
                } else {
                    holder.txtMain.setVisibility(View.GONE);
                    if (isAdministrators(number.getUid())) {
                        holder.txtMain.setVisibility(View.VISIBLE);
                        holder.txtMain.setBackgroundResource(R.drawable.shape_circle_head_blue);
                        holder.txtMain.setText("管理员");
                    } else {
                        holder.txtMain.setVisibility(View.GONE);
                    }
                }
            } else {
                if (isAdmin() && position == ginfo.getUsers().size() - 1) {
                    //  holder.imgHead.setImageURI((new Uri.Builder()).scheme("res").path(String.valueOf(R.mipmap.ic_group_c)).build());
                    holder.imgHead.setImageResource(R.mipmap.ic_group_c);
                    holder.txtName.setText("");
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            taskDel();
                        }
                    });

                } else {
                    // holder.imgHead.setImageURI((new Uri.Builder()).scheme("res").path(String.valueOf(R.mipmap.ic_group_a)).build());
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
                txtName = convertView.findViewById(R.id.txt_name);
                txtMain = convertView.findViewById(R.id.txt_main);
            }

        }
    }


    private boolean isAdmin() {
        return ginfo.getMaster().equals("" + UserAction.getMyId());
    }

    private UserDao userDao = new UserDao();
    private UserAction userAction = new UserAction();
    private MsgAction msgAction = new MsgAction();

    /***
     * 获取群成员
     * @return
     */
    private List<MemberUser> taskGetNumbers() {
        //进入这个信息的时候会统一给的
        List<MemberUser> userInfos = ginfo.getUsers();

        for (int i = userInfos.size() - 1; i > 0; i--) {
            if (userInfos.get(i) == null) {
                userInfos.remove(i);
            }

        }
        userInfos = userInfos == null ? new ArrayList() : userInfos;
        return userInfos;
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

    /***
     * 获取通讯录
     * @return
     */
    private List<UserInfo> taskGetFriends() {
        List<UserInfo> userInfos = userDao.friendGetAll(false);
        userInfos = userInfos == null ? new ArrayList() : userInfos;

        return userInfos;
    }

    private void taskGetInfo() {
        CallBack callBack = new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                if (response.body().isOk()) {
                    ginfo = response.body().getData();

                    //8.8 如果是有群昵称显示自己群昵称
                    for (MemberUser number : ginfo.getUsers()) {
                        if (StringUtil.isNotNull(number.getMembername())) {
                            number.setName(number.getMembername());
                        }
                    }

                    actionbar.setTitle("群成员(" + ginfo.getUsers().size() + ")");
                    listSort();
                    if (isAdmin()) {
                        ginfo.getUsers().add(null);
                        ginfo.getUsers().add(null);

                    } else {
                        ginfo.getUsers().add(null);
                    }
                    initData();
                }
            }
        };

        msgAction.groupInfo4Db(gid, callBack);
        msgAction.groupInfo(gid, callBack);
    }


    private void taskSearch() {
        isSearchMode = true;
        InputUtil.hideKeyboard(edtSearch);
        String key = edtSearch.getText().toString();
        if (key.length() <= 0)
            return;
        List<MemberUser> temp = new ArrayList<>();
        for (MemberUser bean : ginfo.getUsers()) {
            if (bean != null) {
                if (bean.getShowName().contains(key)) {
                    temp.add(bean);
                }
            }
        }
        ginfo.getUsers().clear();

        ginfo.getUsers().addAll(temp);

        mtListView.notifyDataSetChange();
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
        for (MemberUser u : userInfos) {
            if (u.getUid() == UserAction.getMyId().longValue()) {
                userInfos.remove(u);
                break;
            }
        }
        String json = gson.toJson(userInfos);
        startActivity(new Intent(getContext(), GroupNumbersActivity.class)
                .putExtra(GroupNumbersActivity.AGM_GID, gid)
                .putExtra(GroupNumbersActivity.AGM_TYPE, GroupNumbersActivity.TYPE_DEL)
                .putExtra(GroupNumbersActivity.AGM_NUMBERS_JSON, json)
        );
    }
}
