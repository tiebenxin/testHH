package net.cb.cb.library.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Liszt
 * @date 2019/8/10
 * Description
 */
public abstract class AbstractRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected List<T> mBeanList;
    protected Context mContext;
    protected LayoutInflater mInflater;
    protected OnItemClickListener mItemClickListener;

    public AbstractRecyclerAdapter(Context ctx) {
        mContext = ctx;
        mInflater = LayoutInflater.from(mContext);
        mBeanList = new ArrayList<T>();
    }

    public void bindData(List<T> l) {
        if (mBeanList == null) {
            mBeanList = l;
        } else {
            mBeanList.clear();
            mBeanList.addAll(l);
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mBeanList == null ? 0 : mBeanList.size();
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T bean);
    }

    public void setItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public Context getContext() {
        return mContext;
    }


}
