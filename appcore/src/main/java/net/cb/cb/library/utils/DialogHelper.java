package net.cb.cb.library.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import net.cb.cb.library.R;
import net.cb.cb.library.inter.ICustomerItemClick;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-10-15
 * @updateAuthor
 * @updateDate
 * @description 弹框工具类
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class DialogHelper {

    private static DialogHelper INSTANCE;

    public static DialogHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DialogHelper();
        }
        return INSTANCE;
    }

    /**
     * 音视频通话弹框
     *
     * @param context
     * @param iCustomerItemClick
     */
    public void createSelectDialog(Context context, final ICustomerItemClick iCustomerItemClick) {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();

        View dialogview = LayoutInflater.from(context).inflate(R.layout.dialog_select, null);
        final Dialog selectDialog = new Dialog(context, R.style.upload_image_methods_dialog);
        selectDialog.setContentView(dialogview);
        Window window = selectDialog.getWindow();
        WindowManager.LayoutParams dialogParams = window.getAttributes();
        dialogParams.gravity = Gravity.BOTTOM;
        dialogParams.width = width;
//        Rect rect = new Rect();
//        View view1 = window.getDecorView();
//        view1.getWindowVisibleDisplayFrame(rect);
//        window.setWindowAnimations(com.internalkye.express.R.style.Animation_Popup);
//        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setAttributes(dialogParams);
        // 视频通话
        dialogview.findViewById(R.id.layout_video).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (selectDialog != null) {
                    selectDialog.dismiss();
                }
                if (!ViewUtils.isFastDoubleClick()) {
                    iCustomerItemClick.onClickItemVideo();
                }
            }
        });
        // 语音通话
        dialogview.findViewById(R.id.layout_voice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectDialog != null) {
                    selectDialog.dismiss();
                }
                if (!ViewUtils.isFastDoubleClick()) {
                    iCustomerItemClick.onClickItemVoice();
                }
            }
        });
        // 取消
        dialogview.findViewById(R.id.txt_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectDialog != null) {
                    selectDialog.dismiss();
                }
                if (!ViewUtils.isFastDoubleClick()) {
                    iCustomerItemClick.onClickItemCancle();
                }
            }
        });
        selectDialog.show();
    }
}
