package com.yanlong.im.adapter;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-09
 * @updateAuthor
 * @updateDate
 * @description Recycler共公适配器
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public abstract class CommonRecyclerViewAdapter <T, VB extends ViewDataBinding> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private int mLayoutId;
    private List<T> mList;

    public CommonRecyclerViewAdapter(Context context, int layoutId) {

        mContext = context;
        mLayoutId = layoutId;
    }

    public void setData(List<T> list) {

        mList = list;
        notifyDataSetChanged();
    }

    public void add(List<T> list) {

        if (list == null || list.size() == 0) return;

        int size = getItemCount();

        if (mList == null) {
            mList =  list;
        } else {
            mList.addAll(list);
        }
        notifyItemRangeInserted(size, list.size());
    }

    public void remove(int position) {

        if (position < 0 || position > getItemCount()) return;

        mList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View root = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
        CommonViewHolder viewHolder = new CommonViewHolder(root);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {

        bind((VB)((CommonViewHolder)holder).getBinding(), mList.get(position), position, holder);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        bind((VB)((CommonViewHolder)holder).getBinding(), mList.get(position), position, holder);
    }

    @Override
    public int getItemCount() {

        return mList == null ? 0 : mList.size();
    }

    public List<T> getList() {

        return mList;
    }

    public abstract void bind(VB binding, T data, int position, RecyclerView.ViewHolder viewHolder);
}