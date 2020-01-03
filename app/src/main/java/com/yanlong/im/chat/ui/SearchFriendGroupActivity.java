package com.yanlong.im.chat.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.ui.UserInfoActivity;
import com.yanlong.im.utils.GlideOptionsUtil;
import com.yanlong.im.utils.GroupHeadImageUtil;
import com.yanlong.im.wight.avatar.MultiImageView;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

/***
 * 搜索群和好友
 */
public class SearchFriendGroupActivity extends AppActivity {
    private net.cb.cb.library.view.HeadView headView;
    private ActionbarView actionbar;
    private net.cb.cb.library.view.ClearEditText edtSearch;
    private net.cb.cb.library.view.MultiListView mtListView;
    private List<UserInfo> listDataUser = new ArrayList<>();
    private List<Group> listDataGroup = new ArrayList<>();
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_frd_grp);
        findViews();
        initEvent();
    }

    //自动寻找控件
    private void findViews() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        edtSearch = findViewById(R.id.edt_search);
        mtListView = findViewById(R.id.mtListView);
        actionbar.setTitle("通讯录搜索");
        edtSearch.setHint("当前通讯录搜索");
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
        mtListView.init(new RecyclerViewAdapter());
        mtListView.getLoadView().setStateNormal();

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    taskSearch();
                } else if (event != null && (KeyEvent.KEYCODE_ENTER == event.getKeyCode() || KeyEvent.ACTION_DOWN == event.getAction())) {
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

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    key = "";
                    listDataUser.clear();
                    listDataGroup.clear();
                    mtListView.notifyDataSetChange();
                } else {
                    taskSearch();
                }
            }
        });
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    private Spannable getSpan(String message, String condition, int fromIndex) {
        if (!message.contains(condition)) {
            return new SpannableString(message);
        }
        SpannableString ss = new SpannableString(message);
        int start = message.indexOf(condition, fromIndex);
        int end = start + condition.length();
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green_500)), start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    //自动生成RecyclerViewAdapter
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        //自动寻找ViewHold
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup view, int viewType) {
            if (viewType == 0) {
                RSearchViewHolder holder = new RSearchViewHolder(getLayoutInflater().inflate(R.layout.item_search_net, view, false));
                return holder;
            } else {
                RCViewHolder holder = new RCViewHolder(getLayoutInflater().inflate(R.layout.item_friend_group, view, false));
                return holder;
            }
        }

        @Override
        public int getItemCount() {
            if (!TextUtils.isEmpty(key)) {
                return listDataGroup.size() + listDataUser.size() + 1;
            } else {
                return listDataGroup.size() + listDataUser.size();

            }
        }

        //自动生成控件事件
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
            if (h instanceof RSearchViewHolder) {
                RSearchViewHolder holder = (RSearchViewHolder) h;
                holder.setKey(key);
            } else if (h instanceof RCViewHolder) {
                RCViewHolder holder = (RCViewHolder) h;
                if (!TextUtils.isEmpty(key)) {
                    //头像地址
                    List<String> headList = new ArrayList<>();
                    String url = "";
                    holder.viewTagGroup.setVisibility(View.GONE);
                    holder.viewTagFried.setVisibility(View.GONE);
                    if (listDataUser.size() > position - 1) {
                        if (position == 1) {
                            holder.viewTagGroup.setVisibility(View.GONE);
                            holder.viewTagFried.setVisibility(View.VISIBLE);
                        }
                        final UserInfo user = listDataUser.get(position - 1);
//                    name = user.getName4Show();
                        url = user.getHead();
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getApplicationContext(), UserInfoActivity.class)
                                        .putExtra(UserInfoActivity.ID, user.getUid()));
                            }
                        });
                        holder.setContactName(user.getMkName(), user.getName(), key);

                    } else {
                        if (position == listDataUser.size() + 1) {
                            holder.viewTagGroup.setVisibility(View.VISIBLE);
                            holder.viewTagFried.setVisibility(View.GONE);
                        }
                        int p = position - listDataUser.size() - 1;
                        final Group group = listDataGroup.get(p);
//                    name = group.getName();
                        url = group.getAvatar();
                        if(!StringUtil.isNotNull(url)){
                            MsgDao msgDao = new MsgDao();
                            String localUrl = msgDao.groupHeadImgGet(group.getGid());
                            if (StringUtil.isNotNull(url)) {
                                url=localUrl;
                            } else {
                                url=creatAndSaveImg(group);
                            }
                        }
//                        LogUtil.getLog().e(position+"=======getAvatar==="+url);
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getApplicationContext(), ChatActivity.class).putExtra(ChatActivity.AGM_TOGID, group.getGid()));
                            }
                        });
                        holder.setGroupName(group, key);
                    }
