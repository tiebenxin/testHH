package net.cb.cb.library.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import net.cb.cb.library.R;

import java.util.List;

/***
 * 对话框
 * @author jyj
 * @date 2017/1/5
 */
public class AlertSelectView {
    private AlertDialog alertDialog;
    private android.support.v7.widget.RecyclerView alertList;
    private Event event;
    private List<String> listData;
    private int selIndex = -1;
    private Context context;

    //自动寻找控件
    private void findViews(View rootView) {
        alertList = (android.support.v7.widget.RecyclerView) rootView.findViewById(R.id.alert_list);
    }


    //自动生成的控件事件
    private void initEvent() {
        alertList.setLayoutManager(new LinearLayoutManager(context));
        alertList.setAdapter(new RecyclerViewAdapter());
    }

    public void dismiss() {
        alertDialog.dismiss();
    }

    public void init(AppCompatActivity context, List<String> strs, Event e) {
        event = e;
        listData = strs;
        this.context = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);


        alertDialog = builder.create();

        View rootView = View.inflate(context, R.layout.view_alert_list, null);

        alertDialog.setView(rootView);
        findViews(rootView);
        initEvent();


    }

    public void show() {

        alertDialog.show();
    }

    public interface Event {
        void onSelect(int i);
    }

    //自动生成RecyclerViewAdapter
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RCViewHolder> {

        @Override
        public int getItemCount() {
            if (listData != null)
                return listData.size();
            return 0;
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(RCViewHolder holder, final int position) {
            if(selIndex==position){
                holder.imgAlertSelect.setVisibility(View.VISIBLE);
                holder.txtAlertSelect.setTextColor(context.getResources().getColor(R.color.black));
            }else{
                holder.imgAlertSelect.setVisibility(View.GONE);
                holder.txtAlertSelect.setTextColor(context.getResources().getColor(R.color.gray_400));
            }
            holder.txtAlertSelect.setText(listData.get(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selIndex=position;
                    event.onSelect(position);
                    alertList.getAdapter().notifyDataSetChanged();
                   dismiss();
                }
            });
        }


        //自动寻找ViewHold
        @Override
        public RCViewHolder onCreateViewHolder(ViewGroup view, int i) {

            RCViewHolder holder = new RCViewHolder(LayoutInflater.from(context).inflate(R.layout.list_alert, view, false));
            return holder;
        }


        //自动生成ViewHold
        public class RCViewHolder extends RecyclerView.ViewHolder {
            private TextView txtAlertSelect;
            private ImageView imgAlertSelect;

            //自动寻找ViewHold
            public RCViewHolder(View convertView) {
                super(convertView);
                txtAlertSelect =  convertView.findViewById(R.id.txt_alert_select);
                imgAlertSelect =  convertView.findViewById(R.id.img_alert_select);
            }

        }
    }

}
