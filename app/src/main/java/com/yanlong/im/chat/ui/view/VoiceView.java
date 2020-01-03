package com.yanlong.im.chat.ui.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;

import net.cb.cb.library.utils.DensityUtil;
import net.cb.cb.library.utils.LogUtil;

public class VoiceView extends LinearLayout {
    private LinearLayout viewOtVoice;
    private TextView txtOtVoice;
    private View viewOtP;
    private ImageView imgOtUnRead;
    private ImageView imgOtLoad;
    private LinearLayout viewMeVoice;
    private View viewMeP;
    private TextView txtMeVoice;
    private ImageView imgOtIcon;
    private ImageView imgMeIcon;

    public ImageView getImgOtIcon() {
        return imgOtIcon;
    }

    public ImageView getImgMeIcon() {
        return imgMeIcon;
    }

    //自动寻找控件
    private void findViews(View rootView) {
        viewOtVoice = (LinearLayout) rootView.findViewById(R.id.view_ot_voice);
        txtOtVoice = (TextView) rootView.findViewById(R.id.txt_ot_voice);
        viewOtP = (View) rootView.findViewById(R.id.view_ot_p);
        imgOtUnRead = rootView.findViewById(R.id.img_ot_unread);
        imgOtLoad = rootView.findViewById(R.id.img_ot_load);
        viewMeVoice = (LinearLayout) rootView.findViewById(R.id.view_me_voice);
        viewMeP = (View) rootView.findViewById(R.id.view_me_p);
        txtMeVoice = (TextView) rootView.findViewById(R.id.txt_me_voice);
        imgOtIcon = (ImageView) rootView.findViewById(R.id.img_ot_icon);
        imgMeIcon = (ImageView) rootView.findViewById(R.id.img_me_icon);


        viewOtVoice.setVisibility(GONE);
        viewMeVoice.setVisibility(GONE);
    }

    public VoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View viewRoot = inflater.inflate(R.layout.view_chat_item_voice, this);
        findViews(viewRoot);

    }

    public void init(final boolean isMe, final int second, boolean isRead, boolean isPlay, int playStatus) {
        LogUtil.getLog().i(VoiceView.class.getSimpleName(), "初始化View--" + "isRead=" + isRead + "--isPlay=" + isPlay + "--isMe=" + isMe + "--playStatus=" + playStatus);
        if (isMe) {
            viewMeVoice.setVisibility(VISIBLE);
            viewOtVoice.setVisibility(GONE);
        } else {
            viewMeVoice.setVisibility(GONE);
            viewOtVoice.setVisibility(VISIBLE);
            imgOtUnRead.setVisibility(playStatus != ChatEnum.EPlayStatus.NO_DOWNLOADED ? GONE : VISIBLE);
            imgOtLoad.setVisibility(GONE);
        }
        txtOtVoice.setText(second + "''");
        txtMeVoice.setText(second + "''");
        if (isPlay && playStatus == ChatEnum.EPlayStatus.PLAYING) {
            LogUtil.getLog().i(VoiceView.class.getSimpleName(), "播放语音动画");
            ((AnimationDrawable) imgMeIcon.getDrawable()).selectDrawable(2);
            ((AnimationDrawable) imgOtIcon.getDrawable()).selectDrawable(2);
            ((AnimationDrawable) imgMeIcon.getDrawable()).start();
            ((AnimationDrawable) imgOtIcon.getDrawable()).start();
        } else {
//            if (!isMe && isRead && playStatus == ChatEnum.EPlayStatus.NO_DOWNLOADED) {
//                LogUtil.getLog().i(VoiceView.class.getSimpleName(), "播放语音动画");
//                ((AnimationDrawable) imgMeIcon.getDrawable()).selectDrawable(2);
//                ((AnimationDrawable) imgOtIcon.getDrawable()).selectDrawable(2);
//                ((AnimationDrawable) imgMeIcon.getDrawable()).start();
//                ((AnimationDrawable) imgOtIcon.getDrawable()).start();
//            } else {
            LogUtil.getLog().i(VoiceView.class.getSimpleName(), "终止语音动画");
            ((AnimationDrawable) imgMeIcon.getDrawable()).stop();
            ((AnimationDrawable) imgOtIcon.getDrawable()).stop();
            ((AnimationDrawable) imgMeIcon.getDrawable()).selectDrawable(0);
            ((AnimationDrawable) imgOtIcon.getDrawable()).selectDrawable(0);
//            }
        }
        if (!isMe && !isRead) {
            setDownloadStatus(playStatus, isRead);
        }

        //语音部分宽度太宽，会造成未读红点不能正常显示，所以增大了x, 60-->84
        int s = second > 60 ? 60 : second;
        int wsum = getScreenWidth() - DensityUtil.dip2px(getContext(), 74) * 2;//-DensityUtil.dip2px(getContext(),35);
        float x = DensityUtil.dip2px(getContext(), 94);//viewOtP.getX();//原始值60
        int w = new Float((wsum - x) / 60 * (s)).intValue();
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewMeP.getLayoutParams();
        lp.width = w;
        lp.weight = 1;
        viewMeP.setLayoutParams(lp);

        lp = (LinearLayout.LayoutParams) viewOtP.getLayoutParams();
        lp.width = w;
        viewOtP.setLayoutParams(lp);


    }

    public void setDownloadStatus(@ChatEnum.EPlayStatus int state, boolean isRead) {
//        LogUtil.getLog().i("setDownloadStatus", "playStatus=" + state + "--isRead=" + isRead);
        switch (state) {
            case ChatEnum.EPlayStatus.DOWNLOADING://正常
                if (!isRead) {
                    Animation rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_circle_rotate);
                    imgOtLoad.startAnimation(rotateAnimation);
                    imgOtLoad.setVisibility(VISIBLE);
                }
                break;
            case ChatEnum.EPlayStatus.NO_PLAY:
                imgOtLoad.clearAnimation();
                imgOtLoad.setVisibility(GONE);
                break;
        }
    }

    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public void stopPlay() {
        ((AnimationDrawable) imgMeIcon.getDrawable()).stop();
        ((AnimationDrawable) imgOtIcon.getDrawable()).stop();
        ((AnimationDrawable) imgMeIcon.getDrawable()).selectDrawable(0);
        ((AnimationDrawable) imgOtIcon.getDrawable()).selectDrawable(0);
    }


}
