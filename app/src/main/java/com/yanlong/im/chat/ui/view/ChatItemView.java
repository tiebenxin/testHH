package com.yanlong.im.chat.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hm.cxpay.global.PayEnum;
import com.luck.picture.lib.tools.StringUtils;
import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.BalanceAssistantMessage;
import com.yanlong.im.chat.bean.ImageMessage;
import com.yanlong.im.chat.bean.LocationMessage;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.VideoMessage;
import com.yanlong.im.chat.bean.VoiceMessage;
import com.yanlong.im.chat.ui.RoundTransform;
import com.yanlong.im.location.LocationUtils;
import com.yanlong.im.utils.ExpressionUtil;
import com.yanlong.im.utils.GlideOptionsUtil;
import com.yanlong.im.utils.PatternUtil;
import com.yanlong.im.utils.audio.AudioPlayManager;
import com.yanlong.im.utils.socket.MsgBean;
import com.yanlong.im.view.CountDownView;
import com.yanlong.im.view.face.FaceView;

import net.cb.cb.library.utils.DensityUtil;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.TimeToString;
import net.cb.cb.library.view.WebPageActivity;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.kareluo.ui.OptionMenu;

public class ChatItemView extends LinearLayout {
    private final int DEFAULT_W = 120;
    private final int DEFAULT_H = 180;

    private TextView txtOtName;
    private TextView txtMeName;
    private TextView txtTime;
    private TextView txtBroadcast;
    private ImageView imgBroadcast;
    private View viewBroadcast;

    private LinearLayout viewOt;
    private ImageView imgOtHead;
    private LinearLayout viewOt1;
    private AppCompatTextView txtOt1;
    private LinearLayout viewOt2;
    private AppCompatTextView txtOt2;
    private LinearLayout viewOt3;
    private ImageView imgOtRbState;
    private TextView txtOtRbTitle;
    private TextView txtOtRbInfo;
    private TextView txtOtRpBt;
    private ImageView imgOtRbIcon;
    private LinearLayout viewMe;
    private LinearLayout viewMe1;
    private AppCompatTextView txtMe1;
    private LinearLayout viewMe2;
    private AppCompatTextView txtMe2;
    private LinearLayout viewMe3;
    private ImageView imgMeRbState;
    private TextView txtMeRbTitle;
    private TextView txtMeRbInfo;
    //    private TextView txtMeRpBt;
    private TextView txtMeRpBt, img_me_4_time, img_ot_4_time;
    private ImageView imgMeRbIcon;
    private ImageView imgMeErr;
    //    private ImageView imgMeHead,img_ot_4_play;
    private ImageView imgMeHead, img_me_4_play, img_ot_4_play;

    private LinearLayout viewMe4;
    private ProgressBar imgMeUp;
    private View viewMeUp;
    private TextView txtMeUp;
    private LinearLayout viewOt4;
    /*    private com.facebook.drawee.view.SimpleDraweeView imgOt4;
        private com.facebook.drawee.view.SimpleDraweeView imgMe4;*/
    private ImageView imgOt4;
    private ImageView imgMe4;


    private LinearLayout viewOt5;
    private ImageView imgOt5;
    private TextView txtOt5Title;
    private TextView txtOt5Info;
    private TextView txtOt5Bt;

    private LinearLayout viewMe5;
    private ImageView imgMe5;
    private TextView txtMe5Title;
    private TextView txtMe5Info;
    private TextView txtMe5Bt;

    private LinearLayout viewMe6;
    private ImageView imgMeTsState;
    private TextView txtMeTsTitle;
    private TextView txtMeTsInfo;
    private TextView txtMeTsBt;
    private ImageView imgMeTsIcon;

    private LinearLayout viewOt6;
    private ImageView imgOtTsState;
    private TextView txtOtTsTitle;
    private TextView txtOtTsInfo;
    private TextView txtOtTsBt;
    private ImageView imgOtTsIcon;

    private VoiceView viewMe7;
    private VoiceView viewOt7;
    private boolean isMe;
    private View viewOtTouch;
    private View viewMeTouch;
    private LinearLayout viewOt8;
    private AppCompatTextView txtOt8;
    private LinearLayout viewMe8;
    private AppCompatTextView txtMe8;
    private View viewLock;
    private TextView tvLock;
    private LinearLayout viewReadDestroy;
    private TextView txtReadDestroy;
    private ImageView imgReadDestroy;

    private LinearLayout viewMeVoiceVideo;
    private LinearLayout viewOtVoiceVideo;
    private LinearLayout viewMeCustomerFace;
    private LinearLayout viewOtCustomerFace;
    private ImageView imgMeCustomerFace;
    private ImageView imgOtCustomerFace;
    private TextView txtMeVoiceVideo;
    private TextView txtOtVoiceVideo;
    private LinearLayout viewRead;
    private TextView tvRead;
    private TextView tvReadTime;
    private CountDownView viewOtSurvivalTime;
    private CountDownView viewMeSurvivalTime;

    private int mHour, mMin, mSecond;

    //游戏分享
    private View viewOtGameShare;
    private View viewMeGameShare;
    private TextView tvOtGameTitle;
    private TextView tvOtGameInfo;
    private ImageView ivOtGameIcon;
    private ImageView ivOtAppIcon;
    private TextView tvMeGameTitle;
    private TextView tvMeGameInfo;
    private ImageView ivMeGameIcon;
    private ImageView ivMeAppIcon;
    private LabelItemView viewOtBalance;

    //位置
    private RelativeLayout location_you_ll, location_me_ll;
    private ImageView location_image_you_iv, location_image_me_iv;
    private TextView location_name_you_tv, location_desc_you_tv, location_name_me_tv, location_desc_me_tv;
    private LinearLayout viewOtChild;


