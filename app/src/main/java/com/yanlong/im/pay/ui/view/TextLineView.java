package com.yanlong.im.pay.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.yanlong.im.R;

public class TextLineView extends RelativeLayout {
    private Context mContext;
    public TextLineView(Context context) {
        super(context);
        initView();
    }

    public TextLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
        initView();
    }

    public TextLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        initView();
    }

    public TextLineView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext=context;
        initView();
    }


    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.text_line_item, this);
    }
}