//                    Glide.with(context).load(url)
//                            .apply(GlideOptionsUtil.headImageOptions()).into(holder.imgHead);
                    headList.add(url);
                    holder.imgHead.setList(headList);

                } else {
                    //头像地址
                    List<String> headList = new ArrayList<>();
                    String url = "";
                    holder.viewTagGroup.setVisibility(View.GONE);
                    holder.viewTagFried.setVisibility(View.GONE);
                    if (listDataUser.size() > position) {
                        if (position == 0) {
                            holder.viewTagGroup.setVisibility(View.GONE);
                            holder.viewTagFried.setVisibility(View.VISIBLE);
                        }
                        final UserInfo user = listDataUser.get(position);
                        url = user.getHead();
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getApplicationContext(), UserInfoActivity.class)
                                        .putExtra(UserInfoActivity.ID, user.getUid()));
                            }
                        });
                        holder.setContactName(user.getMkName(), user.getName(), key);

                    } else {
                        if (position == listDataUser.size()) {
                            holder.viewTagGroup.setVisibility(View.VISIBLE);
                            holder.viewTagFried.setVisibility(View.GONE);
                        }
                        int p = position - listDataUser.size();
                        final Group group = listDataGroup.get(p);
//                    name = group.getName();
                        url = group.getAvatar();
                        if(!StringUtil.isNotNull(url)){
                            MsgDao msgDao = new MsgDao();
                            String localUrl = msgDao.groupHeadImgGet(group.getGid());
                            if (StringUtil.isNotNull(url)) {
                                url=localUrl;
                            } else {
                                url=creatAndSaveImg(group);
                            }
                        }

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getApplicationContext(), ChatActivity.class).putExtra(ChatActivity.AGM_TOGID, group.getGid()));
                            }
                        });
                        holder.setGroupName(group, key);
                    }

