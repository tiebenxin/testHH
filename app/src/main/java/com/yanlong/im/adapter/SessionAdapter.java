package com.yanlong.im.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;
import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.GroupImageHead;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.ui.ChatActivity;
import com.yanlong.im.interf.ISessionListener;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.utils.DaoUtil;
import com.yanlong.im.utils.GlideOptionsUtil;
import com.yanlong.im.utils.GroupHeadImageUtil;

import net.cb.cb.library.base.AbstractRecyclerAdapter;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.TimeToString;
import net.cb.cb.library.view.StrikeButton;

import java.io.File;

/**
 * @author Liszt
 * @date 2019/9/25
 * Description
 */
public class SessionAdapter extends AbstractRecyclerAdapter<Session> {

    private UserDao userDao;
    private MsgDao msgDao;
    private final ISessionListener listener;

    public SessionAdapter(Context ctx, ISessionListener l) {
        super(ctx);
        listener = l;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        RCViewHolder viewHolder = (RCViewHolder) holder;
        viewHolder.bindData(mBeanList.get(position));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RCViewHolder holder = new RCViewHolder(mInflater.inflate(R.layout.item_msg_session, parent, false));
        return holder;
    }

    //自动生成ViewHold
    public class RCViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgHead;
        private StrikeButton sb;
        private View viewIt;
        private Button btnDel;
        private SwipeMenuLayout swipeLayout;
        private TextView txtName;
        private TextView txtInfo;
        private TextView txtTime;
        private final ImageView iv_disturb;

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
        }

        public void bindData(final Session bean) {
            String icon = "";
            String title = "";
            String info = "";
            MsgAllBean msginfo = null;
            if (bean.getType() == 0) {//单人
                if (userDao == null) {
                    userDao = new UserDao();
                }
                UserInfo finfo = userDao.findUserInfo(bean.getFrom_uid());
                if (finfo != null) {
                    icon = finfo.getHead();
                    title = finfo.getName4Show();
                }
                if (msgDao == null) {
                    msgDao = new MsgDao();
                }
                //获取最后一条消息
                msginfo = msgDao.msgGetLast4FUid(bean.getFrom_uid());
                if (msginfo != null) {
                    info = msginfo.getMsg_typeStr();
                }

                if (StringUtil.isNotNull(bean.getDraft())) {
                    info = "草稿:" + bean.getDraft();
                }
                txtInfo.setText(info);

                Glide.with(getContext()).load(icon)
                        .apply(GlideOptionsUtil.headImageOptions()).into(imgHead);

            } else if (bean.getType() == 1) {//群
                Group ginfo = msgDao.getGroup4Id(bean.getGid());
                if (ginfo != null) {
                    icon = ginfo.getAvatar();
                    //获取最后一条群消息
                    msginfo = msgDao.msgGetLast4Gid(bean.getGid());
//                    title = ginfo.getName();
                    title = msgDao.getGroupName(bean.getGid());
                    if (msginfo != null) {
                        if (msginfo.getMsg_type() == ChatEnum.EMessageType.NOTICE || msginfo.getMsg_type() == ChatEnum.EMessageType.MSG_CANCEL) {//通知不要加谁发的消息
                            info = msginfo.getMsg_typeStr();
                        } else {
                            String name = "";
                            if (msginfo.getFrom_uid().longValue() != UserAction.getMyId().longValue()) {//自己的不加昵称
                               /* name = msginfo.getFrom_nickname() + " : ";
                                UserInfo fuser = msginfo.getFrom_user();

                                if (fuser != null && StringUtil.isNotNull(fuser.getMkName())) {
                                    name = fuser.getMkName() + " : ";

                                }*/
                                //8.9 处理群昵称
                                name = msgDao.getUsername4Show(msginfo.getGid(), msginfo.getFrom_uid(), msginfo.getFrom_nickname(), msginfo.getFrom_group_nickname()) + " : ";
                            }

                            info = name + msginfo.getMsg_typeStr();
                        }

                    }
                } else {
                    LogUtil.getLog().e("taf", "11来消息的时候没有创建群");
                }

                int type = bean.getMessageType();
                switch (type) {
                    case 0:
                        if (StringUtil.isNotNull(bean.getAtMessage())) {
                            if (msginfo.getMsg_type() == ChatEnum.EMessageType.AT) {
                                SpannableStringBuilder style = new SpannableStringBuilder();
                                style.append("[有人@我]" + bean.getAtMessage());
                                ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red_all_notify));
                                style.setSpan(protocolColorSpan, 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                txtInfo.setText(style);
                            } else {
                                SpannableStringBuilder style = new SpannableStringBuilder();
                                style.append("[有人@我]" + info);
                                ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red_all_notify));
                                style.setSpan(protocolColorSpan, 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                txtInfo.setText(style);
                            }
                        }
                        break;
                    case 1:
                        if (StringUtil.isNotNull(bean.getAtMessage())) {
                            if (msginfo.getMsg_type() == null) {
                                return;
                            }
                            if (msginfo.getMsg_type() == ChatEnum.EMessageType.AT) {
                                SpannableStringBuilder style = new SpannableStringBuilder();
                                style.append("[@所有人]" + bean.getAtMessage());
                                ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red_all_notify));
                                style.setSpan(protocolColorSpan, 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                txtInfo.setText(style);
                            } else {
                                SpannableStringBuilder style = new SpannableStringBuilder();
                                style.append("[@所有人]" + info);
                                ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.red_all_notify));
                                style.setSpan(protocolColorSpan, 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                txtInfo.setText(style);
                            }
                        }
                        break;
                    case 2:
                        if (StringUtil.isNotNull(bean.getDraft())) {
                            info = "草稿:" + bean.getDraft();
                        }
                        txtInfo.setText(info);
                        break;
                    default:
                        txtInfo.setText(info);
                        break;
                }

                LogUtil.getLog().e("TAG", icon.toString());
                if (StringUtil.isNotNull(icon)) {
                    Glide.with(getContext()).load(icon)
                            .apply(GlideOptionsUtil.headImageOptions()).into(imgHead);
                } else {
                    if (bean.getType() == 1) {
                        String imgUrl = "";
                        try {
                            imgUrl = ((GroupImageHead) DaoUtil.findOne(GroupImageHead.class, "gid", bean.getGid())).getImgHeadUrl();
                        } catch (Exception e) {
                            createAndSaveImg(bean, imgHead);
                        }
                        if (StringUtil.isNotNull(imgUrl)) {
                            Glide.with(getContext()).load(imgUrl)
                                    .apply(GlideOptionsUtil.headImageOptions()).into(imgHead);
                        } else {
                            createAndSaveImg(bean, imgHead);
                        }
                    } else {
                        Glide.with(getContext()).load(icon)
                                .apply(GlideOptionsUtil.headImageOptions()).into(imgHead);
                    }
                }

            }


            txtName.setText(title);
            sb.setButtonBackground(R.color.transparent);
            sb.setNum(bean.getUnread_count(), false);
            txtTime.setText(TimeToString.getTimeWx(bean.getUp_time()));
            viewIt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContext().startActivity(new Intent(getContext(), ChatActivity.class)
                            .putExtra(ChatActivity.AGM_TOUID, bean.getFrom_uid())
                            .putExtra(ChatActivity.AGM_TOGID, bean.getGid())
                    );
                }
            });
            btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swipeLayout.quickClose();
                    if (listener != null) {
                        listener.deleteSession(bean.getFrom_uid(), bean.getGid());
                    }
                }
            });
