package com.yanlong.im.chat.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;
import com.yanlong.im.MainActivity;
import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.eventbus.EventRefreshMainMsg;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.user.ui.FriendAddAcitvity;
import com.yanlong.im.user.ui.HelpActivity;
import com.yanlong.im.utils.ExpressionUtil;
import com.yanlong.im.utils.PatternUtil;
import com.yanlong.im.utils.QRCodeManage;
import com.yanlong.im.utils.socket.MsgBean;
import com.yanlong.im.utils.socket.SocketEvent;
import com.yanlong.im.utils.socket.SocketUtil;
import com.yanlong.im.wight.avatar.MultiImageView;
import com.zhaoss.weixinrecorded.util.RxJavaUtil;

import net.cb.cb.library.AppConfig;
import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.EventNetStatus;
import net.cb.cb.library.utils.DensityUtil;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.NetUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.TimeToString;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.EllipsizedTextView;
import net.cb.cb.library.view.PopView;
import net.cb.cb.library.view.StrikeButton;
import net.cb.cb.library.zxing.activity.CaptureActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

/**
 * 首页消息
 *
 * @version V1.0
 * @createAuthor （test4j）
 * @createDate 2019-4-12
 * @updateAuthor （Geoff）
 * @updateDate 2019-12-2
 * @description 视频通话
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class MsgMainFragment extends Fragment {

    private View rootView;
    private net.cb.cb.library.view.ActionbarView actionBar;
    private net.cb.cb.library.view.MultiListView mtListView;
    private RecyclerViewAdapter mAdapter;

    private LinearLayout viewPopGroup;
    private LinearLayout viewPopAdd;
    private LinearLayout viewPopQr;
    private LinearLayout viewPopHelp;
    private View mHeadView;
    private boolean onlineState = true;//判断网络状态 true在线 false离线
    private final String TYPE_FACE = "[动画表情]";

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mAdapter.viewNetwork != null) {
                mAdapter.viewNetwork.setVisibility(View.GONE);
            }
        }
    };

    Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
            //解决断网后又马上连网造成的提示显示异常问题
            if (!NetUtil.isNetworkConnected()) {
                if (mAdapter.viewNetwork != null) {
                    mAdapter.viewNetwork.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    private void findViewsPop(View rootView) {
        viewPopGroup = rootView.findViewById(R.id.view_pop_group);
        viewPopAdd = rootView.findViewById(R.id.view_pop_add);
        viewPopQr = rootView.findViewById(R.id.view_pop_qr);
        viewPopHelp = rootView.findViewById(R.id.view_pop_help);

    }

    private void findViews(View rootView) {
        actionBar = rootView.findViewById(R.id.actionBar);
        mtListView = rootView.findViewById(R.id.mtListView);

        mHeadView = View.inflate(getContext(), R.layout.view_head_main_message, null);

        View pView = getLayoutInflater().inflate(R.layout.view_pop_main, null);
        findViewsPop(pView);
        popView.init(getContext(), pView);
    }

    private PopView popView = new PopView();
    private SocketEvent socketEvent;

    private void initEvent() {
        mAdapter = new RecyclerViewAdapter(mHeadView);
        mtListView.init(mAdapter);

        mtListView.getLoadView().setStateNormal();
        SocketUtil.getSocketUtil().addEvent(socketEvent = new SocketEvent() {
            @Override
            public void onHeartbeat() {

            }

            @Override
            public void onACK(MsgBean.AckMessage bean) {

            }

            @Override
            public void onMsg(MsgBean.UniversalMessage bean) {

            }

            @Override
            public void onSendMsgFailure(MsgBean.UniversalMessage.Builder bean) {

            }

            @Override
            public void onLine(final boolean state) {
                getActivityMe().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.getLog().d("tyad", "run: state=" + state);
                        AppConfig.setOnline(state);
                        actionBar.getLoadBar().setVisibility(state ? View.GONE : View.VISIBLE);
                        if (!state && getActivityMe().isActivityStop()) {
                            return;
                        }
                        resetNetWorkView(state ? CoreEnum.ENetStatus.SUCCESS_ON_SERVER : CoreEnum.ENetStatus.ERROR_ON_SERVER);
                        onlineState = state;
                    }
                });

            }
        });

        actionBar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {

            }

            @Override
            public void onRight() {
                int x = DensityUtil.dip2px(getContext(), -92);
                int y = DensityUtil.dip2px(getContext(), 5);
                popView.getPopupWindow().showAsDropDown(actionBar.getBtnRight(), x, y);

            }
        });

        viewPopAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), FriendAddAcitvity.class));
                popView.dismiss();
            }
        });
        viewPopGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), GroupCreateActivity.class));
                popView.dismiss();
            }
        });
        viewPopQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // 申请权限
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CaptureActivity.REQ_PERM_CAMERA);
                    return;
                }
                // 二维码扫码
                Intent intent = new Intent(getActivity(), CaptureActivity.class);
                startActivityForResult(intent, CaptureActivity.REQ_QR_CODE);

                popView.dismiss();
            }
        });
        viewPopHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), HelpActivity.class));
                popView.dismiss();
            }
        });
    }

    public void hidePopView() {
        if (popView != null) {
            popView.dismiss();
        }
    }

    private void resetNetWorkView(@CoreEnum.ENetStatus int status) {
        LogUtil.getLog().i(MsgMainFragment.class.getSimpleName(), "resetNetWorkView--status=" + status);
        if (mAdapter == null || mAdapter.viewNetwork == null) {
            return;
        }
        switch (status) {
            case CoreEnum.ENetStatus.ERROR_ON_NET:
//                viewNetwork.setVisibility(View.VISIBLE);
                if (mAdapter.viewNetwork.getVisibility() == View.GONE) {
                    mAdapter.viewNetwork.postDelayed(showRunnable, 15 * 1000);
                }
                break;
            case CoreEnum.ENetStatus.SUCCESS_ON_NET:
                if (NetUtil.isNetworkConnected()) {//无网络链接，无效指令
                    mAdapter.viewNetwork.setVisibility(View.GONE);
                }
                removeHandler();
                break;
            case CoreEnum.ENetStatus.ERROR_ON_SERVER:
                if (mAdapter.viewNetwork.getVisibility() == View.GONE) {
                    mAdapter.viewNetwork.postDelayed(showRunnable, 10 * 1000);
                }
                break;
            case CoreEnum.ENetStatus.SUCCESS_ON_SERVER:
                mAdapter.viewNetwork.setVisibility(View.GONE);
                removeHandler();
                break;
            default:
                mAdapter.viewNetwork.setVisibility(View.GONE);
                removeHandler();
                break;

        }
    }

    private void removeHandler() {
        if (mAdapter.viewNetwork != null && runnable != null) {
            mAdapter.viewNetwork.removeCallbacks(runnable);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
            QRCodeManage.goToPage(getContext(), scanResult);
        }
    }

    public MsgMainFragment() {
        // Required empty public constructor
    }


    public static MsgMainFragment newInstance() {
        MsgMainFragment fragment = new MsgMainFragment();
        Bundle args = new Bundle();
    /*    args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);*/
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        taskListData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SocketUtil.getSocketUtil().removeEvent(socketEvent);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventRefresh(EventRefreshMainMsg event) {
        if (MessageManager.getInstance().isMessageChange()) {
            MessageManager.getInstance().setMessageChange(false);
            int refreshTag = event.getRefreshTag();
            if (refreshTag == CoreEnum.ESessionRefreshTag.ALL) {
                LogUtil.getLog().d("a=", MsgMainFragment.class.getSimpleName() + "-- 刷新Session-ALL");
                taskListData();
            } else if (refreshTag == CoreEnum.ESessionRefreshTag.SINGLE) {
                refreshPosition(event.getGid(), event.getUid(), event.getMsgAllBean(), event.getSession(), event.isRefreshTop());
                LogUtil.getLog().d("a=", MsgMainFragment.class.getSimpleName() + "-- 刷新Session-SINGLE");
            } else if (refreshTag == CoreEnum.ESessionRefreshTag.DELETE) {
                LogUtil.getLog().d("a=", MsgMainFragment.class.getSimpleName() + "-- 刷新Session-DELETE");
                taskDelSession(event.getUid(), event.getGid());
            }
        }
    }

    /*
     * 刷新单一位置
     * TODO　增加文件头，默认的位置都需要加1
     * */
    @SuppressLint("CheckResult")
    private void refreshPosition(String gid, Long uid, MsgAllBean bean, Session s, boolean isRefreshTop) {
        Observable.just(0)
                .map(new Function<Integer, Session>() {
                    @Override
                    public Session apply(Integer integer) throws Exception {
                        if (s == null) {
                            Session session = msgDao.sessionGet(gid, uid);
                            if (bean != null) {
                                session.setMessage(bean);
                            }
                            prepareSession(session, false);
                            return session;
                        } else {
                            if (bean != null) {
                                s.setMessage(bean);
                            }
                            prepareSession(s, true);
                            return s;
                        }

                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(Observable.<Session>empty())
                .subscribe(new Consumer<Session>() {
                    @Override
                    public void accept(Session session) throws Exception {
                        if (listData != null) {
                            int index = listData.indexOf(session);
                            if (index >= 0) {
                                Session s = listData.get(index);
                                if (isRefreshTop /*|| session.getIsTop() == 1*/) {//是否刷新置顶
                                    if (session.getIsTop() == 1) {//修改了置顶状态
                                        listData.remove(index);
                                        listData.add(0, session);//放在首位
                                        mtListView.getListView().getAdapter().notifyItemRangeChanged(1, index + 1);//范围刷新
                                        LogUtil.getLog().d("a=", MsgMainFragment.class.getSimpleName() + "置顶刷新--session=" + session.getSid());
                                    } else {//取消置顶
                                        listData.set(index, session);
                                        sortSession(index == 0);
                                        int newIndex = listData.indexOf(session);//获取重排后新位置
                                        int start = index > newIndex ? newIndex : index;//谁小，取谁
                                        int count = Math.abs(newIndex - index) + 1;
                                        mtListView.getListView().getAdapter().notifyItemRangeChanged(start + 1, count);////范围刷新,刷新旧位置和新位置之间即可
                                        LogUtil.getLog().d("a=", MsgMainFragment.class.getSimpleName() + "取消置顶刷新--session=" + session.getSid());

                                    }
                                } else {
                                    listData.set(index, session);
                                    if (s != null && s.getUp_time().equals(session.getUp_time())) {//时间未更新，所以不要重新排序
                                        mtListView.getListView().getAdapter().notifyItemChanged(index + 1, index);
                                        LogUtil.getLog().d("a=", MsgMainFragment.class.getSimpleName() + "时间未更新--session=" + session.getSid());
                                    } else {//有时间更新,需要重排
                                        sortSession(index == 0);
                                        int newIndex = listData.indexOf(session);
                                        int start = index > newIndex ? newIndex : index;//谁小，取谁
                                        int count = Math.abs(newIndex - index) + 1;
                                        mtListView.getListView().getAdapter().notifyItemRangeChanged(start + 1, count);//范围刷新
                                        LogUtil.getLog().d("a=", MsgMainFragment.class.getSimpleName() + "时间更新重排--session=" + session.getSid());
                                    }
                                }
                            } else {
                                int position = insertSession(session);
                                LogUtil.getLog().d("a=", MsgMainFragment.class.getSimpleName() + "新session--session=" + session.getSid());
                                if (position == 0) {
                                    mtListView.notifyDataSetChange();
                                } else {
                                    mtListView.getListView().getAdapter().notifyItemRangeInserted(position + 1, 1);
                                    mtListView.getListView().scrollToPosition(0);
                                }
                            }
                        } else {
                            int position = insertSession(session);
                            LogUtil.getLog().d("a=", MsgMainFragment.class.getSimpleName() + "新session--session=" + session.getSid());
                            if (position == 0) {
                                mtListView.notifyDataSetChange();
                            } else {
                                mtListView.getListView().getAdapter().notifyItemRangeInserted(position + 1, 1);
                                mtListView.getListView().scrollToPosition(0);
                            }
                        }
                    }
                });
    }

    /*
     * 重新排序,置顶和非置顶分别重排
     * @param isTop 当前要更新的session 是否在列表第一位置（置顶）
     * */
    private void sortSession(boolean isTop) {
        if (listData != null) {
            int len = listData.size();
            if (len > 0) {
                Session first = null;
                if (!isTop) {
                    first = listData.get(0);
                } else {
                    if (len >= 2) {
                        first = listData.get(1);
                    }
                }
                if (first != null && first.getIsTop() == 1) {//有置顶
                    List<Session> topList = new ArrayList<>();
                    List<Session> list = new ArrayList<>();
                    for (int i = 0; i < len; i++) {
                        Session session = listData.get(i);
                        if (session.getIsTop() == 1) {
                            topList.add(session);
                        } else {
                            list.add(session);
                        }
                    }
                    listData.clear();
                    if (topList.size() > 0) {
                        Collections.sort(topList);
                        listData.addAll(topList);
                    }
                    if (list.size() > 0) {
                        Collections.sort(list);
                        listData.addAll(list);
                    }
                } else {//无置顶
                    Collections.sort(listData);
                }
            }
        }
    }

    /*
     * 插入位置需要考虑置顶
     * */
    private int insertSession(Session s) {
        int position = 0;//需要插入位置
        if (listData != null) {
            int len = listData.size();
            boolean hasTop = false;
            if (s.getIsTop() == 1) {
                listData.add(0, s);
            } else {
                for (int i = 0; i < len; i++) {
                    Session session = listData.get(i);
                    if (session.getIsTop() != 1) {
                        position = i;
                        break;//结束循环
                    } else {
                        hasTop = true;
                    }
                }
                if (hasTop && position == 0) {//全是置顶
                    position = len;
                }
                listData.add(position, s);
            }
        } else {
            listData = new ArrayList<>();
            listData.add(s);
        }
        return position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fgm_msg_main, null);
        ViewGroup.LayoutParams layparm = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.setLayoutParams(layparm);
        findViews(rootView);
        initEvent();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //  initEvent();
    }


    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventNetStatus(EventNetStatus event) {
        resetNetWorkView(event.getStatus());
    }

    private MainActivity mainActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();

    }

    private MainActivity getActivityMe() {
        if (mainActivity == null) {
            return (MainActivity) getActivity();
        }
        return mainActivity;
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int TYPE_HEADER = 0;
        public static final int TYPE_NORMAL = 1;
        private View mHeaderView;
        public View viewNetwork;

        public RecyclerViewAdapter(View headerView) {
            mHeaderView = headerView;
            notifyItemInserted(0);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup view, int viewType) {
            if (mHeaderView != null && viewType == TYPE_HEADER)
                return new HeadViewHolder(mHeaderView);
            RCViewHolder holder = new RCViewHolder(getLayoutInflater().inflate(R.layout.item_msg_session, view, false));
            return holder;
        }

        @Override
        public int getItemViewType(int position) {
            if (mHeaderView == null) return TYPE_NORMAL;
            if (position == 0) return TYPE_HEADER;
            return TYPE_NORMAL;
        }

        @Override
        public int getItemCount() { // TODO　增加文件头，默认的位置加1
            return listData == null ? 1 : listData.size() + 1;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);
            } else {
                int type = (int) payloads.get(0);
                switch (type) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
                onBindViewHolder(holder, position);
            }
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

            if (viewHolder instanceof RCViewHolder) {
                RCViewHolder holder = (RCViewHolder) viewHolder;
                final Session bean = listData.get(position - 1);
//                Log.i("1212", "Session:" + new Gson().toJson(bean));
                String icon = bean.getAvatar();
                String title = bean.getName();
                MsgAllBean msginfo = bean.getMessage();
                String name = bean.getSenderName();
                // 头像集合
                List<String> headList = new ArrayList<>();

                String info = "";
                if (msginfo != null) {
                    info = msginfo.getMsg_typeStr();
                }
                int type = bean.getMessageType();
                if (bean.getType() == 0) {//单人
                    if (type == ChatEnum.ESessionType.ENVELOPE_FAIL) {
                        SpannableString style = new SpannableString("[红包发送失败]" + info);
                        ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red_all_notify));
                        style.setSpan(protocolColorSpan, 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        showMessage(holder.txtInfo, info, style);
                    } else {
                        if (StringUtil.isNotNull(bean.getDraft())) {
                            SpannableString style = new SpannableString("[草稿]" + bean.getDraft());
                            ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red_all_notify));
                            style.setSpan(protocolColorSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            showMessage(holder.txtInfo, bean.getDraft(), style);
                        } else {
                            // 判断是否是动画表情
                            if (info.length() == PatternUtil.FACE_CUSTOMER_LENGTH) {
                                Pattern patten = Pattern.compile(PatternUtil.PATTERN_FACE_CUSTOMER, Pattern.CASE_INSENSITIVE); // 通过传入的正则表达式来生成一个pattern
                                Matcher matcher = patten.matcher(info);
                                if (matcher.matches()) {
                                    holder.txtInfo.setText(TYPE_FACE);
                                } else {
                                    showMessage(holder.txtInfo, info, null);
                                }
                            } else {
                                showMessage(holder.txtInfo, info, null);
                            }
                        }
                    }
                    headList.add(icon);
                    holder.imgHead.setList(headList);

                } else if (bean.getType() == 1) {//群
                    if (type == 0) {
                        if (!TextUtils.isEmpty(bean.getAtMessage()) && !TextUtils.isEmpty(name)) {
                            info = name + bean.getAtMessage();
                        } else {
                            info = name + info;

                        }
                    } else if (type == 1) {
                        if (!TextUtils.isEmpty(bean.getAtMessage()) && !TextUtils.isEmpty(name)) {
                            info = bean.getAtMessage();
                            if (StringUtil.isNotNull(info) && info.startsWith("@所有人")) {
                                info = info.replace("@所有人", "");
                            }
                            info = name + info;
                        } else {
                            info = name + info;
                        }
                    } else if (msginfo != null && (ChatEnum.EMessageType.CHANGE_SURVIVAL_TIME + "").equals(msginfo.getMsg_type() + "")) {
                        //阅后即焚不通知 不显示谁发的 肯定是群主修改的
                        // info=info;
                    } else if (!TextUtils.isEmpty(info) && !TextUtils.isEmpty(name)) {//草稿除外
                        if ((ChatEnum.EMessageType.AT + "").equals(msginfo.getMsg_type() + "")
                                && StringUtil.isNotNull(info) && info.startsWith("@所有人")) {
                            info = info.replace("@所有人", "");
                        }
                        info = name + info;
                    }
                    // 处理公告...问题
                    info = info.replace("\r\n", "  ");

                    switch (type) {
                        case 0:
                            if (StringUtil.isNotNull(bean.getAtMessage())) {
                                if (msginfo != null && msginfo.getMsg_type() == ChatEnum.EMessageType.AT) {
                                    SpannableString style = new SpannableString("[有人@我]" + info);
                                    ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red_all_notify));
                                    style.setSpan(protocolColorSpan, 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    showMessage(holder.txtInfo, info, style);
                                } else {
                                    SpannableString style = new SpannableString("[有人@我]" + info);
                                    ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red_all_notify));
                                    style.setSpan(protocolColorSpan, 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    showMessage(holder.txtInfo, info, style);
                                }
                            }
                            break;
                        case 1:
                            if (StringUtil.isNotNull(bean.getAtMessage())) {
                                if (msginfo == null || msginfo.getMsg_type() == null) {
                                    return;
                                }
                                if (msginfo.getMsg_type() == ChatEnum.EMessageType.AT) {
                                    SpannableString style = new SpannableString("[有人@我]" + info);
                                    ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red_all_notify));
                                    style.setSpan(protocolColorSpan, 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    showMessage(holder.txtInfo, info, style);
                                } else {
                                    SpannableString style = new SpannableString("[@所有人]" + info);
                                    ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red_all_notify));
                                    style.setSpan(protocolColorSpan, 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    showMessage(holder.txtInfo, info, style);
                                }
                            }
                            break;
                        case 2:
                            if (StringUtil.isNotNull(bean.getDraft())) {
                                SpannableString style = new SpannableString("[草稿]" + bean.getDraft());
                                ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red_all_notify));
                                style.setSpan(protocolColorSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                showMessage(holder.txtInfo, bean.getDraft(), style);
                            } else {
                                // 判断是否是动画表情
                                Pattern patten = Pattern.compile(PatternUtil.PATTERN_FACE_CUSTOMER, Pattern.CASE_INSENSITIVE); // 通过传入的正则表达式来生成一个pattern
                                Matcher matcher = patten.matcher(info);
                                if (matcher.find()) {
                                    info = info.substring(0, info.indexOf("["));
                                    holder.txtInfo.setText(info + " " + TYPE_FACE);
                                } else {
                                    showMessage(holder.txtInfo, info, null);
                                }
                            }
                            break;
                        case 3:
                            SpannableString style = new SpannableString("[红包发送失败]" + info);
                            ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red_all_notify));
                            style.setSpan(protocolColorSpan, 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            showMessage(holder.txtInfo, info, style);
                            break;
                        default:
                            // 判断是否是动画表情
                            Pattern patten = Pattern.compile(PatternUtil.PATTERN_FACE_CUSTOMER, Pattern.CASE_INSENSITIVE); // 通过传入的正则表达式来生成一个pattern
                            Matcher matcher = patten.matcher(info);
                            if (matcher.find()) {
                                info = info.substring(0, info.indexOf("["));
                                holder.txtInfo.setText(info + " " + TYPE_FACE);
                            } else {
                                showMessage(holder.txtInfo, info, null);
                            }
                            break;
                    }

                    if (StringUtil.isNotNull(icon)) {
                        headList.add(icon);
                        holder.imgHead.setList(headList);
                    } else {
                        loadGroupHeads(bean, holder.imgHead);
                    }
                }

                holder.txtName.setText(title);
                if (bean.isSystemUser()) {
                    //系统会话
                    holder.txtName.setTextColor(getResources().getColor(R.color.blue_title));
                    holder.usertype_tv.setVisibility(View.VISIBLE);
                } else {
                    holder.txtName.setTextColor(getResources().getColor(R.color.black));
                    holder.usertype_tv.setVisibility(View.GONE);
                }
                setUnreadCountOrDisturb(holder, bean, msginfo);

                holder.txtTime.setText(TimeToString.getTimeWx(bean.getUp_time()));


                holder.viewIt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getContext(), ChatActivity.class)
                                .putExtra(ChatActivity.AGM_TOUID, bean.getFrom_uid())
                                .putExtra(ChatActivity.AGM_TOGID, bean.getGid())
                                .putExtra(ChatActivity.ONLINE_STATE, onlineState)
                        );
