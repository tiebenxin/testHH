package com.yanlong.im.user.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.yanlong.im.R;

import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.ClearEditText;
import net.cb.cb.library.view.HeadView;

import java.util.ArrayList;
import java.util.List;


@Route(path = "/app/HelpActivity")
public class HelpActivity extends AppActivity {

    private ClearEditText mEdtSearch;
    private RecyclerView mRecyclerViewHot;
    private RecyclerView mRecyclerViewType;
    private HelpAdapter hotAdapter;
    private HelpAdapter typeAdapter;
    private HeadView mHeadView;
    private TextView tvFeedback;
    private WebView activity_help_web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hlep);
        initView();
        initEvent();
        initData();
    }

    @Override
    public boolean
    onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && (activity_help_web != null && activity_help_web.canGoBack())) {
            activity_help_web.goBack();//返回上个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);//退出H5界面
    }

    private void initView() {
//        activity_help_web = findViewById(R.id.activity_help_web);
//        activity_help_web.getSettings().setJavaScriptEnabled(true);
//        activity_help_web.addJavascriptInterface(new JSInterface(HelpActivity.this),"androidMethod");
//        activity_help_web.loadUrl("http://192.168.10.102:8080/");
/*        activity_help_web.loadUrl("https://helper.zhixun6.com:8000/");
        activity_help_web.setWebViewClient(new MyWebViewClient());*/

        mHeadView = findViewById(R.id.headView);
        mEdtSearch = findViewById(R.id.edt_search);
        tvFeedback = findViewById(R.id.tv_feedback);
        mRecyclerViewHot = findViewById(R.id.recyclerView_hot);
        LinearLayoutManager hotManger = new LinearLayoutManager(this);
        mRecyclerViewHot.setLayoutManager(hotManger);
        mRecyclerViewHot.setNestedScrollingEnabled(false);

        mRecyclerViewType = findViewById(R.id.recyclerView_type);
        LinearLayoutManager typeManger = new LinearLayoutManager(this);
        mRecyclerViewType.setLayoutManager(typeManger);
        mRecyclerViewType.setNestedScrollingEnabled(false);

        hotAdapter = new HelpAdapter();
        typeAdapter = new HelpAdapter();
        mRecyclerViewHot.setAdapter(hotAdapter);
        mRecyclerViewType.setAdapter(typeAdapter);
    }

    class MyWebViewClient extends WebViewClient {
        @Override  //WebView代表是当前的WebView
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    private void initEvent() {
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });

        tvFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(FeedbackActivity.class);
            }
        });
    }

    private void initData() {
        List<String> hotList = new ArrayList<>();
        hotList.add("为什么我常信没有声音");
        hotList.add("如何下载常信聊天软件?");
        hotList.add("如何添加好友?");

        List<String> typeList = new ArrayList<>();
        typeList.add("好友添加");
        typeList.add("收发消息");
        typeList.add("账号设置");
        typeList.add("群聊");

        hotAdapter.setData(hotList);
        typeAdapter.setData(typeList);
    }


    class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.HelpViewHolder> {
        private List<String> list;


        public void setData(List<String> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public HelpViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new HelpViewHolder(inflater.inflate(R.layout.item_help_list, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(HelpViewHolder viewHolder, final int i) {
            viewHolder.mTvTitle.setText(list.get(i));
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HelpActivity.this, HelpInfoActivity.class);
                    intent.putExtra("content", list.get(i));
                    startActivity(intent);
                }
            });
        }

        @Override
        public long getItemId(int position) {
            return position;

        }

        @Override
        public int getItemCount() {
            if (list != null && list.size() > 0) {
                return list.size();
            }
            return 0;
        }


        class HelpViewHolder extends RecyclerView.ViewHolder {
            private TextView mTvTitle;

            public HelpViewHolder(@NonNull View itemView) {
                super(itemView);
                mTvTitle = itemView.findViewById(R.id.tv_title);
            }
        }
    }


}
