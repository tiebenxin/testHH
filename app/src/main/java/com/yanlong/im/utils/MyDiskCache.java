package com.yanlong.im.utils;

import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator;
import com.bumptech.glide.signature.EmptySignature;
import com.luck.picture.lib.glide.OriginalKey;

import java.io.File;



public class MyDiskCache {

    public static String TYPE_IMA="image";
    public static String TYPE_VOICE="voice";
    public static String TYPE_VIDEO="video";
    public static String TYPE_DEFALUT="defalut";
    public File getFile(String path){
        try{
//            DiskLruCache diskLruCache=DiskLruCache.open(new File(""),0,0,0);
//            diskLruCache.get()

        }catch (Exception e){

        }

        return new File(path);
    }


    public static String getFileType(String url){
        String type=null;
        if (url.endsWith("mp4")){
            type=TYPE_VIDEO;
        }else if(url.endsWith("png")||url.endsWith("jpg")||url.endsWith("gif")||url.endsWith("jpeg")){
            type=TYPE_IMA;
        }else if(url.endsWith("caf")){
            type=TYPE_VOICE;
        }else {
            type=TYPE_DEFALUT;
        }
//        DiskLruCache diskLruCache = DiskLruCache.open(new File(cachePath, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
//        DiskLruCache.Value value = diskLruCache.get(safeKey);

        return type;
    }

    public static String getFileNmae(String url){
        OriginalKey originalKey = new OriginalKey(url, EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey =  safeKeyGenerator.getSafeKey(originalKey);


        return safeKey;
    }

    public static long getFileVailable(String type){
            if (type.equals(TYPE_IMA)){
                return MyDiskCacheController.MAX_IMAGE_SIZE;
            }else if(type.equals(TYPE_VIDEO)){
                return MyDiskCacheController.MAX_VIDEO_SIZE;
            }else if(type.equals(TYPE_VOICE)){
                return MyDiskCacheController.MAX_VOICE_SIZE;
            }else{
                return MyDiskCacheController.USABLE_DEFALUT_SIZE;
            }
//            return 0;
    }

}
