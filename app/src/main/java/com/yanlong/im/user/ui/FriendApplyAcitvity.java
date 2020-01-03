package com.yanlong.im.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;
import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.ApplyBean;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.utils.GlideOptionsUtil;
import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.EventRefreshFriend;
import net.cb.cb.library.bean.RefreshApplyEvent;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

/***
 * 新的申请
 */
public class FriendApplyAcitvity extends AppActivity {
    private net.cb.cb.library.view.HeadView headView;
    private ActionbarView actionbar;
    private net.cb.cb.library.view.MultiListView mtListView;
    private List<ApplyBean> listData;
    private UserAction userAction = new UserAction();
    private MsgAction msgAction = new MsgAction();
    private MsgDao msgDao = new MsgDao();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_apply);
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
        findViews();
        initEvent();

        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        mtListView = findViewById(R.id.mtListView);
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

    }


    private void initData() {
        listData=msgDao.getApplyBeanList();
        mtListView.notifyDataSetChange();
    }

    //自动生成RecyclerViewAdapter
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RCViewHolder> {

        //自动寻找ViewHold
        @Override
        public RCViewHolder onCreateViewHolder(ViewGroup view, int i) {
            RCViewHolder holder = new RCViewHolder(inflater.inflate(R.layout.item_friend_apply, view, false));
            return holder;
        }

        @Override
        public int getItemCount() {
            return listData == null ? 0 : listData.size();
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(final RCViewHolder holder, int position) {
            ApplyBean bean=listData.get(position);
            if (CoreEnum.EChatType.PRIVATE==bean.getChatType()) {
                holder.txtName.setText(bean.getNickname());
                // holder.imgHead.setImageURI(bean.getHead());
                Glide.with(context).load(bean.getAvatar())
                        .apply(GlideOptionsUtil.headImageOptions()).into(holder.imgHead);

                if (TextUtils.isEmpty(bean.getSayHi())) {
                    holder.txtInfo.setText("想加你为好友");
                } else {
                    holder.txtInfo.setText(bean.getSayHi());
                }

                holder.btnComit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        //同意
                        taskFriendAgree(bean);
                    }
                });

                holder.mLayoutItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FriendApplyAcitvity.this, UserInfoActivity.class);
                        intent.putExtra(UserInfoActivity.ID, bean.getUid());
                        intent.putExtra(UserInfoActivity.SAY_HI, bean.getSayHi());
                        intent.putExtra(UserInfoActivity.IS_APPLY, 1);
                        startActivity(intent);
                    }
                });
                holder.mBtnDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //拒绝
                        holder.mSwipeLayout.quickClose();
                        taskDelRequestFriend(bean);
                    }
                });


            } else if (CoreEnum.EChatType.GROUP==bean.getChatType()) {
                holder.txtName.setText(bean.getNickname());
                Glide.with(context).load(bean.getAvatar())
                        .apply(GlideOptionsUtil.headImageOptions()).into(holder.imgHead);

                holder.txtInfo.setText("申请进群:" + bean.getGroupName());


                holder.btnComit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        //  同意
                        taskRequest(bean);
                    }
                });
                holder.mBtnDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //拒绝
                        holder.mSwipeLayout.quickClose();
//                        bean.setStat(3);
//                        msgDao.applyGroup(bean);
                        msgDao.applyRemove(bean.getAid());
                        initData();
                    }
                });
            }

            //  holder.txtState.setText("已添加");

            holder.btnComit.setVisibility(View.VISIBLE);
            holder.txtState.setVisibility(View.GONE);

            if(bean.getStat()==1){
                holder.btnComit.setText("接受");
            }else if(bean.getStat()==2){
                holder.btnComit.setText("已接受");
                holder.btnComit.setTextColor(getResources().getColor(R.color.gray_300));
                holder.btnComit.setBackgroundColor(getResources().getColor(R.color.transparent));
                holder.btnComit.setEnabled(false);
            }
//            else if(bean.getStat()==3){
//                holder.btnComit.setText("已拒绝");
//                holder.btnComit.setEnabled(false);
//            }
        }



        //自动生成ViewHold
        public class RCViewHolder extends RecyclerView.ViewHolder {
            private ImageView imgHead;
            private TextView txtName;
            private TextView txtInfo;
            private TextView txtState;
            private Button btnComit;
            private SwipeMenuLayout mSwipeLayout;
            private Button mBtnDel;
            private LinearLayout mLayoutItem;

            //自动寻找ViewHold
            public RCViewHolder(View convertView) {
                super(convertView);
                mLayoutItem = convertView.findViewById(R.id.layout_item);
                imgHead = convertView.findViewById(R.id.img_head);
                txtName = convertView.findViewById(R.id.txt_name);
                txtInfo = convertView.findViewById(R.id.txt_info);
                txtState = convertView.findViewById(R.id.txt_state);
                btnComit = convertView.findViewById(R.id.btn_comit);
                mSwipeLayout = convertView.findViewById(R.id.swipeLayout);
                mBtnDel = convertView.findViewById(R.id.btn_del);
            }

        }
    }

    private void taskDelRequestFriend(ApplyBean bean) {
        if(bean.getStat()==2){
            msgDao.applyRemove(bean.getAid());
            initData();
        }else {
            userAction.delRequestFriend(bean.getUid(), new CallBack<ReturnBean>() {
                @Override
                public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                    if (response.body() == null) {
                        return;
                    }
                    ToastUtil.show(getContext(), response.body().getMsg());
                    if (response.body().isOk()) {
//                    bean.setStat(3);
//                   msgDao.applyFriend(bean);
                        msgDao.applyRemove(bean.getAid());

                        initData();
                    }

                }
            });
        }
    }

    private void taskRequest(ApplyBean bean) {
        msgAction.groupRequest(bean.getAid(), bean.getGid(), bean.getUid() + "", bean.getNickname(), bean.getAvatar(),
                bean.getJoinType(), bean.getInviter() + "", bean.getInviterName(), new CallBack<ReturnBean>() {
                    @Override
                    public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                        if (response.body().isOk()) {
                            bean.setStat(2);
                            msgDao.applyGroup(bean);
                            initData();

                            groupInfo(bean.getGid());
                        }
                        ToastUtil.show(getContext(), response.body().getMsg());
                    }
                });
    }

    private void groupInfo(String gid){
        msgAction.groupInfo(gid, new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                if (response.body().isOk()) {
                    MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, -1L, gid, CoreEnum.ESessionRefreshTag.SINGLE, null);
                }
            }
        });
    }


    private void taskFriendAgree(ApplyBean bean) {
//        bean.getNickname()
        userAction.friendAgree(bean.getUid(), "", new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }

                ToastUtil.show(getContext(), response.body().getMsg());
                if (response.body().isOk()) {
                    EventBus.getDefault().post(new EventRefreshFriend());
//                    taskGetList();

                    bean.setStat(2);
                    msgDao.applyFriend(bean);
                    initData();
                } else {
                    // ToastUtil.show(getContext(),response.body().getMsg());
                }
            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshApplyEvent(RefreshApplyEvent event) {
        for (int i = 0; i < listData.size(); i++) {
            ApplyBean bean=listData.get(i);
            if(bean.getUid()==event.uid&&bean.getChatType()==event.chatType&&bean.getStat()==event.stat){
                bean.setStat(2);
                msgDao.applyFriend(bean);
                initData();
            }
        }
    }
}
