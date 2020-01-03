package net.cb.cb.library.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.cb.cb.library.R;
import net.cb.cb.library.utils.DensityUtil;

/***
 * 对话框
 * @author jyj
 * @date 2017/1/5
 */
public class AlertYesNo {
    private AlertDialog alertDialog;
    private Event event;

    private Context context;
    private TextView txtAlertTitle;
    private TextView txtAlertMsg;
    private LinearLayout viewNo;
    private Button btnCl;
    private Button btnOk;


    //自动寻找控件
    private void findViews(View rootview) {
        txtAlertTitle = (TextView) rootview.findViewById(R.id.txt_alert_title);
        txtAlertMsg = (TextView) rootview.findViewById(R.id.txt_alert_msg);
        viewNo = (LinearLayout) rootview.findViewById(R.id.view_no);
        btnCl = (Button) rootview.findViewById(R.id.btn_cl);
        btnOk = (Button) rootview.findViewById(R.id.btn_ok);
    }


    //自动生成的控件事件
    private void initEvent(String title, String msg, String y, String n) {
        if (n == null) {
            viewNo.setVisibility(View.GONE);
        } else {
            btnCl.setText(n);
        }
        if (title == null) {
            txtAlertTitle.setVisibility(View.GONE);
        } else {
            txtAlertTitle.setText(title);
        }
        txtAlertMsg.setText(msg);
        btnOk.setText(y);
        btnCl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                event.onON();
                dismiss();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                event.onYes();
                dismiss();
            }
        });

    }


    public void dismiss() {
        alertDialog.dismiss();
    }

    public void init(Context activity, String title, String msg, String y, String n, Event e) {
        event = e;

        this.context = activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);


        alertDialog = builder.create();
        alertDialog.setCancelable(false);

        View rootView = View.inflate(context, R.layout.view_alert_yes_no, null);

        alertDialog.setView(rootView);
        findViews(rootView);

        initEvent(title, msg, y, n);


    }

    public boolean isShowing() {
        return alertDialog.isShowing();
    }

    public void show() {

        alertDialog.show();

        Window window = alertDialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams p = window.getAttributes();
        // p.height = DensityUtil.dip2px(activity, 226);
        p.width = DensityUtil.dip2px(context, 300);

        window.setAttributes(p);
    }

    public interface Event {
        void onON();

        void onYes();
    }


}
