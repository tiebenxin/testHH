package com.yanlong.im.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.utils.ExpressionUtil;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.bean.EventFindHistory;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.TimeToString;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/***
 * 搜索消息记录
 */
public class SearchMsgActivity extends AppActivity {
    public static final String AGM_GID = "gid";

    public static final String AGM_FUID = "fuid";
    private Long fuid;
    private String gid;

    private net.cb.cb.library.view.HeadView headView;
    private ActionbarView actionbar;
    private net.cb.cb.library.view.ClearEditText edtSearch;
    private net.cb.cb.library.view.MultiListView mtListView;
    private List<UserInfo> listDataUser = new ArrayList<>();
    private List<Group> listDataGroup = new ArrayList<>();


    //自动寻找控件
    private void findViews() {
        headView = (net.cb.cb.library.view.HeadView) findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        edtSearch = (net.cb.cb.library.view.ClearEditText) findViewById(R.id.edt_search);
        mtListView = (net.cb.cb.library.view.MultiListView) findViewById(R.id.mtListView);
    }


    //自动生成的控件事件
    private void initEvent() {
        gid = getIntent().getStringExtra(AGM_GID);
        fuid = getIntent().getLongExtra(AGM_FUID, 0);
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
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_frd_grp);
        findViews();
        initEvent();
    }


    //自动生成RecyclerViewAdapter
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RCViewHolder> {

        @Override
        public int getItemCount() {
            return listData == null ? 0 : listData.size();
        }

        //自动生成控件事件
        @Override
        public void onBindViewHolder(RCViewHolder holder, int position) {
            final MsgAllBean msgbean = listData.get(position);
            String url = "";
            String name = "";
            String msg = "";


            if (StringUtil.isNotNull(msgbean.getGid())) {
//                Group g = msgbean.getGroup();
//                url = g.getFrom_avatar();
//                name = g.getName();
                url = msgbean.getFrom_avatar();
                name = msgbean.getFrom_nickname();
            } else {
                UserInfo u = msgbean.getShow_user();
                url = msgbean.getFrom_avatar(); //u.getHead();
                name = u.getName4Show();
            }
            msg = msgbean.getChat().getMsg();
            int index = msg.indexOf(key);

            SpannableString style = new SpannableString(msg);
            ForegroundColorSpan protocolColorSpan = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.green_500));
            style.setSpan(protocolColorSpan, index, index + key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.txtName.setText(name);

            holder.txtTimer.setText(TimeToString.YYYY_MM_DD_HH_MM_SS(msgbean.getTimestamp()));

            showMessage(holder.txtContext, msg, style);

            Glide.with(context).load(url)
                    .apply(GlideOptionsUtil.headImageOptions()).into(holder.imgHead);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventFindHistory eventFindHistory = new EventFindHistory();
                    eventFindHistory.setStime(msgbean.getTimestamp());
                    EventBus.getDefault().post(eventFindHistory);
                    startActivity(new Intent(getContext(), ChatActivity.class)
                            .putExtra(ChatActivity.AGM_TOGID, gid)
                            .putExtra(ChatActivity.AGM_TOUID, fuid)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    );
                }
            });
        }


        //自动寻找ViewHold
        @Override
        public RCViewHolder onCreateViewHolder(ViewGroup view, int i) {
            RCViewHolder holder = new RCViewHolder(inflater.inflate(R.layout.item_search_msg, view, false));
            return holder;
        }


        //自动生成ViewHold
        public class RCViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout viewIt;
            private ImageView imgHead;
            private TextView txtName;
            private TextView txtTimer;
            private TextView txtContext;

            //自动寻找ViewHold
            public RCViewHolder(View convertView) {
                super(convertView);
                viewIt = convertView.findViewById(R.id.view_it);
                imgHead = convertView.findViewById(R.id.img_head);
                txtName = convertView.findViewById(R.id.txt_name);
                txtTimer = convertView.findViewById(R.id.txt_timer);
                txtContext = convertView.findViewById(R.id.txt_context);
            }

        }

        /**
         * 显示草稿内容
         *
         * @param message
         */
        protected void showMessage(TextView txtInfo, String message, SpannableString spannableString) {
            if (spannableString == null) {
                spannableString = ExpressionUtil.getExpressionString(getContext(), ExpressionUtil.DEFAULT_SMALL_SIZE, message);
            } else {
                spannableString = ExpressionUtil.getExpressionString(getContext(), ExpressionUtil.DEFAULT_SMALL_SIZE, spannableString);
            }
            txtInfo.setText(spannableString, TextView.BufferType.SPANNABLE);
        }
    }

    private MsgAction msgAction = new MsgAction();
    private List<MsgAllBean> listData;
    private String key = "";

    private void taskSearch() {
        key = edtSearch.getText().toString();
        if (key.length() <= 0) {
            return;
        }

        listData = msgAction.searchMsg4key(key, gid, fuid);
        mtListView.notifyDataSetChange();
    }

}
