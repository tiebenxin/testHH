package com.yanlong.im.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.FontsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.user.bean.ProfessionBean;

import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;
import net.cb.cb.library.view.MultiListView;

import java.util.ArrayList;
import java.util.List;

public class SelectProfessionActivity extends AppActivity {
    public static final String SELECT_PROFEESION = "profession";
    public static final String JOB_TYPE = "jobType";
    private HeadView mHeadView;
    private MultiListView mMtListView;
    private List<ProfessionBean> list = new ArrayList<>();
    private ProfessionAdapter adapter;
    private String string[] = {"党政机关人员", "企事业单位工作人员", "商业及服务业工作人员", "农林牧副渔劳动者", "学生", "军人", "无业", "其他"};
    private String jobType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_profession);
        initView();
        initEvent();
        initData();
    }


    private void initView() {
        mHeadView = findViewById(R.id.headView);
        mMtListView = findViewById(R.id.mtListView);
        mHeadView.getActionbar().setTxtRight("完成");
        adapter = new ProfessionAdapter();
        mMtListView.init(adapter);
        mMtListView.getLoadView().setStateNormal();
        jobType = getIntent().getStringExtra(JOB_TYPE);

    }

    private void initEvent() {
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                Intent intent = new Intent();
                intent.putExtra(SELECT_PROFEESION, getProfession());
                setResult(RESULT_OK, intent);
                onBackPressed();
            }
        });
    }


    private String getProfession() {
        String profession = "";
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isSelect) {
                profession = list.get(i).profession;
            }
        }
        return profession;
    }


    private void initData() {
        for (int i = 0; i < string.length; i++) {
            ProfessionBean bean = new ProfessionBean();
            if (!TextUtils.isEmpty(jobType)) {
                if (string[i].equals(jobType)) {
                    bean.isSelect = true;
                } else {
                    bean.isSelect = false;
                }
            } else {
                bean.isSelect = false;
            }
            bean.profession = string[i];
            list.add(bean);
        }
    }


    class ProfessionAdapter extends RecyclerView.Adapter<ProfessionAdapter.ProfessionViewHolder> {

        @Override
        public ProfessionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = inflater.inflate(R.layout.item_select_profession, viewGroup, false);
            return new ProfessionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ProfessionViewHolder viewHolder, final int i) {
            ProfessionBean bean = list.get(i);
            viewHolder.mTvProfession.setText(bean.profession);
            if (bean.isSelect) {
                viewHolder.mIvSelect.setImageResource(R.drawable.bg_cheack_green_s);
            } else {
                viewHolder.mIvSelect.setImageResource(R.drawable.bg_cheack_green_e);
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int j = 0; j < list.size(); j++) {
                        if (j == i) {
                            list.get(j).isSelect = true;
                        } else {
                            list.get(j).isSelect = false;
                        }
                    }

                    mMtListView.getListView().getAdapter().notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            if (list != null && list.size() > 0) {
                return list.size();
            }
            return 0;
        }

        class ProfessionViewHolder extends RecyclerView.ViewHolder {
            private TextView mTvProfession;
            private ImageView mIvSelect;

            public ProfessionViewHolder(@NonNull View itemView) {
                super(itemView);
                mTvProfession = itemView.findViewById(R.id.tv_profession);
                mIvSelect = itemView.findViewById(R.id.iv_select);
            }
        }

    }

}
