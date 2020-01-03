package com.yanlong.im.utils.audio;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.UserSeting;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.utils.MyDiskCacheUtils;

import net.cb.cb.library.bean.EventVoicePlay;
import net.cb.cb.library.utils.DownloadUtil;
import net.cb.cb.library.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

import static android.media.AudioAttributes.CONTENT_TYPE_UNKNOWN;

public class AudioPlayManager implements SensorEventListener {
    private static final String TAG = AudioPlayManager.class.getSimpleName();
    private MediaPlayer _mediaPlayer;
    //    private IAudioPlayListener _playListener;
    private IVoicePlayListener voicePlayListener;
    private Uri _playingUri;
    private Sensor _sensor;
    //  private SensorManager _sensorManager;
    private static AudioManager _audioManager;
    private PowerManager _powerManager;
    private SensorManager _sensorManager;
    private PowerManager.WakeLock _wakeLock;
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private Context context;
    private MsgAllBean currentPlayingMsg;
    private boolean isCanAutoPlay = false;
    private int currentPosition;
    private MsgAllBean currentDownBean;
    public String msg_id;// 当前播放的语音ID

    public AudioPlayManager() {
    }

    public static AudioPlayManager getInstance() {
        return SingletonHolder.sInstance;
    }

    @TargetApi(11)
    public void onSensorChanged(SensorEvent event) {
        MsgDao msgDao = new MsgDao();
        UserSeting userSeting = msgDao.userSetingGet();
        int voice = userSeting.getVoicePlayer();
        float range = event.values[0];
        if (this._sensor != null && this._mediaPlayer != null) {
            if (this._mediaPlayer.isPlaying()) {
                if ((double) range > 0.0D) {
                    if (this._audioManager.getMode() == 0) {
                        return;
                    }

                    if (voice == 0) {
                        changeToSpeaker();
                    } else {
                        changeToReceiver();
                    }

//                    this._audioManager.setMode(0);
//                    this._audioManager.setSpeakerphoneOn(true);
                    final int positions = this._mediaPlayer.getCurrentPosition();

                    try {
                        this._mediaPlayer.reset();
                        this._mediaPlayer.setAudioStreamType(CONTENT_TYPE_UNKNOWN);
                        this._mediaPlayer.setVolume(1.0F, 1.0F);
                        //   this._mediaPlayer.setDataSource(this.context, this._playingUri);
                        String path = context.getExternalCacheDir().getAbsolutePath();
                        File file = new File(path, getFileName(this._playingUri.toString()));
                        if (file.exists()) {
//                            LogUtil.getLog().v(TAG, "本地播放" + file.getPath());

                            this._mediaPlayer.setDataSource(context, Uri.parse(file.getPath()));
                        } else {
//                            LogUtil.getLog().v(TAG, "在线播放--" + this._playingUri);
//                            this._mediaPlayer.setDataSource(context, this._playingUri);
//                            downloadAudio(context, this._playingUri.toString());
                        }


                        this._mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            public void onPrepared(MediaPlayer mp) {
                                mp.seekTo(positions);
                            }
                        });
                        this._mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                            public void onSeekComplete(MediaPlayer mp) {
                                mp.start();
                            }
                        });
                        this._mediaPlayer.prepareAsync();
                    } catch (IOException var5) {
                        var5.printStackTrace();
                    }

