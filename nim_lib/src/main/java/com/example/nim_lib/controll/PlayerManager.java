package com.example.nim_lib.controll;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;

import com.example.nim_lib.R;

import java.io.IOException;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-11-07
 * @updateAuthor
 * @updateDate
 * @description
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class PlayerManager {
    /**
     * 外放模式
     */
    public static final int MODE_SPEAKER = 0;
    /**
     * 听筒模式
     */
    public static final int MODE_EARPIECE = 2;
    // 1 音视频铃声  2消息铃声
    public int mType;

    public static final int VOICE_TYPE = 1;

    public static final int MESSAGE_TYPE = 2;

    private static PlayerManager playerManager;

    private MediaPlayer mediaPlayer;

    private AudioManager audioManager;

    private Context context;

    public static PlayerManager getManager() {
        if (playerManager == null) {
            synchronized (PlayerManager.class) {
                playerManager = new PlayerManager();
            }
        }
        return playerManager;
    }

    public void init(Context context, int type) {
        this.context = context;
        this.mType = type;
        initBeepSound();
    }

    private static final float BEEP_VOLUME = 0.10f;

    private void initBeepSound() {
        // The volume on STREAM_SYSTEM is not adjustable, and users found it
        // too loud,
        // so we now play on the music stream.
//        ((Activity) context).setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (mType == PlayerManager.VOICE_TYPE) {
            mediaPlayer.setLooping(true);
        } else {
            mediaPlayer.setLooping(false);
        }
        mediaPlayer.setOnCompletionListener(beepListener);

        AssetFileDescriptor file;
        if (mType == PlayerManager.VOICE_TYPE) {
            file = context.getResources().openRawResourceFd(R.raw.audio_video_hint);
        } else {
            file = context.getResources().openRawResourceFd(R.raw.news_push);
        }
        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
        } catch (Exception e) {
            mediaPlayer = null;
        }
    }

    public void play(int mode) {
        if (mediaPlayer != null) {
            if (MODE_EARPIECE == mode) {
                audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);// 把模式调成听筒放音模式
                } else {
                    audioManager.setMode(AudioManager.MODE_IN_CALL);// 把模式调成听筒放音模式
                }
                audioManager.setSpeakerphoneOn(false);
            } else {
                audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//                audioManager.setMode(AudioManager.MODE_NORMAL);// 铃声:MODE_RINGTONE    普通:MODE_NORMAL
                audioManager.setSpeakerphoneOn(false);// 把模式调成外放模式
            }
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });
        }
    }

    public void setMode(int mode) {
        if (AudioManager.MODE_NORMAL == mode) {

        } else if (AudioManager.MODE_IN_COMMUNICATION == mode) {

        }
    }

    private static int currVolume = 0;

    /**
     * 打开扬声器
     */
    public void openSpeaker() {
        try {
            //判断扬声器是否在打开
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.ROUTE_SPEAKER);
            //获取当前通话音量
            currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            if (!audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(true);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                        AudioManager.STREAM_VOICE_CALL);
            }
        } catch (Exception e) {

        }
    }

    /**
     * 铃声模式
     * RINGER_MODE_NORMAL（普通）
     *
     * RINGER_MODE_SILENT（静音）
     *
     * RINGER_MODE_VIBRATE（震动）
     * @return
     */
    public int getRingerMode(){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getRingerMode();
    }

    /**
     * 是否打开声声器
     *
     * @return
     */
    public boolean isOpenSpeaker() {
        boolean isOpen = false;
        try {
            //判断扬声器是否在打开
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.ROUTE_SPEAKER);

            //获取当前通话音量
            currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            isOpen = audioManager.isSpeakerphoneOn();
        } catch (Exception e) {

        }
        return isOpen;
    }

    /**
     * 关闭扬声器
     */
    public void closeSpeaker() {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                if (audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume, AudioManager.STREAM_VOICE_CALL);
                }
            }
        } catch (Exception e) {
        }
    }

    public void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };
}
