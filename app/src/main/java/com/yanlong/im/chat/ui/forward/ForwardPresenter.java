package com.yanlong.im.chat.ui.forward;

import android.annotation.SuppressLint;

import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.user.bean.UserInfo;

import net.cb.cb.library.base.BasePresenter;
import net.cb.cb.library.base.DBOptionObserver;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author Liszt
 * @date 2019/8/10
 * Description
 */
public class ForwardPresenter extends BasePresenter<ForwardModel, ForwardView> {


    @SuppressLint("CheckResult")
    public void loadAndSetData(boolean isSession) {
        if (isSession) {
            Observable<List<Session>> observable = model.loadSession();
            observable.subscribe(new DBOptionObserver<List<Session>>() {
                @Override
                public void onOptionSuccess(List<Session> list) {
                    getView().setSessionData(list);
                }
            });
        } else {
            Observable<List<UserInfo>> observable = model.loadRoster();
            observable.subscribe(new DBOptionObserver<List<UserInfo>>() {
                @Override
                public void onOptionSuccess(List<UserInfo> list) {
                    getView().setRosterData(list);
                }
            });
        }

    }


    @Override
    protected void onViewDestroy() {

    }

    @Override
    protected void onViewStart() {

    }
}
