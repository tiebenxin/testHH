package net.cb.cb.library.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.cb.cb.library.R;

/***
 * 协调头部布局
 *
 * @author jyj
 * @date 2016/12/8
 */
public class HeadView extends LinearLayout {
    private String TAG = "HeadView";
    private Context context;
    private View rootView;
    private boolean isReLoad = false;
    private ActionbarView actionbar;
    ViewGroup appbar;

    public HeadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.view_head, null);
        actionbar = rootView.findViewById(R.id.actionbar);
        appbar = rootView.findViewById(R.id.appbar);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeadView);

        int res_bg = typedArray.getResourceId(R.styleable.HeadView_headview_background, 0);
        if (res_bg != 0)
            setHeadBackground(res_bg);

        String title = typedArray.getString(R.styleable.HeadView_headview_title);
        if (title != null) {
            actionbar.setTitle(title);
        }

        int res_top = typedArray.getResourceId(R.styleable.HeadView_headview_top_layout, 0);
        if (res_top != 0) {
            View topView = inflater.inflate(res_top, null);
            setAppBar(topView);
        }
    }

    //设置title
    public void setTitle(int rid) {
        if (actionbar != null) {
            actionbar.setTitle(context.getString(rid));
        }
    }

    //设置title
    public void setTitle(String  title) {
        if (actionbar != null) {
            actionbar.setTitle(title);
        }
    }

    public ActionbarView getActionbar() {
        return actionbar;
    }

    public AppBarLayout getAppBarLayout() {
        return rootView.findViewById(R.id.app_bg);
    }

    /***
     * 设置自定义头部
     * @param v
     */
    public void setAppBar(View v) {

        appbar.removeAllViews();
        v.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        appbar.addView(v);
    }

    public View getAppBar() {
        return rootView.findViewById(R.id.appbar);
    }

    /***
     * 设置头部背景
     */
    public void setHeadBackground(int resid) {
        View appbg = rootView.findViewById(R.id.app_bg);
        appbg.setBackgroundResource(resid);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isReLoad) {
            //    Log.i(TAG,">>>>装载子布局:"+getChildCount());
            isReLoad = true;
            ViewGroup inptv = rootView.findViewById(R.id.lay_impt);
            // inptv.setPadding(0,200,0,0);

            for (int i = 0; i < getChildCount(); ) {
                View cv = this.getChildAt(i);
                this.removeViewAt(i);
                inptv.addView(cv);
            }


            rootView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            this.addView(rootView);


        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
}
