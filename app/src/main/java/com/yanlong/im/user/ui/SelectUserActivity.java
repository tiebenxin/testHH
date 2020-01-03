package com.yanlong.im.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.yanlong.im.R;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.utils.GlideOptionsUtil;
import com.yanlong.im.utils.UserUtil;

import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.PySortView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/***
 * 从通讯录中选择单个用户
 */
public class SelectUserActivity extends AppActivity {
    public static final int RET_CODE_SELECTUSR = 18246;
    public static final String RET_JSON = "json";
    private net.cb.cb.library.view.HeadView headView;
    private ActionbarView actionbar;

    private net.cb.cb.library.view.MultiListView mtListView;
    private PySortView viewType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);
        findViews();
        initEvent();
        initData();
    }

    //自动寻找控件
    private void findViews() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        mtListView = findViewById(R.id.mtListView);
        viewType = findViewById(R.id.view_type);
    }


    //自动生成的控件事件
    private void initEvent() {
        actionbar.setTxtRight("确定");
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                if (getSelectItem() != null) {
                    String json = new Gson().toJson(getSelectItem());
                    setResult(RET_CODE_SELECTUSR, new Intent().putExtra(RET_JSON, json));
                }
                finish();
            }
        });

        mtListView.init(new RecyclerViewAdapter());
        mtListView.getLoadView().setStateNormal();
        viewType.setLinearLayoutManager(mtListView.getLayoutManager());
        //联动
        viewType.setListView(mtListView.getListView());
    }


    private void initData() {
        taskListData();
    }

    private UserInfo getSelectItem() {
        if (listData != null) {
            for (int i = 0; i < listData.size(); i++) {
                if (listData.get(i).isChecked()) {
                    return listData.get(i);
                }
            }
        }
        return null;
    }


    //自动生成RecyclerViewAdapter
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RCViewHolder> {

        @Override
        public int getItemCount() {
            return listData == null ? 0 : listData.size();
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(RCViewHolder hd, int position) {

            final UserInfo bean = listData.get(position);


            hd.txtType.setText(bean.getTag());
            //hd.imgHead.setImageURI(Uri.parse("" + bean.getHead()));
            Glide.with(context).load(bean.getHead())
                    .apply(GlideOptionsUtil.headImageOptions()).into(hd.imgHead);

            hd.txtName.setText(bean.getName4Show());

            hd.viewType.setVisibility(View.VISIBLE);
            if (position > 0) {
                UserInfo lastbean = listData.get(position - 1);
                if (lastbean.getTag().equals(bean.getTag())) {
                    hd.viewType.setVisibility(View.GONE);
                }
            }

            if (listData.get(position).isChecked()) {
                hd.imSelect.setImageResource(R.drawable.bg_cheack_green_s);
            } else {
                hd.imSelect.setImageResource(R.drawable.bg_cheack_green_e);
            }


            hd.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectItem(position);
                    mtListView.notifyDataSetChange();
                }
            });

        }

        private void selectItem(int postion) {
            for (int i = 0; i < listData.size(); i++) {
                if (i == postion) {
                    listData.get(i).setChecked(true);
                } else {
                    listData.get(i).setChecked(false);
                }
            }
        }


        //自动寻找ViewHold
        @Override
        public RCViewHolder onCreateViewHolder(ViewGroup view, int i) {
            RCViewHolder holder = new RCViewHolder(inflater.inflate(R.layout.item_group_create1, view, false));
            return holder;
        }


        //自动生成ViewHold
        public class RCViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout viewType;
            private TextView txtType;
            private ImageView imgHead;
            private TextView txtName;
            private ImageView imSelect;

            //自动寻找ViewHold
            public RCViewHolder(View convertView) {
                super(convertView);
                viewType = convertView.findViewById(R.id.view_type);
                txtType = convertView.findViewById(R.id.txt_type);
                imgHead = convertView.findViewById(R.id.img_head);
                txtName = convertView.findViewById(R.id.txt_name);
                imSelect = convertView.findViewById(R.id.im_select);
            }

        }
    }

    private UserDao userDao = new UserDao();
    private List<UserInfo> listData = new ArrayList<>();
    private List<UserInfo> tempData = new ArrayList<>();

    private void taskListData() {
        listData = userDao.friendGetAll(true);
        // 升序
        Collections.sort(listData, new Comparator<UserInfo>() {
            @Override
            public int compare(UserInfo o1, UserInfo o2) {
                return o1.getTag().hashCode() - o2.getTag().hashCode();
            }
        });

        // 把#数据放到末尾
        tempData.clear();
        for (int i = listData.size() - 1; i >= 0; i--) {
            UserInfo bean = listData.get(i);
            if (bean.getTag().hashCode() == 35) {
                tempData.add(bean);
                listData.remove(i);
            }
        }
        listData.addAll(tempData);

        for (int i = listData.size() - 1; i >= 0; i--) {
            // 推荐名片不显示客服号
            if (UserUtil.isSystemUser(listData.get(i).getUid())) {
                listData.remove(i);
            } else {
                viewType.putTag(listData.get(i).getTag(), i);
            }
        }
        // 添加存在用户的首字母列表
        viewType.addItemView(UserUtil.userParseString(listData));
        mtListView.notifyDataSetChange();
    }


}
