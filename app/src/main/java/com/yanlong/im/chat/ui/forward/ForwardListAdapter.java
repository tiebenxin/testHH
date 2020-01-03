package com.yanlong.im.chat.ui.forward;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.yanlong.im.R;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.wight.avatar.MultiImageView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zgd on 2017/7/20.
 */
public class ForwardListAdapter extends BaseQuickAdapter<MoreSessionBean, BaseViewHolder> {
    private Context context;
    private MsgDao msgDao = new MsgDao();

    public ForwardListAdapter(@LayoutRes int layoutResId, @Nullable Context context, @Nullable List<MoreSessionBean> data) {
        super(layoutResId, data);
    }

    public ForwardListAdapter(@Nullable Context context, @Nullable List<MoreSessionBean> data) {
        this(R.layout.item_forward_list, context, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, final MoreSessionBean item) {

        MultiImageView img_head = helper.getView(R.id.img_head);
        if (!TextUtils.isEmpty(item.getAvatar())) {
            // 头像集合
            List<String> headList = new ArrayList<>();
            headList.add(item.getAvatar());
            img_head.setList(headList);
        } else {
            loadGroupHeads(item.getGid(), img_head);
        }
    }

    /**
     * 加载群头像
     *
     * @param gid
     * @param imgHead
     */
    public synchronized void loadGroupHeads(String gid, MultiImageView imgHead) {
        Group gginfo = msgDao.getGroup4Id(gid);
        if (gginfo != null) {
            int i = gginfo.getUsers().size();
            i = i > 9 ? 9 : i;
            //头像地址
            List<String> headList = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                MemberUser userInfo = gginfo.getUsers().get(j);
                headList.add(userInfo.getHead());
            }
            imgHead.setList(headList);
        }
    }
}
