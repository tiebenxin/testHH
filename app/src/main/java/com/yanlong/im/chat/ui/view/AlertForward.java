package com.yanlong.im.chat.ui.view;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yanlong.im.R;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.ui.forward.ForwardListAdapter;
import com.yanlong.im.chat.ui.forward.MsgForwardActivity;
import com.yanlong.im.utils.ExpressionUtil;
import com.yanlong.im.wight.avatar.MultiImageView;

import net.cb.cb.library.utils.DensityUtil;
import net.cb.cb.library.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/***
 * 对话框
 * @author jyj
 * @date 2017/1/5
 */
public class AlertForward {
    private AlertDialog alertDialog;
    private Event event;
    private Context context;
    private MultiImageView imgHead;
    private TextView txtName;
    private ImageView ivImage;
    private TextView txtMsg;
    private EditText edContent;
    private LinearLayout viewNo;
    private Button btnCl;
    private Button btnOk;
    private RecyclerView recyclerview;

    private MsgDao msgDao = new MsgDao();

    //自动寻找控件
    private void findViews(View rootView) {
        imgHead = rootView.findViewById(R.id.img_head);
        txtName = rootView.findViewById(R.id.txt_name);
        ivImage = rootView.findViewById(R.id.iv_image);
        txtMsg = rootView.findViewById(R.id.txt_msg);
        edContent = rootView.findViewById(R.id.ed_content);
        viewNo = rootView.findViewById(R.id.view_no);
        btnCl = rootView.findViewById(R.id.btn_cl);
        btnOk = rootView.findViewById(R.id.btn_ok);
        recyclerview = rootView.findViewById(R.id.recyclerview);
    }


    //自动生成的控件事件
    private void initEvent() {
        btnCl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

    }


    //自动生成的控件事件
    private void initEvent(int msgType,String head, String name, String txt, String imgurl, String btnText, String gid) {

        //imgHead.setImageURI(Uri.parse(head));
        if (MsgForwardActivity.isSingleSelected) {
            imgHead.setVisibility(View.VISIBLE);
            txtName.setVisibility(View.VISIBLE);
            recyclerview.setVisibility(View.GONE);

            // 头像集合
            if (!TextUtils.isEmpty(head)) {
                List<String> headList = new ArrayList<>();
                headList.add(head);
                imgHead.setList(headList);
            } else {
                loadGroupHeads(gid, imgHead);
            }
            txtName.setText(name);
        } else if (!MsgForwardActivity.isSingleSelected && MsgForwardActivity.moreSessionBeanList.size() > 0) {
            imgHead.setVisibility(View.GONE);
            txtName.setVisibility(View.GONE);
            recyclerview.setVisibility(View.VISIBLE);

            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 5);
            recyclerview.setLayoutManager(gridLayoutManager);
            ForwardListAdapter forwardListAdapter = new ForwardListAdapter(context, MsgForwardActivity.moreSessionBeanList);
            recyclerview.setAdapter(forwardListAdapter);
        }

        if (StringUtil.isNotNull(txt)) {
            SpannableString spannableString = ExpressionUtil.getExpressionString(context, ExpressionUtil.DEFAULT_SIZE, txt);
            txtMsg.setText(spannableString);
            txtMsg.setVisibility(View.VISIBLE);
        } else {
            txtMsg.setVisibility(View.GONE);
        }

        if (StringUtil.isNotNull(imgurl)) {
            Glide.with(context).load(imgurl).into(ivImage);

            ivImage.setVisibility(View.VISIBLE);
        } else {
            ivImage.setVisibility(View.GONE);
        }


        btnOk.setText(btnText);
        btnCl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                event.onON();
                dismiss();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                event.onYes(edContent.getText().toString());
                dismiss();
            }
        });


    }


    public void dismiss() {
        alertDialog.dismiss();
    }

    public void init(Activity activity, int msgType,String head, String name, String txt, String imgurl, String btnText, String gid, Event e) {
        event = e;
        this.context = activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        alertDialog = builder.create();
        View rootView = View.inflate(context, R.layout.view_alert_forward, null);
        alertDialog.setView(rootView);
        findViews(rootView);
        initEvent(msgType,head, name, txt, imgurl, btnText, gid);
    }


    public void setContent(String content) {
        edContent.setText(content);
    }


    public void setEdHintOrSize(String hint, int size) {
        if (!TextUtils.isEmpty(hint)) {
            edContent.setHint(hint);
        }
        edContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(size)});
    }


    public void show() {
        alertDialog.show();
        WindowManager.LayoutParams p = alertDialog.getWindow().getAttributes();
        // p.height = DensityUtil.dip2px(activity, 226);
        p.width = DensityUtil.dip2px(context, 300);
        alertDialog.getWindow().setAttributes(p);
    }


    public interface Event {
        void onON();

        void onYes(String content);
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
