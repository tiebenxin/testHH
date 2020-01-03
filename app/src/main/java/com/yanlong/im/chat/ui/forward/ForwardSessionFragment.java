package com.yanlong.im.chat.ui.forward;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;

import com.yanlong.im.R;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.databinding.FragmentForwardSessionBinding;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import net.cb.cb.library.base.BaseMvpFragment;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Liszt
 * @date 2019/8/10
 * Description  转发session选择列表 最近聊天列表
 */

public class ForwardSessionFragment extends BaseMvpFragment<ForwardModel, ForwardView, ForwardPresenter> implements ForwardView {

    private FragmentForwardSessionBinding ui;
    private UserDao userDao = new UserDao();
    private MsgDao msgDao = new MsgDao();
    private AdapterForwardSession adapter;
    private IForwardListener listener;
    private List<Session> sessionsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ui = DataBindingUtil.inflate(inflater, R.layout.fragment_forward_session, container, false);
        EventBus.getDefault().register(this);
        return ui.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAdapter();
        if (presenter != null) {
            presenter.loadAndSetData(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initAdapter() {
        adapter = new AdapterForwardSession(getActivity());
        ui.listView.init(adapter);
        ui.listView.getLoadView().setStateNormal();
        adapter.initDao(userDao, msgDao);
        adapter.setForwardListener(listener);
    }

    @Override
    public ForwardModel createModel() {
        return new ForwardModel();
    }

    @Override
    public ForwardView createView() {
        return this;
    }

    @Override
    public ForwardPresenter createPresenter() {
        return new ForwardPresenter();
    }


    @Override
    public void setSessionData(List<Session> sessions) {
        if (sessions == null) {
            return;
        }
        sessionsList=sessions;

        List<Session> temp=searchSessionBykey(sessionsList,MsgForwardActivity.searchKey);
        adapter.bindData(temp);
        ui.listView.init(adapter);
    }

    @Override
    public void setRosterData(List<UserInfo> list) {

    }

    public void setForwardListener(IForwardListener l) {
        listener = l;
    }


    private List<Session> searchSessionBykey(List<Session> sessions,String key){
        LogUtil.getLog().e("======转发搜索====最近聊天====key=="+key);
        if(!StringUtil.isNotNull(key)){
            return sessions;
        }

        List<Session> temp=new ArrayList<>();
        for (Session bean : sessions) {
            String name = "";
            if (bean.getType() == 0) {//单人
                UserInfo finfo = userDao.findUserInfo(bean.getFrom_uid());
                name = finfo.getName4Show();
            } else if (bean.getType() == 1) {//群
                //Group ginfo = msgDao.getGroup4Id(bean.getGid());
                name = msgDao.getGroupName(bean.getGid());

            }

            if (StringUtil.isNotNull(name)&&(name.contains(key))) {
//                LogUtil.getLog().e("====name==="+name);
                bean.setUnread_count(0);
                temp.add(bean);
            }
        }
//        LogUtil.getLog().e("====temp==="+temp.size());
        return  temp;
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void posting(SingleOrMoreEvent event) {
        ui.listView.init(adapter);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void posting(SearchKeyEvent event) {
        List<Session> temp=searchSessionBykey(sessionsList,MsgForwardActivity.searchKey);
        adapter.bindData(temp);
        ui.listView.init(adapter);

    }

}
