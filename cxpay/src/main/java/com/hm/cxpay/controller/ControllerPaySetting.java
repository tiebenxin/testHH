package com.hm.cxpay.controller;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hm.cxpay.R;
import com.hm.cxpay.utils.UIUtils;

import net.cb.cb.library.AppConfig;

/**
 * @author Liszt
 * @date 2019/11/27
 * Description 支付设置controller
 */
public class ControllerPaySetting {

    private final View rootView;
    private final ImageView ivIcon;
    private final TextView tvTitle;
    private final TextView tvRight;
    private OnControllerClickListener listener;

    public ControllerPaySetting(View v) {
        rootView = v;
        ivIcon = v.findViewById(R.id.iv_icon);
        tvTitle = v.findViewById(R.id.tv_title);
        tvRight = v.findViewById(R.id.tv_right);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick();
                }
            }
        });
    }

    public void init(int icon, int string, String right) {
        ivIcon.setBackground(UIUtils.getDrawable(AppConfig.getContext(), icon));
        tvTitle.setText(string);
        if (!TextUtils.isEmpty(right)) {
            tvRight.setVisibility(View.VISIBLE);
            tvRight.setText(right);
        } else {
            tvRight.setVisibility(View.GONE);
        }
    }

    public void setOnClickListener(OnControllerClickListener l) {
        listener = l;
    }

    public interface OnControllerClickListener {
        void onClick();
    }

    //获取右侧文字内容
    public TextView getRightTitle() {
        return tvRight;
    }

    public void setEnabled(boolean enabled) {
        rootView.setEnabled(enabled);
    }

    public void setVisible(boolean isVisible) {
        rootView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
