package com.yanlong.im.chat.ui.forward;

import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.user.bean.UserInfo;

import net.cb.cb.library.base.IView;

import java.util.List;

/**
 * @author Liszt
 * @date 2019/8/10
 * Description
 */
public interface ForwardView extends IView {

    void setSessionData(List<Session> list);

    void setRosterData(List<UserInfo> list);
}
