package net.cb.cb.library.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.cb.cb.library.R;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-10-11
 * @updateAuthor
 * @updateDate
 * @description 根据手指移动而移动的button
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class ImageMoveView extends RelativeLayout {
    /**
     * 获取状态栏和标题栏的接口
     */
    private OnSingleTapListener moveViewListenner;
    /**
     * 按下的点距离右边控件边缘的大小
     */
    private float btn_widthY = 0;
    /**
     * 按下的点距离下边控件边缘的大小
     */
    private float btn_widthX = 0;
    /**
     * 获取该组件在屏幕的x坐标
     */
    private float deltaX;
    /**
     * 获取该组件在屏幕的y坐标
     */
    private float deltaY;
    /**
     * 获取控件距底部的距离
     */
    private float btn_bottomY;
    /**
     * 获取控件距右边的距离
     */
    private float btn_rightX;
    /**
     * 记录点下去X坐标
     */
    private float downX;
    /**
     * 记录点下去Y坐标
     */
    private float downY;
    /**
     * 可移动按钮
     */
    private View mViewMove;
    /**
     * 获取该布局在屏幕的位置，以左上角为参考点
     */
    private int[] inWindow = new int[2];
    /**
     * 获取按钮里面的x轴
     */
    private float inX = 0;
    /**
     * 获取按钮里面的y轴
     */
    private float inY = 0;
    /**
     * 获取按钮maginleft
     */
    private float btn_maginLeft;
    /**
     * 获取按钮magintop
     */
    private float btn_maginTop;

    private LayoutParams layoutParams;
    /**
     * 通话时间
     */
    private TextView txtTime;
    /**
     * 浮动窗口在屏幕中的x坐标
     */
    private static float x = 0;
    /**
     * 浮动窗口在屏幕中的y坐标
     */
    private static float y = 200;
    /**
     * 屏幕触摸状态，暂时未使用
     */
    private static float state = 0;
    /**
     * 鼠标触摸开始位置
     */
    private static float mTouchStartX = 0;
    /**
     * 鼠标触摸结束位置
     */
    private static float mTouchStartY = 0;
    /**
     * windows 窗口管理器
     */
    private static WindowManager wm = null;

    /**
     * 浮动显示对象
     */
    private static View floatingViewObj = null;

    /**
     * 参数设定类
     */
    public static WindowManager.LayoutParams params = new WindowManager.LayoutParams();
    public static int TOOL_BAR_HIGH = 0;
    /**
     * 要显示在窗口最前面的对象
     */
    private static View view_obj = null;

    /**
     * 设置移动按钮点击事件
     */
    public void setOnClickListener(OnSingleTapListener moveViewListenner) {
        this.moveViewListenner = moveViewListenner;
    }

    public ImageMoveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ImageMoveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ImageMoveView(Context context) {
        super(context);
        init(context);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        getLocationInWindow(inWindow);
        super.onWindowFocusChanged(hasWindowFocus);
    }

    /**
     * 显示浮动按钮
     * @param context
     * @param window
     */
    public void show(Context context, Window window) {
        // 关闭浮动显示对象然后再显示
        close(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        floatingViewObj = inflater.inflate(R.layout.view_minimize_voice, null);

        txtTime = floatingViewObj.findViewById(R.id.txt_time);

        view_obj = floatingViewObj;
        Rect frame = new Rect();
        // 这一句是关键，让其在top 层显示
        window.getDecorView().getWindowVisibleDisplayFrame(frame);
        TOOL_BAR_HIGH = frame.top;

        wm = (WindowManager) context.getSystemService(Activity.WINDOW_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                    | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }

        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        // 设置悬浮窗口长宽数据
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // 设定透明度
        params.alpha = 10;
        // 设定内部文字对齐方式
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        // 设置背景透明
        params.format= PixelFormat.RGBA_8888;

        // 以屏幕左上角为原点，设置x、y初始值ֵ
        params.x = (int) x;
        params.y = (int) y;

        // 设置悬浮窗的Touch监听
        floatingViewObj.setOnTouchListener(new OnTouchListener() {
            int lastX, lastY;
            int paramX, paramY;

            public boolean onTouch(View v, MotionEvent event) {
                /** 获取该组件在屏幕的x坐标 */
                deltaX = event.getRawX();
                /** 获取该组件在屏幕的y坐标 */
                deltaY = event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        paramX = params.x;
                        paramY = params.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params.x = paramX + dx;
                        params.y = paramY + dy;
                        // 更新悬浮窗位置
//                        wm.updateViewLayout(floatingViewObj, params);
                        break;
                    case MotionEvent.ACTION_UP:
                        // 判断是否触发点击事件
                        if (Math.abs(lastX - deltaX) < 10 && Math.abs(lastY - deltaY) < 10 && null != moveViewListenner) {
                            moveViewListenner.onClick();
                        }
                        break;

                }
                return true;
            }
        });

        //Android6.0以下，不用动态声明权限
        wm.addView(floatingViewObj, params);
    }

    /**
     * 关闭浮动显示对象
     */
    public void close(Context context) {

        if (view_obj != null && view_obj.isShown()) {
            WindowManager wm = (WindowManager) context
                    .getSystemService(Activity.WINDOW_SERVICE);
            wm.removeView(view_obj);
        }
    }

    public boolean isShown(){
        boolean flg =false;
        if(view_obj!=null){
            flg = view_obj.isShown();
        }
        return flg;
    }

    private void init(Context context) {
        mViewMove = View.inflate(context, R.layout.view_minimize_voice, null);
        txtTime = mViewMove.findViewById(R.id.txt_time);
        LayoutParams layoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParam.setMargins(0, 0, 10, 10);
        addView(mViewMove, layoutParam);

        layoutParams = (LayoutParams) mViewMove.getLayoutParams();
        mViewMove.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /** 获取该组件在屏幕的x坐标 */
                deltaX = event.getRawX();
                /** 获取该组件在屏幕的y坐标 */
                deltaY = event.getRawY();
                /** 获取按下的点控件距底部的距离 */
                btn_bottomY = getHeight() - (deltaY - inWindow[1]);
                /** 获取按下的点控件距右边的距离 */
                btn_rightX = getWidth() - (deltaX - inWindow[0]);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        inX = event.getX();
                        inY = event.getY();
                        // 获取按下去时候的点在跟buttom最下面的边缘的距离
                        btn_widthY = mViewMove.getHeight() - inY;
                        // 获取按下去时候的点在跟buttom最右面的边缘的距离
                        btn_widthX = mViewMove.getWidth() - inX;
                        // 获取x坐标
                        downX = event.getRawX();
                        // 获取y坐标
                        downY = event.getRawY();
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 判断右边是否越界
                        if (deltaX + btn_widthX >= (inWindow[0] + getWidth())) {
                            layoutParams.rightMargin = (int) (0);
                            // 判断左边是否越界
                        } else if (deltaX - (mViewMove.getWidth() - btn_widthX) <= inWindow[0]) {// 判断x-buttom按下点的另一半的距离（btn_AddPost.getWidth()-movex）是否小于等于0
                            layoutParams.rightMargin = (int) (getWidth() - mViewMove.getWidth());
                        } else {
                            // 移动时候用屏幕宽度减去移动的距离再减去（按下去的点跟buttom最右边的边缘的距离）
                            layoutParams.rightMargin = (int) (btn_rightX - btn_widthX);
                        }

                        // 判断下边是否越界
                        if (deltaY + btn_widthY >= (inWindow[1] + getHeight())) {
                            layoutParams.bottomMargin = (int) (0);
                            // 判断上边是否越界
                        } else if (deltaY - (mViewMove.getHeight() - btn_widthY) <= inWindow[1]) {// 判断y-标题栏-状态栏-按下buttom点的另一半距离是否小于等于0
                            layoutParams.bottomMargin = (int) (getHeight() - mViewMove.getHeight());
                        } else {
                            // 移动时候用屏幕宽度减去移动的距离再减去（按下去的点跟buttom最下边的边缘的距离）
                            layoutParams.bottomMargin = (int) (btn_bottomY - btn_widthY);
                        }

                        mViewMove.setLayoutParams(layoutParams);

                        break;
                    case MotionEvent.ACTION_UP:
                        // 判断是否触发点击事件
                        if (Math.abs(downX - deltaX) < 10 && Math.abs(downY - deltaY) < 10 && null != moveViewListenner) {
                            moveViewListenner.onClick();
                        }

                        btn_maginLeft = getWidth() - mViewMove.getWidth() - layoutParams.rightMargin;
                        btn_maginTop = getHeight() - mViewMove.getHeight() - layoutParams.bottomMargin;

                        if (btn_maginTop != 0 && btn_maginLeft != 0 && layoutParams.rightMargin != 0 && layoutParams.bottomMargin != 0) {// 左右上下边不越界

                            if (layoutParams.bottomMargin <= btn_maginTop && layoutParams.bottomMargin < btn_maginLeft
                                    && layoutParams.bottomMargin < layoutParams.rightMargin) {// 是否抬起时在屏幕的下方
                                Animation translateIn = new TranslateAnimation(0, 0, -layoutParams.bottomMargin, 0);
                                translateIn.setDuration(200);
                                // translateIn.setFillAfter(true);这个方法会冲突事件
                                mViewMove.startAnimation(translateIn);
                                layoutParams.bottomMargin = 0;
                                mViewMove.setLayoutParams(layoutParams);
                                break;
                            }
                            if (btn_maginTop < layoutParams.bottomMargin && btn_maginTop < btn_maginLeft && btn_maginTop < layoutParams.rightMargin) {// 是否抬起时在屏幕的上方
                                Animation translateIn = new TranslateAnimation(0, 0, btn_maginTop, 0);
                                translateIn.setDuration(200);
                                // translateIn.setFillAfter(true);这个方法会冲突事件
                                mViewMove.startAnimation(translateIn);
                                layoutParams.bottomMargin = getHeight() - mViewMove.getHeight();
                                mViewMove.setLayoutParams(layoutParams);
                                break;
                            }
                            if (btn_maginLeft < btn_maginTop && btn_maginLeft < layoutParams.bottomMargin && btn_maginLeft < layoutParams.rightMargin) {// 是否抬起时在屏幕的左边
                                Animation translateIn = new TranslateAnimation(btn_maginLeft, 0, 0, 0);
                                translateIn.setDuration(200);
                                // translateIn.setFillAfter(true);这个方法会冲突事件
                                mViewMove.startAnimation(translateIn);
                                layoutParams.rightMargin = (int) getWidth() - mViewMove.getWidth();
                                mViewMove.setLayoutParams(layoutParams);
                                break;
                            }
                            if (layoutParams.rightMargin < btn_maginTop && layoutParams.rightMargin < layoutParams.bottomMargin
                                    && layoutParams.rightMargin <= btn_maginLeft) {// 是否抬起时在屏幕的右边
                                Animation translateIn = new TranslateAnimation(-layoutParams.rightMargin, 0, 0, 0);
                                translateIn.setDuration(200);
                                mViewMove.startAnimation(translateIn);
                                layoutParams.rightMargin = 0;
                                mViewMove.setLayoutParams(layoutParams);
                                break;
                            }
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

    }


    /**
     * 获取button实例
     */
    public View getButtonMove() {
        return mViewMove;
    }

    /**
     * 更新通话时长
     *
     * @param time
     */
    public void updateCallTime(String time) {
        txtTime.setText(time);
    }

    /**
     * 按钮单击事件回调口
     *
     * @author CodeApe
     * @version 1.0
     * @Description TODO
     * @date 2014年8月25日
     * @Copyright: Copyright (c) 2014 Shenzhen Utoow Technology Co., Ltd. All rights reserved.
     */
    public interface OnSingleTapListener {
        /**
         * 可移动按钮点击时间
         *
         * @version 1.0
         * @createTime 2014年8月21日, 上午10:51:53
         * @updateTime 2014年8月21日, 上午10:51:53
         * @createAuthor WangYuWen
         * @updateAuthor WangYuWen
         * @updateInfo (此处输入修改内容, 若无修改可不写.)
         */
        public void onClick();
    }
}
