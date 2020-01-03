package net.cb.cb.library.view;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.cb.cb.library.R;

/***
 * 对话框
 * @author jyj
 * @date 2017/1/5
 */
public class AlertMyView {
    private AlertDialog alertDialog;


    private Context context;
    private TextView txtAlertTitle;
    private LinearLayout viewAdd;


    //自动寻找控件
    private void findViews(View rootView) {
        txtAlertTitle = (TextView) rootView.findViewById(R.id.txt_alert_title);
        viewAdd = (LinearLayout) rootView.findViewById(R.id.view_add);
    }

    public AlertDialog getAlertDialog() {
        return alertDialog;
    }

    //自动生成的控件事件
    private void initEvent(String title,View add) {
        viewAdd.addView(add);
        if(title==null){
            txtAlertTitle.setVisibility(View.GONE);
        }
        txtAlertTitle.setText(title);
    }


    public void dismiss() {
        alertDialog.dismiss();
    }

    public void init(AppCompatActivity activity,String title, View view) {


        this.context = activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);


        alertDialog = builder.create();

        View rootView = View.inflate(context, R.layout.view_alert_my, null);

        alertDialog.setView(rootView);
        findViews(rootView);

        initEvent(title,view);


    }

    public void show() {

        alertDialog.show();
    }


}
