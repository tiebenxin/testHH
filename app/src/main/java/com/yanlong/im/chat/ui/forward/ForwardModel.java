package com.yanlong.im.chat.ui.forward;

import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;

import net.cb.cb.library.base.IModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * @author Liszt
 * @date 2019/8/10
 * Description
 */
public class ForwardModel implements IModel {


    private MsgDao msgDao;
    private UserDao userDao;

    public Observable<List<Session>> loadSession() {
        return Observable.create(new ObservableOnSubscribe<List<Session>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Session>> e) throws Exception {
                if (msgDao == null) {
                    msgDao = new MsgDao();
                }
                List<Session> list = msgDao.sessionGetAllValid();
                e.onNext(list);
            }
        });
    }

    public Observable<List<UserInfo>> loadRoster() {
        return Observable.create(new ObservableOnSubscribe<List<UserInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<UserInfo>> e) throws Exception {
                if (userDao == null) {
                    userDao = new UserDao();
                }
                List<UserInfo> list = userDao.friendGetAll(true);
                e.onNext(list);
            }
        });
    }

}
