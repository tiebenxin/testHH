package net.cb.cb.library.view;

import android.app.Activity;
import android.content.DialogInterface.OnCancelListener;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.cb.cb.library.AppConfig;
import net.cb.cb.library.R;
import net.cb.cb.library.utils.DensityUtil;


/***
 * 等待对话框
 *
 * @author 姜永健
 * @date 2015年8月12日
 */
public class AlertWait {

    private AlertDialog dialog;

    private Activity act;

    public AlertWait(Activity act) {
        this.act = act;
    }

    private ProgressBar progressNum;

    /***
     * 写在activity中onDestroy中super.onDestroy();前 防止窗体泄漏
     */
    public void dismiss4distory() {


        if (dialog != null)
            dialog.dismiss();

    }

    public void dismiss() {
        if (dialog != null)
            dialog.dismiss();
    }

    public ProgressBar getProgressNum() {
        return progressNum;
    }
    public void setNum(int num){

        progressNum.setProgress(num);
     //   progressNum.postInvalidate();
    }

    /***
     * 显示等待框(默认可取消)
     *
     */
    public void show() {
        // TODO Auto-generated method stub
        show(null, false, false, null);
    }

    public void show(String msg,Boolean isNum) {
        show(msg, isNum, false, null);
    }

    /***
     * 显示等待框
     *
     * @param msg        提示信息
     * @param cancelable 是否可取消
     */
    public void show(String msg, Boolean isNum, Boolean cancelable, OnCancelListener dismissListener) {
        // TODO Auto-generated method stub
        AlertDialog.Builder alert = new AlertDialog.Builder(act);

        // View v = LayoutInflater.from(act).inflate(R.layout.view_alert_wait, null);
        View v = LayoutInflater.from(act).inflate(R.layout.fgm_wait, null);
        TextView txtTitle = (TextView) v.findViewById(R.id.txt_title);
        progressNum = (ProgressBar) v.findViewById(R.id.progress_num);
        View viewNet = v.findViewById(R.id.view_net);



            alert.setCancelable(cancelable);
      /*  if (msg != null) {
            txtMsg.setText(msg);
        } else {
            txtMsg.setVisibility(View.GONE);
        }*/

        if (msg == null) {
            txtTitle.setVisibility(View.GONE);
        } else {
            txtTitle.setText(msg);
            txtTitle.setVisibility(View.VISIBLE);
        }
        if (isNum) {
            progressNum.setVisibility(View.VISIBLE);
            viewNet.setVisibility(View.GONE);
        } else {
            progressNum.setVisibility(View.GONE);
            viewNet.setVisibility(View.VISIBLE);
        }

        alert.setView(v);
        dialog = alert.create();
        //设置背景透明
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        // p.height = DensityUtil.dip2px(activity, 226);
        p.width = DensityUtil.dip2px(AppConfig.APP_CONTEXT,200);
        dialog.getWindow().setAttributes(p);

        dialog.setOnCancelListener(dismissListener);

        dialog.show();
    }

    public boolean isShown() {
        if (dialog == null) {
            return false;
        }
        return dialog.isShowing();
    }


}
