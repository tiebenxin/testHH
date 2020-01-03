package com.luck.picture.lib.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;


import com.luck.picture.lib.R;

import java.util.Arrays;
import java.util.List;


public class PopupSelectView extends PopupWindow {
    Context mContext;
    Activity activity;
    private LayoutInflater mInflater;
    private View mContentView;
    private RecyclerView recyclerView;
    private PopupSelectAdpter PopupSelectAdpter;
    private OnClickItemListener listener;
    private List<String> list;


    public PopupSelectView(Context context, String[] list) {
        super(context);
        this.activity = (Activity) context;
        this.list = Arrays.asList(list);
        this.mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = mInflater.inflate(R.layout.popup_select_view, null);

        //设置View
        setContentView(mContentView);

        //设置宽与高
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new ColorDrawable());
        setFocusable(true);
        setOutsideTouchable(true);
        setTouchable(true);
        setAnimationStyle(R.style.popup_anim);

        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 0.3f;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        activity.getWindow().setAttributes(lp);
        /**
         * 设置点击外部可以消失
         */
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /**
                 * 判断是不是点击了外部
                 */
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    return true;
                }
                //不是点击外部
                return false;
            }
        });

        initView();
        initListener();
    }



    private void initView() {
        PopupSelectAdpter = new PopupSelectAdpter(list, mContext);
        recyclerView = mContentView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(PopupSelectAdpter);
    }


    private void initListener() {
        PopupSelectAdpter.setListener(new PopupSelectAdpter.OnClickItemListener() {
            @Override
            public void onItem(String string, int postsion) {
                if (listener != null) {
                    listener.onItem(string, postsion);
                }
            }
        });

        this.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                lp.alpha = 1.0f;
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                activity.getWindow().setAttributes(lp);
            }
        });
    }

    public void setListener(OnClickItemListener onClickItemListener) {
        this.listener = onClickItemListener;
    }

    public interface OnClickItemListener {
        void onItem(String string, int postsion);
    }


}
