package net.cb.cb.library.manager;

import android.os.Environment;

import java.io.File;

/**
 * @author Liszt
 * @date 2019/9/28
 * Description  缓存文件管理类，图片，语音，视频，其他
 */
public class FileManager {
    public static final String CACHE_ROOT = "/changxin";
    public static final String CACHE = "/cache";
    public static final String IMAGE = "/image";
    public static final String VOICE = "/voice";
    public static final String VIDEO = "/video";
    public static final String OTHER = "/other";


    static {
        initCacheFile();
    }

    /*
     * 初始化本地缓存文件夹
     * */
    private static void initCacheFile() {
        File storageFile = Environment.getExternalStorageDirectory();
        File packFile = new File(storageFile.getAbsolutePath() + CACHE_ROOT);
        if (!packFile.exists()) {
            packFile.mkdir();
        }
        File cacheFile = new File(storageFile.getAbsolutePath() + CACHE_ROOT + CACHE);
        if (!cacheFile.exists()) {
            cacheFile.mkdir();
        }

        File imageFile = new File(storageFile.getAbsolutePath() + CACHE_ROOT + CACHE + IMAGE);
        if (!imageFile.exists()) {
            imageFile.mkdir();
        }

        File voiceFile = new File(storageFile.getAbsolutePath() + CACHE_ROOT + CACHE + VOICE);
        if (!voiceFile.exists()) {
            voiceFile.mkdir();
        }

        File videoFile = new File(storageFile.getAbsolutePath() + CACHE_ROOT + CACHE + VIDEO);
        if (!videoFile.exists()) {
            videoFile.mkdir();
        }

        File otherFile = new File(storageFile.getAbsolutePath() + CACHE_ROOT + CACHE + OTHER);
        if (!otherFile.exists()) {
            otherFile.mkdir();
        }
    }

    public static String getImageCachePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + CACHE_ROOT + CACHE + IMAGE;
    }

    public static String getVoiceCachePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + CACHE_ROOT + CACHE + VOICE;
    }

    public static String getVedioCachePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + CACHE_ROOT + CACHE + VIDEO;
    }

}
