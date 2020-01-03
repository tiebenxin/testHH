package com.yanlong.im.chat.ui.chat;

import com.yanlong.im.chat.bean.MsgAllBean;

import net.cb.cb.library.base.IView;

import java.util.List;

/**
 * @author Liszt
 * @date 2019/9/19
 * Description
 */
public interface ChatView extends IView {

    void setAndRefreshData(List<MsgAllBean> l);

    void initUIAndListener();

    void initUnreadCount(String s);

    void replaceListDataAndNotify(MsgAllBean bean);

    void startUploadServer(MsgAllBean bean, String file, boolean isOrigin);

    void scrollListView(boolean isMustBottom);

    void notifyDataAndScrollBottom(boolean isScrollBottom);

    void bindData(List<MsgAllBean> l);

    void scrollToPositionWithOff(int position, int offset);

    void setDraft(String draft);

    void setBanView(boolean isExited);

    void setRobotView(boolean isMaster);

    void initTitle();

    void updateOnlineStatus();

}