//            holder.viewIt.setBackgroundColor(bean.getIsTop() == 0 ? Color.WHITE : Color.parseColor("#f1f1f1"));
            viewIt.setBackgroundColor(bean.getIsTop() == 0 ? Color.WHITE : Color.parseColor("#ececec"));
            iv_disturb.setVisibility(bean.getIsMute() == 0 ? View.GONE : View.VISIBLE);

        }

        private void createAndSaveImg(Session bean, ImageView imgHead) {
            Group gInfo = msgDao.getGroup4Id(bean.getGid());
            int i = gInfo.getUsers().size();
            i = i > 9 ? 9 : i;
            //头像地址
            String url[] = new String[i];
            for (int j = 0; j < i; j++) {
                MemberUser userInfo = gInfo.getUsers().get(j);
//            if (j == i - 1) {
//                name += userInfo.getName();
//            } else {
//                name += userInfo.getName() + "、";
//            }
                url[j] = userInfo.getHead();
            }
            File file = GroupHeadImageUtil.synthesis(getContext(), url);
            Glide.with(getContext()).load(file)
                    .apply(GlideOptionsUtil.headImageOptions()).into(imgHead);

            MsgDao msgDao = new MsgDao();
            msgDao.groupHeadImgCreate(gInfo.getGid(), file.getAbsolutePath());
        }

    }
}
