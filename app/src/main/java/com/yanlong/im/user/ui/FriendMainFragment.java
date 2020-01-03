package com.yanlong.im.user.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.chat.ui.ChatActivity;
import com.yanlong.im.chat.ui.GroupSaveActivity;
import com.yanlong.im.chat.ui.SearchFriendGroupActivity;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.utils.GlideOptionsUtil;
import com.yanlong.im.utils.UserUtil;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.EventRefreshFriend;
import net.cb.cb.library.bean.EventRunState;
import net.cb.cb.library.bean.OnlineBean;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.TimeToString;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.PySortView;
import net.cb.cb.library.view.StrikeButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

/***
 * 首页通讯录
 */
public class FriendMainFragment extends Fragment {
    private View rootView;
    private View viewSearch;
    private net.cb.cb.library.view.MultiListView mtListView;
    private PySortView viewType;
    private ActionbarView actionbar;


    //自动寻找控件
    private void findViews(View rootView) {
        viewSearch = rootView.findViewById(R.id.view_search);
        mtListView = rootView.findViewById(R.id.mtListView);
        viewType = rootView.findViewById(R.id.view_type);
        actionbar = rootView.findViewById(R.id.action_bar);
    }

    //自动生成的控件事件
    private void initEvent() {
        mtListView.init(new RecyclerViewAdapter());

        //联动
        viewType.setLinearLayoutManager(mtListView.getLayoutManager());
        viewType.setListView(mtListView.getListView());

        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {

            }

            @Override
            public void onRight() {
                startActivity(new Intent(getContext(), FriendAddAcitvity.class));
            }
        });
        viewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SearchFriendGroupActivity.class));
            }
        });
    }

    private List<UserInfo> listData = new ArrayList<>();
    private List<UserInfo> tempData = new ArrayList<>();

    private void initData() {
        taskListData();
//        if (listData == null || listData.size() <= 0) {
//            taskRefreshListData();
//        }
    }

    public FriendMainFragment() {
        // Required empty public constructor
    }


    public static FriendMainFragment newInstance() {
        FriendMainFragment fragment = new FriendMainFragment();
        Bundle args = new Bundle();
    /*    args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);*/
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        //taskListData();重新从书库刷新数据,还是只是刷新页面重新显示在线时间
        mtListView.notifyDataSetChange();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
/*            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);*/
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fgm_msg_friend, null);
        ViewGroup.LayoutParams layparm = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.setLayoutParams(layparm);
        findViews(rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initEvent();
        initData();
    }


    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
    /*
   private MainActivity getActivityMe() {
        return (MainActivity) getActivity();
    }

    */

    //自动生成RecyclerViewAdapter
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemCount() {
            return listData == null ? 0 : listData.size();
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof RCViewFuncHolder) {
                final RCViewFuncHolder hd = (RCViewFuncHolder) holder;
                hd.viewAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // ToastUtil.show(getContext(), "添加朋友");
                        taskApplyNumClean();
                        hd.sbApply.setNum(0, false);
                        startActivity(new Intent(getContext(), FriendApplyAcitvity.class));
                    }
                });
                hd.viewAddFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getContext(), FriendAddAcitvity.class));
                    }
                });
                hd.viewGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // ToastUtil.show(getContext(), "群消息");
                        startActivity(new Intent(getContext(), GroupSaveActivity.class));
                    }
                });
                hd.viewMatch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // ToastUtil.show(getContext(), "匹配");
                        startActivity(new Intent(getContext(), FriendMatchActivity.class));
                    }
                });
                hd.sbApply.setNum(taskGetApplyNum(), false);
            } else if (holder instanceof RCViewHolder) {

                final UserInfo bean = listData.get(position);
                RCViewHolder hd = (RCViewHolder) holder;
                hd.txtType.setText(bean.getTag());
                //      hd.imgHead.setImageURI(Uri.parse("" + bean.getHead()));

                Glide.with(getActivity()).load(bean.getHead())
                        .apply(GlideOptionsUtil.headImageOptions()).into(hd.imgHead);

                hd.txtName.setText(bean.getName4Show());
                hd.viewLine.setVisibility(View.VISIBLE);
                if (bean.isSystemUser()) {
                    hd.txtName.setTextColor(getResources().getColor(R.color.blue_title));
                    hd.txtTime.setVisibility(View.GONE);
                    hd.usertype_tv.setVisibility(View.VISIBLE);
                } else {
                    hd.txtName.setTextColor(getResources().getColor(R.color.black));
                    hd.txtTime.setVisibility(View.VISIBLE);
                    hd.usertype_tv.setVisibility(View.GONE);

                    if (bean.getLastonline() > 0) {
                        hd.txtTime.setText(TimeToString.getTimeOnline(bean.getLastonline(), bean.getActiveType(), false));
                        hd.txtTime.setVisibility(View.VISIBLE);
                    } else {
                        hd.txtTime.setVisibility(View.GONE);
                    }
                }


                UserInfo lastBean = listData.get(position - 1);
                if (lastBean != null && lastBean.getTag().equals(bean.getTag()) && position != 1) {
                    hd.viewType.setVisibility(View.GONE);
                } else {
                    hd.viewType.setVisibility(View.VISIBLE);
                }
                if (position == getItemCount() - 1) {
                    hd.viewLine.setVisibility(View.GONE);
                } else {
                    UserInfo lastbean = listData.get(position + 1);
                    if (!lastbean.getTag().equals(bean.getTag())) {
                        hd.viewLine.setVisibility(View.GONE);
                    }
                }
                hd.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bean.getuType() == ChatEnum.EUserType.ASSISTANT) {
                            startActivity(new Intent(getContext(), ChatActivity.class)
                                    .putExtra(ChatActivity.AGM_TOUID, bean.getUid()));
                        } else {
                            startActivity(new Intent(getContext(), UserInfoActivity.class)
                                    .putExtra(UserInfoActivity.ID, bean.getUid()));
                        }


                    }
                });
            }


        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
            onBindViewHolder(holder, position);
        }

        @Override
        public int getItemViewType(int position) {

            return position == 0 ? 0 : 1;
        }

        //自动寻找ViewHold
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup view, int i) {
            if (i == 0) {
                RCViewFuncHolder holder = new RCViewFuncHolder(getLayoutInflater().inflate(R.layout.item_msg_friend_fun, view, false));
                return holder;
            } else {
                RCViewHolder holder = new RCViewHolder(getLayoutInflater().inflate(R.layout.item_msg_friend, view, false));
                return holder;
            }

        }


        //自动生成ViewHold
        public class RCViewHolder extends RecyclerView.ViewHolder {
            private TextView txtType;
            private ImageView imgHead;
            private TextView txtName;
            private TextView txtTime;
            private View viewType;
            private TextView usertype_tv;
            private View viewLine;

            //自动寻找ViewHold
            public RCViewHolder(View convertView) {
                super(convertView);
                txtType = convertView.findViewById(R.id.txt_type);
                imgHead = convertView.findViewById(R.id.img_head);
                txtName = convertView.findViewById(R.id.txt_name);
                txtTime = convertView.findViewById(R.id.txt_time);
                viewType = convertView.findViewById(R.id.view_type);
                usertype_tv = convertView.findViewById(R.id.usertype_tv);
                viewLine = convertView.findViewById(R.id.view_line);
            }

        }


        //自动生成ViewHold
        public class RCViewFuncHolder extends RecyclerView.ViewHolder {
            private LinearLayout viewAdd;
            private LinearLayout viewAddFriend;
            private LinearLayout viewMatch;
            private LinearLayout viewGroup;
            private StrikeButton sbApply;

            //自动寻找ViewHold
            public RCViewFuncHolder(View convertView) {
                super(convertView);
                viewAdd = convertView.findViewById(R.id.view_add);
                viewAddFriend = convertView.findViewById(R.id.view_add_friend);
                viewMatch = convertView.findViewById(R.id.view_match);
                viewGroup = convertView.findViewById(R.id.view_group);
                sbApply = convertView.findViewById(R.id.sb_apply);
            }
        }
    }

    private UserDao userDao = new UserDao();
    private UserAction userAction = new UserAction();

    @SuppressLint("CheckResult")
    private void taskListData() {
        Observable.just(0)
                .map(new Function<Integer, List<UserInfo>>() {
                    @Override
                    public List<UserInfo> apply(Integer integer) throws Exception {
                        listData = userDao.getAllUserInBook();
                        if (listData != null) {
                            // 升序
                            Collections.sort(listData, new Comparator<UserInfo>() {
                                @Override
                                public int compare(UserInfo o1, UserInfo o2) {
                                    return o1.getTag().hashCode() - o2.getTag().hashCode();
                                }
                            });

                            // 把#数据放到末尾
                            tempData.clear();
                            for (int i = listData.size() - 1; i >= 0; i--) {
                                UserInfo bean = listData.get(i);
                                if (bean.getTag().hashCode() == 35) {
                                    tempData.add(bean);
                                    listData.remove(i);
                                }
                            }
                            listData.addAll(tempData);

                            UserInfo topBean = new UserInfo();
                            topBean.setTag("↑");
                            listData.add(0, topBean);
                            viewType.clearAllTag();
                            for (int i = 1; i < listData.size(); i++) {
                                viewType.putTag(listData.get(i).getTag(), i);
                            }

                        }
                        return listData;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(Observable.<List<UserInfo>>empty())
                .subscribe(new Consumer<List<UserInfo>>() {
                    @Override
                    public void accept(List<UserInfo> userInfo) throws Exception {
                        mtListView.notifyDataSetChange();
                        // 添加存在用户的首字母列表
                        viewType.addItemView(UserUtil.userParseString(listData));
                    }
                });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventRefreshFriend(EventRefreshFriend event) {
        if (event.isLocal()) {
            long uid = event.getUid();
            @CoreEnum.ERosterAction int action = event.getRosterAction();
            switch (action) {
                case CoreEnum.ERosterAction.REMOVE_FRIEND:
                    taskRemoveUser(uid);
                    break;
                case CoreEnum.ERosterAction.BLACK://添加或者解除黑名单
                    taskListData();
                    break;
                case CoreEnum.ERosterAction.REQUEST_FRIEND://请求添加为好友
                    mtListView.getListView().getAdapter().notifyItemChanged(0, 0);
                    break;
                default:
                    if (uid > 0) {
                        refreshPosition(uid);
                    } else {
                        taskListData();
                    }
                    break;
            }

        } else {
            long uid = event.getUid();
            if (uid > 0) {
                @CoreEnum.ERosterAction int action = event.getRosterAction();
                switch (action) {
                    case CoreEnum.ERosterAction.REQUEST_FRIEND:
                        taskRefreshUser(uid, action);
                        break;
                    case CoreEnum.ERosterAction.ACCEPT_BE_FRIENDS:
                        taskRefreshUser(uid, action);
                        break;
//                    case CoreEnum.ERosterAction.REMOVE_FRIEND:
//                        taskRemoveUser(uid);
//                        break;
                    default:
                        taskListData();
                        break;
                }
            } else {
                taskListData();

            }
        }
    }

    private void refreshPosition(long uid) {
        if (listData != null) {
            UserInfo info = userDao.findUserInfo(uid);
            if (info != null) {
                if (listData.contains(info)) {
                    int index = listData.indexOf(info);
                    if (index >= 0) {
                        listData.set(index, info);
                        mtListView.getListView().getAdapter().notifyItemChanged(index, index);
                    }
                }
            }
        }
    }

    private void taskRemoveUser(long uid) {
        userDao.updateUserUtype(uid, 0);//设置为陌生人
        userDao.updateReadDestroy(uid, 0);//关闭阅后即焚
        // 更新置顶状态
        msgDao.updateUserSessionTop(uid, 0);
        // 刷新列表
        MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.PRIVATE, uid, "", CoreEnum.ESessionRefreshTag.ALL, null);
        taskListData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventRefreshOnlineStatus(EventRunState event) {
        if (event.getRun()) {
            taskGetUsersOnlineStatus();
        }
    }

    public void taskRefreshListData() {
        userAction.friendGet4Me(new CallBack<ReturnBean<List<UserInfo>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<UserInfo>>> call, Response<ReturnBean<List<UserInfo>>> response) {
                taskListData();
            }

            @Override
            public void onFailure(Call<ReturnBean<List<UserInfo>>> call, Throwable t) {
                super.onFailure(call, t);
                taskListData();
            }
        });
    }

    public void taskRefreshUser(long uid, @CoreEnum.ERosterAction int action) {
        userAction.getUserInfoAndSave(uid, action == CoreEnum.ERosterAction.ACCEPT_BE_FRIENDS ? ChatEnum.EUserType.FRIEND : ChatEnum.EUserType.STRANGE, new CallBack<ReturnBean<UserInfo>>() {
            @Override
            public void onResponse(Call<ReturnBean<UserInfo>> call, Response<ReturnBean<UserInfo>> response) {
                taskListData();
            }
        });
    }


    private MsgDao msgDao = new MsgDao();

    /***
     * 申请数量
     * @return
     */
    private int taskGetApplyNum() {

        return msgDao.remidGet("friend_apply");


    }

    private void taskApplyNumClean() {
        msgDao.remidClear("friend_apply");
        MessageManager.getInstance().notifyRefreshMsg();
//        MessageManager.getInstance().notifyRefreshFriend(false,-1, CoreEnum.ERosterAction.REQUEST_FRIEND);
    }

    public void taskGetUsersOnlineStatus() {
        userAction.getUsersOnlineStatus(new CallBack<ReturnBean<List<OnlineBean>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<OnlineBean>>> call, Response<ReturnBean<List<OnlineBean>>> response) {
                taskListData();
            }

            @Override
            public void onFailure(Call<ReturnBean<List<OnlineBean>>> call, Throwable t) {
                super.onFailure(call, t);
//                                                taskListData();
            }
        });
    }


}