//                    Glide.with(context).load(url)
//                            .apply(GlideOptionsUtil.headImageOptions()).into(holder.imgHead);

                    headList.add(url);
                    holder.imgHead.setList(headList);
                }
            }

        }



        @Override
        public int getItemViewType(int position) {
            if (!TextUtils.isEmpty(key) && position == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        //自动生成ViewHold
        public class RCViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout viewTagFried;
            private LinearLayout viewTagGroup;
            private LinearLayout viewIt;
            private MultiImageView imgHead;
            private TextView txtName;
            private TextView txtContent;

            //自动寻找ViewHold
            public RCViewHolder(View convertView) {
                super(convertView);
                viewTagFried = convertView.findViewById(R.id.view_tag_fried);
                viewTagGroup = convertView.findViewById(R.id.view_tag_group);
                viewIt = convertView.findViewById(R.id.view_it);
                imgHead = convertView.findViewById(R.id.img_head);
                txtName = convertView.findViewById(R.id.txt_name);
                txtContent = convertView.findViewById(R.id.txt_content);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("SetTextI18n")
            private void setContactName(String mkName, String nick, String key) {
                if (!TextUtils.isEmpty(mkName)) {
                    if (mkName.contains(key)) {
                        txtContent.setVisibility(View.GONE);
                        txtName.setText(getSpan(mkName, key, 0));
                    } else {
                        txtName.setText(mkName);
                        if (!TextUtils.isEmpty(nick)) {
                            txtContent.setVisibility(View.VISIBLE);
                            if (nick.contains(nick)) {
                                txtContent.setText("昵称:" + getSpan(nick, key, 3));
                            } else {
                                txtContent.setText("昵称:" + getSpan(nick, key, 3));
                            }
                        } else {
                            txtContent.setVisibility(View.GONE);
                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(nick)) {
                        txtContent.setVisibility(View.GONE);
                        if (nick.contains(nick)) {
                            txtName.setText(getSpan(nick, key, 0));
                        } else {
                            txtName.setText(getSpan(nick, key, 0));
                        }
                    }
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            private void setGroupName(Group group, String key) {
                if (group == null) {
                    return;
                }
//                String groupName = group.getName();//群名
                MsgDao dao = new MsgDao();
                String groupName = dao.getGroupName(group.getGid());//群名
                MemberUser userInfo = group.getKeyUser();
                String userMucName = "";//用户群昵称
                String userNickName = "";//用户昵称
                String userMkName = "";//好友备注名
                if (userInfo != null) {
                    userMucName = userInfo.getMembername();//用户群昵称
                    userNickName = userInfo.getName();//用户昵称
//                    userMkName = userInfo.getMkName();//好友备注名
                }
                if (!TextUtils.isEmpty(groupName)) {
                    if (group.getName().contains(key)) {
                        txtContent.setVisibility(View.GONE);
                        txtName.setText(getSpan(groupName, key, 0));
                    } else {
                        txtName.setText(groupName);
                        txtContent.setVisibility(View.VISIBLE);
                        if (!TextUtils.isEmpty(userMkName)) {//好友备注
                            if (userMkName.contains(key)) {
                                txtContent.setText(getSpan(userMkName, key, 0));
                            } else {
                                if (!TextUtils.isEmpty(userNickName)) {//用户昵称
                                    if (userNickName.contains(key)) {
                                        String content = "包含:" + userMkName + "(" + userNickName + ")";
                                        txtContent.setText(getSpan(content, key, 3));
                                    } else {
                                        if (!TextUtils.isEmpty(userMucName)) {//群备注
                                            if (userMucName.contains(key)) {
                                                String content = "包含:" + userMkName + "(" + userMucName + ")";
                                                txtContent.setText(getSpan(content, key, 3));
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (!TextUtils.isEmpty(userNickName)) {
                                if (userNickName.contains(key)) {
                                    String content = "包含:" + userNickName;
                                    txtContent.setText(getSpan(content, key, 0));
                                } else {
                                    if (!TextUtils.isEmpty(userMucName)) {
                                        if (userMucName.contains(key)) {
                                            String content = "包含:" + userNickName + "(" + userMucName + ")";
                                            txtContent.setText(getSpan(content, key, 3));
                                        }
                                    }
                                }
                            } else {
                                if (!TextUtils.isEmpty(userMucName)) {
                                    if (userMucName.contains(key)) {
                                        String content = "包含:" + userMucName;
                                        txtContent.setText(getSpan(content, key, 3));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public class RSearchViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_content;
        private final LinearLayout ll_root;

        public RSearchViewHolder(@NonNull View itemView) {
            super(itemView);
            ll_root = itemView.findViewById(R.id.ll_root);
            tv_content = itemView.findViewById(R.id.tv_content);
            ll_root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    taskSearchToNet();
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        public void setKey(String key) {
            String content = "网络查找常信号:" + key;
            tv_content.setText(getSpan(content, key, 8));
        }

    }

    private MsgAction msgAction = new MsgAction();
    private UserAction userAction = new UserAction();

    private void taskSearch() {
        key = edtSearch.getText().toString();
        if (key.length() <= 0)
            return;
        listDataUser = userAction.searchUser4key(key);
        listDataGroup = msgAction.searchGroup4key(key);
        mtListView.notifyDataSetChange();
    }

    private void taskSearchToNet() {
        userAction.getUserInfoByKeyword(key, new CallBack<ReturnBean<List<UserInfo>>>(mtListView) {
            @Override
            public void onResponse(Call<ReturnBean<List<UserInfo>>> call, Response<ReturnBean<List<UserInfo>>> response) {
                if (response.body() == null) {
                    return;
                }
                listDataUser.clear();
                listDataUser.addAll(response.body().getData());
                listDataGroup.clear();
                key = "";
                mtListView.notifyDataSetChange(response);
            }
        });

    }


    private String creatAndSaveImg(Group bean) {
        Group gginfo = bean;
        int i = gginfo.getUsers().size();
        i = i > 9 ? 9 : i;
        //头像地址
        String url[] = new String[i];
        for (int j = 0; j < i; j++) {
            MemberUser userInfo = gginfo.getUsers().get(j);
            url[j] = userInfo.getHead();
        }
        File file = GroupHeadImageUtil.synthesis(getContext(), url);

        MsgDao msgDao = new MsgDao();
        msgDao.groupHeadImgCreate(gginfo.getGid(), file.getAbsolutePath());

        return file.getAbsolutePath();
    }

}
