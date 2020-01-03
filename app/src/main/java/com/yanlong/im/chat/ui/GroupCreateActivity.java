package com.yanlong.im.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.utils.GlideOptionsUtil;
import com.yanlong.im.utils.GroupHeadImageUtil;
import com.yanlong.im.utils.UserUtil;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.UpFileAction;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.PySortView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/***
 * 创建群聊
 */
public class GroupCreateActivity extends AppActivity {
    private net.cb.cb.library.view.HeadView headView;
    private ActionbarView actionbar;
    private LinearLayout viewSearch;
    private android.support.v7.widget.RecyclerView topListView;
    private net.cb.cb.library.view.MultiListView mtListView;
    private PySortView viewType;
    public static final String AGM_SELECT_UID = "select_uid";
    private String select_uid;
    private List<UserInfo> listDataTop = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);
        findViews();
        initEvent();
        initData();
    }


    //自动寻找控件
    private void findViews() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        viewSearch = findViewById(R.id.view_search);
        topListView = findViewById(R.id.topListView);
        mtListView = findViewById(R.id.mtListView);
        viewType = findViewById(R.id.view_type);
    }


    //自动生成的控件事件
    private void initEvent() {
        select_uid = getIntent().getStringExtra(AGM_SELECT_UID);
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        actionbar.setTxtRight("确定");
        actionbar.getViewRight().setClickable(true);
        actionbar.getViewRight().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.show();
                if (listDataTop != null && listDataTop.size() >= 2) {
                    actionbar.getViewRight().setEnabled(false);
                    actionbar.getViewRight().setClickable(false);
                }
                taskCreate();
            }
        });

        mtListView.init(new RecyclerViewAdapter());
        mtListView.getLoadView().setStateNormal();
        //联动
        viewType.setLinearLayoutManager(mtListView.getLayoutManager());
        viewType.setListView(mtListView.getListView());

        //顶部处理
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        topListView.setLayoutManager(linearLayoutManager);
        topListView.setAdapter(new RecyclerViewTopAdapter());

    }

    private void initData() {
        taskListData();

    }

    public void selectUser() {
        listDataTop.clear();
        for (UserInfo bean : listData) {
            if (StringUtil.isNotNull(select_uid) && select_uid.equals("" + bean.getUid())) {

                if (!bean.isChecked()) {
                    listDataTop.add(bean);

                    bean.setChecked(true);
                    topListView.getAdapter().notifyDataSetChanged();
                }
            }
        }

    }

    //自动生成RecyclerViewAdapter
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RCViewHolder> {

        @Override
        public int getItemCount() {
            return listData == null ? 0 : listData.size();
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(RCViewHolder hd, final int position) {

            final UserInfo bean = listData.get(position);


            hd.txtType.setText(bean.getTag());

            Glide.with(context).load(bean.getHead())
                    .apply(GlideOptionsUtil.headImageOptions()).into(hd.imgHead);

            // hd.txtName.setText(bean.getName());
            hd.txtName.setText(bean.getName4Show());

            hd.viewType.setVisibility(View.VISIBLE);
            if (position > 0) {
                UserInfo lastbean = listData.get(position - 1);
                if (lastbean.getTag().equals(bean.getTag())) {
                    hd.viewType.setVisibility(View.GONE);
                }
            }

            hd.ckSelect.setOnCheckedChangeListener(null);//清掉监听器
            hd.ckSelect.setChecked(bean.isChecked());


            hd.ckSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        listDataTop.add(bean);
                    } else {
                        listDataTop.remove(bean);
                    }
                    listData.get(position).setChecked(isChecked);
                    topListView.getAdapter().notifyDataSetChanged();
                }
            });
            //8.19 已选择用户处理
            if (StringUtil.isNotNull(select_uid) && select_uid.equals("" + bean.getUid())) {

                if (!listData.get(position).isChecked()) {

                    hd.ckSelect.setChecked(true);
                }

                hd.itemView.setAlpha(0.3f);
                hd.ckSelect.setEnabled(false);
            } else {
                hd.itemView.setAlpha(1f);
                hd.ckSelect.setEnabled(true);
            }


        }


        //自动寻找ViewHold
        @Override
        public RCViewHolder onCreateViewHolder(ViewGroup view, int i) {
            RCViewHolder holder = new RCViewHolder(inflater.inflate(R.layout.item_group_create, view, false));
            return holder;
        }


        //自动生成ViewHold
        public class RCViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout viewType;
            private TextView txtType;
            private ImageView imgHead;
            private TextView txtName;
            private CheckBox ckSelect;

            //自动寻找ViewHold
            public RCViewHolder(View convertView) {
                super(convertView);
                viewType = convertView.findViewById(R.id.view_type);
                txtType = convertView.findViewById(R.id.txt_type);
                imgHead = convertView.findViewById(R.id.img_head);
                txtName = convertView.findViewById(R.id.txt_name);
                ckSelect = convertView.findViewById(R.id.ck_select);
            }

        }
    }


    //自动生成RecyclerViewAdapter
    class RecyclerViewTopAdapter extends RecyclerView.Adapter<RecyclerViewTopAdapter.RCViewTopHolder> {

        @Override
        public int getItemCount() {
            return listDataTop == null ? 0 : listDataTop.size();
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(RCViewTopHolder holder, int position) {

            Glide.with(context).load(listDataTop.get(position).getHead())
                    .apply(GlideOptionsUtil.headImageOptions()).into(holder.imgHead);

            //  showThumb( holder.imgHead,listDataTop.get(position).getHead(),10,10);
        }


        //自动寻找ViewHold
        @Override
        public RCViewTopHolder onCreateViewHolder(ViewGroup view, int i) {
            RCViewTopHolder holder = new RCViewTopHolder(inflater.inflate(R.layout.item_group_create_top, view, false));
            return holder;
        }


        //自动生成ViewHold
        public class RCViewTopHolder extends RecyclerView.ViewHolder {
            private ImageView imgHead;

            //自动寻找ViewHold
            public RCViewTopHolder(View convertView) {
                super(convertView);
                imgHead = convertView.findViewById(R.id.img_head);
            }

        }
    }

    private MsgAction msgACtion = new MsgAction();
    private UserDao userDao = new UserDao();


    private List<UserInfo> listData = new ArrayList<>();
    private List<UserInfo> tempData = new ArrayList<>();

    private void taskListData() {

        listData = userDao.friendGetAll(false);
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
            if (UserUtil.isSystemUser(listData.get(i).getUid())) {
                listData.remove(i);
            }
        }
        for (int i = 0; i < listData.size(); i++) {
            viewType.putTag(listData.get(i).getTag(), i);
        }
        // 添加存在用户的首字母列表
        viewType.addItemView(UserUtil.userParseString(listData));
        selectUser();
    }

    private UpFileAction upFileAction = new UpFileAction();
    private File fileImg;

    private void taskCreate() {
        if (listDataTop.size() < 2) {
            ToastUtil.show(getContext(), "人数必须大于3人");
            actionbar.getViewRight().setEnabled(true);
            alert.dismiss();
            return;
        }
        final ArrayList<UserInfo> templist = new ArrayList<>();
        templist.addAll(listDataTop);
        templist.add(0, UserAction.getMyInfo());
        String name = "";

        // "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3507975290,3418373437&fm=27&gp=0.jpg";
        int i = templist.size();
        i = i > 9 ? 9 : i;
        //头像地址
        String url[] = new String[i];
        for (int j = 0; j < i; j++) {
            UserInfo userInfo = templist.get(j);

            url[j] = userInfo.getHead();
        }
        fileImg = GroupHeadImageUtil.synthesis(this, url);


        msgACtion.groupCreate(UserAction.getMyInfo().getName(), "", "", templist, new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {
                try {
                    Thread.sleep(1000);
                    alert.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                actionbar.getViewRight().setEnabled(true);
                if (response.body() == null) {
                    actionbar.getViewRight().setEnabled(true);
                    actionbar.getViewRight().setClickable(true);
                    return;
                }
                if (response.body().isOk()) {
                    MsgDao msgDao = new MsgDao();
                    msgDao.groupHeadImgCreate(response.body().getData().getGid(), fileImg.getAbsolutePath());
                    MessageManager.getInstance().setMessageChange(true);
                    MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, -1L, response.body().getData().getGid(), CoreEnum.ESessionRefreshTag.SINGLE, null);
                    startActivity(new Intent(getContext(), ChatActivity.class).putExtra(ChatActivity.AGM_TOGID, response.body().getData().getGid()).putExtra(ChatActivity.GROUP_CREAT, "creat"));
                    finish();
                } else {
                    actionbar.getViewRight().setEnabled(true);
                    actionbar.getViewRight().setClickable(true);
                    ToastUtil.show(getContext(), response.body().getMsg());

                }

            }

            @Override
            public void onFailure(Call<ReturnBean<Group>> call, Throwable t) {
                actionbar.getViewRight().setEnabled(true);
                alert.dismiss();
                super.onFailure(call, t);
            }
        });
    }
}
