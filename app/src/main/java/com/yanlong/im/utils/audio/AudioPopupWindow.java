package com.yanlong.im.utils.audio;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import net.cb.cb.library.R;


public class AudioPopupWindow {
    private Context context;
    private TextView mTimerTV;
    private TextView mStateTV;
    private ImageView mStateIV;
    private PopupWindow mRecordWindow;

    public AudioPopupWindow(Context context) {
        this.context = context;
        initPopup();
    }

    private void initPopup() {
        View view = View.inflate(context, R.layout.popup_audio_wi_vo, null);
        mStateIV =  view.findViewById(R.id.rc_audio_state_image);
        mStateTV =  view.findViewById(R.id.rc_audio_state_text);
        mTimerTV =  view.findViewById(R.id.rc_audio_timer);
        mRecordWindow = new PopupWindow(view, -1, -1);
        mRecordWindow.setFocusable(true);
        mRecordWindow.setOutsideTouchable(false);
        mRecordWindow.setTouchable(false);
    }


    public void startAudio(){
        if (this.mRecordWindow != null) {
            this.mTimerTV.setVisibility(View.GONE);
            this.mStateIV.setVisibility(View.VISIBLE);
            this.mStateIV.setImageResource(R.mipmap.ic_volume_cancel);
            this.mStateTV.setVisibility(View.VISIBLE);
            this.mStateTV.setText("手指上滑，取消发送");
            this.mStateTV.setBackgroundResource(R.drawable.corner_voice_style);
        }
    }

    public void setAudioShort(){
        if (this.mRecordWindow != null) {
            mStateIV.setImageResource(R.mipmap.ic_volume_wraning);
            mStateTV.setText("录音时间太短");
        }
    }

    public void setCancel(){
        if (this.mRecordWindow != null) {
            this.mTimerTV.setVisibility(View.GONE);
            this.mStateIV.setVisibility(View.VISIBLE);
            this.mStateIV.setImageResource(R.mipmap.ic_volume_cancel);
            this.mStateTV.setVisibility(View.VISIBLE);
            this.mStateTV.setText("松开手指，取消发送");
            this.mStateTV.setBackgroundResource(R.drawable.corner_voice_style);
        }
    }


    public void setTimeout(int counter){
        if (this.mRecordWindow != null) {
            this.mStateIV.setVisibility(View.GONE);
            this.mStateTV.setVisibility(View.VISIBLE);
            this.mStateTV.setText("手指上滑，取消发送");
            this.mStateTV.setBackgroundResource(R.drawable.bg_voice_popup);
            this.mTimerTV.setText(String.format("%s", new Object[]{Integer.valueOf(counter)}));
            this.mTimerTV.setVisibility(View.VISIBLE);
        }
    }


    public void destroy(){
        if (this.mRecordWindow != null) {
            this.mRecordWindow.dismiss();
            this.mRecordWindow = null;
        }
    }

    public void audioDBChanged(int db){
        switch (db / 3) {
            case 0:
                this.mStateIV.setImageResource(R.mipmap.ic_volume_1);
                break;
            case 1:
                this.mStateIV.setImageResource(R.mipmap.ic_volume_2);
                break;
            case 2:
                this.mStateIV.setImageResource(R.mipmap.ic_volume_3);
                break;
            case 3:
                this.mStateIV.setImageResource(R.mipmap.ic_volume_4);
                break;
            case 4:
                this.mStateIV.setImageResource(R.mipmap.ic_volume_5);
                break;
            case 5:
                this.mStateIV.setImageResource(R.mipmap.ic_volume_6);
                break;
            case 6:
                this.mStateIV.setImageResource(R.mipmap.ic_volume_7);
                break;
            default:
                this.mStateIV.setImageResource(R.mipmap.ic_volume_8);
        }
    }


    public void showPopup(View view) {
        mRecordWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }


}
