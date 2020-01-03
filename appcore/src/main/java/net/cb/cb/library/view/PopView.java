package net.cb.cb.library.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import net.cb.cb.library.R;

/***
 * jyj 弹出框
 */
public class PopView {
    private PopupWindow popupWindow;
    private Context mContext;

    public void init(Context mContext,View contentView) {

        popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Log.i("mengdd", "onTouch : ");

                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        popupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(
                R.drawable.bg_popview));
    }

    public void show(View view) {


        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);


    }

    public PopupWindow getPopupWindow() {
        return popupWindow;
    }

    public void dismiss() {
        if(popupWindow != null){
            popupWindow.dismiss();
        }
    }
}
