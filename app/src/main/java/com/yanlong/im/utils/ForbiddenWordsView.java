package com.yanlong.im.utils;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yanlong.im.R;

import net.cb.cb.library.view.pick.PickValueView;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-25
 * @updateAuthor
 * @updateDate
 * @description
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class ForbiddenWordsView implements View.OnClickListener {

    private Context context;
    TextView tvCancel;
    TextView tvConfirm;
    TextView tvContent;
    PickValueView pickString;
    private DestroyTimeView.OnClickItem onClickItem;
    private Dialog dialog;
    private String[] valueStr = new String[]{"无", "5分钟", "15分钟", "1小时", "12小时", "1天", "3天"};
    private int survivaltime;
    private String content;


    public ForbiddenWordsView(Context context) {
        this.context = context;
    }

    public void initView() {
        dialog = new Dialog(context, R.style.ActionSheetDialogStyle);
        View contentView = LayoutInflater.from(context).inflate(R.layout.view_destroy_time, null);
        //获取组件
        tvCancel = contentView.findViewById(R.id.tv_cancel);
        tvContent = contentView.findViewById(R.id.tv_content);
        tvConfirm = contentView.findViewById(R.id.tv_confirm);
        pickString = contentView.findViewById(R.id.pickString);
        tvCancel.setOnClickListener(this);
        tvConfirm.setOnClickListener(this);
        //获取Dialog的监听

        tvContent.setText("禁言设置");
        pickString.setOnSelectedChangeListener(new PickValueView.onSelectedChangeListener() {
            @Override
            public void onSelected(PickValueView view, Object leftValue, Object middleValue, Object rightValue) {
                content = (String) leftValue;
                survivaltime = getSurvivaltime(content);
            }
        });
        pickString.setValueData(valueStr, valueStr[0]);
        dialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = context.getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        dialog.getWindow().setGravity(Gravity.BOTTOM);//弹窗位置
        dialog.getWindow().setWindowAnimations(R.style.ActionSheetDialogStyle);//弹窗样式
        dialog.show();//显示弹窗
    }


    public void setPostion(int time) {
        survivaltime = time;
        switch (time) {
            case 0:
                pickString.setValueData(valueStr, valueStr[0]);
                break;
            case 300:
                pickString.setValueData(valueStr, valueStr[1]);
                break;
            case 900:
                pickString.setValueData(valueStr, valueStr[2]);
                break;
            case 3600:
                pickString.setValueData(valueStr, valueStr[3]);
                break;
            case 43200:
                pickString.setValueData(valueStr, valueStr[4]);
                break;
            case 86400:
                pickString.setValueData(valueStr, valueStr[5]);
                break;
            case 259200:
                pickString.setValueData(valueStr, valueStr[6]);
                break;
            default:
                pickString.setValueData(valueStr, valueStr[0]);
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                dialog.dismiss();
                break;
            case R.id.tv_confirm:
                if (onClickItem != null) {
                    onClickItem.onClickItem(content, survivaltime);
                }
                dialog.dismiss();
                break;
        }
    }

    public void setListener(DestroyTimeView.OnClickItem onClickItem) {
        this.onClickItem = onClickItem;
    }


    private int getSurvivaltime(String content) {
        if (TextUtils.isEmpty(content)) {
            return 0;
        }
        if (content.equals("无")) {
            return 0;
        } else if (content.equals("5分钟")) {
            return 300;
        } else if (content.equals("15分钟")) {
            return 900;
        } else if (content.equals("1小时")) {
            return 3600;
        } else if (content.equals("12小时")) {
            return 43200;
        } else if (content.equals("1天")) {
            return 86400;
        } else if (content.equals("3天")) {
            return 259200;
        } else {
            return 0;
        }
    }
}
