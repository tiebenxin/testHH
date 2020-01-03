package com.yanlong.im.user.ui;

import android.content.Intent;
import android.os.Build;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.ClearEditText;
import net.cb.cb.library.view.HeadView;
import net.cb.cb.library.view.MultiListView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FindFriendActivity extends AppActivity {

    private HeadView mHeadView;
    private ClearEditText edtSearch;
    private MultiListView mMtListView;
    private UserAction userAction;
    private List<UserInfo> userInfos;
    private FindFriendAdapter adapter;

    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_frd_grp);
        initView();
        initEvent();
        initData();
    }

    private void initView() {
        mHeadView = findViewById(R.id.headView);
        edtSearch = findViewById(R.id.edt_search);
        mMtListView = findViewById(R.id.mtListView);
        mMtListView.getLoadView().setStateNormal();
        edtSearch.setHint("输入常信号/手机号搜索联系人");
        mHeadView.getActionbar().setTitle("搜索好友");
    }

    private void initEvent() {
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
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
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    switch (event.getAction()) {
                        case KeyEvent.ACTION_DOWN:
                            taskFindFriend();
                            return true;
                    }
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

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    key="";
                } else {
                    key = edtSearch.getText().toString();
                }
                userInfos.clear();
                mMtListView.notifyDataSetChange();
            }
        });
    }


    private void initData() {
        userAction = new UserAction();
        userInfos = new ArrayList<>();
        adapter = new FindFriendAdapter();
        mMtListView.init(adapter);
    }


    private void taskFindFriend() {
        if(!StringUtil.isNotNull(key)){
            ToastUtil.show("请输入关键字");
            return;
        }
        userAction.getUserInfoByKeyword(key, new CallBack<ReturnBean<List<UserInfo>>>(mMtListView) {
            @Override
            public void onResponse(Call<ReturnBean<List<UserInfo>>> call, Response<ReturnBean<List<UserInfo>>> response) {
                if (response.body() == null) {
                    return;
                }
                userInfos.clear();
                userInfos.addAll(response.body().getData());
                key="";
                mMtListView.notifyDataSetChange(response);
            }
        });
    }


    class FindFriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == 0) {
                RSearchViewHolder holder = new RSearchViewHolder(getLayoutInflater().inflate(R.layout.item_search_net, viewGroup, false));
                return holder;
            } else {
                FindFriendHolderView holder = new FindFriendHolderView(getLayoutInflater().inflate(R.layout.item_find_friend, viewGroup, false));
                return holder;
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder instanceof RSearchViewHolder) {
                RSearchViewHolder holder = (RSearchViewHolder) viewHolder;
                holder.setKey();
            } else if (viewHolder instanceof FindFriendHolderView) {
                FindFriendHolderView holder = (FindFriendHolderView) viewHolder;
                final UserInfo userInfo = userInfos.get(i);
                //holder.mImgHead.setImageURI(userInfo.getHead() + "");
                Glide.with(context).load(userInfo.getHead())
                        .apply(GlideOptionsUtil.headImageOptions()).into(holder.mImgHead);

                holder.mTxtName.setText(userInfo.getName());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (userInfo.getUid().equals(UserAction.getMyId())) {
                            Intent intent = new Intent(FindFriendActivity.this, MyselfInfoActivity.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(FindFriendActivity.this, UserInfoActivity.class);
                            intent.putExtra(UserInfoActivity.ID, userInfo.getUid());
                            intent.putExtra(UserInfoActivity.FROM, ChatEnum.EFromType.SEARCH);
                            startActivity(intent);
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            if (userInfos != null) {
                if (!TextUtils.isEmpty(key)) {
                    return userInfos.size() + 1;
                } else {
                    return userInfos.size() ;
                }
            }
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (!TextUtils.isEmpty(key) && position == 0) {
                return 0;
            } else {
                return 1;
            }
        }


        class FindFriendHolderView extends RecyclerView.ViewHolder {
            private ImageView mImgHead;
            private TextView mTxtName;

            public FindFriendHolderView(@NonNull View itemView) {
                super(itemView);
                mImgHead = itemView.findViewById(R.id.img_head);
                mTxtName = itemView.findViewById(R.id.txt_name);
            }
        }

        class RSearchViewHolder extends RecyclerView.ViewHolder {
            private final TextView tv_content;
            private final LinearLayout ll_root;

            public RSearchViewHolder(@NonNull View itemView) {
                super(itemView);
                ll_root = itemView.findViewById(R.id.ll_root);
                tv_content = itemView.findViewById(R.id.tv_content);
                ll_root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        taskFindFriend();
                    }
                });
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            public void setKey( ) {
                String content = "网络查找常信号:" + key;
//                tv_content.setText(getSpan(content, key, 8));
                tv_content.setText(content);
            }

        }
    }


}
