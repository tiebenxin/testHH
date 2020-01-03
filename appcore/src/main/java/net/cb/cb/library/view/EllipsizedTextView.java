package net.cb.cb.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-14
 * @updateAuthor
 * @updateDate
 * @description 超过一定行数显示固定行数时末尾添加 "..."的TextView(适配图文混排)
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class EllipsizedTextView extends android.support.v7.widget.AppCompatTextView {

    private int mMaxLines;

    public EllipsizedTextView(Context context) {
        this(context, null);
    }

    public EllipsizedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EllipsizedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context.obtainStyledAttributes(attrs, new int[]{
                android.R.attr.maxLines
        }, defStyle, 0);

        mMaxLines = a.getInteger(0, 1);
        a.recycle();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {

        if ((text != null && text.length() > 0) && (mMaxLines != Integer.MAX_VALUE && mMaxLines > 0) && getWidth() != 0) {
            StaticLayout layout = new StaticLayout(text, getPaint(), getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            //需要显示的文字加上"..."的总宽度
//            float textAndEllipsizeWidth = 0;
//            for (int i = 0; i < mMaxLines; i++) {
//                //此处用getWidth()计算的话会有误差，所以用getLineWidth() getLineWidth
//                textAndEllipsizeWidth += layout.getWidth();
//            }
//            textAndEllipsizeWidth += layout.getWidth();
            text = TextUtils.ellipsize(text, getPaint(), layout.getWidth(), TextUtils.TruncateAt.END);
        }
        super.setText(text, type);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width > 0 && oldWidth != width) {
            setText(getText());
        }
    }

    @Override
    public int getMaxLines() {
        return mMaxLines;
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        mMaxLines = maxLines;
    }
}
