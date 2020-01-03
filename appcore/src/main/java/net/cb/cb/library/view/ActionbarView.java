package net.cb.cb.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import net.cb.cb.library.R;
import net.cb.cb.library.utils.ClickFilter;
import net.cb.cb.library.utils.DensityUtil;
import net.cb.cb.library.utils.StringUtil;


/***
 * 替换actionbar 1.可通过配置设置标题或者setTitle设置标题 2.setListenEvent(ListenEvent)来添加监听事件
 *
 * @author 姜永健
 * @date 2015年10月14日
 */
public class ActionbarView extends LinearLayout {

    private View rootView;
    private TextView txtTitle;
    private TextView txtTitleMore;
    private TextView txtLeft;
    private TextView txtRight;
    private TextView tvNumber;//消息数群聊人数
    private TextView tvChatTitle;//聊天界面标题加粗

    private ImageView btnBack;
    private ImageView btnRight;
    private View ViewLeft;
    private LinearLayout ViewRight;
    private ImageView ivLoadBar;//普通聊天离线圆形加载条->显示标题右侧
    private ImageView ivGroupLoadBar;//群聊离线圆形加载条->显示标题底部

    private Context context;
    private ListenEvent listenEvent;
    private ImageView iv_disturb;
    private ImageView btnIconRightRight;

    public void setOnListenEvent(ListenEvent listenEvent) {
        this.listenEvent = listenEvent;
    }

    /***
     * 设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        txtTitle.setText(title);
        txtTitle.setVisibility(VISIBLE);
        tvChatTitle.setVisibility(GONE);
    }

    /**
     * 新增-> 设置聊天标题 (单独加粗效果)
     * @param title
     */
    public void setChatTitle(String title) {
        tvChatTitle.setText(title);
        tvChatTitle.setVisibility(VISIBLE);
        txtTitle.setVisibility(GONE);
    }

    /**
     * 新增-> 单独显示群聊人数/消息数 (避免标题过长挤压)
     * @param number 具体数字
     * @param ifShow 控制是否显示
     */
    public void setNumber(int number,boolean ifShow){
        tvNumber.setText("("+number+")");
        tvNumber.setVisibility(ifShow ? VISIBLE : GONE);
    }

    /**
     * 优化-> 标题底部文字显示(在线状态/几小时前)
     * @param title 文字内容
     * @param ifShow 控制是否显示
     */
    public void setTitleMore(Spanned title,boolean ifShow) {
        if (StringUtil.isNotNull(title.toString())) {
            txtTitleMore.setText(title);
        }else {
            ifShow = false;
        }
        txtTitleMore.setVisibility(ifShow ? VISIBLE : GONE);
    }

    public TextView getTxtTitleMore() {
        return txtTitleMore;
    }

    public String getTitle() {
        return txtTitle.getText().toString();
    }

    /***
     * 设置左边文字
     *
     * @param txt
     */
    public void setTxtLeft(String txt) {
        txtLeft.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
        if (!TextUtils.isEmpty(txt)) {
            txtLeft.setText(txt);
            txtLeft.setVisibility(View.VISIBLE);
        } else {
            txtLeft.setVisibility(View.GONE);
        }
    }

    /***
     * 设置左边文字
     *
     * @param txt
     */
    public void setTxtLeft(String txt, int drawableId, int size) {
        txtLeft.setBackgroundResource(drawableId);
        if (size > 0) {
            txtLeft.setTextSize(size);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            if (txt.contains("+")) {
                params.width = DensityUtil.dip2px(getContext(), 35);
            } else {
                params.width = DensityUtil.dip2px(getContext(), 22);
            }
            params.height = DensityUtil.dip2px(getContext(), 22);
            txtLeft.setLayoutParams(params);
        } else {
            txtLeft.setTextSize(DensityUtil.sp2px(getContext(), 9));

        }
        if (!TextUtils.isEmpty(txt)) {
            txtLeft.setText(txt);
            txtLeft.setVisibility(View.VISIBLE);
        } else {
            txtLeft.setVisibility(View.GONE);
        }
    }

