package net.cb.cb.library.view.springview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import net.cb.cb.library.R;
import net.cb.cb.library.view.springview.listener.DragHelper;


/**
 * @author wenqin 2017-08-07 11:39
 */

public class BaseSpringView extends ViewGroup {

  protected static final String TAG = "SpringView";

  //是否需要回调接口：TOP 只回调刷新、BOTTOM 只回调加载更多、BOTH 都需要、NONE 都不
  public enum Give {
    BOTH, TOP, BOTTOM, NONE
  }

  public enum Type {
    OVERLAP, FOLLOW
  }

  protected static final int STATUS_FOLLOW_NONE = 0;
  protected static final int STATUS_FOLLOW_HEADER = 1;
  protected static final int STATUS_FOLLOW_FOOTER = 2;

  //移动参数：计算手指移动量的时候会用到这个值，值越大，移动量越小，若值为1则手指移动多少就滑动多少px
  protected static final double MOVE_PARA = 2;

  protected Context mContext;
  protected LayoutInflater mInflater;

  protected OverScroller mOverScroller;
  protected View contentView;
  protected View mLeftView, mRightView;

  protected View header, footer;
  protected Give give = Give.BOTH;

  protected Type type = Type.OVERLAP;

  protected int mMoveTime = 400;
  //最大拉动距离，拉动距离越靠近这个值拉动就越缓慢
  protected int mMaxFullLeftWidth = 600;
  protected int mMaxFullRightWidth = 600;

  protected int mLeftSpringWidth, mRightSpringWidth;

  protected int mCallLeftOrRight = 0;

  protected float dy, dx;
  protected float mLastX, mLastY;
  protected float dsX;
  protected float mFirstX;
  protected int mActivePointerId = MotionEvent.INVALID_POINTER_ID;

  //存储拉动前的位置
  protected Rect mRect = new Rect();
  //拉动多少距离被认定为刷新(加载)动作
  protected int mLeftLimitWidth, mRightLimitWidth;

  protected boolean isHorizontalScroll = false;

  protected int mLeftViewId, mRightViewId;

  protected DragHelper mRightViewHelper, mLeftViewHelper;

  public BaseSpringView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    mInflater = LayoutInflater.from(context);

    mOverScroller = new OverScroller(context);

    TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SpringView);
    if (ta.hasValue(R.styleable.SpringView_type)) {
      int type_int = ta.getInt(R.styleable.SpringView_type, 0);
      type = Type.values()[type_int];
    }
    if (ta.hasValue(R.styleable.SpringView_give)) {
      int give_int = ta.getInt(R.styleable.SpringView_give, 0);
      give = Give.values()[give_int];
    }
    ta.recycle();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (getChildCount() > 0) {
      for (int i = 0; i < getChildCount(); i++) {
        View child = getChildAt(i);
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
      }
    }
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    contentView = getChildAt(0);
    if (mLeftViewId != 0) {
      mInflater.inflate(mLeftViewId, this, true);
      mLeftView = getChildAt(getChildCount() - 1);
    }
    if (mRightViewId != 0) {
      mInflater.inflate(mRightViewId, this, true);
      mRightView = getChildAt(getChildCount() - 1);
      mRightView.setVisibility(INVISIBLE);
    }
    if (contentView != null) {
      contentView.bringToFront();
    }

    super.onFinishInflate();
  }

  @Override
  protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
    if (contentView == null) {
      return;
    }
    if (type == Type.OVERLAP) {
      if (mLeftView != null) {
        mLeftView.layout(0, 0, mLeftView.getMeasuredWidth(), getHeight());
      }
      if (mRightView != null) {
        mRightView.layout(getWidth() - mRightView.getMeasuredWidth(), 0, getWidth(), getHeight());
      }

      if (header != null) {
        header.layout(0, 0, getWidth(), header.getMeasuredHeight());
      }
      if (footer != null) {
        footer.layout(0, getHeight() - footer.getMeasuredHeight(), getWidth(), getHeight());
      }
    } else if (type == Type.FOLLOW) {
      if (mLeftView != null) {
        mLeftView.layout(-mLeftView.getMeasuredWidth(), 0, 0, getHeight());
      }
      if (mRightView != null) {
        mRightView.layout(getWidth(), 0, getWidth() + mRightView.getMeasuredWidth(), getHeight());
      }

      if (header != null) {
        header.layout(0, -header.getMeasuredHeight(), getWidth(), 0);
      }
      if (footer != null) {
        footer.layout(0, getHeight(), getWidth(), getHeight() + footer.getMeasuredHeight());
      }
    }
    contentView.layout(0, 0, contentView.getMeasuredWidth(), contentView.getMeasuredHeight());
  }

  protected void delMulTouchEvent(MotionEvent ev) {
    final int action = MotionEventCompat.getActionMasked(ev);
    switch (action) {
      case MotionEvent.ACTION_DOWN: {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final float x = MotionEventCompat.getX(ev, pointerIndex);
        final float y = MotionEventCompat.getY(ev, pointerIndex);
        mLastX = x;
        mLastY = y;
        mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
        break;
      }
      case MotionEvent.ACTION_MOVE: {
        final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
        final float x = MotionEventCompat.getX(ev, pointerIndex);
        final float y = MotionEventCompat.getY(ev, pointerIndex);
        dx = x - mLastX;
        dy = y - mLastY;
        mLastY = y;
        mLastX = x;
        break;
      }
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        mActivePointerId = MotionEvent.INVALID_POINTER_ID;
        break;
      case MotionEvent.ACTION_POINTER_DOWN: {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId != mActivePointerId) {
          mLastX = MotionEventCompat.getX(ev, pointerIndex);
          mLastY = MotionEventCompat.getY(ev, pointerIndex);
          mActivePointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        }
        break;
      }
      case MotionEvent.ACTION_POINTER_UP: {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
          final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
          mLastX = MotionEventCompat.getX(ev, newPointerIndex);
          mLastY = MotionEventCompat.getY(ev, newPointerIndex);
          mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
        break;
      }
    }
  }

  protected void measureSpecDes(boolean isHeader, DragHelper dragHelper, View view) {
    int limitWidth = 0;
    if (dragHelper != null) {
      int xw = dragHelper.getDragMaxWidth(view);
      int w = dragHelper.getDragLimitWidth(view);
      limitWidth = w > 0 ? w : view.getMeasuredWidth();
      int sh = dragHelper.getDragSpringWidth(view);

      setWidth(isHeader, limitWidth, sh > 0 ? sh : limitWidth, xw);
    } else {
      if (view != null) {
        limitWidth = view.getMeasuredWidth();
      }
      setWidth(isHeader, limitWidth, limitWidth, 0);
    }
  }

  private void setWidth(boolean isHeader, int limitWidth, int springWidth, int maxWidth) {
    // left view
    if (isHeader && isHorizontalScroll) {
      mLeftLimitWidth = limitWidth;
      mLeftSpringWidth = springWidth;
      if (maxWidth > 0) {
        mMaxFullLeftWidth = maxWidth;
      }
    }

    // right view
    if (!isHeader && isHorizontalScroll) {
      mRightLimitWidth = limitWidth;
      mRightSpringWidth = springWidth;
      if (maxWidth > 0) {
        mMaxFullRightWidth = maxWidth;
      }
    }
  }

  protected boolean isChildScrollToLeft() {
    return !ViewCompat.canScrollHorizontally(contentView, -1);
  }

  protected boolean isChildScrollToRight() {
    return !ViewCompat.canScrollHorizontally(contentView, 1);
  }

  protected boolean isLeft() {
    Log.d("isLeft", contentView.getLeft() + "--" + getScrollX());
    if (type == Type.OVERLAP) {
      return contentView.getLeft() > 0 && isChildScrollToLeft();
    } else if (type == Type.FOLLOW) {
      return getScrollX() < 0 && isChildScrollToLeft();
    }
    return false;
  }

  protected boolean isRight() {
    Log.d("isRight", contentView.getLeft() + "--" + getScrollX());
    if (type == Type.OVERLAP) {
      return contentView.getLeft() < 0 && isChildScrollToRight();
    } else if (type == Type.FOLLOW) {
      return getScrollX() > 0 && isChildScrollToRight();
    }
    return false;
  }

  protected boolean isLeftOverFarm() {
    if (type == Type.OVERLAP) {
      return contentView.getLeft() > mLeftLimitWidth;
    } else if (type == Type.FOLLOW) {
      return -getScrollX() > mLeftLimitWidth;
    }
    return false;
  }


  protected boolean isRightOverFarm() {
    if (type == Type.OVERLAP) {
      return getWidth() - contentView.getRight() > mRightLimitWidth;
    } else if (type == Type.FOLLOW) {
      return getScrollX() > mRightLimitWidth;
    }
    return false;
  }

  protected void setHorizontalScroll(boolean horizontalScroll) {
    isHorizontalScroll = horizontalScroll;
  }
}
