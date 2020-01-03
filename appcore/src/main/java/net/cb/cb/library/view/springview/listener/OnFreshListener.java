package net.cb.cb.library.view.springview.listener;

/**
 * @author wenqin 2017-08-07 14:52
 */

public interface OnFreshListener {

  /**
   * 下拉刷新，回调接口
   */
  void onRefresh();

  /**
   * 上拉加载，回调接口
   */
  void onLoadMore();
}