    /***
     * 设置右边文字
     *
     * @param txt
     */
    public void setTxtRight(String txt) {
        txtRight.setText(txt);
        txtRight.setVisibility(View.VISIBLE);
    }

    public TextView getTxtRight() {
        return txtRight;
    }

    /**
     * 获取中间TextView
     *
     * @return
     */
    public TextView getCenterTitle() {
        return txtTitle;
    }

    /***
     * 获取左边图标按钮
     *
     * @return
     */
    public ImageView getBtnLeft() {
        return btnBack;
    }

    /***
     * 获取右边图标按钮
     *
     * @return
     */
    public ImageView getBtnRight() {
        return btnRight;
    }

    /***
     * 获取右侧所有控件
     *
     * @return
     */
    public LinearLayout getViewRight() {
        return ViewRight;
    }

    /***
     * 右侧添加新控件
     */
    public void addViewRight(View child) {
        ViewRight.addView(child, 0);
    }

    /**
     * 阅后即焚图标
     * */
//    public void setImageShow(int image){
//        actionRightRight.setVisibility(View.VISIBLE);
//        btnIconRightRight.setImageResource(image);
//    }

    public ImageView getRightImage(){
        return btnIconRightRight;
    }


    /**
     * 根据给进来的颜色值设置文本颜色
     *
     * @param color
     */
    public void setTextColor(@ColorInt int color) {
        txtTitle.setTextColor(color);
        txtLeft.setTextColor(color);
        txtRight.setTextColor(color);
    }

    public void setTxtRightEnabled(boolean enabled) {
        ViewRight.setEnabled(enabled);


        txtRight.setTextColor(enabled ? Color.parseColor("#49C481") : Color.parseColor("#cccccc"));


    }
    //普通界面->离线加载条
    public ImageView getLoadBar() {
        if(ivGroupLoadBar.getVisibility() == VISIBLE){
            ivGroupLoadBar.setVisibility(GONE);
        }
        return ivLoadBar;
    }

    //聊天界面->离线加载条
    public ImageView getGroupLoadBar() {
        if(ivLoadBar.getVisibility() == VISIBLE){
            ivLoadBar.setVisibility(GONE);
        }
        return ivGroupLoadBar;
    }


    public ActionbarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.view_actionbar, this);
        txtTitle = rootView.findViewById(R.id.txt_title);
        tvChatTitle = rootView.findViewById(R.id.tv_chat_title);
        tvNumber = rootView.findViewById(R.id.tv_number);
        txtTitleMore = rootView.findViewById(R.id.txt_title_more);
        btnBack = rootView.findViewById(R.id.btn_icon);
        btnRight = rootView.findViewById(R.id.btn_icon_right);
        ViewLeft = rootView.findViewById(R.id.action_left);
        ViewRight = rootView.findViewById(R.id.action_right);
        ivLoadBar = rootView.findViewById(R.id.iv_load_bar);
        ivGroupLoadBar = rootView.findViewById(R.id.iv_group_load_bar);
        iv_disturb = rootView.findViewById(R.id.iv_disturb);
