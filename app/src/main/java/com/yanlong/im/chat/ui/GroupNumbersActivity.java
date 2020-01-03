package com.yanlong.im.chat.ui;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.utils.GlideOptionsUtil;
import com.yanlong.im.utils.GroupHeadImageUtil;
import com.yanlong.im.utils.UserUtil;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.TouchUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.PySortView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/***
 * 群成员操作
 */
public class GroupNumbersActivity extends AppActivity {
    public static final String AGM_GID = "gid";

    //成员列表
    public static final String AGM_NUMBERS_JSON = "number_json";
    //1:添加,2:删除
    public static final String AGM_TYPE = "type";
    public static final int TYPE_ADD = 1;
    public static final int TYPE_DEL = 2;

    private String gid;
    private List<UserInfo> listData;
    private List<UserInfo> tempData = new ArrayList<>();
    private Integer type;

    private Gson gson = new Gson();

    private net.cb.cb.library.view.HeadView headView;
    private ActionbarView actionbar;
    private LinearLayout viewSearch;
    private RecyclerView topListView;
    private net.cb.cb.library.view.MultiListView mtListView;
    private PySortView viewType;
    private int isClickble = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);
        findViews();
        initEvent();
        initData();
    }

    private void initData() {
        taskListData();


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
        listData = gson.fromJson(getIntent().getStringExtra(AGM_NUMBERS_JSON), new TypeToken<List<UserInfo>>() {
        }.getType());
        // 处理NullPointerException
        if (listData == null) {
            listData = new ArrayList<>();
        } else {
            Collections.sort(listData);
        }
        type = getIntent().getIntExtra(AGM_TYPE, TYPE_ADD);
        gid = getIntent().getStringExtra(AGM_GID);

        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                if (isClickble == 0) {
                    taskOption();
                }
            }
        });
        actionbar.setTxtRight("确定");

        actionbar.setTitle(type == TYPE_ADD ? "加入群" : "移出群");
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
            // hd.imgHead.setImageURI(Uri.parse("" + bean.getHead()));
            Glide.with(context).load(bean.getHead())
                    .apply(GlideOptionsUtil.headImageOptions()).into(hd.imgHead);

            hd.txtName.setText(bean.getName4Show());

            hd.viewType.setVisibility(View.VISIBLE);
            hd.viewLine.setVisibility(View.VISIBLE);
            if (position > 0 && listData.size() > 1) {
                UserInfo lastbean = listData.get(position - 1);
                if (lastbean.getTag().equals(bean.getTag())) {
                    hd.viewType.setVisibility(View.GONE);
                }
            }
            if (position == getItemCount() - 1) {
                hd.viewLine.setVisibility(View.GONE);
            } else {
                UserInfo lastbean = listData.get(position + 1);
                if (!lastbean.getTag().equals(bean.getTag())) {
                    hd.viewLine.setVisibility(View.GONE);
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

            TouchUtil.expandTouch(hd.ckSelect);


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
            private View viewLine;

            //自动寻找ViewHold
            public RCViewHolder(View convertView) {
                super(convertView);
                viewType = convertView.findViewById(R.id.view_type);
                txtType = convertView.findViewById(R.id.txt_type);
                imgHead = convertView.findViewById(R.id.img_head);
                txtName = convertView.findViewById(R.id.txt_name);
                ckSelect = convertView.findViewById(R.id.ck_select);
                viewLine = convertView.findViewById(R.id.view_line);
            }

        }
    }

    private List<UserInfo> listDataTop = new ArrayList<>();

    //自动生成RecyclerViewAdapter
    class RecyclerViewTopAdapter extends RecyclerView.Adapter<RecyclerViewTopAdapter.RCViewTopHolder> {

        @Override
        public int getItemCount() {
            return listDataTop == null ? 0 : listDataTop.size();
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(RCViewTopHolder holder, int position) {

            //  holder.imgHead.setImageURI(Uri.parse(listDataTop.get(position).getHead()));

            Glide.with(context).load(listDataTop.get(position).getHead())
                    .apply(GlideOptionsUtil.headImageOptions()).into(holder.imgHead);
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

    private void taskListData() {

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
        for (int i = 0; i < listData.size(); i++) {
            //UserInfo infoBean:
            viewType.putTag(listData.get(i).getTag(), i);
        }
        // 添加存在用户的首字母列表
        viewType.addItemView(UserUtil.userParseString(listData));

    }

    /***
     * 提交处理
     */
    private void taskOption() {
        if (listDataTop.size() < 1) {
            ToastUtil.show(getContext(), "请至少选择一个用户");
            return;
        }
        isClickble = 1;
        LogUtil.getLog().e("GroupNumbersActivity", "taskOption");
        alert.show();
        MsgDao dao = new MsgDao();
        List<MemberUser> list = new ArrayList<>();
        List<Long> listLong = new ArrayList<>();
//        Group group= dao.getGroup4Id(gid);
        List<MemberUser> mem = dao.getGroup4Id(gid).getUsers();
        CallBack<ReturnBean> callback = new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                try {
                    Thread.sleep(1000);
                    alert.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                GroupHeadImageUtil.creatAndSaveImg(GroupNumbersActivity.this, gid);
                if (response.body() == null) {
                    isClickble = 0;
                    return;
                }

                ToastUtil.show(getContext(), response.body().getMsg());
                if (response.body().isOk()) {
                    if (type != TYPE_DEL) {
                        dao.removeGroupMember(gid, listLong);
                    }
                    MessageManager.getInstance().notifyGroupChange(true);

                    finish();
                }

            }

            @Override
            public void onFailure(Call<ReturnBean> call, Throwable t) {
                super.onFailure(call, t);
                alert.dismiss();
            }
        };

        for (int k = 0; k < listDataTop.size(); k++) {
            MessageManager msg = new MessageManager();
            list.add(msg.userToMember(listDataTop.get(k), gid));
        }

        for (int i = 0; i < listDataTop.size(); i++) {
//            dao.addGroupMember(gid,listDataTop)
            for (int j = 0; j < mem.size(); j++) {
                if (listDataTop.get(i).getUid() == mem.get(j).getUid()) {
//                    list.add(mem.get(j));
                    listLong.add(listDataTop.get(i).getUid());
                }
            }
        }

        if (type == TYPE_ADD) {
            msgACtion.groupAdd(gid, listDataTop, UserAction.getMyInfo().getName(), callback);
//            dao.addGroupMember(gid,list);
        } else {
            msgACtion.groupRemove(gid, listDataTop, callback);
        }


//        GroupHeadImageUtil.creatAndSaveImg(GroupNumbersActivity.this, gid);
    }


}