                    this.setScreenOn();
                } else {
                    this.setScreenOff();
                    if (Build.VERSION.SDK_INT >= 11) {
                        if (this._audioManager.getMode() == 3) {
                            return;
                        }

                        this._audioManager.setMode(3);
                    } else {
                        if (this._audioManager.getMode() == 2) {
                            return;
                        }

                        this._audioManager.setMode(2);
                    }

                    this._audioManager.setSpeakerphoneOn(false);
                    this.replay();
                }
            } else if ((double) range > 0.0D) {
                if (this._audioManager.getMode() == 0) {
                    return;
                }

                if (voice == 0) {
                    changeToSpeaker();
                } else {
                    changeToReceiver();
                }

//                this._audioManager.setMode(0);
//                this._audioManager.setSpeakerphoneOn(true);
                this.setScreenOn();
            }

        }
    }

    @SuppressLint("InvalidWakeLockTag")
    @TargetApi(21)
    private void setScreenOff() {
        if (this._wakeLock == null) {
            if (Build.VERSION.SDK_INT >= 21) {
                this._wakeLock = this._powerManager.newWakeLock(32, "AudioPlayManager");
            } else {
                LogUtil.getLog().e(TAG, "Does not support on level " + Build.VERSION.SDK_INT);
            }
        }

        if (this._wakeLock != null) {
            this._wakeLock.acquire();
        }

    }

    private void setScreenOn() {
        if (this._wakeLock != null) {
            this._wakeLock.setReferenceCounted(false);
            this._wakeLock.release();
            this._wakeLock = null;
        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void replay() {
        try {
            this._mediaPlayer.reset();
            this._mediaPlayer.setAudioStreamType(CONTENT_TYPE_UNKNOWN);
            this._mediaPlayer.setVolume(1.0F, 1.0F);
            this._mediaPlayer.setDataSource(this.context, this._playingUri);
            this._mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            this._mediaPlayer.prepareAsync();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public void startPlay(Context context, final MsgAllBean bean, int position, boolean canAutoPlay, IVoicePlayListener playListener) {
        LogUtil.getLog().i(TAG, "startPlay--" + bean.getVoiceMessage().getUrl());
        if (context != null && bean != null) {
            this.context = context;
            if (bean.getVoiceMessage() == null) {
                return;
            }
            if (this.voicePlayListener != null && this.currentPlayingMsg != null) {
                this.voicePlayListener.onStop(currentPlayingMsg);
            }
            msg_id = bean.getMsg_id();
            currentPlayingMsg = bean;
            currentPosition = position;
            isCanAutoPlay = canAutoPlay;
            String url = "";
            if (bean.isMe()) {
                url = bean.getVoiceMessage().getLocalUrl();
            } else {
                url = bean.getVoiceMessage().getUrl();
            }
            if (TextUtils.isEmpty(url)) {
                return;
            }
            Uri audioUri = Uri.parse(url);

            this.resetMediaPlayer();
            this.afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    LogUtil.getLog().d(TAG, "OnAudioFocusChangeListener " + focusChange);
                    if (AudioPlayManager.this._audioManager != null && focusChange == -1) {
                        AudioPlayManager.this._audioManager.abandonAudioFocus(AudioPlayManager.this.afChangeListener);
                        AudioPlayManager.this.afChangeListener = null;
                        AudioPlayManager.this.resetMediaPlayer();
                    }

                }
            };

            try {
                this._powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                this._audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                if (!this._audioManager.isWiredHeadsetOn()) {
                    this._sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                    this._sensor = this._sensorManager.getDefaultSensor(8);
                    this._sensorManager.registerListener(this, this._sensor, 3);
                }

                MsgDao msgDao = new MsgDao();
                UserSeting userSeting = msgDao.userSetingGet();
                int voice = userSeting.getVoicePlayer();
                if (voice == 0) {
                    changeToSpeaker();
                } else {
                    changeToReceiver();
                }

                this.muteAudioFocus(this._audioManager, true);
                AudioPlayManager.this.voicePlayListener = playListener;
                this._playingUri = audioUri;
                this._mediaPlayer = new MediaPlayer();
                this._mediaPlayer = new MediaPlayer();
                this._mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        LogUtil.getLog().i(TAG, "onCompletion--" + (AudioPlayManager.this.voicePlayListener == null));
                        if (AudioPlayManager.this.voicePlayListener != null) {
                            AudioPlayManager.this.voicePlayListener.onComplete(bean);
                            AudioPlayManager.this._playingUri = null;
                            AudioPlayManager.this.context = null;
                        }
                        AudioPlayManager.this.reset(true);
                    }
                });
                this._mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        AudioPlayManager.this.reset(false);
                        return true;
                    }
                });

                String path = context.getExternalCacheDir().getAbsolutePath();
                File file = new File(path, getFileName(audioUri.toString()));
                if (file.exists()) {
//                    LogUtil.getLog().v(TAG, "本地播放" + file.getPath());
                    this._mediaPlayer.setDataSource(context, Uri.parse(file.getPath()));
                } else {
//                    LogUtil.getLog().v(TAG, "在线播放--" + bean.getVoiceMessage().getUrl());
//                    this._mediaPlayer.setDataSource(context, audioUri);
//                    downloadAudio(context, audioUri.toString());
                }

                this._mediaPlayer.setAudioStreamType(CONTENT_TYPE_UNKNOWN);
                this._mediaPlayer.prepare();
                this._mediaPlayer.start();
                if (this.voicePlayListener != null) {
                    this.voicePlayListener.onStart(bean);
                }
            } catch (Exception var5) {
                var5.printStackTrace();
                if (this.voicePlayListener != null) {
                    this.voicePlayListener.onStop(bean);
                    this.voicePlayListener = null;
                }
                this.reset(false);
            }

        } else {
            LogUtil.getLog().e(TAG, "startPlay context or audioUri is null.");
        }
    }


    /**
     * 切换到外放
     */
    public static void changeToSpeaker() {
        if (_audioManager != null) {
            LogUtil.getLog().e(TAG, "扬声器播放");
            _audioManager.setMode(AudioManager.MODE_NORMAL);
            _audioManager.setSpeakerphoneOn(true);
        }
    }

    /**
     * 切换到耳机模式
     */
    public static void changeToHeadset() {
        if (_audioManager != null) {
            LogUtil.getLog().e(TAG, "耳机播放");
            _audioManager.setSpeakerphoneOn(false);
        }
    }

    /**
     * 切换到听筒
     */
    public static void changeToReceiver() {
        if (_audioManager != null) {
            LogUtil.getLog().e(TAG, "听筒播放");
            _audioManager.setSpeakerphoneOn(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                _audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

            } else {
                _audioManager.setMode(AudioManager.MODE_IN_CALL);
            }
        }
    }


    private void downloadAudio(final Context context, final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = context.getExternalCacheDir().getAbsolutePath();
                DownloadUtil.get().download(url, path, getFileName(url));
            }
        }).start();
    }

    public void downloadAudio(final Context context, final MsgAllBean bean, final DownloadUtil.IDownloadVoiceListener listener) {
//        LogUtil.getLog().i(TAG, "downloadAudio");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = context.getExternalCacheDir().getAbsolutePath();
                String url = bean.getVoiceMessage().getUrl();
                DownloadUtil.get().download(url, path, getFileName(url), new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {
                        listener.onDownloadSuccess(file);
                        currentDownBean = null;
                        LogUtil.getLog().i(TAG, "语音下载成功");

                        MyDiskCacheUtils.getInstance().putFileNmae(path,url);
                    }

                    @Override
                    public void onDownloading(int progress) {

                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        listener.onDownloadFailed(e);
                        currentDownBean = null;
                    }
                });
            }
        }).start();
    }


    private String getFileName(String url) {
        String fileName = "";
        if (!TextUtils.isEmpty(url)) {
            String strings[] = url.split("/");
            if (strings != null && strings.length > 0) {
                fileName = strings[strings.length - 1];
            }
        }
        return fileName;
    }


    public void setPlayListener(IVoicePlayListener listener) {
        this.voicePlayListener = listener;
    }

    public void stopPlay() {
//        LogUtil.getLog().i(TAG, "stopPlay--" + "voicePlayListener=" + voicePlayListener + "_playingUri=" + _playingUri);
        if (this.voicePlayListener != null && this.currentPlayingMsg != null) {
//            this.voicePlayListener.onStop(this._playingUri);
            this.voicePlayListener.onStop(currentPlayingMsg);
        }

        this.reset(false);
    }

    private void reset(boolean isCompleted) {
        this.resetMediaPlayer();
        this.resetAudioPlayManager(isCompleted);
    }

    private void resetAudioPlayManager(boolean isCompleted) {
//        LogUtil.getLog().i(TAG, "resetAudioPlayManager--" + (voicePlayListener == null));
        if (this._audioManager != null) {
            this.muteAudioFocus(this._audioManager, false);
        }

        if (this._sensorManager != null) {
            this._sensorManager.unregisterListener(this);
        }
        this._sensorManager = null;
        this._sensor = null;
        this._powerManager = null;
        this._audioManager = null;
        this._wakeLock = null;
        this._playingUri = null;
        if (voicePlayListener != null) {
            this.voicePlayListener = null;
        }
        if (isCompleted && isCanAutoPlay) {
            EventVoicePlay eventVoicePlay = new EventVoicePlay();
            eventVoicePlay.setPosition(currentPosition);
            eventVoicePlay.setBean(currentPlayingMsg);
            EventBus.getDefault().post(eventVoicePlay);
            currentPlayingMsg = null;
        }
    }

    private void resetMediaPlayer() {
        if (this._mediaPlayer != null) {
            try {
                this._mediaPlayer.stop();
                // TODO 处理Bugly上指针 #25602
//                this._mediaPlayer.reset();
                this._mediaPlayer.release();
                this._mediaPlayer = null;
            } catch (Exception e) {

            }
        }
    }


    public boolean isPlay(Uri url) {

        if (_playingUri == null)
            return false;
        boolean isPlay = false;
        if (_playingUri.equals(url)) {
            isPlay = true;
//            LogUtil.getLog().d(TAG, "isPlay: " + isPlay /*+ "--isPlaying=" + isPlaying*/ + "--url=" + url);
        }
//        LogUtil.getLog().d(TAG, "isPlay: " + isPlay + "--url=" + url);
        return isPlay;
    }

    public Uri getPlayingUri() {
        return this._playingUri;
    }

    @TargetApi(8)
    private void muteAudioFocus(AudioManager audioManager, boolean bMute) {
        if (Build.VERSION.SDK_INT < 8) {
            LogUtil.getLog().d(TAG, "muteAudioFocus Android 2.1 and below can not stop music");
        } else {
            if (bMute) {
                audioManager.requestAudioFocus(this.afChangeListener, 3, 2);
            } else {
                audioManager.abandonAudioFocus(this.afChangeListener);
                this.afChangeListener = null;
            }

        }
    }

    static class SingletonHolder {
        static AudioPlayManager sInstance = new AudioPlayManager();

        SingletonHolder() {
        }
    }


    public static class HeadsetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MsgDao msgDao = new MsgDao();
            UserSeting userSeting = msgDao.userSetingGet();
            int voice = userSeting.getVoicePlayer();

            switch (action) {
                //插入和拔出耳机会触发此广播
                case Intent.ACTION_HEADSET_PLUG:
                    int state = intent.getIntExtra("state", 0);
                    if (state == 1) {
                        changeToHeadset();
                    } else if (state == 0) {
                        if (voice == 0) {
                            changeToSpeaker();
                        } else {
                            changeToReceiver();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

}