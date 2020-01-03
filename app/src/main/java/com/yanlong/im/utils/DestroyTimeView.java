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
 * @创建人 shenxin
 * @创建时间 2019/10/23 0023 19:44
 */
public class DestroyTimeView implements View.OnClickListener {
    private Context context;
    TextView tvCancel;
    TextView tvConfirm;
    TextView tvContent;
    PickValueView pickString;
    private OnClickItem onClickItem;
    private Dialog dialog;
    String[] valueStr = new String[]{"关闭", "退出即焚", "5秒", "10秒", "30秒", "1分钟",
            "5分钟", "30分钟", "1小时", "6小时", "12小时", "1天", "一个星期"};
    private int survivaltime;
    private String content;


    public DestroyTimeView(Context context) {
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


        pickString.setOnSelectedChangeListener(new PickValueView.onSelectedChangeListener() {
            @Override
            public void onSelected(PickValueView view, Object leftValue, Object middleValue, Object rightValue) {
                content = (String) leftValue;
                tvContent.setText(content);
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
            case -1:
                pickString.setValueData(valueStr, valueStr[1]);
                break;
            case 5:
                pickString.setValueData(valueStr, valueStr[2]);
                break;
            case 10:
                pickString.setValueData(valueStr, valueStr[3]);
                break;
            case 30:
                pickString.setValueData(valueStr, valueStr[4]);
                break;
            case 60:
                pickString.setValueData(valueStr, valueStr[5]);
                break;
            case 300:
                pickString.setValueData(valueStr, valueStr[6]);
                break;
            case 1800:
                pickString.setValueData(valueStr, valueStr[7]);
                break;
            case 3600:
                pickString.setValueData(valueStr, valueStr[8]);
                break;
            case 21000:
                pickString.setValueData(valueStr, valueStr[9]);
                break;
            case 43200:
                pickString.setValueData(valueStr, valueStr[10]);
                break;
            case 86400:
                pickString.setValueData(valueStr, valueStr[11]);
                break;
            case 604800:
                pickString.setValueData(valueStr, valueStr[12]);
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


    public interface OnClickItem {
        void onClickItem(String content, int survivaltime);

    }


    public void setListener(OnClickItem onClickItem) {
        this.onClickItem = onClickItem;
    }


    private int getSurvivaltime(String content) {
        if (TextUtils.isEmpty(content)) {
            return 0;
        }
        if (content.equals("关闭")) {
            return 0;
        } else if (content.equals("退出即焚")) {
            return -1;
        } else if (content.equals("5秒")) {
            return 5;
        } else if (content.equals("10秒")) {
            return 10;
        } else if (content.equals("30秒")) {
            return 30;
        } else if (content.equals("1分钟")) {
            return 60;
        } else if (content.equals("5分钟")) {
            return 300;
        } else if (content.equals("30分钟")) {
            return 1800;
        } else if (content.equals("1小时")) {
            return 3600;
        } else if (content.equals("6小时")) {
            return 21000;
        } else if (content.equals("12小时")) {
            return 43200;
        } else if (content.equals("1天")) {
            return 86400;
        } else if (content.equals("一个星期")) {
            return 604800;
        } else {
            return 0;
        }
    }
}
