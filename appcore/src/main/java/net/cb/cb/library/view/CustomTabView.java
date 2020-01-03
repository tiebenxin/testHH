package net.cb.cb.library.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import net.cb.cb.library.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Liszt
 * @date 2019/8/10
 * Description  自定义tab
 */
public class CustomTabView extends LinearLayout {

    private View root;

    private int currentPosition = ETabPosition.LEFT;//默认左边选中
    private TextView tv_left, tv_right;
    private OnTabSelectListener listener;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public CustomTabView(Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public CustomTabView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public CustomTabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        resetTab(currentPosition);
    }


    private void init(Context context) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        root = mInflater.inflate(R.layout.layout_tab_custom, this);
        tv_left = root.findViewById(R.id.tv_left);
        tv_right = root.findViewById(R.id.tv_right);

        tv_left.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onLeft();
                }
                getHandler().postDelayed(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        resetTab(ETabPosition.LEFT);
                    }
                }, 100);
            }

        });

        tv_right.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRight();
                }
                getHandler().postDelayed(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        resetTab(ETabPosition.RIGHT);
                    }
                }, 100);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void resetTab(@ETabPosition int tab) {
        currentPosition = tab;
        switch (tab) {
            case ETabPosition.LEFT:
                tv_left.setSelected(true);
                tv_left.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                tv_right.setSelected(false);
                tv_right.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_select));
                break;
            case ETabPosition.RIGHT:
                tv_left.setSelected(false);
                tv_left.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_select));
                tv_right.setSelected(true);
                tv_right.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                break;
        }
        invalidate();
    }

    public interface OnTabSelectListener {

        void onLeft();

        void onRight();
    }

    public void setTabSelectListener(OnTabSelectListener l) {
        listener = l;
    }

    @IntDef({ETabPosition.LEFT, ETabPosition.RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ETabPosition {
        int LEFT = 0; //左
        int RIGHT = 1; //右

    }
}
