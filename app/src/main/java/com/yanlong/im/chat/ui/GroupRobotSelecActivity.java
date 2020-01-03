package com.yanlong.im.chat.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.RobotInfoBean;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 搜索群助手
 */
public class GroupRobotSelecActivity extends AppActivity {
    private net.cb.cb.library.view.HeadView headView;
    private ActionbarView actionbar;
    private net.cb.cb.library.view.ClearEditText edtSearch;
    private net.cb.cb.library.view.MultiListView mtListView;

    private List<RobotInfoBean> listData = new ArrayList<>();
    private String gid;

    //自动寻找控件
    private void findViews() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        edtSearch = findViewById(R.id.edt_search);
        mtListView = findViewById(R.id.mtListView);
    }


    //自动生成的控件事件
    private void initEvent() {
        gid = getIntent().getStringExtra(GroupRobotActivity.AGM_GID);
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });

        mtListView.init(new RecyclerViewAdapter());
        mtListView.getLoadView().setStateNormal();

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    taskSearch();
                }
                return false;
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    key="";
                } else {
                    key = edtSearch.getText().toString();
                }

                listData.clear();
                mtListView.notifyDataSetChange();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_robot_select);
        findViews();
        initEvent();
    }

    //自动生成RecyclerViewAdapter
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        //自动寻找ViewHold
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup view, int viewType) {
            if (viewType == 0) {
                RSearchViewHolder holder = new RSearchViewHolder(getLayoutInflater().inflate(R.layout.item_search_net, view, false));
                return holder;
            }else {
                RCViewHolder holder = new RCViewHolder(inflater.inflate(R.layout.item_group_robot, view, false));
                return holder;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (!TextUtils.isEmpty(key) && position == 0) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public int getItemCount() {
            if (listData != null) {
                if (!TextUtils.isEmpty(key)) {
                    return listData.size() + 1;
                } else {
                    return listData.size() ;
                }
            }
            return 0;
        }

        //自动生成控件事件
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof RSearchViewHolder) {
                RSearchViewHolder holder = (RSearchViewHolder) viewHolder;
                holder.setKey();
            }else if (viewHolder instanceof RCViewHolder) {
                RCViewHolder holder = (RCViewHolder) viewHolder;
                final RobotInfoBean infobean = listData.get(position);
                holder.txtInfoTitle.setText(infobean.getRname());
                //   holder.imgInfoIcon.setImageURI(Uri.parse(infobean.getAvatar()));

                Glide.with(context).load(infobean.getAvatar())
                        .apply(GlideOptionsUtil.headImageOptions()).into(holder.imgInfoIcon);
                holder.txtInfoMore.setText(infobean.getRobotDescription());

                holder.btnInfoAdd.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        setResult(GroupRobotActivity.RET_SELECT, new Intent()
                                .putExtra(GroupRobotActivity.AGM_GID, gid)
                                .putExtra(GroupRobotActivity.AGM_RID, infobean.getRid()));
                        finish();

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getContext(), GroupRobotActivity.class).putExtra(GroupRobotActivity.AGM_SHOW_TYPE, GroupRobotActivity.AGM_SHOW_TYPE_ADD)
                                .putExtra(GroupRobotActivity.AGM_GID, gid)
                                .putExtra(GroupRobotActivity.AGM_RID, infobean.getRid())

                        );
                    }
                });

            }
        }

        //自动生成ViewHold
        public class RCViewHolder extends RecyclerView.ViewHolder {
            private ImageView imgInfoIcon;
            private TextView txtInfoTitle;
            private TextView txtInfoMore;
            private Button btnInfoAdd;

            //自动寻找ViewHold
            public RCViewHolder(View convertView) {
                super(convertView);
                imgInfoIcon = convertView.findViewById(R.id.img_info_icon);
                txtInfoTitle = convertView.findViewById(R.id.txt_info_title);
                txtInfoMore = convertView.findViewById(R.id.txt_info_more);
                btnInfoAdd = convertView.findViewById(R.id.btn_info_add);
            }

        }

        class RSearchViewHolder extends RecyclerView.ViewHolder {
            private final TextView tv_content;
            private final LinearLayout ll_root;

            public RSearchViewHolder(@NonNull View itemView) {
                super(itemView);
                ll_root = itemView.findViewById(R.id.ll_root);
                tv_content = itemView.findViewById(R.id.tv_content);
                ll_root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        taskSearch();
                    }
                });
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            public void setKey( ) {
                String content = "网络查找常信号:" + key;
//                tv_content.setText(getSpan(content, key, 8));
                tv_content.setText(content);
            }

        }
    }


    private String key = "";

    private MsgAction msgAction = new MsgAction();

    private void taskSearch() {
        key = edtSearch.getText().toString();
        if (key.length() <= 0) {
            return;
        }
        msgAction.robotSearch(key, new CallBack<ReturnBean<List<RobotInfoBean>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<RobotInfoBean>>> call, Response<ReturnBean<List<RobotInfoBean>>> response) {
                if (response.body() == null)
                    return;

                key="";
                listData.clear();
                listData.addAll(response.body().getData());
                mtListView.notifyDataSetChange(response);
            }
        });


    }


}
