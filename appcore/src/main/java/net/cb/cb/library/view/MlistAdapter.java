package net.cb.cb.library.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.cb.cb.library.R;
import net.cb.cb.library.utils.LogUtil;

import java.util.List;

//自动生成RecyclerViewAdapter
public class MlistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private RecyclerView.Adapter adapter;

    public MlistAdapter(Context c, RecyclerView.Adapter adapter) {
        context = c;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.adapter = adapter;
    }

    @Override
    public int getItemCount() {
        return adapter.getItemCount() + 1;
    }

    /***
     * 数据列表
     * @return
     */
    public int getItemCounts() {
        return adapter.getItemCount();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
//        try {
            if (payloads == null || payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads);
            } else {
                adapter.onBindViewHolder(holder, position, payloads);
            }

//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    //自动生成控件事件
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if ((position + 1) == getItemCount()) {//底部
            RCLoadMoreViewHolder holdLoad = (RCLoadMoreViewHolder) holder;
            switch (loadState) {
                case LOAD_STATE_NOMORL:
                    holdLoad.progressBar.setVisibility(View.VISIBLE);
                    holdLoad.txtLoadOver.setVisibility(View.GONE);
                    break;
                case LOAD_STATE_COMPLETE:
                    holdLoad.progressBar.setVisibility(View.GONE);
                    holdLoad.txtLoadOver.setVisibility(View.VISIBLE);
                    break;
                case LOAD_STATE_NULL:
                    holdLoad.progressBar.setVisibility(View.GONE);
                    holdLoad.txtLoadOver.setVisibility(View.GONE);
                    break;
            }

            //   setLoadState(0);
        } else {
            adapter.onBindViewHolder(holder, position);
        }
    }

    /**
     * 处理当时Gridview类型的效果时，也把头部和尾部设置成一整行（这就是RecyclerView的其中一个优秀之处，列表的每行可以不同数量的列）
     *
     * @param recyclerView
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            /**
             * getSpanSize的返回值的意思是：position位置的item的宽度占几列
             * 比如总的是4列，然后头部全部显示的话就应该占4列，此时就返回4
             * 其他的只占一列，所以就返回1，剩下的三列就由后面的item来依次填充。
             */
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {

                    if (position == getItemCount() - 1) {
                        return ((GridLayoutManager) layoutManager).getSpanCount();
                    }
                    return 1;
                }
            });
        }
    }

    private int loadState = LOAD_STATE_NULL;
    public static final int LOAD_STATE_NULL = -1;
    public static final int LOAD_STATE_NOMORL = 0;
    public static final int LOAD_STATE_COMPLETE = 1;

    public void setLoadState(int LOAD_STATE_NOMORL) {
        loadState = LOAD_STATE_NOMORL;
    }

    private int loadMoreType = 9999;

    @Override
    public int getItemViewType(int position) {
        if ((position + 1) == getItemCount())
            return loadMoreType;
        return adapter.getItemViewType(position);
    }

    //自动寻找ViewHold
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup view, int i) {
        if (i == loadMoreType) {
            return new RCLoadMoreViewHolder(inflater.inflate(R.layout.list_loadmore, view, false));
        } else {
            return adapter.onCreateViewHolder(view, i);
        }


    }


    //自动生成ViewHold
    public class RCLoadMoreViewHolder extends RecyclerView.ViewHolder {
        private View progressBar;
        private TextView txtLoadOver;

        //自动寻找ViewHold
        public RCLoadMoreViewHolder(View convertView) {
            super(convertView);
            progressBar = convertView.findViewById(R.id.progressBar);
            txtLoadOver = convertView.findViewById(R.id.txt_load_over);
        }

    }

    /*
     * adapter嵌套，无效方法
     * */
    public void notifyItemRangeChange(int start, int size) {
        if (adapter != null) {
            adapter.notifyItemRangeInserted(start, size);
        }
    }


}
