package com.yanlong.im.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.RobotInfoBean;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.TimeToString;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AlertYesNo;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.WebPageActivity;

import retrofit2.Call;
import retrofit2.Response;

/**
 * 我的群助手
 */
public class GroupRobotActivity extends AppActivity {
    public static final String AGM_SHOW_TYPE = "SHOW_TYPE";
    public static final int AGM_SHOW_TYPE_ADD = 1; //待添加
    public static final String AGM_RID = "RID";
    public static final String AGM_GID = "GID";
    public static final int RET_SELECT = 4653;

    private String gid;
    private String rid;
    private int showType = 0;

    private net.cb.cb.library.view.HeadView headView;
    private ActionbarView actionbar;
    private LinearLayout viewInfo;
    private ImageView imgInfoIcon;
    private TextView txtInfoTitle;
    private Button btnInfoAdd;
    private Button btnInfoDel;
    private Button btnInfoChange;
    private TextView txtInfoMore;
    private TextView txtInfoNote;
    private Button btnConfig;
    private LinearLayout viewAdd;
    private Button btnAdd;
    private RobotInfoBean infoBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_robot);
        findViews();
        initEvent();

    }

    //自动寻找控件
    private void findViews() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        viewInfo = findViewById(R.id.view_info);
        imgInfoIcon = findViewById(R.id.img_info_icon);
        txtInfoTitle = findViewById(R.id.txt_info_title);
        btnInfoAdd = findViewById(R.id.btn_info_add);
        btnInfoDel = findViewById(R.id.btn_info_del);
        btnInfoDel.setPressed(true);
        btnInfoChange = findViewById(R.id.btn_info_change);
        txtInfoMore = findViewById(R.id.txt_info_more);
        txtInfoNote = findViewById(R.id.txt_info_note);
        btnConfig = findViewById(R.id.btn_config);
        viewAdd = findViewById(R.id.view_add);
        btnAdd = findViewById(R.id.btn_add);
    }


    //自动生成的控件事件
    private void initEvent() {
        gid = getIntent().getStringExtra(AGM_GID);
        rid = getIntent().getStringExtra(AGM_RID);
        showType = getIntent().getIntExtra(AGM_SHOW_TYPE, 0);

        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });

        btnInfoAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                taskChange(rid);
            }
        });

        btnInfoDel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertYesNo alertYesNo = new AlertYesNo();
                alertYesNo.init(GroupRobotActivity.this, "提示", "确定删除该群助手？", "确定", "取消",
                        new AlertYesNo.Event() {
                            @Override
                            public void onON() {

                            }

                            @Override
                            public void onYes() {
                                taskDel();
                            }
                        });
                alertYesNo.show();

            }
        });


        btnConfig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startActivity(new Intent(getContext(), WebPageActivity.class).putExtra(WebPageActivity.AGM_URL, infoBean.getUrl()));

            }
        });

        View.OnClickListener listener;
        btnInfoChange.setOnClickListener(listener = new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(getContext(), GroupRobotSelecActivity.class).putExtra(AGM_GID, gid), RET_SELECT);
            }
        });
        btnAdd.setOnClickListener(listener);

    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        gid = intent.getStringExtra(AGM_GID);
        rid = intent.getStringExtra(AGM_RID);
        showType = intent.getIntExtra(AGM_SHOW_TYPE, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RET_SELECT) {
            rid = data.getStringExtra(AGM_RID);
            showType = 0;
            taskChange(rid);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        taskInfo();
    }

    private void initData() {

        if (infoBean != null) { //有添加
            viewAdd.setVisibility(View.GONE);
            viewInfo.setVisibility(View.VISIBLE);

        } else {
            viewAdd.setVisibility(View.VISIBLE);
            viewInfo.setVisibility(View.GONE);
            return;
        }


        if (showType == AGM_SHOW_TYPE_ADD) {//查看详情界面

            btnInfoAdd.setVisibility(View.VISIBLE);
            btnInfoDel.setVisibility(View.GONE);
            btnInfoChange.setVisibility(View.GONE);
            btnConfig.setVisibility(View.GONE);
        } else {
            btnInfoAdd.setVisibility(View.GONE);
            btnInfoDel.setVisibility(View.VISIBLE);
            btnInfoChange.setVisibility(View.VISIBLE);
            btnConfig.setVisibility(View.VISIBLE);
        }

        // imgInfoIcon.setImageURI(Uri.parse(infoBean.getAvatar()));

        Glide.with(this).load(infoBean.getAvatar())
                .apply(GlideOptionsUtil.defImageOptions()).into(imgInfoIcon);

        txtInfoTitle.setText(infoBean.getRname());
        txtInfoMore.setText(infoBean.getRobotDescription());
        String note = "更新时间:" + TimeToString.YYYY_MM_DD_HH_MM(infoBean.getUpdateTime()) + "\n" +
                "开发者:" + infoBean.getMerchantName() + "\n" +
                "免责声明:" + infoBean.getDisclaimer();


        txtInfoNote.setText(note);

    }

    private MsgAction msgAction = new MsgAction();

    private void taskInfo() {


        msgAction.robotInfo(rid, gid, new CallBack<ReturnBean<RobotInfoBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<RobotInfoBean>> call, Response<ReturnBean<RobotInfoBean>> response) {
                if (response.body() == null) {

                    return;
                }

                if (response.body().isOk()) {
                    infoBean = response.body().getData();

                }
                initData();
            }
        });

    }

    private void taskChange(String rid) {
        msgAction.robotChange(gid, rid, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {
                    showType = 0;
                    taskInfo();
                    MessageManager.getInstance().notifyGroupChange(true);
                } else {
                    ToastUtil.show(getContext(), response.body().getMsg());
                }
            }
        });
    }

    private void taskDel() {
        msgAction.robotDel(gid, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {
                    // finish();
                    rid="-1";
                    showType = 0;
                    infoBean = null;
                    initData();
                    MessageManager.getInstance().notifyGroupChange(true);
                } else {
                    ToastUtil.show(getContext(), response.body().getMsg());
                }
            }
        });
    }


}
