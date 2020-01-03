package com.yanlong.im.view.face.wight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-11-26
 * @updateAuthor
 * @updateDate
 * @description 列表页富文本，支持图片换行居中
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class HotelListImageSpan extends ImageSpan {
    public HotelListImageSpan(Context context, Bitmap bitmap) {
        super(context,bitmap);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getDrawable();
        canvas.save();
        int transY;
        //要显示的文本高度-图片高度除2等居中位置+top(换行情况)
        transY = ((bottom - top) - b.getBounds().bottom) / 2 + top;
        //偏移画布后开始绘制
        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }
}
