package com.yanlong.im.utils.audio;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;


import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.UpFileAction;
import net.cb.cb.library.utils.UpFileUtil;


public class IAudioRecord implements IAudioRecordListener {
    private Context context;
    private View view;
    private AudioPopupWindow audioPopupWindow;
    private UrlCallback callback;

    /**
     * @param view        参照物view
     * @param urlCallback 返回上传音频url回调
     */
    public IAudioRecord(Context context, View view, UrlCallback urlCallback) {
        this.context = context;
        this.view = view;
        this.callback = urlCallback;
    }


    @Override
    public void initTipView() {
        audioPopupWindow = new AudioPopupWindow(context);
        audioPopupWindow.showPopup(view);
    }

    @Override
    public void setTimeoutTipView(int counter) {
        audioPopupWindow.setTimeout(counter);
    }

    @Override
    public void setRecordingTipView() {
        audioPopupWindow.startAudio();
    }

    @Override
    public void setAudioShortTipView() {
        audioPopupWindow.setAudioShort();
    }

    @Override
    public void setCancelTipView() {
        audioPopupWindow.setCancel();
    }

    @Override
    public void destroyTipView() {
        audioPopupWindow.destroy();
    }

    @Override
    public void onStartRecord() {

    }

    @Override
    public void onFinish(final Uri audioPath, final int duration) {

        if (audioPath != null) {
//            new UpFileAction().upFile(UpFileAction.PATH.VOICE, context, new UpFileUtil.OssUpCallback() {
//                @Override
//                public void success(String url) {
//                    if (callback != null) {
//                        LogUtil.getLog().e("AudioUploadPath", url + "");
//                        callback.getUrl(url, duration);
//                    }
//                }
//
//                @Override
//                public void fail() {
//                    ToastUtil.show(context, "发送失败!");
//                }
//
//                @Override
//                public void inProgress(long progress, long zong) {
//
//                }
//            }, audioPath.getPath());
            callback.completeRecord(audioPath.getPath(),duration);
        }

    }

    @Override
    public void onAudioDBChanged(int db) {
        audioPopupWindow.audioDBChanged(db);
    }


    public interface UrlCallback {
//        void getUrl(String url, int duration);

        //录制完成
        void completeRecord(String file,int duration);

    }


}