//        actionRightRight = rootView.findViewById(R.id.action_right_right);
        btnIconRightRight = rootView.findViewById(R.id.btn_icon_right_right);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ActionbarView);
        // 左图标
        if (typedArray.getBoolean(R.styleable.ActionbarView_actionbar_showIconLeft, true)) {
            btnBack.setVisibility(View.VISIBLE);
        } else {
            btnBack.setVisibility(View.GONE);
        }
        // 左图标
        int bid = typedArray.getResourceId(R.styleable.ActionbarView_actionbar_btnBackIcon, 0);
        if (bid != 0) {
            btnBack.setImageResource(bid);
        }

        // 右图标
        int rid = typedArray.getResourceId(R.styleable.ActionbarView_actionbar_rightIcon, 0);
        if (rid != 0) {
            btnRight.setVisibility(View.VISIBLE);
            btnRight.setImageResource(rid);
        } else {
            btnRight.setVisibility(View.GONE);
        }

        // 标题
        String title = typedArray.getString(R.styleable.ActionbarView_actionbar_txtTitle);
        txtTitle.setText(title);
        // 左文字
        txtLeft = rootView.findViewById(R.id.txt_back);
        String txtl = typedArray.getString(R.styleable.ActionbarView_actionbar_txtLeft);
        if (txtl == null || txtl.equals("")) {
            txtLeft.setVisibility(View.GONE);
        } else {
            txtLeft.setText(txtl);
            txtLeft.setVisibility(View.VISIBLE);
        }
        // 右文字
        txtRight = rootView.findViewById(R.id.txt_right);

        String txtr = typedArray.getString(R.styleable.ActionbarView_actionbar_txtRight);
        if (txtr == null || txtr.equals("")) {
            txtRight.setVisibility(View.GONE);
        } else {
            txtRight.setText(txtr);
            txtRight.setVisibility(View.VISIBLE);
        }

        ClickFilter.onClick(ViewRight, new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listenEvent != null
                        && (txtRight.getVisibility() == View.VISIBLE || btnRight.getVisibility() == View.VISIBLE))
                    listenEvent.onRight();
            }
        });

        ViewLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (listenEvent != null)
                    listenEvent.onBack();
            }
        });
        //断网加载圆圈改为gif
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        Glide.with(context).load(R.drawable.offline_loading).apply(options).into(ivLoadBar);
        Glide.with(context).load(R.drawable.offline_loading).apply(options).into(ivGroupLoadBar);

        // int color = typedArray.getColor(
        // R.styleable.ActionbarView_backgroundColor, Integer.MAX_VALUE);
        // if (color != Integer.MAX_VALUE) {
        // rootView.findViewById(R.id.ll_main).setBackgroundColor(color);
        // }

        for (int i = 0, size = attrs.getAttributeCount(); i < size; i++) {
            String name = attrs.getAttributeName(i);
            String value = attrs.getAttributeValue(i);

//            Log.i("ActionbarView", "#onCreate attr[" + i + "] name = " + name + " and value = " + value);
            if ("background".equals(name)) {
                if (value.startsWith("@")) {
                    int bgResId = Integer.parseInt(value.substring(1));
                    rootView.findViewById(R.id.ll_main).setBackgroundResource(bgResId);
                } else if (value.startsWith("#")) {
                    String alphaStr = null;
                    String colorStr = null;
                    if (value.length() == 9) {
                        alphaStr = value.substring(1, 3);
                        colorStr = value.substring(3);
                    } else if (value.length() <= 7) {
                        colorStr = value.substring(1);
                    }
                    if (colorStr != null) {
                        try {
                            int color = Integer.parseInt(colorStr, 16);
                            rootView.findViewById(R.id.ll_main).setBackgroundColor(color);
                        } catch (NumberFormatException e) {

                        }
                    }

                    if (alphaStr != null) {
                        try {
                            int alpha = Integer.parseInt(alphaStr, 16);
                            rootView.findViewById(R.id.ll_main).setAlpha(alpha);
                        } catch (NumberFormatException e) {

                        }
                    }

                }
            }

        }

        String value = attrs.getAttributeValue("android", "background");

        Log.i("ActionbarView", "#onCreate background = " + value);

    }


    /***
     * 监听事件
     *
     * @author 姜永健
     * @date 2015年10月14日
     */
    public interface ListenEvent {
        /***
         * 左边按钮监听
         */
        void onBack();

        /***
         * 右边按钮监听
         */
        void onRight();

    }


    public void setWhite() {
        //白色主题 1.16
        rootView.findViewById(R.id.ll_main).setBackgroundColor(Color.parseColor("#ffffff"));
        txtTitle.setTextColor(Color.parseColor("#000000"));
    }

    public void showDisturb(boolean isShow) {
        if (iv_disturb == null) {
            return;
        }
        iv_disturb.setVisibility(isShow ? VISIBLE : GONE);
    }

    /**
     * 零钱顶部背景样式
     */
    public void setChangeStyleBg(){
        rootView.findViewById(R.id.ll_main).setBackgroundColor(Color.parseColor("#c85749"));

    }

}
