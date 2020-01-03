package net.cb.cb.library.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.cb.cb.library.R;

import java.util.List;

public class PopupSelectAdpter extends RecyclerView.Adapter<PopupSelectAdpter.SelectViewHolder> {

    private List<String> list;
    private Context context;
    private OnClickItemListener listener;

    public PopupSelectAdpter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setListener(OnClickItemListener onClickItemListener) {
        this.listener = onClickItemListener;
    }

    @Override
    public PopupSelectAdpter.SelectViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        SelectViewHolder holder = new SelectViewHolder(LayoutInflater.from(context).inflate(R.layout.list_select, viewGroup, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PopupSelectAdpter.SelectViewHolder viewHolder, final int i) {
        viewHolder.mTvContent.setText(list.get(i));
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItem(list.get(i), i);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        if (list != null && list.size() > 0) {
            return list.size();
        }
        return 0;
    }

    public interface OnClickItemListener {
        void onItem(String string, int postsion);
    }

    public class SelectViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvContent;
        private View mViewLine;
        private View view;


        public SelectViewHolder(View convertView) {
            super(convertView);
            this.view = convertView;
            mTvContent = convertView.findViewById(R.id.tv_content);
            mViewLine = convertView.findViewById(R.id.view_line);
        }
    }
}