    public ChatItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        options = new RequestOptions().centerCrop().transform(new RoundTransform(mContext, 10));
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View viewRoot = inflater.inflate(R.layout.view_chat_item, this);
        findViews(viewRoot);
        initEvent();
    }

    //自动寻找控件
    private void findViews(View rootView) {

        txtMeName = rootView.findViewById(R.id.txt_me_name);
        txtOtName = rootView.findViewById(R.id.txt_ot_name);
        txtTime = rootView.findViewById(R.id.txt_time);
        txtBroadcast = rootView.findViewById(R.id.txt_broadcast);
        imgBroadcast = rootView.findViewById(R.id.img_broadcast);
        viewBroadcast = rootView.findViewById(R.id.view_broadcast);

        viewOt = rootView.findViewById(R.id.view_ot);
        viewOtChild = rootView.findViewById(R.id.view_ot_touch_child);
        imgOtHead = rootView.findViewById(R.id.img_ot_head);
        viewOt1 = rootView.findViewById(R.id.view_ot_1);
        txtOt1 = rootView.findViewById(R.id.txt_ot_1);
        viewOt2 = rootView.findViewById(R.id.view_ot_2);
        txtOt2 = rootView.findViewById(R.id.txt_ot_2);
        viewOt3 = rootView.findViewById(R.id.view_ot_3);
        imgOtRbState = rootView.findViewById(R.id.img_ot_rb_state);
        txtOtRbTitle = rootView.findViewById(R.id.txt_ot_rb_title);
        txtOtRbInfo = rootView.findViewById(R.id.txt_ot_rb_info);
        txtOtRpBt = rootView.findViewById(R.id.txt_ot_rp_bt);
        imgOtRbIcon = rootView.findViewById(R.id.img_ot_rb_icon);
        viewMe = rootView.findViewById(R.id.view_me);
        viewMe1 = rootView.findViewById(R.id.view_me_1);
        txtMe1 = rootView.findViewById(R.id.txt_me_1);
        viewMe2 = rootView.findViewById(R.id.view_me_2);
        txtMe2 = rootView.findViewById(R.id.txt_me_2);
        viewMe3 = rootView.findViewById(R.id.view_me_3);
        imgMeRbState = rootView.findViewById(R.id.img_me_rb_state);
        txtMeRbTitle = rootView.findViewById(R.id.txt_me_rb_title);
        txtMeRbInfo = rootView.findViewById(R.id.txt_me_rb_info);
        txtMeRpBt = rootView.findViewById(R.id.txt_me_rp_bt);
        imgMeRbIcon = rootView.findViewById(R.id.img_me_rb_icon);
        imgMeHead = rootView.findViewById(R.id.img_me_head);
        imgMeErr = rootView.findViewById(R.id.img_me_err);
        img_me_4_time = rootView.findViewById(R.id.img_me_4_time);
        img_ot_4_time = rootView.findViewById(R.id.img_ot_4_time);
        img_me_4_play = rootView.findViewById(R.id.img_me_4_play);
        img_ot_4_play = rootView.findViewById(R.id.img_ot_4_play);

        viewOt4 = rootView.findViewById(R.id.view_ot_4);
        imgOt4 = rootView.findViewById(R.id.img_ot_4);
        viewMe4 = rootView.findViewById(R.id.view_me_4);
        imgMeUp = rootView.findViewById(R.id.img_me_up);
        viewMeUp = rootView.findViewById(R.id.view_me_up);
        txtMeUp = rootView.findViewById(R.id.txt_me_up);
        imgMe4 = rootView.findViewById(R.id.img_me_4);

        viewOt5 = rootView.findViewById(R.id.view_ot_5);
        imgOt5 = rootView.findViewById(R.id.img_ot_5);
        txtOt5Title = rootView.findViewById(R.id.txt_ot_5_title);
        txtOt5Info = rootView.findViewById(R.id.txt_ot_5_info);
        txtOt5Bt = rootView.findViewById(R.id.txt_ot_5_bt);

        viewMe5 = rootView.findViewById(R.id.view_me_5);
        imgMe5 = rootView.findViewById(R.id.img_me_5);
        txtMe5Title = rootView.findViewById(R.id.txt_me_5_title);
        txtMe5Info = rootView.findViewById(R.id.txt_me_5_info);
        txtMe5Bt = rootView.findViewById(R.id.txt_me_5_bt);


        viewMe6 = rootView.findViewById(R.id.view_me_6);
        imgMeTsState = rootView.findViewById(R.id.img_me_ts_state);
        txtMeTsTitle = rootView.findViewById(R.id.txt_me_ts_title);
        txtMeTsInfo = rootView.findViewById(R.id.txt_me_ts_info);
        txtMeTsBt = rootView.findViewById(R.id.txt_me_ts_bt);
        imgMeTsIcon = rootView.findViewById(R.id.img_me_ts_icon);

        viewOt6 = rootView.findViewById(R.id.view_ot_6);
        imgOtTsState = rootView.findViewById(R.id.img_ot_ts_state);
        txtOtTsTitle = rootView.findViewById(R.id.txt_ot_ts_title);
        txtOtTsInfo = rootView.findViewById(R.id.txt_ot_ts_info);
        txtOtTsBt = rootView.findViewById(R.id.txt_ot_ts_bt);
        imgOtTsIcon = rootView.findViewById(R.id.img_ot_ts_icon);

        viewOt7 = rootView.findViewById(R.id.view_ot_7);
        viewMe7 = rootView.findViewById(R.id.view_me_7);
        viewMeTouch = rootView.findViewById(R.id.view_me_touch);
        viewOtTouch = rootView.findViewById(R.id.view_ot_touch);

        //小助手消息
        viewOt8 = rootView.findViewById(R.id.view_ot_8);
        txtOt8 = rootView.findViewById(R.id.txt_ot_8);
        viewMe8 = rootView.findViewById(R.id.view_me_8);
        txtMe8 = rootView.findViewById(R.id.txt_me_8);

        //端到端加密提示消息
        viewLock = rootView.findViewById(R.id.view_lock);
        tvLock = rootView.findViewById(R.id.tv_lock);

        viewMeVoiceVideo = rootView.findViewById(R.id.view_me_voice_video);
        viewOtVoiceVideo = rootView.findViewById(R.id.view_ot_voice_video);
        txtMeVoiceVideo = rootView.findViewById(R.id.txt_me_voice_video);
        txtOtVoiceVideo = rootView.findViewById(R.id.txt_ot_voice_video);
        viewMeCustomerFace = rootView.findViewById(R.id.view_me_customer_face);
        viewOtCustomerFace = rootView.findViewById(R.id.view_ot_customer_face);
        imgMeCustomerFace = rootView.findViewById(R.id.img_me_customer_face);
        imgOtCustomerFace = rootView.findViewById(R.id.img_ot_customer_face);

        //阅后即焚
        viewReadDestroy = rootView.findViewById(R.id.view_read_destroy);
        txtReadDestroy = rootView.findViewById(R.id.txt_read_destroy);
        imgReadDestroy = rootView.findViewById(R.id.img_read_destroy);
        viewOtSurvivalTime = findViewById(R.id.view_ot_survival_time);
        viewMeSurvivalTime = findViewById(R.id.view_me_survival_time);

        viewRead = rootView.findViewById(R.id.view_read);
        tvRead = rootView.findViewById(R.id.tv_read);
        tvReadTime = rootView.findViewById(R.id.tv_read_time);

        //游戏分享
        viewOtGameShare = rootView.findViewById(R.id.view_ot_game_share);
        tvOtGameTitle = rootView.findViewById(R.id.tv_ot_game_title);
        tvOtGameInfo = rootView.findViewById(R.id.tv_ot_game_info);
        ivOtGameIcon = rootView.findViewById(R.id.iv_ot_game_icon);
        ivOtAppIcon = rootView.findViewById(R.id.iv_ot_app_icon);

        viewMeGameShare = rootView.findViewById(R.id.view_me_game_share);
        tvMeGameTitle = rootView.findViewById(R.id.tv_me_game_title);
        tvMeGameInfo = rootView.findViewById(R.id.tv_me_game_info);
        ivMeGameIcon = rootView.findViewById(R.id.iv_me_game_icon);
        ivMeAppIcon = rootView.findViewById(R.id.iv_me_app_icon);

        //零钱助手消息，只有接收消息
//        viewMeBalance = rootView.findViewById(R.id.view_me_balance);
        viewOtBalance = rootView.findViewById(R.id.view_ot_balance);

        //位置
        location_you_ll = rootView.findViewById(R.id.location_you_ll);
        location_me_ll = rootView.findViewById(R.id.location_me_ll);
        location_image_you_iv = rootView.findViewById(R.id.location_image_you_iv);
        location_image_me_iv = rootView.findViewById(R.id.location_image_me_iv);
        location_name_you_tv = rootView.findViewById(R.id.location_name_you_tv);
        location_desc_you_tv = rootView.findViewById(R.id.location_desc_you_tv);
        location_name_me_tv = rootView.findViewById(R.id.location_name_me_tv);
        location_desc_me_tv = rootView.findViewById(R.id.location_desc_me_tv);
    }

    public void setOnLongClickListener(OnLongClickListener onLongClick) {


        viewOtTouch.setOnLongClickListener(onLongClick);
        viewMeTouch.setOnLongClickListener(onLongClick);
       /* imgMe4.setOnLongClickListener(onLongClick);
        imgMe4.setOnLongClickListener(onLongClick);

        viewMe7.setOnLongClickListener(onLongClick);*/

    }

    public void setHeadOnLongClickListener(OnLongClickListener onLongClick) {


        //  imgMeHead.setOnLongClickListener(onLongClick);
        imgOtHead.setOnLongClickListener(onLongClick);


    }


    //自动生成的控件事件
    private void initEvent() {


    }

    /***
     * 显示类型
     * @param type
     * @param isMe
     */
    public void setShowType(int type, boolean isMe, String headUrl, String nikeName, String time, boolean isGroup) {

        this.isMe = isMe;
        if (isMe) {
            viewMe.setVisibility(VISIBLE);
            viewOt.setVisibility(GONE);
        } else {
            viewMe.setVisibility(GONE);
            viewOt.setVisibility(VISIBLE);
        }
        imgOtHead.setVisibility(VISIBLE);
        viewBroadcast.setVisibility(GONE);
        //  imgMeErr.setVisibility(GONE);
        viewMe1.setVisibility(GONE);
        viewOt1.setVisibility(GONE);
        viewMe2.setVisibility(GONE);
        viewOt2.setVisibility(GONE);
        viewMe3.setVisibility(GONE);
        viewOt3.setVisibility(GONE);
        viewMe4.setVisibility(GONE);
        viewOt4.setVisibility(GONE);
        viewMe5.setVisibility(GONE);
        viewOt5.setVisibility(GONE);
        viewMe6.setVisibility(GONE);
        viewOt6.setVisibility(GONE);
        viewMe7.setVisibility(GONE);
        viewOt7.setVisibility(GONE);
        viewMe8.setVisibility(GONE);
        viewOt8.setVisibility(GONE);
        viewLock.setVisibility(GONE);
        viewRead.setVisibility(GONE);
//        viewOtSurvivalTime.setVisibility(GONE);
//        viewMeSurvivalTime.setVisibility(GONE);
        viewReadDestroy.setVisibility(GONE);
        img_me_4_play.setVisibility(View.GONE);
        img_me_4_time.setVisibility(View.GONE);
        img_ot_4_time.setVisibility(View.GONE);
        img_ot_4_play.setVisibility(View.GONE);
        viewMeVoiceVideo.setVisibility(GONE);
        viewOtVoiceVideo.setVisibility(GONE);
        viewMeCustomerFace.setVisibility(GONE);
        viewOtCustomerFace.setVisibility(GONE);
        viewMeGameShare.setVisibility(GONE);
        viewOtGameShare.setVisibility(GONE);
        viewOtBalance.setVisibility(GONE);

        //位置
        location_you_ll.setVisibility(GONE);
        location_me_ll.setVisibility(GONE);


        switch (type) {
            case ChatEnum.EMessageType.MSG_CANCEL://撤回的消息
            case 0://公告
                viewBroadcast.setVisibility(VISIBLE);
                viewMe.setVisibility(GONE);
                viewOt.setVisibility(GONE);
                break;
            case 1:
                viewMe1.setVisibility(VISIBLE);
                viewOt1.setVisibility(VISIBLE);
                break;
            case 2:
                viewMe2.setVisibility(VISIBLE);
                viewOt2.setVisibility(VISIBLE);
                break;
            case 3:
                viewMe3.setVisibility(VISIBLE);
                viewOt3.setVisibility(VISIBLE);
                break;
            case 4:
                viewMe4.setVisibility(VISIBLE);
                viewOt4.setVisibility(VISIBLE);
                break;
            case 5:
                viewMe5.setVisibility(VISIBLE);
                viewOt5.setVisibility(VISIBLE);
                break;
            case 6:
                viewMe6.setVisibility(VISIBLE);
                viewOt6.setVisibility(VISIBLE);
                break;
            case 7:
                viewMe7.setVisibility(VISIBLE);
                viewOt7.setVisibility(VISIBLE);
                break;
            case 8:
                viewMe1.setVisibility(VISIBLE);
                viewOt1.setVisibility(VISIBLE);
                break;
            case ChatEnum.EMessageType.ASSISTANT:
                viewMe8.setVisibility(VISIBLE);
                viewOt8.setVisibility(VISIBLE);
                break;
            case ChatEnum.EMessageType.LOCK:
                viewLock.setVisibility(VISIBLE);
                viewMe.setVisibility(GONE);
                viewOt.setVisibility(GONE);
                break;
            case ChatEnum.EMessageType.CHANGE_SURVIVAL_TIME:
                viewReadDestroy.setVisibility(VISIBLE);
                viewMe.setVisibility(GONE);
                viewOt.setVisibility(GONE);
                break;
            case ChatEnum.EMessageType.MSG_VIDEO:
                viewMe4.setVisibility(VISIBLE);
                viewOt4.setVisibility(VISIBLE);
//                img_me_4_time.setVisibility(View.VISIBLE);
//                img_me_4_play.setVisibility(View.VISIBLE);
                img_ot_4_time.setVisibility(View.VISIBLE);
                img_ot_4_play.setVisibility(View.VISIBLE);
                break;
            case ChatEnum.EMessageType.MSG_VOICE_VIDEO:
                viewMeVoiceVideo.setVisibility(VISIBLE);
                viewOtVoiceVideo.setVisibility(VISIBLE);
                break;
            case ChatEnum.EMessageType.LOCATION:
                location_you_ll.setVisibility(VISIBLE);
                location_me_ll.setVisibility(VISIBLE);
                break;
            case ChatEnum.EMessageType.BALANCE_ASSISTANT:
                setNoAvatarUI(isMe);
                viewOtBalance.setVisibility(VISIBLE);
                break;
        }

        if (headUrl != null) {
            Glide.with(this).load(headUrl)
                    .apply(GlideOptionsUtil.headImageOptions()).into(imgMeHead);
            Glide.with(this).load(headUrl)
                    .apply(GlideOptionsUtil.headImageOptions()).into(imgOtHead);
        }
        if (nikeName != null && isGroup) {
            txtMeName.setText(nikeName);
            txtOtName.setText(nikeName);
            txtOtName.setVisibility(VISIBLE);
            //  txtMeName.setVisibility(VISIBLE);
            txtMeName.setVisibility(GONE);
        } else {
            txtOtName.setVisibility(GONE);
            txtMeName.setVisibility(GONE);
        }

        if (TextUtils.isEmpty(time)) {
            txtTime.setVisibility(GONE);
        } else {
            txtTime.setText(time);
            txtTime.setVisibility(VISIBLE);
        }

        viewMeTouch.setOnClickListener(null);
        viewOtTouch.setOnClickListener(null);
        viewMeTouch.setOnLongClickListener(null);
        viewOtTouch.setOnLongClickListener(null);
    }

    //设置无头像UI
    private void setNoAvatarUI(boolean isMe) {
        if (isMe) {
            imgMeHead.setVisibility(GONE);//不显示头像
            viewMe.setPadding(10, 0, 10, 0);
        } else {
            imgOtHead.setVisibility(GONE);//不显示头像
            viewOt.setPadding(10, 0, 10, 0);
            viewOtTouch.setPadding(0, 0, 0, 0);
        }
//        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        viewOtChild.setLayoutParams(params);
    }

    //公告
    public void setData0(String msghtml) {
        imgBroadcast.setVisibility(GONE);
        txtBroadcast.setText(Html.fromHtml(msghtml));
    }

    public void setNoticeString(Spanned string) {
        imgBroadcast.setVisibility(GONE);
        txtBroadcast.setText(string);
    }

    public void setData0(SpannableStringBuilder stringBuilder) {
        imgBroadcast.setVisibility(GONE);
        txtBroadcast.setText(stringBuilder);
        txtBroadcast.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void showBroadcastIcon(Boolean isShow, Integer rid) {
        imgBroadcast.setVisibility(isShow ? VISIBLE : GONE);
        if (rid != null)
            imgBroadcast.setImageResource(rid);
    }

    /**
     * 普通消息
     *
     * @param msg
     * @param menus
     * @param fontSize
     */
    public void setData1(String msg, List<OptionMenu> menus, Integer fontSize) {
        SpannableString spannableString = null;
        if (fontSize != null) {
            spannableString = ExpressionUtil.getExpressionString(getContext(), fontSize.intValue(), msg);
        } else {
            spannableString = ExpressionUtil.getExpressionString(getContext(), ExpressionUtil.DEFAULT_SIZE, msg);
        }
        if (spannableString != null && spannableString.length() == PatternUtil.FACE_CUSTOMER_LENGTH) {// 自定义表情
            Pattern patten = Pattern.compile(PatternUtil.PATTERN_FACE_CUSTOMER, Pattern.CASE_INSENSITIVE); // 通过传入的正则表达式来生成一个pattern
            Matcher matcher = patten.matcher(spannableString);
            if (matcher.matches()) {
                if (FaceView.map_FaceEmoji != null && FaceView.map_FaceEmoji.get(msg) != null) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), Integer.parseInt(FaceView.map_FaceEmoji.get(msg).toString()));
                    if (bitmap != null) {
                        viewMeCustomerFace.setVisibility(VISIBLE);
                        viewOtCustomerFace.setVisibility(VISIBLE);
                        viewMe1.setVisibility(GONE);
                        viewOt1.setVisibility(GONE);
                        imgMeCustomerFace.setImageBitmap(bitmap);
                        imgOtCustomerFace.setImageBitmap(bitmap);
                    }
                }
                menus.add(new OptionMenu("转发"));
                menus.add(new OptionMenu("删除"));
            } else {// 普通消息
                showCommonMessage(spannableString, menus);
            }
        } else {// 普通消息
            showCommonMessage(spannableString, menus);
        }
    }

    /**
     * 显示普通消息
     *
     * @param spannableString
     * @param menus
     */
    private void showCommonMessage(SpannableString spannableString, List<OptionMenu> menus) {
        viewMeCustomerFace.setVisibility(GONE);
        viewOtCustomerFace.setVisibility(GONE);
        viewMe1.setVisibility(VISIBLE);
        viewOt1.setVisibility(VISIBLE);
        txtMe1.setText(spannableString);
        txtOt1.setText(spannableString);
        menus.add(new OptionMenu("复制"));
        menus.add(new OptionMenu("转发"));
        menus.add(new OptionMenu("删除"));
    }

    //AT消息
    public void setDataAt(String msg) {
        SpannableString spannableString = ExpressionUtil.getExpressionString(getContext(), ExpressionUtil.DEFAULT_SIZE, msg);
        txtMe1.setText(spannableString);
        txtOt1.setText(spannableString);
    }

    //已读消息
    public void setDataRead(int sendState, long time) {
        if (sendState != ChatEnum.ESendStatus.NORMAL || time == 0) {
            viewRead.setVisibility(GONE);
        } else {
            viewRead.setVisibility(VISIBLE);
            tvRead.setText("已读");
            tvReadTime.setText(TimeToString.HH_MM(time) + "");
        }
    }

    //设置阅后即焚消息显示
    public void setDataSurvivalTimeShow(int type) {
        LogUtil.getLog().d("CountDownView", type + "");
        //   timerCancel();
        if (isMe) {
            if (type == -1) {
                viewMeSurvivalTime.setVisibility(View.VISIBLE);
            } else if (type == 0) {
                viewMeSurvivalTime.setVisibility(View.GONE);
            } else {
                viewMeSurvivalTime.setVisibility(View.VISIBLE);
            }
        } else {
            if (type == -1) {
                viewOtSurvivalTime.setVisibility(View.VISIBLE);
            } else if (type == 0) {
                viewOtSurvivalTime.setVisibility(View.GONE);
            } else {
                viewOtSurvivalTime.setVisibility(View.VISIBLE);
            }
        }

    }


    //阅后即焚倒计时
    public void setDataSt(long startTime, long endTime) {
        if (isMe) {
            viewMeSurvivalTime.setRunTimer(startTime, endTime);
        } else {
            viewOtSurvivalTime.setRunTimer(startTime, endTime);
        }

    }

    //阅后即焚倒计时销毁
    public void timerCancel() {
        if (isMe) {
            viewMeSurvivalTime.timerStop();
        } else {
            viewOtSurvivalTime.timerStop();
        }
    }


    /**
     * 音视频消息
     *
     * @param msg
     */
    public void setDataVoiceOrVideo(String msg, int type, OnClickListener onk) {
        txtMeVoiceVideo.setText(msg);
        txtOtVoiceVideo.setText(msg);
        Drawable drawableVoice = getResources().getDrawable(R.drawable.svg_small_voice2);
        Drawable drawableVideo = getResources().getDrawable(R.drawable.svg_small_video2);
        if (type == MsgBean.AuVideoType.Audio.getNumber()) {
            StringUtils.modifyTextViewDrawable(txtMeVoiceVideo, drawableVoice, 2);
            StringUtils.modifyTextViewDrawable(txtOtVoiceVideo, drawableVoice, 0);
        } else {
            StringUtils.modifyTextViewDrawable(txtMeVoiceVideo, drawableVideo, 2);
            StringUtils.modifyTextViewDrawable(txtOtVoiceVideo, drawableVideo, 0);
        }
        viewMeTouch.setOnClickListener(onk);
        viewOtTouch.setOnClickListener(onk);
    }

    //戳一下消息
    public void setData2(String msg) {

        String textSource = "<font color='#079892'>戳一下　</font>" + msg;

        txtMe2.setText(Html.fromHtml(textSource));
        txtOt2.setText(txtMe2.getText());
    }

    //红包消息
    public void setData3(final boolean isInvalid, String title, String info, String typeName, int typeIconRes, int reType, final EventRP eventRP) {
        if (isInvalid) {//失效
            imgMeRbState.setImageResource(R.mipmap.ic_rb_zfb_n);
            imgOtRbState.setImageResource(R.mipmap.ic_rb_zfb_n);
//            viewMe3.setBackgroundResource(R.drawable.bg_chat_me_rp_h);
//            viewOt3.setBackgroundResource(R.drawable.bg_chat_other_rp_h);
            viewMe3.setBackgroundResource(R.drawable.selector_rp_h_me_touch);
            viewOt3.setBackgroundResource(R.drawable.selector_rp_h_other_touch);
        } else {
            imgMeRbState.setImageResource(R.mipmap.ic_rb_zfb_un);
            imgOtRbState.setImageResource(R.mipmap.ic_rb_zfb_un);
//            viewMe3.setBackgroundResource(R.drawable.bg_chat_me_rp);
//            viewOt3.setBackgroundResource(R.drawable.bg_chat_other_rp);
            viewMe3.setBackgroundResource(R.drawable.selector_rp_me_touch);
            viewOt3.setBackgroundResource(R.drawable.selector_rp_other_touch);
        }

        if (eventRP != null) {
            OnClickListener onk;
            viewMeTouch.setOnClickListener(onk = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    eventRP.onClick(isInvalid, reType);
                }
            });
            viewOtTouch.setOnClickListener(onk);
        }


        txtMeRbTitle.setText(title);
        txtOtRbTitle.setText(title);

        txtMeRbInfo.setText(info);
        txtOtRbInfo.setText(info);

        if (typeName != null) {
            txtMeRpBt.setText(typeName);
            txtOtRpBt.setText(typeName);
        }

        if (typeIconRes != 0) {
            imgMeRbIcon.setImageResource(typeIconRes);
            imgOtRbIcon.setImageResource(typeIconRes);
        }
    }


    //转账消息
    public void setData6(final int transferStatus, String title, String info, String typeName, int typeIconRes, int reType, final EventRP eventRP) {
        if (transferStatus == PayEnum.ETransferOpType.TRANS_SEND) {
            imgMeTsState.setImageResource(R.mipmap.ic_transfer_rb);
            imgOtTsState.setImageResource(R.mipmap.ic_transfer_rb);
            viewMe6.setBackgroundResource(R.drawable.bg_chat_me_rp);
            viewOt6.setBackgroundResource(R.drawable.bg_chat_other_rp);
        } else if (transferStatus == PayEnum.ETransferOpType.TRANS_RECEIVE) {
            imgMeTsState.setImageResource(R.mipmap.ic_transfer_receive_rb);
            imgOtTsState.setImageResource(R.mipmap.ic_transfer_receive_rb);
            viewMe6.setBackgroundResource(R.drawable.bg_chat_me_rp_h);
            viewOt6.setBackgroundResource(R.drawable.bg_chat_other_rp_h);
        } else if (transferStatus == PayEnum.ETransferOpType.TRANS_REJECT) {
            imgMeTsState.setImageResource(R.mipmap.ic_transfer_return_rb);
            imgOtTsState.setImageResource(R.mipmap.ic_transfer_return_rb);
            viewMe6.setBackgroundResource(R.drawable.bg_chat_me_rp_h);
            viewOt6.setBackgroundResource(R.drawable.bg_chat_other_rp_h);
        } else if (transferStatus == PayEnum.ETransferStatus.PAST) {
            imgMeTsState.setImageResource(R.mipmap.ic_transfer_return_rb);
            imgOtTsState.setImageResource(R.mipmap.ic_transfer_return_rb);
            viewMe6.setBackgroundResource(R.drawable.bg_chat_me_rp_h);
            viewOt6.setBackgroundResource(R.drawable.bg_chat_other_rp_h);
        }

        if (eventRP != null) {
            OnClickListener onk;
            viewMeTouch.setOnClickListener(onk = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    eventRP.onClick(true, reType);
                }
            });
            viewOtTouch.setOnClickListener(onk);
        }


        txtMeTsTitle.setText(title);
        txtOtTsTitle.setText(title);

        txtMeTsInfo.setText(info);
        txtOtTsInfo.setText(info);

        if (typeName != null) {
            txtMeTsBt.setText(typeName);
            txtOtTsBt.setText(typeName);
        }

        if (typeIconRes != 0) {
            imgMeTsIcon.setImageResource(typeIconRes);
            imgOtTsIcon.setImageResource(typeIconRes);
        }
    }

    //语音
    public void setData7(int second, boolean isRead, boolean isPlay, int playStatus, final OnClickListener onk) {
        viewOt7.init(isMe, second, isRead, isPlay, playStatus);
        viewMe7.init(isMe, second, isRead, isPlay, playStatus);
        viewMeTouch.setOnClickListener(onk);
        viewOtTouch.setOnClickListener(onk);
    }

    public void updateVoice(MsgAllBean bean) {
        VoiceMessage voice = bean.getVoiceMessage();
        String url = bean.isMe() ? voice.getLocalUrl() : voice.getUrl();
        viewOt7.init(bean.isMe(), voice.getTime(), bean.isRead(), AudioPlayManager.getInstance().isPlay(Uri.parse(url)), voice.getPlayStatus());
        viewMe7.init(bean.isMe(), voice.getTime(), bean.isRead(), AudioPlayManager.getInstance().isPlay(Uri.parse(url)), voice.getPlayStatus());
    }

    //普通消息
    public void setDataAssistant(String msg) {
//        msg = "http://baidu.com\n回复报告白拿的\nhttp://baidu.com\n发改委复合物号单位自己\nhttp://baidu.com";
        if (!StringUtil.isNotNull(msg)) {
            return;
        }
        Matcher matcher = StringUtil.URL.matcher(msg);
        int i = 0;
        int preLast = 0;
        int len = msg.length();
        SpannableStringBuilder builder = new SpannableStringBuilder();

        while (matcher.find()) {
            int groupCount = matcher.groupCount();
            if (groupCount >= 0) {
                int start = matcher.start();
                int end = matcher.end();
                if (i == 0) {
                    if (start != 0) {
                        builder.append(msg.substring(0, start));
                        builder.append(setClickableSpan(msg.substring(start, end)));
                    } else {
                        builder.append(setClickableSpan(msg.substring(start, end)));
                    }
                } else {
                    if (end != len - 1) {
                        builder.append(msg.substring(preLast, start));
                        builder.append(setClickableSpan(msg.substring(start, end)));
                    }
                }
                preLast = end;
            }
            i++;
        }
        if (preLast != 0) {
            builder.append(msg.substring(preLast));
            txtMe8.setMovementMethod(LinkMovementMethod.getInstance());
            txtOt8.setMovementMethod(LinkMovementMethod.getInstance());
            txtMe8.setText(builder);
            txtOt8.setText(builder);
        } else {
            txtMe8.setText(msg);
            txtOt8.setText(msg);
        }
    }

    private SpannableString setClickableSpan(final String url) {
        SpannableString span = new SpannableString(url);
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent(getContext(), WebPageActivity.class);
                intent.putExtra(WebPageActivity.AGM_URL, url);
//                Uri uri = Uri.parse(url);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                intent.putExtra(Browser.EXTRA_APPLICATION_ID, getContext().getPackageName());
                getContext().startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);//去除下划线
            }
        }, 0, url.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(Color.BLUE), 0, url.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }


    //端到端加密
    public void setLock(SpannableStringBuilder stringBuilder) {
        tvLock.setText(stringBuilder);
        tvLock.setMovementMethod(LinkMovementMethod.getInstance());
    }

    //零钱助手消息
    public void setBalanceMsg(BalanceAssistantMessage message, EventBalance eventBalance) {
        if (viewOtBalance != null && message != null) {
            viewOtBalance.bindData(message);
            viewOtBalance.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (eventBalance != null) {
                        eventBalance.onClick(message.getTradeId(), message.getDetailType());
                    }
                }
            });
        }
    }


    public void setFont(Integer size) {
        txtMe1.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        txtOt1.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);

        txtMe2.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        txtOt2.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }


    public interface EventRP {
        void onClick(boolean isInvalid, int reType);
    }

    //视频消息
    public void setDataVideo(VideoMessage videoMessage, final String url, final EventPic eventPic, Integer pg) {
        if (url != null) {
            final int width = DensityUtil.dip2px(getContext(), DEFAULT_W);
            final int height = DensityUtil.dip2px(getContext(), DEFAULT_H);

            //设定大小
            ViewGroup.LayoutParams lp = viewMeUp.getLayoutParams();
            if (videoMessage != null) {
                double mh = videoMessage.getHeight();
                double mw = videoMessage.getWidth();
                if (mh == 0) {
                    mh = height;
                }
                if (mw == 0) {
                    mw = width;
                }

                double cp = 1;
                if (mh > mw) {
                    cp = height / mh;
                } else {
                    cp = width / mw;
                }
                int w = new Double(mw * cp).intValue();
                int h = new Double(mh * cp).intValue();

                imgMe4.setLayoutParams(new FrameLayout.LayoutParams(w, h));
                imgOt4.setLayoutParams(new RelativeLayout.LayoutParams(w, h));

                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) img_me_4_time.getLayoutParams();
                layoutParams.setMargins(w - 105, h - 55, 0, 0);
                img_me_4_time.setLayoutParams(layoutParams);

                RelativeLayout.LayoutParams layoutParamsOT = (RelativeLayout.LayoutParams) img_ot_4_time.getLayoutParams();
                layoutParamsOT.setMargins(w - 105, h - 55, 0, 0);
                img_ot_4_time.setLayoutParams(layoutParamsOT);
                long currentTime = videoMessage.getDuration();
                // 转成秒
                currentTime = currentTime / 1000;
                mHour = (int) currentTime / 3600;
                mMin = (int) currentTime % 3600 / 60;
                mSecond = (int) currentTime % 60;

                if (mHour > 0) {
                    img_me_4_time.setText(String.format(Locale.CHINESE, "%02d:%02d:%02d", mHour, mMin, mSecond));
                    img_ot_4_time.setText(String.format(Locale.CHINESE, "%02d:%02d:%02d", mHour, mMin, mSecond));
                } else {
                    img_me_4_time.setText(String.format(Locale.CHINESE, "%02d:%02d", mMin, mSecond));
                    img_ot_4_time.setText(String.format(Locale.CHINESE, "%02d:%02d", mMin, mSecond));
                }
                lp.width = w;
                lp.height = h;

            } else {
                lp.width = width;
                lp.height = height;
            }

            viewMeUp.setLayoutParams(lp);
        }


        if (eventPic != null) {
            OnClickListener onk;
            viewMeTouch.setOnClickListener(onk = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    eventPic.onClick(url.toString());
                }
            });
            viewOtTouch.setOnClickListener(onk);
        }
