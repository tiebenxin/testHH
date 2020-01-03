package com.yanlong.im.chat.task;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @author Liszt
 * @date 2019/10/11
 * Description 加载保存群数据
 */
public class TaskLoadSavedGroup extends AsyncTask<Void, Integer, Boolean> {
    private MsgDao msgDao = new MsgDao();
    private final List<Group> groups;
    private Map<Integer, JsonArray> arrayMap = new HashMap<>();
    private int position = 0;

    public TaskLoadSavedGroup(List<Group> g) {
        groups = g;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (groups != null) {
            int len = groups.size();
            if (len > 0) {
                JsonArray array = null;
                int index = 0;
                for (int i = 0; i < len; i++) {
                    if (array == null) {
                        array = new JsonArray();
                    }
                    Group g = groups.get(i);
                    array.add(g.getGid());
                    if (array.size() == 20) {
                        arrayMap.put(index, array);
                        array = null;
                        index++;
                    } else if (i == len - 1) {
                        arrayMap.put(index, array);
                        array = null;
                    }
                }
                loadGroups(position);
            }
        }
        return true;
    }

    private void loadGroups(int position) {
        if (arrayMap != null) {
            if (position < arrayMap.size()) {
                sendRequest(arrayMap.get(position).toString());
            } else if (position == arrayMap.size()) {//已经记载完毕
                msgDao.updateNoSaveGroup(MessageManager.getInstance().getSavedGroups());
            }
        }
    }

    /*
     * 发送请求
     * */
    private void sendRequest(String gids) {
        new MsgAction().getGroupsByIds(gids, new CallBack<ReturnBean<List<Group>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<Group>>> call, Response<ReturnBean<List<Group>>> response) {
                if (response != null && response.body() != null && response.body().isOk()) {
                    List<Group> groups = response.body().getData();
                    if (groups != null) {
//                        LogUtil.getLog().d("a=", "群信息--gids=" + gids);
                        msgDao.saveGroups(groups);
                        createGroupHead(groups);
                        MessageManager.getInstance().addSavedGroup(groups);
                        int next = ++position;
                        loadGroups(next);
                    }
                }
            }
        });
    }

    /**
     *  创建群聊头像
     * @param groupList
     */
    private void createGroupHead(List<Group> groupList) {
        // TODO 创建头像时需要异步处理，否则用户快速点击会出现ANR
        new Thread(new Runnable() {
            @Override
            public void run() {
                int len = groupList.size();
                for (int i = 0; i < len; i++) {
                    Group group = groupList.get(i);
                    if (TextUtils.isEmpty(group.getAvatar())) {
                        String avatar = msgDao.groupHeadImgGet(group.getGid());
                        if (TextUtils.isEmpty(avatar)) {
                            MessageManager.getInstance().doImgHeadChange(group.getGid(), group);
                        }
                    }
                }
            }
        }).start();
    }
}
