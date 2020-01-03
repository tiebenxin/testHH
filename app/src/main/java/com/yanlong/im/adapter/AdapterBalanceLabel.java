package com.yanlong.im.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.chat.bean.LabelItem;

import java.util.List;

/**
 * @author Liszt
 * @date 2019/12/16
 * Description
 */
public class AdapterBalanceLabel extends BaseAdapter {

    private final List<LabelItem> mList;
    private final Context context;
    private final LayoutInflater inflater;

    public AdapterBalanceLabel(List<LabelItem> l, Context con) {
        mList = l;
        context = con;
        inflater = LayoutInflater.from(con);
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LabelItem bean = mList.get(position);
        LabelViewHolder holder;
        if (convertView == null) {
            holder = new LabelViewHolder(inflater.inflate(R.layout.item_lable, parent));
            convertView = holder.getView();
            convertView.setTag(holder);
        } else {
            holder = (LabelViewHolder) convertView.getTag();
        }
        holder.bindData(bean);
        return convertView;
    }

    class LabelViewHolder {
        private final View root;
        private final TextView tvTitle;
        private final TextView tvContent;

        public LabelViewHolder(View v) {
            root = v;
            tvTitle = v.findViewById(R.id.tv_title);
            tvContent = v.findViewById(R.id.tv_content);
        }

        public View getView() {
            return root;
        }

        public void bindData(LabelItem item) {
            tvTitle.setText(item.getLabel());
            tvContent.setText(item.getValue());
        }
    }
}
