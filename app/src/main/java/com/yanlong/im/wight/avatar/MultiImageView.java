package com.yanlong.im.wight.avatar;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.AppConfig;
import net.cb.cb.library.utils.DensityUtil;

import java.util.List;

/**
 * @author shoyu
 * @ClassName MultiImageView.java
 * @Description: 显示1~N张图片的View
 */
public class MultiImageView extends LinearLayout {

    public int MAX_WIDTH = 0;

    // 照片的Url列表
    private List<String> imagesList;

    private SparseArray<ImageView> imageviews;

    /**
     * 长度 单位为Pixel
     **/
    private int pxOneMaxWandH;  // 单张图最大允许宽高
    private int pxMoreWandH = 0;// 多张图的宽高
    private int pxTwoWandH = 0;// 三、四张图的宽高
    private int pxImagePadding = (DensityUtil.dip2px(AppConfig.getContext(), 1));// 图片间的间距

    private int MAX_PER_ROW_COUNT = 3;// 每行显示最大数

    private LayoutParams onePicPara;
    private LayoutParams morePara, moreParaColumnFirst;
    private LayoutParams rowPara;
    private LayoutParams rowTwoPara;
    private LayoutParams rowTwoParaFirst;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemClickListener) {
        mOnItemLongClickListener = onItemClickListener;
    }

    public MultiImageView(Context context) {
        super(context);
    }

    public MultiImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setList(List<String> lists) throws IllegalArgumentException {
        if (lists == null) {
            throw new IllegalArgumentException("imageList is null...");
        }
        imagesList = lists;

        if (MAX_WIDTH > 0) {
            pxMoreWandH = (MAX_WIDTH - pxImagePadding * 2) / 3; //解决右侧图片和内容对不齐问题
            pxOneMaxWandH = MAX_WIDTH * 2 / 3;
            pxTwoWandH = MAX_WIDTH / 2 - 1;
            initImageLayoutParams();
        }

        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MAX_WIDTH == 0) {
            int width = measureWidth(widthMeasureSpec);
            if (width > 0) {
                MAX_WIDTH = width;
                if (imagesList != null && imagesList.size() > 0) {
                    setList(imagesList);
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            // result = (int) mTextPaint.measureText(mText) + getPaddingLeft()
            // + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by
                // measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private void initImageLayoutParams() {
        int wrap = LayoutParams.WRAP_CONTENT;
        int match = LayoutParams.MATCH_PARENT;

        onePicPara = new LayoutParams(match, match);
        moreParaColumnFirst = new LayoutParams(pxMoreWandH, pxMoreWandH);
        morePara = new LayoutParams(pxMoreWandH, pxMoreWandH);
        morePara.setMargins(pxImagePadding, 0, 0, 0);
        rowPara = new LayoutParams(match, wrap);

        rowTwoParaFirst = new LayoutParams(pxTwoWandH, pxTwoWandH);
        rowTwoPara = new LayoutParams(pxTwoWandH, pxTwoWandH);
        rowTwoPara.setMargins(pxImagePadding, 0, 0, 0);
    }

    // 根据imageView的数量初始化不同的View布局,还要为每一个View作点击效果
    private void initView() {
        if (imageviews == null) {
            imageviews = new SparseArray<>();
        }
        this.setOrientation(VERTICAL);
        this.setGravity(Gravity.CENTER);
        this.removeAllViews();
        if (MAX_WIDTH == 0) {
            //为了触发onMeasure()来测量MultiImageView的最大宽度，MultiImageView的宽设置为match_parent
            addView(new View(getContext()));
            return;
        }

        if (imagesList == null || imagesList.size() == 0) {
            return;
        }

        if (imagesList.size() == 1) {
            ImageView view = createImageView(0, 0, false);
            addView(view);
            imageviews.put(0, view);
        } else {
            int allCount = imagesList.size();
            if (allCount == 2) {
                MAX_PER_ROW_COUNT = 1;
            } else if (allCount > 2 && allCount < 7) {
                MAX_PER_ROW_COUNT = 2;
            } else {
                MAX_PER_ROW_COUNT = 3;
            }
            for (int rowCursor = 0; rowCursor < MAX_PER_ROW_COUNT; rowCursor++) {
                LinearLayout rowLayout = new LinearLayout(getContext());
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setGravity(Gravity.CENTER);

                rowLayout.setLayoutParams(rowPara);
                if (rowCursor != 0) {
                    rowLayout.setPadding(0, pxImagePadding, 0, 0);
                }
                int columnCount = allCount % MAX_PER_ROW_COUNT == 0 ? MAX_PER_ROW_COUNT
                        : allCount % MAX_PER_ROW_COUNT;//每行的列数
                if (rowCursor != MAX_PER_ROW_COUNT - 1) {
                    columnCount = MAX_PER_ROW_COUNT;
                }
                addView(rowLayout);
                int rowOffset = rowCursor * MAX_PER_ROW_COUNT;// 行偏移

                if (allCount == 2) {
                    columnCount = 2;
                    rowOffset = 0;
                } else if (allCount == 3) {
                    columnCount = rowCursor + 1;
                    rowOffset = rowCursor;
                } else if (allCount == 4) {
                    columnCount = 2;
                    rowOffset = rowCursor * 2;
                } else if (allCount == 5) {
                    columnCount = rowCursor + 2;
                    rowOffset = rowCursor * 2;
                } else if (allCount == 6) {
                    columnCount = 3;
                    rowOffset = rowCursor * 3;
                }
                for (int columnCursor = 0; columnCursor < columnCount; columnCursor++) {
                    int position = columnCursor + rowOffset;
                    ImageView view = createImageView(position, columnCursor, true);
                    rowLayout.addView(view);
                    imageviews.put(position, view);
                }
            }
        }
    }

    private ImageView createImageView(int position, int columnCursor, final boolean isMultiImage) {
        String url = imagesList.get(position);
        ImageView imageView = new ColorFilterImageView(getContext());
        if (isMultiImage) {
            imageView.setScaleType(ScaleType.CENTER_CROP);
            if (imagesList.size() == 2 || imagesList.size() == 3 || imagesList.size() == 4) {
                imageView.setLayoutParams(columnCursor == 0 ? rowTwoParaFirst : rowTwoPara);
            } else {
                imageView.setLayoutParams(columnCursor == 0 ? moreParaColumnFirst : morePara);
            }
        } else {
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ScaleType.FIT_START);
//            imageView.setMaxHeight(pxOneMaxWandH);
            imageView.setLayoutParams(onePicPara);
        }
        if (!TextUtils.isEmpty(url)) {
            imageView.setId(url.hashCode());
        }
//        imageView.setOnClickListener(new ImageOnClickListener(position));
//        imageView.setOnLongClickListener(new ImageOnLongClickListener(position));
        loadImage(url, imageView);
        return imageView;
    }

    private class ImageOnClickListener implements OnClickListener {

        private int position;

        public ImageOnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, position);
            }
        }
    }

    private class ImageOnLongClickListener implements OnLongClickListener {

        private int position;

        public ImageOnLongClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onLongClick(View v) {
            if (mOnItemLongClickListener != null) {
                mOnItemLongClickListener.onItemLongClick(v, position);
            }
            return true;
        }
    }

    public interface OnItemClickListener {

        public void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {

        public void onItemLongClick(View view, int position);
    }

    public SparseArray<ImageView> getImageviews() {
        return imageviews;
    }

    public void setImageviews(SparseArray<ImageView> imageviews) {
        this.imageviews = imageviews;
    }

    private void loadImage(String url, ImageView imageView) {

        Glide.with(imageView.getContext()).load(url)
                .apply(GlideOptionsUtil.headImageOptions()).into(imageView);

    }
}