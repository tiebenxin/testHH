package com.yanlong.im.chat.ui.forward;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.wight.avatar.MultiImageView;

import net.cb.cb.library.base.AbstractRecyclerAdapter;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Liszt
 * @date 2019/8/10
 * Description
 *  消息转发 最近聊天
 */
public class AdapterForwardSession extends AbstractRecyclerAdapter {

    private UserDao userDao;
    private MsgDao msgDao;
    private IForwardListener listener;
    private Context context;

    public AdapterForwardSession(Context ctx) {
        super(ctx);
        context = ctx;
    }

    public void initDao(UserDao user, MsgDao msg) {
        userDao = user;
        msgDao = msg;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RCViewHolder(mInflater.inflate(R.layout.item_msg_forward, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        RCViewHolder viewHolder = (RCViewHolder) holder;
        viewHolder.bindData((com.yanlong.im.chat.bean.Session) mBeanList.get(position));
    }

    //自动生成ViewHold
    class RCViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout viewIt;
        private ImageView ivSelect;
        private MultiImageView imgHead;
        private TextView txtName;

        //自动寻找ViewHold
        public RCViewHolder(View convertView) {
            super(convertView);
            viewIt = convertView.findViewById(R.id.view_it);
            imgHead = convertView.findViewById(R.id.img_head);
            txtName = convertView.findViewById(R.id.txt_name);
            ivSelect = convertView.findViewById(R.id.iv_select);
        }

        public void bindData(final com.yanlong.im.chat.bean.Session bean) {
            String icon = "";
            String title = "";
            boolean isGroup = false;
            // 头像集合
            List<String> headList = new ArrayList<>();
            if (bean.getType() == 0) {//单人
                userDao = new UserDao();
                UserInfo finfo = userDao.findUserInfo(bean.getFrom_uid());
                if (finfo != null) {
                    icon = finfo.getHead();
                    title = finfo.getName4Show();
                }
            } else if (bean.getType() == 1) {//群
                isGroup = true;
                msgDao = new MsgDao();
                Group ginfo = msgDao.getGroup4Id(bean.getGid());
                if (ginfo != null) {
                    icon = ginfo.getAvatar();
                    //获取最后一条群消息
//                    title = ginfo.getName();
                    title = msgDao.getGroupName(ginfo.getGid());
                } else {

                }
            }

            if (StringUtil.isNotNull(icon)) {
                headList.add(icon);
                imgHead.setList(headList);
            } else {
                loadGroupHeads(bean, imgHead);
            }

            txtName.setText(title);

            final String finalTitle = title;
            final String finalIcon = icon;

            final boolean finalIsGroup = isGroup;
            viewIt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ViewUtils.isFastDoubleClick()){
                        return;
                    }
                    if(MsgForwardActivity.isSingleSelected){
                        if (listener != null) {
                            listener.onForward(finalIsGroup ? -1L : bean.getFrom_uid(), bean.getGid(), finalIcon, finalTitle);
                        }
                    }else {
                        if(bean.getSelect()){
                            bean.setSelect(false);
                            ivSelect.setSelected(false);

                            MsgForwardActivity.addOrDelectMoreSessionBeanList(false,finalIsGroup ? -1L : bean.getFrom_uid(), bean.getGid(), finalIcon, finalTitle);
                        }else {

                            if(MsgForwardActivity.moreSessionBeanList.size()>=MsgForwardActivity.maxNumb){
                                ToastUtil.show(context, "最多选择"+MsgForwardActivity.maxNumb+"个");
                                return;
                            }

                            bean.setSelect(true);
                            ivSelect.setSelected(true);
                            MsgForwardActivity.addOrDelectMoreSessionBeanList(true,finalIsGroup ? -1L : bean.getFrom_uid(), bean.getGid(), finalIcon, finalTitle);
                        }

//                        LogUtil.getLog().e(getAdapterPosition()+"=信息==="+(finalIsGroup? -1L : bean.getFrom_uid())+"==0=="+ bean.getGid()+ "==0="+finalIcon+"=0===="+ finalTitle);
                    }
                }
            });

            if(MsgForwardActivity.isSingleSelected){
                ivSelect.setVisibility(View.GONE);
            }else {
                ivSelect.setVisibility(View.VISIBLE);

                boolean hasSelect=MsgForwardActivity.findMoreSessionBeanList(finalIsGroup ? -1L : bean.getFrom_uid(), bean.getGid());
//                LogUtil.getLog().e(getAdapterPosition()+"======hasSelect=="+hasSelect);
                if(hasSelect){
                    bean.setSelect(true);
                    ivSelect.setSelected(true);
                }else {
                    bean.setSelect(false);
                    ivSelect.setSelected(false);
                }
            }
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

    }

    public void setForwardListener(IForwardListener l) {
        listener = l;
    }
}