//                    if (bean.getUnread_count() > 0) {
//                        MessageManager.getInstance().setMessageChange(true);
//                    }

                    }
                });
                holder.btnDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.swipeLayout.quickClose();
                        taskDelSession(bean.getFrom_uid(), bean.getGid());
                    }
                });
//            holder.viewIt.setBackgroundColor(bean.getIsTop() == 0 ? Color.WHITE : Color.parseColor("#f1f1f1"));
                holder.viewIt.setBackgroundColor(bean.getIsTop() == 0 ? Color.WHITE : Color.parseColor("#ececec"));
                holder.iv_disturb.setVisibility(bean.getIsMute() == 0 ? View.GONE : View.VISIBLE);
            } else if (viewHolder instanceof HeadViewHolder) {
                HeadViewHolder headHolder = (HeadViewHolder) viewHolder;
                headHolder.edtSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), MsgSearchActivity.class);
                        intent.putExtra("online_state", onlineState);
                        intent.putExtra("conversition_data", new Gson().toJson(listData));
                        startActivity(intent);
                    }
                });
                viewNetwork = headHolder.viewNetwork;
            }
        }

        private void setUnreadCountOrDisturb(RCViewHolder holder, Session bean, MsgAllBean msg) {
            holder.sb.setButtonBackground(R.color.transparent);
            if (bean.getIsMute() == 1) {
                if (msg != null && !msg.isRead()) {
                    holder.iv_disturb_unread.setVisibility(View.VISIBLE);
                    holder.iv_disturb_unread.setBackgroundResource(R.drawable.shape_disturb_unread_bg);
                    holder.sb.setVisibility(View.GONE);
                } else {
                    holder.iv_disturb_unread.setVisibility(View.GONE);
                    holder.sb.setVisibility(View.VISIBLE);
                    holder.sb.setNum(bean.getUnread_count(), false);
                }
            } else {
                holder.iv_disturb_unread.setVisibility(View.GONE);
                holder.sb.setVisibility(View.VISIBLE);
                holder.sb.setNum(bean.getUnread_count(), false);
            }
        }

        /**
         * 富文本显示最后一條内容
         *
         * @param txtInfo
         * @param message
         * @param spannableString
         */
        protected void showMessage(TextView txtInfo, String message, SpannableString spannableString) {
            if (spannableString == null) {
                if (StringUtil.isNotNull(message) && message.startsWith("@所有人  ")) {
                    message = message.replace("@所有人  ", "");
                }
                spannableString = ExpressionUtil.getExpressionString(getContext(), ExpressionUtil.DEFAULT_SMALL_SIZE, message);
            } else {
                spannableString = ExpressionUtil.getExpressionString(getContext(), ExpressionUtil.DEFAULT_SMALL_SIZE, spannableString);
            }
            txtInfo.setText(spannableString, TextView.BufferType.SPANNABLE);
        }

        /**
         * 加载群头像
         *
         * @param bean
         * @param imgHead
         */
        public synchronized void loadGroupHeads(Session bean, MultiImageView imgHead) {
            Group gginfo = msgDao.getGroup4Id(bean.getGid());
            if (gginfo != null) {
                int i = gginfo.getUsers().size();
                i = i > 9 ? 9 : i;
                //头像地址
                List<String> headList = new ArrayList<>();
                for (int j = 0; j < i; j++) {
                    MemberUser userInfo = gginfo.getUsers().get(j);
                    headList.add(userInfo.getHead());
                }
                imgHead.setList(headList);
            }
        }


        public class RCViewHolder extends RecyclerView.ViewHolder {
            private MultiImageView imgHead;
            private StrikeButton sb;

            private View viewIt;
            private Button btnDel;
            private SwipeMenuLayout swipeLayout;
            private TextView txtName;
            private EllipsizedTextView txtInfo;
            private TextView txtTime;
            private final ImageView iv_disturb, iv_disturb_unread;
            //            private final TextView tv_num;
            private TextView usertype_tv;

            //自动寻找ViewHold
            public RCViewHolder(View convertView) {
                super(convertView);
                imgHead = convertView.findViewById(R.id.img_head);
                swipeLayout = convertView.findViewById(R.id.swipeLayout);
                sb = convertView.findViewById(R.id.sb);
                viewIt = convertView.findViewById(R.id.view_it);
                btnDel = convertView.findViewById(R.id.btn_del);
                txtName = convertView.findViewById(R.id.txt_name);
                txtInfo = convertView.findViewById(R.id.txt_info);
                txtTime = convertView.findViewById(R.id.txt_time);
                iv_disturb = convertView.findViewById(R.id.iv_disturb);
//                tv_num = convertView.findViewById(R.id.tv_num);
                iv_disturb_unread = convertView.findViewById(R.id.iv_disturb_unread);
                usertype_tv = convertView.findViewById(R.id.usertype_tv);
            }
        }

        public class HeadViewHolder extends RecyclerView.ViewHolder {

            private net.cb.cb.library.view.ClearEditText edtSearch;
            private View viewNetwork;

            public HeadViewHolder(View convertView) {
                super(convertView);
                edtSearch = convertView.findViewById(R.id.edt_search);
                viewNetwork = convertView.findViewById(R.id.view_network);
            }
        }

    }

    private MsgDao msgDao = new MsgDao();
    private UserDao userDao = new UserDao();
    private MsgAction msgAction = new MsgAction();
    private List<Session> listData = new ArrayList<>();

    @SuppressLint("CheckResult")
    private void taskListData() {
//        if (isSearchMode) {
//            return;
//        }
//        LogUtil.getLog().d("a=", "MsgMainFragment --开始获取session数据" + System.currentTimeMillis());
        Observable.just(0)
                .map(new Function<Integer, List<Session>>() {
                    @Override
                    public List<Session> apply(Integer integer) throws Exception {
                        listData = msgDao.sessionGetAll(true);
//                        LogUtil.getLog().d("a=", "MsgMainFragment --结束获取session数据" + System.currentTimeMillis());
                        doListDataSort();
//                        LogUtil.getLog().d("a=", "MsgMainFragment --结束准备session数据" + System.currentTimeMillis());
                        return listData;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(Observable.<List<Session>>empty())
                .subscribe(new Consumer<List<Session>>() {
                    @Override
                    public void accept(List<Session> list) throws Exception {
                        mtListView.notifyDataSetChange();
                        checkSessionData(list);
//                        LogUtil.getLog().d("a=", "MsgMainFragment --获取session数据后刷新" + System.currentTimeMillis());
                    }
                });

    }

    /**
     * TODO　临时处理办法 异步检查Sessiong列表是否有异常数据，比如头像、群名称没有,如果有则重新拉取在刷新列表
     *
     * @param list
     */
    private void checkSessionData(List<Session> list) {
        RxJavaUtil.run(new RxJavaUtil.OnRxAndroidListener<Object>() {

            @Override
            public Object doInBackground() throws Throwable {
                if (list != null && list.size() > 0) {
                    for (Session session : list) {
                        if (session.getType() == 1 && !TextUtils.isEmpty(session.getGid())) {
                            Group gginfo = msgDao.getGroup4Id(session.getGid());
                            if (gginfo != null) {
                                // 群聊的时候 检查头像跟群名是否存在
                                if (gginfo.getUsers() == null || gginfo.getUsers().size() == 0 ||
                                        (TextUtils.isEmpty(gginfo.getName()) && TextUtils.isEmpty(session.getName()))) {
//                                    Log.i("1212", "checkSessionData:" + session.getGid());
                                    MessageManager.getInstance().refreshGroupInfo(session.getGid());
                                }
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            public void onFinish(Object result) {

            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    private void doListDataSort() {
        if (listData != null) {
            int len = listData.size();
            for (int i = 0; i < len; i++) {
                Session session = listData.get(i);
                prepareSession(session, false);

            }
        }
    }

    /*
     * 准备session
     * @param isNew 是否是新数据，（主要针对置顶，免打扰）
     * */
    private void prepareSession(Session session, boolean isNew) {
        if (session == null) {
            return;
        }
        if (session.getType() == 1) {
            Group group = msgDao.getGroup4Id(session.getGid());
            if (group != null) {
                session.setName(msgDao.getGroupName(group));
                session.setIsMute(group.getNotNotify());
                session.setAvatar(group.getAvatar());


            } else {
                session.setName(msgDao.getGroupName(session.getGid()));
            }
            MsgAllBean msg = session.getMessage();
            if (msg == null) {
                msg = msgDao.msgGetLast4Gid(session.getGid());
            }
            if (msg != null) {
                session.setMessage(msg);
                if (msg.getMsg_type() == ChatEnum.EMessageType.NOTICE || msg.getMsg_type() == ChatEnum.EMessageType.MSG_CANCEL) {//通知不要加谁发的消息
                    session.setSenderName("");
                } else {
                    if (msg.getFrom_uid().longValue() != UserAction.getMyId().longValue()) {//自己的不加昵称
                        //8.9 处理群昵称
                        String name = msgDao.getUsername4Show(msg.getGid(), msg.getFrom_uid(), msg.getFrom_nickname(), msg.getFrom_group_nickname()) + " : ";
                        session.setSenderName(name);
                    }
                }
            }
        } else {
            UserInfo info = userDao.findUserInfo(session.getFrom_uid());
            if (info != null) {
                session.setName(info.getName4Show());
                session.setIsMute(info.getDisturb());
                session.setAvatar(info.getHead());
            }
            MsgAllBean msg = session.getMessage();
            if (msg == null) {
                msg = msgDao.msgGetLast4FUid(session.getFrom_uid());
            }
            if (msg != null) {
                session.setMessage(msg);
            }
        }
    }

    private void taskDelSession(Long from_uid, String gid) {
        MessageManager.getInstance().deleteSessionAndMsg(from_uid, gid);
        MessageManager.getInstance().notifyRefreshMsg();
        taskListData();
    }


}
