package net.cb.cb.library.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import net.cb.cb.library.R;
import net.cb.cb.library.utils.DensityUtil;

/***
 * 对话框
 * @author jyj
 * @date 2017/1/5
 */
public class AlertTouch {
    private AlertDialog alertDialog;
    private Event event;
    private Context context;
    private ImageView mIvImage;
    private TextView mTxtAlertMsg;
    private EditText mEdContent;
    private Button mBtnCl;
    private Button mBtnOk;


    //自动寻找控件
    private void findViews(View rootview) {
        mIvImage = rootview.findViewById(R.id.iv_image);
        mTxtAlertMsg = rootview.findViewById(R.id.txt_alert_msg);
        mEdContent = rootview.findViewById(R.id.ed_content);
        mBtnCl = rootview.findViewById(R.id.btn_cl);
        mBtnOk = rootview.findViewById(R.id.btn_ok);
    }


    //自动生成的控件事件
    private void initEvent(String title, String y, int image) {
        if (image == 0) {
            mIvImage.setVisibility(View.GONE);
        } else {
            mIvImage.setImageResource(image);
        }

        mTxtAlertMsg.setText(title);
        mBtnOk.setText(y);
        mBtnCl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                event.onON();
                dismiss();
            }
        });

        mBtnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                event.onYes(mEdContent.getText().toString());
                dismiss();
            }
        });


    }


    public void dismiss() {
        alertDialog.dismiss();
    }

    public void init(Activity activity, String title, String y, int image, Event e) {
        event = e;
        this.context = activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        alertDialog = builder.create();
        View rootView = View.inflate(context, R.layout.view_alert_touch, null);
        alertDialog.setView(rootView);
        findViews(rootView);
        initEvent(title, y, image);
    }


    public void setContent(String content){
        mEdContent.setText(content);
    }


    public void setEdHintOrSize(String hint,int size){
        if(!TextUtils.isEmpty(hint)){
            mEdContent.setHint(hint);
        }
        mEdContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(size)});
    }


    public void show() {
        alertDialog.show();
        Window window = alertDialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams p = window.getAttributes();
        // p.height = DensityUtil.dip2px(activity, 226);
        p.width = DensityUtil.dip2px(context, 300);
        alertDialog.getWindow().setAttributes(p);
    }


    public interface Event {
        void onON();

        void onYes(String content);
    }


}
