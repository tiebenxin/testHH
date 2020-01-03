package com.yanlong.im.utils;

public class MyDiskCacheController {

    public  static long MAX_IMAGE_SIZE = 1024 * 1024 * 1024;
    public  static long USABLE_IMAGE_SIZE = 1024 * 1024 * 1024;

    public  static long MAX_VIDEO_SIZE = 1024 * 1024 * 1024;
    public  static long USABLE_VIDEO_SIZE = 1024 * 1024 * 1024;

    public  static long MAX_VOICE_SIZE = 1024 * 1024 * 1024;
    public  static long USABLE_VOICE_SIZE = 1024 * 1024 * 1024;

    public  static long USABLE_DEFALUT_SIZE = 1024 * 1024 * 1024;

    public  void setMaxImageSize(long maxImageSize) {
        MAX_IMAGE_SIZE = maxImageSize;
    }

    public  void setUsableImageSize(long usableImageSize) {
        USABLE_IMAGE_SIZE = usableImageSize;
    }

    public  void setMaxVideoSize(long maxVideoSize) {
        MAX_VIDEO_SIZE = maxVideoSize;
    }

    public  void setUsableVideoSize(long usableVideoSize) {
        USABLE_VIDEO_SIZE = usableVideoSize;
    }

    public  void setMaxVoiceSize(long maxVoiceSize) {
        MAX_VOICE_SIZE = maxVoiceSize;
    }

    public  void setUsableVoiceSize(long usableVoiceSize) {
        USABLE_VOICE_SIZE = usableVoiceSize;
    }
}
