package com.yanlong.im.chat.bean;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserSeting extends RealmObject {
    @PrimaryKey
    private Long uid;
    private boolean shake = true;
    private boolean voice = true;
    private int voicePlayer = 0;

    public int getImageBackground() {
        return imageBackground;
    }

    public void setImageBackground(int imageBackground) {
        this.imageBackground = imageBackground;
    }

    private int imageBackground = 0;


    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public boolean isShake() {
        return shake;
    }

    public void setShake(boolean shake) {
        this.shake = shake;
    }

    public boolean isVoice() {
        return voice;
    }

    public void setVoice(boolean voice) {
        this.voice = voice;
    }

    public int getVoicePlayer() {
        return voicePlayer;
    }

    public void setVoicePlayer(int voicePlayer) {
        this.voicePlayer = voicePlayer;
    }
}
