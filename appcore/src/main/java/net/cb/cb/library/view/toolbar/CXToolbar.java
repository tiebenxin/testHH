package net.cb.cb.library.view.toolbar;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.cb.cb.library.R;


/**
 * Created by Liszt on 2019/11/27.
 * 自定义标题栏
 */

public class CXToolbar extends Toolbar {
    protected Context mContext;
    /**
     * 标题
     */
    protected TextView mTvTitle;
    /**
     * 可以在“ll_title_right” 添加其他的布局
     */
    protected LinearLayout ll_title_right;

    private OnClickListener confirmListener;
    private View rootView;

    public CXToolbar(Context context) {
        this(context, null);
    }

    public CXToolbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CXToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView(mContext);
    }

    private void initView(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.fg_toolbar, this, true);
        ll_title_right = rootView.findViewById(R.id.ll_right);
        mTvTitle = rootView.findViewById(R.id.toolbar_title_tv);

        //默认为隐藏
        mTvTitle.setVisibility(GONE);
        ll_title_right.setVisibility(GONE);
    }

    public CXToolbar setTitleText(String title) {
        if (!TextUtils.isEmpty(title)) {
            mTvTitle.setVisibility(VISIBLE);
            mTvTitle.setText(title);
        }
        return this;
    }

    public CXToolbar setTitleText(int textId) {
        mTvTitle.setVisibility(VISIBLE);
        mTvTitle.setText(textId);

        return this;
    }


    public CXToolbar initRightView(View v) {
        if (v == null) {
            ll_title_right.removeAllViews();
            return this;
        }
        ll_title_right.removeAllViews();
        ll_title_right.addView(v);
        ll_title_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRightFirstListener != null) {
                    mRightFirstListener.onClick();
                }
            }
        });
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRightFirstListener != null) {
                    mRightFirstListener.onClick();
                }
            }
        });
        return this;
    }

    public void setBackGround(int rid) {
        rootView.setBackgroundResource(rid);
    }

    private OnFGToolbarClickListenter mRightFirstListener;

    public void setConfirmListener(OnFGToolbarClickListenter l) {
        mRightFirstListener = l;
    }

    public interface OnFGToolbarClickListenter {

        void onClick();

    }
}