//        Glide.with(this).load(imageHead)
//                .apply(GlideOptionsUtil.headImageOptions()).into(mSdImageHead);
        Glide.with(this).load(videoMessage.getBg_url()).apply(options).into(imgOt4);
        Glide.with(this).load(videoMessage.getBg_url()).apply(options).into(imgMe4);

        if (pg != null) {
            setImageProgress(pg);
        } else {
            if (netState == -1) {
                setImageProgress(0);
            } else {
                setImageProgress(null);
            }
        }
        if (null != pg) {
            if (pg.intValue() == 100 || pg.intValue() == 0) {
                setVideoIMGShow(true);
            } else {
                setVideoIMGShow(false);
            }
        }

    }

    //图片消息
    public void setData4(ImageMessage image, String url, final EventPic eventPic, Integer pg) {
        if (url != null) {
            setData4(image, Uri.parse(url), eventPic, pg);
        }

    }

    private RequestOptions options = null;

    //uri不再使用
    public void setData4(final ImageMessage image, final Uri uri, final EventPic eventPic, Integer pg) {
        if (uri != null) {
            final int width = DensityUtil.dip2px(getContext(), DEFAULT_W);
            final int height = DensityUtil.dip2px(getContext(), DEFAULT_H);

            //设定大小
            ViewGroup.LayoutParams lp = viewMeUp.getLayoutParams();
            if (image != null) {
                double mh = image.getHeight();
                double mw = image.getWidth();
                double rate = mw * 1.00 / mh;
                int w = 0;
                int h = 0;
                if (rate < 0.2) {
                    w = width;
                    h = height;
                } else {
                    if (mh == 0) {
                        mh = height;
                    }
                    if (mw == 0) {
                        mw = width;
                    }
                    double cp = 1;
                    if (mh > mw) {
                        cp = height / mh;
                    } else {
                        cp = width / mw;
                    }

                    w = new Double(mw * cp).intValue();
                    h = new Double(mh * cp).intValue();
                }

                imgMe4.setLayoutParams(new FrameLayout.LayoutParams(w, h));

                imgOt4.setLayoutParams(new RelativeLayout.LayoutParams(w, h));


                lp.width = w;
                lp.height = h;
                LogUtil.getLog().e(ChatItemView.class.getSimpleName(), "w=" + w + "--h=" + h);

            } else {
                lp.width = width;
                lp.height = height;
            }

            viewMeUp.setLayoutParams(lp);


            RequestListener requestListener = new RequestListener() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, final Object model, Target target, boolean isFirstResource) {
                    //加载失败后以静态图加载
                    imgOt4.post(new Runnable() {
                        @Override
                        public void run() {
                            // 处理Bulgy#29303 java.lang.IllegalArgumentException You cannot start a load for a destroyed activity
                            if (getContext() != null && !((Activity) getContext()).isFinishing()) {
                                Glide.with(getContext()).asBitmap().load(options).into(imgOt4);
                                Glide.with(getContext()).asBitmap().load(options).into(imgMe4);
                            }
                        }
                    });


                    return true;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {

                    return false;
                }


            };

            RequestOptions rOptions = new RequestOptions();
            // 处理Bulgy#29303 java.lang.IllegalArgumentException You cannot start a load for a destroyed activity
            if (getContext() != null && !((Activity) getContext()).isFinishing()) {

                RequestManager in = Glide.with(getContext());

                RequestBuilder rb;
                if (image.getThumbnailShow().toLowerCase().endsWith(".gif")) {
                    LogUtil.getLog().e("gif", "setData4: isgif");
                    rb = in.asGif();
                    rOptions.priority(Priority.LOW).diskCacheStrategy(DiskCacheStrategy.ALL);
                } else {
                    rb = in.asBitmap();
                    rOptions.override(width, height)
                            .priority(Priority.HIGH).diskCacheStrategy(DiskCacheStrategy.ALL);
                }

//            rb.apply(rOptions).listener(requestListener).load(uri);
                rb.apply(options).listener(requestListener).load(image.getThumbnailShow());
                rb.into(imgMe4);
                rb.into(imgOt4);
                if (pg != null) {
                    setImageProgress(pg);
                } else {
                    if (netState == -1) {
                        setImageProgress(0);
                    } else {
                        setImageProgress(null);
                    }
                }
            }
            if (eventPic != null) {

                OnClickListener onk;
                viewMeTouch.setOnClickListener(onk = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        eventPic.onClick(image.getThumbnailShow());
                    }
                });
                viewOtTouch.setOnClickListener(onk);
            }
        }
    }

    public void setImageProgress(Integer pg) {
        if (pg != null && pg != 100 && pg != 0) {
            viewMeUp.setVisibility(VISIBLE);
            txtMeUp.setText(pg + "%");
            imgMeErr.setVisibility(GONE);
        } else {
            viewMeUp.setVisibility(GONE);
        }
    }

    public void setVideoIMGShow(boolean show) {
        if (show) {
            img_me_4_play.setVisibility(View.VISIBLE);
            img_me_4_time.setVisibility(View.VISIBLE);
        } else {
            img_me_4_play.setVisibility(View.INVISIBLE);
            img_me_4_time.setVisibility(View.INVISIBLE);
        }

    }

    public interface EventPic {
        void onClick(String uri);

    }

    //名片消息
    public void setData5(String name, String info, String headUrl, String moreInfo, OnClickListener onk) {
        if (moreInfo != null) {
            txtMe5Bt.setText(moreInfo);
            txtOt5Bt.setText(moreInfo);
        }

        txtMe5Title.setText(name);
        txtMe5Info.setText(info);

        //   imgMe5.setImageURI(Uri.parse(headUrl));
        Glide.with(this).load(headUrl)
                .apply(GlideOptionsUtil.headImageOptions()).into(imgMe5);

        txtOt5Title.setText(name);
        txtOt5Info.setText(info);

        //       imgOt5.setImageURI(Uri.parse(headUrl));
        Glide.with(this).load(headUrl)
                .apply(GlideOptionsUtil.headImageOptions()).into(imgOt5);


        viewMeTouch.setOnClickListener(onk);
        viewOtTouch.setOnClickListener(onk);
    }


    //位置消息
    public void setDataLocation(LocationMessage locationMessage, OnClickListener onk) {
        if (locationMessage == null) {
            return;
        }
        location_name_you_tv.setText(locationMessage.getAddress());
        location_desc_you_tv.setText(locationMessage.getAddressDescribe());

        location_name_me_tv.setText(locationMessage.getAddress());
        location_desc_me_tv.setText(locationMessage.getAddressDescribe());

        //百度地图参数
        if(StringUtil.isNotNull(locationMessage.getImg())){
            Glide.with(this).load(locationMessage.getImg()).apply(GlideOptionsUtil.imageOptions()).into(location_image_you_iv);
            Glide.with(this).load(locationMessage.getImg()).apply(GlideOptionsUtil.imageOptions()).into(location_image_me_iv);
        }else {
            String baiduImageUrl = LocationUtils.getLocationUrl(locationMessage.getLatitude(), locationMessage.getLongitude());
            Glide.with(this).load(baiduImageUrl).apply(GlideOptionsUtil.imageOptions()).into(location_image_you_iv);
            Glide.with(this).load(baiduImageUrl).apply(GlideOptionsUtil.imageOptions()).into(location_image_me_iv);
        }

        viewMeTouch.setOnClickListener(onk);
        viewOtTouch.setOnClickListener(onk);
    }

    private Context mContext;


    public void setReadDestroy(String content) {
        txtReadDestroy.setText(content);

//        if (type == 0) {
//            imgReadDestroy.setImageResource(R.mipmap.icon_read_destroy_cancel);
//        } else {
//            imgReadDestroy.setImageResource(R.mipmap.icon_read_destroy_seting);
//        }
    }


    private int netState;

    public void setErr(int state, boolean isShowLoad) {
        this.netState = state;
        switch (state) {
            case 0://正常
                imgMeErr.clearAnimation();
                imgMeErr.setVisibility(INVISIBLE);
                break;
            case 1://失败
                imgMeErr.clearAnimation();
                imgMeErr.setVisibility(VISIBLE);
                imgMeErr.setImageResource(R.mipmap.ic_net_err);
                if (viewMeUp != null && viewMeUp.getVisibility() == VISIBLE) {//隐藏进度
                    viewMeUp.setVisibility(GONE);
                }
                break;
            case 2://发送中
                if (isShowLoad) {
                    imgMeErr.setImageResource(R.mipmap.ic_net_load);
                    Animation rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_circle_rotate);
                    imgMeErr.startAnimation(rotateAnimation);
                    imgMeErr.setVisibility(VISIBLE);
                } else {
                    imgMeErr.clearAnimation();
                    imgMeErr.setVisibility(INVISIBLE);
                }
                break;
            case -1://图片待发送
                LogUtil.getLog().e("===state=" + state + "===isShowLoad=" + isShowLoad);
                if (isShowLoad) {
                    imgMeErr.setImageResource(R.mipmap.ic_net_load);
                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_circle_rotate);
                    imgMeErr.startAnimation(animation);
                    imgMeErr.setVisibility(VISIBLE);
                } else {
                    imgMeErr.clearAnimation();
                    imgMeErr.setVisibility(INVISIBLE);
                }
                break;
            default: // 其他状态如-1:待发送

                break;
        }
    }

    public void setOnErr(OnClickListener onk) {
        imgMeErr.setOnClickListener(onk);
    }

    public void setOnHead(OnClickListener onk) {
        imgMeHead.setOnClickListener(onk);
        imgOtHead.setOnClickListener(onk);
    }

    public void selectTextBubble(boolean flag) {
        viewOtTouch.setSelected(flag);
        viewMeTouch.setSelected(flag);

        viewOt1.setSelected(flag);
        viewMe1.setSelected(flag);

        viewOt2.setSelected(flag);
        viewMe2.setSelected(flag);

        viewOt3.setSelected(flag);
        viewMe3.setSelected(flag);

        viewOt4.setSelected(flag);
        viewMe4.setSelected(flag);

        viewOt5.setSelected(flag);
        viewMe5.setSelected(flag);

        viewOt6.setSelected(flag);
        viewMe6.setSelected(flag);

        viewOt7.setSelected(flag);
        viewMe7.setSelected(flag);

        viewOt8.setSelected(flag);
        viewMe8.setSelected(flag);

    }

    public interface EventBalance {
        void onClick(long tradeId, int detailType);
    }


}
