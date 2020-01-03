package com.yanlong.im.user.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.yanlong.im.R;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.FriendInfoBean;
import com.yanlong.im.utils.GlideOptionsUtil;
import com.yanlong.im.utils.PhoneListUtil;
import com.yanlong.im.utils.UserUtil;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AlertTouch;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.ClearEditText;
import net.cb.cb.library.view.HeadView;
import net.cb.cb.library.view.MultiListView;
import net.cb.cb.library.view.PySortView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FriendMatchActivity extends AppActivity {
    private HeadView headView;
    private ActionbarView actionbar;
    private MultiListView mtListView;
    private ClearEditText mCeSearch;

    private PySortView viewType;
    private UserAction userAction;
    private PhoneListUtil phoneListUtil = new PhoneListUtil();
    private List<FriendInfoBean> tempData = new ArrayList<>();
    private List<FriendInfoBean> listData = new ArrayList<>();
    private List<FriendInfoBean> seacchData = new ArrayList<>();
    private RecyclerViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_match);
        findViews();
        initEvent();
        initData();
    }


    //自动寻找控件
    private void findViews() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        mtListView = findViewById(R.id.mtListView);
        viewType = findViewById(R.id.view_type);
        mCeSearch = findViewById(R.id.ce_search);
        mCeSearch.setHint("输入联系人昵称搜索");
    }


    //自动生成的控件事件
    private void initEvent() {
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        //联动
        viewType.setLinearLayoutManager(mtListView.getLayoutManager());
        viewType.setListView(mtListView.getListView());
        adapter = new RecyclerViewAdapter();
        mtListView.init(adapter);
        mtListView.getLoadView().setStateNormal();
        mCeSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String name = mCeSearch.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    switch (event.getAction()) {
                        case KeyEvent.ACTION_DOWN:
                            searchName(name);
                            return true;
                    }
                }
                return false;
            }
        });

        mCeSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String content = s.toString();
                if (TextUtils.isEmpty(content)) {
                    adapter.setList(listData);
                    mtListView.notifyDataSetChange();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void initData() {
        userAction = new UserAction();
        //  alert.show("正在匹配中...", false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                phoneListUtil.getPhones(FriendMatchActivity.this, new PhoneListUtil.Event() {
                    @Override
                    public void onList(final List<PhoneListUtil.PhoneBean> list) {

                        if (list == null)
                            return;

                        taskUserMatchPhone(list);

                    }
                });

            }
        }).start();


    }


    public void searchName(String name) {
        if (!TextUtils.isEmpty(name)) {
            seacchData.clear();
            for (FriendInfoBean bean : listData) {
                if (bean.getNickname().contains(name)) {
                    seacchData.add(bean);
                }
            }
            adapter.setList(seacchData);
            mtListView.notifyDataSetChange();
        }
    }


    /***
     * 初始化
     */
    private void initViewTypeData() {
        //排序
        Collections.sort(listData);
        //筛选
        for (int i = 0; i < listData.size(); i++) {
            viewType.putTag(listData.get(i).getTag(), i);
        }
        // 添加存在用户的首字母列表
        viewType.addItemView(UserUtil.friendParseString(listData));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        phoneListUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //自动生成RecyclerViewAdapter
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RCViewHolder> {
        private List<FriendInfoBean> list;


        public void setList(List<FriendInfoBean> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(RCViewHolder holder, final int position) {
            final FriendInfoBean bean = list.get(position);
            holder.txtType.setText(bean.getTag());
            //    holder.imgHead.setImageURI(bean.getAvatar() + "");
            Glide.with(context).load(bean.getAvatar())
                    .apply(GlideOptionsUtil.headImageOptions()).into(holder.imgHead);
            holder.txtName.setText(bean.getNickname());
            holder.txtRemark.setText("通讯录: " + bean.getPhoneremark());

            if (position != 0) {
                FriendInfoBean lastbean = list.get(position - 1);
                if (lastbean.getTag().equals(bean.getTag())) {
                    holder.viewType.setVisibility(View.GONE);
                } else {
                    holder.viewType.setVisibility(View.VISIBLE);
                }
            } else {
                holder.viewType.setVisibility(View.VISIBLE);
            }

            holder.btnAdd.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    AlertTouch alertTouch = new AlertTouch();
                    alertTouch.init(FriendMatchActivity.this, "好友验证", "确定", 0, new AlertTouch.Event() {
                        @Override
                        public void onON() {

                        }

                        @Override
                        public void onYes(String content) {
                            taskFriendApply(bean.getUid(), content, bean.getPhoneremark(), position);
                        }
                    });
                    alertTouch.show();
                    alertTouch.setContent("我是" + UserAction.getMyInfo().getName());
                    alertTouch.setEdHintOrSize(null, 60);

                }
            });
        }


        //自动寻找ViewHold
        @Override
        public RCViewHolder onCreateViewHolder(ViewGroup view, int i) {
            RCViewHolder holder = new RCViewHolder(inflater.inflate(R.layout.item_friend_match, view, false));
            return holder;
        }


        //自动生成ViewHold
        public class RCViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout viewType;
            private TextView txtType;
            private ImageView imgHead;
            private TextView txtName;
            private Button btnAdd;
            private TextView txtRemark;

            //自动寻找ViewHold
            public RCViewHolder(View convertView) {
                super(convertView);
                viewType = convertView.findViewById(R.id.view_type);
                txtType = convertView.findViewById(R.id.txt_type);
                imgHead = convertView.findViewById(R.id.img_head);
                txtName = convertView.findViewById(R.id.txt_name);
                btnAdd = convertView.findViewById(R.id.btn_add);
                txtRemark = convertView.findViewById(R.id.txt_remark);
            }
        }
    }


    private void taskUserMatchPhone(List<PhoneListUtil.PhoneBean> phoneList) {
        userAction.getUserMatchPhone(new Gson().toJson(phoneList), new CallBack<ReturnBean<List<FriendInfoBean>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<FriendInfoBean>>> call, Response<ReturnBean<List<FriendInfoBean>>> response) {
                //  alert.dismiss();
                if (response.body() == null) {
                    return;
                }
                if (response.body().isOk()) {
                    listData.addAll(response.body().getData());
                    for (FriendInfoBean bean : listData) {
                        bean.toTag();
                    }
                    //筛选
                    Collections.sort(listData, new Comparator<FriendInfoBean>() {
                        @Override
                        public int compare(FriendInfoBean o1, FriendInfoBean o2) {
                            return o1.getTag().hashCode() - o2.getTag().hashCode();
                        }
                    });
                    // 把#数据放到末尾
                    tempData.clear();
                    for (int i = listData.size() - 1; i >= 0; i--) {
                        FriendInfoBean bean = listData.get(i);
                        if (bean.getTag().hashCode() == 35) {
                            tempData.add(bean);
                            listData.remove(i);
                        }
                    }
                    listData.addAll(tempData);
                    adapter.setList(listData);
                    initViewTypeData();
                    mtListView.notifyDataSetChange();
                    if (listData == null || listData.size() == 0) {
                        ToastUtil.show(context, "没有匹配的手机联系人");
                    }
                }
            }

            /*@Override
            public void onFailure(Call<ReturnBean<List<FriendInfoBean>>> call, Throwable t) {
                alert.dismiss();
                super.onFailure(call, t);
            }*/
        });
    }

    private void taskFriendApply(final Long uid, String sayHi, String contactName, final int position) {
        userAction.friendApply(uid, sayHi, contactName, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                if (response.body().isOk()) {
                    listData.remove(position);
                    mtListView.notifyDataSetChange();
//                    EventRefreshFriend eventRefreshFriend = new EventRefreshFriend();
//                    eventRefreshFriend.setUid(uid);
//                    eventRefreshFriend.setLocal(false);
//                    eventRefreshFriend.setRosterAction(CoreEnum.ERosterAction.REQUEST_FRIEND);
//                    EventBus.getDefault().post(eventRefreshFriend);
                }
                ToastUtil.show(FriendMatchActivity.this, response.body().getMsg());
            }
        });
    }

}
