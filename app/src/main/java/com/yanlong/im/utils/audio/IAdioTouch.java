package com.yanlong.im.utils.audio;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.example.nim_lib.controll.AVChatProfile;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.yanlong.im.R;

import net.cb.cb.library.utils.ToastUtil;

public class IAdioTouch implements View.OnTouchListener {
    private Context context;
    private MTouchListener listener;

    public IAdioTouch(Context context, MTouchListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //LogUtil.getLog().d("-------", "_______onTouch: "+event.getAction());
        // 判断是否正在音视频通话
        if (AVChatProfile.getInstance().isCallIng() || AVChatProfile.getInstance().isCallEstablished()) {
            if (AVChatProfile.getInstance().isChatType() == AVChatType.VIDEO.getValue()) {
                ToastUtil.show(context, context.getString(R.string.avchat_peer_busy_video));
            } else {
                ToastUtil.show(context, context.getString(R.string.avchat_peer_busy_voice));
            }
            return true;
        }
        //Log.d("-------", "_______onTouch: "+event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                AudioPlayManager.getInstance().stopPlay();
                AudioRecordManager.getInstance(context).startRecord();
                if (listener != null) {
                    listener.onDown();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isCancelled(v, event)) {
                    AudioRecordManager.getInstance(context).willCancelRecord();
                } else {
                    AudioRecordManager.getInstance(context).continueRecord();
                }
                if (listener != null) {
                    listener.onMove();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                AudioRecordManager.getInstance(context).stopRecord();
                AudioRecordManager.getInstance(context).destroyRecord();
                if (listener != null) {
                    listener.onUp();
                }
                break;
        }
        return true;
    }


    private boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth() || event.getRawY() < location[1] - 40) {
            return true;
        }
        return false;
    }


    public interface MTouchListener {
        void onDown();

        void onMove();

        void onUp();
    }

}
