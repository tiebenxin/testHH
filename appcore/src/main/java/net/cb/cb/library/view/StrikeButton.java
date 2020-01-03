package net.cb.cb.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.cb.cb.library.R;
import net.cb.cb.library.utils.DensityUtil;


/***
 * 带数字的按钮
 *
 * @author 姜永健
 * @date 2015年12月3日
 */
public class StrikeButton extends RelativeLayout {
    private LayoutInflater inflater;
    private ImageView btn;
    private TextView tv;
    private int sktype;
    private View small;

    public StrikeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StrikeButtonView);
        int icon = typedArray.getResourceId(R.styleable.StrikeButtonView_strike_icon_res, R.mipmap.ic_launcher);

        sktype = typedArray.getInt(R.styleable.StrikeButtonView_strike_type, 0);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.view_strike_button, this);

        btn = rootView.findViewById(R.id.img_strike_src);

        btn.setImageResource(icon);
        tv = rootView.findViewById(R.id.txt_strike_num);
        small = rootView.findViewById(R.id.img_strike_small);

        tv.setVisibility(View.INVISIBLE);
        small.setVisibility(View.INVISIBLE);

        setNum(0,false);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub

        super.onMeasure(btn.getMeasuredWidth(), btn.getMeasuredHeight());
    }

    /***
     * 0是普通,1是小点
     * @param sktype
     */
    public void setSktype(int sktype) {
        this.sktype = sktype;
    }

    /***
     *
     * @param num 设置红点个数
     *            0:红点消失,小于0显示无数字小红点
     */
    public void setNum(int num, boolean isShowAvatar) {
        String numStr = null;

        numStr = num > 99 ? 99 + "+" : num + "";

        switch (sktype) {
            case 0:
                if (num > 0) {
                    tv.setVisibility(View.VISIBLE);
                    tv.setText(numStr);
                } else if (num < 0) {
                    tv.setVisibility(View.VISIBLE);
                    tv.setText(" ");
                } else {
                    tv.setVisibility(View.INVISIBLE);
                }

                break;
            case 1:
                if (num > 0) {
                    small.setVisibility(View.VISIBLE);

                } else {
                    small.setVisibility(View.INVISIBLE);
                }

                break;
        }
        btn.setVisibility(isShowAvatar ? VISIBLE : INVISIBLE);
    }


    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        btn.setOnClickListener(l);
    }

    /**
     * 设置按钮背景
     *
     * @param res
     */
    public void setButtonBackground(int res) {
        btn.setImageResource(res);
    }

}
