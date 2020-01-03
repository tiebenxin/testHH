package net.cb.cb.library.utils;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;

/***
 *
 *
 * @author jyj
 * @date 2018/2/1
 */
public class TouchUtil {

    /**
     * 扩大View的触摸和点击响应范围,最大不超过其父View范围
     *
     * @param view
     * @param top
     * @param bottom
     * @param left
     * @param right
     */
    public static void expandTouch(final View view, final int top,
                                               final int bottom, final int left, final int right) {

        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                view.setEnabled(true);
                view.getHitRect(bounds);

                bounds.top -=DensityUtil.dip2px(view.getContext(),top) ;
                bounds.bottom +=DensityUtil.dip2px(view.getContext(), bottom);
                bounds.left -=DensityUtil.dip2px(view.getContext(), left);
                bounds.right += DensityUtil.dip2px(view.getContext(),right);

                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }


    public static void expandTouch(final View view) {

        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                view.setEnabled(true);
                view.getHitRect(bounds);

                bounds.top -=9999;
                bounds.bottom +=9999;
                bounds.left -=9999;
                bounds.right += 9999;



                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }

}
