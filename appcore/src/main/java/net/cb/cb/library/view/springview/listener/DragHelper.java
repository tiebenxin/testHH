package net.cb.cb.library.view.springview.listener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author wenqin 2017-08-07 14:51
 */

public interface DragHelper {

  View getView(LayoutInflater inflater, ViewGroup viewGroup);

  int getDragLimitHeight(View rootView);

  int getDragMaxHeight(View rootView);

  int getDragSpringHeight(View rootView);

  int getDragLimitWidth(View rootView);

  int getDragMaxWidth(View rootView);

  int getDragSpringWidth(View rootView);

  void onPreDrag(View rootView);

  /**
   * 手指拖动控件过程中的回调，用户可以根据拖动的距离添加拖动过程动画
   *
   * @param distance 拖动距离，下拉为+，上拉为-
   */
  void onDropAnim(View rootView, int distance);

  /**
   * 手指拖动控件过程中每次抵达临界点时的回调，用户可以根据手指方向设置临界动画
   *
   * @param upOrDown 是上拉还是下拉
   */
  void onLimitDes(View rootView, boolean upOrDown);

  /**
   * 拉动超过临界点后松开时回调
   */
  void onStartAnim();

  /**
   * 头(尾)已经全部弹回时回调
   */
  void onFinishAnim();
}
