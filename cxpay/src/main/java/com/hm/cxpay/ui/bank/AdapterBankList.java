package com.hm.cxpay.ui.bank;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hm.cxpay.R;
import com.hm.cxpay.bean.BankBean;

import net.cb.cb.library.base.AbstractRecyclerAdapter;

/**
 * @author Liszt
 * @date 2019/11/30
 * Description
 */
public class AdapterBankList extends AbstractRecyclerAdapter<BankBean> {

    private Context context;

    public AdapterBankList(Context c) {
        super(c);
        context = c;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new BankViewHolder(mInflater.inflate(R.layout.item_bank, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof BankViewHolder) {
            BankViewHolder holder = (BankViewHolder) viewHolder;
            holder.bindData(mBeanList.get(i));
        }
    }

    @Override
    public int getItemCount() {
        return mBeanList == null ? 0 : mBeanList.size();
    }

    class BankViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivIcon;
        private final TextView tvBankName;
        private final TextView tvBankNum;
        private final View rootView;
        private RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        public BankViewHolder(@NonNull View v) {
            super(v);
            rootView = v;
            ivIcon = v.findViewById(R.id.iv_icon);
            tvBankName = v.findViewById(R.id.tv_bank_name);
            tvBankNum = v.findViewById(R.id.tv_bank_num);


        }

        private void bindData(final BankBean bank) {
            if(!TextUtils.isEmpty(bank.getLogo())){
                Glide.with(context).load(bank.getLogo())
                        .apply(options).into(ivIcon);
            }else {
                ivIcon.setImageResource(R.mipmap.ic_bank_zs);
            }
            if(!TextUtils.isEmpty(bank.getBankName())){
                tvBankName.setText(bank.getBankName());
            }
            if(!TextUtils.isEmpty(bank.getCardNo())){
                tvBankNum.setText(bank.getCardNo());
            }

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(bank);
                    }
                }
            });
        }
    }
}
