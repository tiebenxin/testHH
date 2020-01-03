package com.hm.cxpay.ui.redenvelope;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hm.cxpay.R;
import com.hm.cxpay.bean.UserBean;
import com.hm.cxpay.global.PayEnum;
import com.hm.cxpay.global.PayEnvironment;
import com.hm.cxpay.bean.BankBean;
import com.hm.cxpay.utils.UIUtils;

import net.cb.cb.library.base.AbstractRecyclerAdapter;

/**
 * @author Liszt
 * @date 2019/12/4
 * Description  选择支付方式adapter
 */
public class AdapterSelectPayStyle extends AbstractRecyclerAdapter<BankBean> {

    private ISelectPayStyleListener listener;
    public int currentSelectPosition = 0;

    public AdapterSelectPayStyle(Context ctx) {
        super(ctx);
    }

    public void setSelectPosition(int p) {
        currentSelectPosition = p;
    }

    @Override
    public int getItemCount() {
        //零钱 和 使用新卡支付 各占一个item
        return mBeanList != null ? mBeanList.size() + 2 : 2;
    }

    @Override
    public int getItemViewType(int position) {
        //0 零钱  1 银行  2 使用新卡支付
        if (position == 0) {
            return 0; //零钱
        } else if (position == getItemCount() - 1) {
            return 2;//添加银行卡
        } else {
            return 1;//银行卡
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        if (viewType == 0) {
            holder = new BankViewHolder(mInflater.inflate(R.layout.item_select_bank, parent, false));
        } else if (viewType == 2) {
            holder = new AddBankViewHolder(mInflater.inflate(R.layout.item_add_bank, parent, false));
        } else {
            holder = new BankViewHolder(mInflater.inflate(R.layout.item_select_bank, parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (position == 0) {
            BankViewHolder firstBank = (BankViewHolder) holder;
            firstBank.bindUserBean(PayEnvironment.getInstance().getUser(), position);
        } else if (position == getItemCount() - 1) {
            AddBankViewHolder add = (AddBankViewHolder) holder;
        } else {
            BankViewHolder bank = (BankViewHolder) holder;
            bank.bindData(mBeanList.get(position - 1), position);
        }
    }

    public class BankViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivIcon;
        private final TextView tvContent;
        private final ImageView ivSelected;

        public BankViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvContent = itemView.findViewById(R.id.tv_content);
            ivSelected = itemView.findViewById(R.id.iv_selected);
        }

        public void bindData(final BankBean bean, int position) {
            Glide.with(getContext()).load(bean.getLogo()).into(ivIcon);
            String num = bean.getCardNo();
            String cardNum = num.substring(num.length() - 4);
            tvContent.setText(bean.getBankName() + "(" + cardNum + ")");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onSelectPay(PayEnum.EPayStyle.BANK, bean);
                    }
                }
            });
            if (position == currentSelectPosition) {
                ivSelected.setVisibility(View.VISIBLE);
            } else {
                ivSelected.setVisibility(View.GONE);
            }
        }

        public void bindUserBean(UserBean bean, int position) {
            ivIcon.setImageResource(R.mipmap.ic_loose);
            tvContent.setText("零钱(剩余¥" + UIUtils.getYuan(bean.getBalance()) + ")");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onSelectPay(PayEnum.EPayStyle.LOOSE, null);
                    }
                }
            });
            if (position == currentSelectPosition) {
                ivSelected.setVisibility(View.VISIBLE);
            } else {
                ivSelected.setVisibility(View.GONE);
            }

        }


    }

    public class AddBankViewHolder extends RecyclerView.ViewHolder {
        public AddBankViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onAddBank();
                    }
                }
            });
        }
    }

    public void setListener(ISelectPayStyleListener l) {
        listener = l;
    }

    public interface ISelectPayStyleListener {
        void onSelectPay(@PayEnum.EPayStyle int style, BankBean bank);

        void onAddBank();

        void onBack();
    }

}
