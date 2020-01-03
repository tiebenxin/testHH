package com.yanlong.im.chat.ui.forward;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yanlong.im.R;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.base.AbstractRecyclerAdapter;
import net.cb.cb.library.utils.TimeToString;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.ViewUtils;

/**
 * @author Liszt
 * @date 2019/8/12
 * Description
 *  消息转发 通讯录
 */
public class AdapterForwardRoster extends AbstractRecyclerAdapter {
    private Context context;
    private IForwardRosterListener listener;

    public AdapterForwardRoster(Context ctx) {
        super(ctx);
        context = ctx;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new RCViewMucHolder(mInflater.inflate(R.layout.item_select_muc, parent, false));
        } else {
            return new RCViewHolder(mInflater.inflate(R.layout.item_msg_friend, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof RCViewHolder) {
            UserInfo info = (UserInfo) mBeanList.get(position - 1);
            RCViewHolder viewHolder = (RCViewHolder) holder;
            viewHolder.bindData(info, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return mBeanList != null ? mBeanList.size() + 1 : 0;
    }

    public void setForwardListener(IForwardRosterListener l) {
        listener = l;
    }

    public UserInfo getUserByPosition(int position) {
        if (position < getItemCount() - 1) {
            return (UserInfo) mBeanList.get(position);
        }
        return null;
    }

    //自动生成ViewHold
    public class RCViewHolder extends RecyclerView.ViewHolder {
        private TextView txtType;
        private ImageView imgHead,ivSelect;
        private TextView txtName;
        private TextView txtTime;
        private View viewType;

        //自动寻找ViewHold
        public RCViewHolder(View convertView) {
            super(convertView);
            txtType = convertView.findViewById(R.id.txt_type);
            imgHead = convertView.findViewById(R.id.img_head);
            txtName = convertView.findViewById(R.id.txt_name);
            txtTime = convertView.findViewById(R.id.txt_time);
            viewType = convertView.findViewById(R.id.view_type);
            ivSelect = convertView.findViewById(R.id.iv_select);
        }

        public void bindData(final UserInfo bean, final int position) {
            txtType.setText(bean.getTag());
            //imgHead.setImageURI(Uri.parse("" + bean.getHead()));

            Glide.with(context).load(bean.getHead())
                    .apply(GlideOptionsUtil.headImageOptions()).into(imgHead);

            txtName.setText(bean.getName4Show());
            if (bean.getLastonline() > 0) {
                txtTime.setText(TimeToString.getTimeOnline(bean.getLastonline(), bean.getActiveType(), false));
                txtTime.setVisibility(View.VISIBLE);
            } else {
                txtTime.setVisibility(View.GONE);
            }

            if (position > 1) {
                UserInfo lastBean = getUserByPosition(position - 2);
                if (lastBean.getTag().equals(bean.getTag())) {
                    viewType.setVisibility(View.GONE);
                } else {
                    viewType.setVisibility(View.VISIBLE);
                }
            } else if (position == 1) {
                viewType.setVisibility(View.VISIBLE);
            } else {
                viewType.setVisibility(View.GONE);
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ViewUtils.isFastDoubleClick()){
                        return;
                    }
                    if (listener == null) {
                        return;
                    }

                    if(MsgForwardActivity.isSingleSelected){
                        if (listener != null) {
                            listener.onForward(bean.getUid(), "", bean.getHead(), bean.getName4Show());
                        }
                    }else {
                        if(bean.isChecked()){
                            bean.setChecked(false);
                            ivSelect.setSelected(false);

                            MsgForwardActivity.addOrDelectMoreSessionBeanList(false,bean.getUid(), "", bean.getHead(), bean.getName4Show());
                        }else {

                            if(MsgForwardActivity.moreSessionBeanList.size()>=MsgForwardActivity.maxNumb){
                                ToastUtil.show(context, "最多选择"+MsgForwardActivity.maxNumb+"个");
                                return;
                            }

                            bean.setChecked(true);
                            ivSelect.setSelected(true);
                            MsgForwardActivity.addOrDelectMoreSessionBeanList(true,bean.getUid(), "", bean.getHead(), bean.getName4Show());
                        }

//                        LogUtil.getLog().e(getAdapterPosition()+"=信息==="+(finalIsGroup? -1L : bean.getFrom_uid())+"==0=="+ bean.getGid()+ "==0="+finalIcon+"=0===="+ finalTitle);
                    }
                }
            });

            if(MsgForwardActivity.isSingleSelected){
                ivSelect.setVisibility(View.GONE);
            }else {
                ivSelect.setVisibility(View.VISIBLE);

                boolean hasSelect=MsgForwardActivity.findMoreSessionBeanList(bean.getUid(), "");
//                LogUtil.getLog().e(getAdapterPosition()+"======hasSelect=="+hasSelect);
                if(hasSelect){
                    bean.setChecked(true);
                    ivSelect.setSelected(true);
                }else {
                    bean.setChecked(false);
                    ivSelect.setSelected(false);
                }
            }
        }

    }


    //自动生成ViewHold
    public class RCViewMucHolder extends RecyclerView.ViewHolder {
        private LinearLayout ll_root;

        public RCViewMucHolder(@NonNull View itemView) {
            super(itemView);
            ll_root = itemView.findViewById(R.id.ll_root);
            ll_root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener == null) {
                        return;
                    }
                    listener.onSelectMuc();
                }
            });
        }
    }

}
