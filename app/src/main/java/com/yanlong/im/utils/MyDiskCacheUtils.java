package com.yanlong.im.utils;

import android.content.Context;

import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator;
import com.bumptech.glide.signature.EmptySignature;
import com.luck.picture.lib.glide.OriginalKey;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyDiskCacheUtils {


    private static MyDiskCacheUtils myDiskCacheUtils;
    public static long MAX_SIZE=2*1024*1024;

    private MyDiskCacheUtils(){}

    //实例化对象
    public static MyDiskCacheUtils getInstance(){

        if (null==myDiskCacheUtils){
            synchronized (MyDiskCacheUtils.class){
                if (null==myDiskCacheUtils){
                    myDiskCacheUtils=new MyDiskCacheUtils();
                }
            }
        }
        return myDiskCacheUtils;
    }

    private MyDiskCacheController diskCacheController=null;
    public MyDiskCacheUtils setDiskController(MyDiskCacheController myDiskCacheController){
        this.diskCacheController=myDiskCacheController;
        return myDiskCacheUtils;
    }
    private Context mContext;
    public MyDiskCacheUtils setContext(Context context){
        this.mContext=context;
        return myDiskCacheUtils;
    }
    public Object getObj(String path){
        return null;
    }


    public  String getFileNmae(String url){
        OriginalKey originalKey = new OriginalKey(url, EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey =  safeKeyGenerator.getSafeKey(originalKey);
        String[] urls= url.split(".");
        String path;
        if (url.endsWith("mp4")){
            path= mContext.getExternalCacheDir().getAbsolutePath()+"/Mp4/"+safeKey+"."+urls[1];
        }else if(url.endsWith("png")||url.endsWith("jpg")||url.endsWith("gif")){
            path= mContext.getExternalCacheDir().getAbsolutePath()+"/Image/"+safeKey+"."+urls[1];
        }else if(url.endsWith("caf")){
            path= mContext.getExternalCacheDir().getAbsolutePath()+safeKey+"."+urls[1];
        }
//        DiskLruCache diskLruCache = DiskLruCache.open(new File(cachePath, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
//        DiskLruCache.Value value = diskLruCache.get(safeKey);

        return safeKey;
    }

    public boolean putFileNmae(String path,String filePath){
        if (null==diskCacheController){
           throw new IllegalStateException("先初始化控制类设置基础属性");
        }
        if (null!=path&&null!=filePath){
            File file =new File(path);
            if (file.isDirectory()){
                long totalSpace= file.length();
                if (totalSpace>MyDiskCache.getFileVailable(MyDiskCache.getFileType(filePath))){
                    clearFile(file);
                    return false;
                }
//           file.getUsableSpace();
//           file.getFreeSpace();
            }

        }
        return true;
    }

    private void clearFile(File path) {
        if (path.isDirectory()){
//            File[] files= path.listFiles();
            List<File> files=getDirAllFile(path);
            for (int i=0;i<files.size();i++){
                files.get(i).delete();
                if (getAvliable(path)){
                    break;
                }
            }
        }else if(path.exists()){
            path.delete();
        }
    }

    private boolean getAvliable(File path) {
        return path.length()<diskCacheController.USABLE_VIDEO_SIZE;
    }

    public static List<File> getDirAllFile(File file) {
        List<File> fileList = new ArrayList<>();
        File[] fileArray = file.listFiles();
        if (fileArray == null)
            return fileList;
        for (File f : fileArray) {
            fileList.add(f);
        }
        fileSortByTime(fileList);
        return fileList;
    }

    public static void fileSortByTime(List<File> fileList) {
        Collections.sort(fileList, new Comparator<File>() {
            public int compare(File p1, File p2) {
                if (p1.lastModified() < p2.lastModified()) {
                    return -1;
                }
                return 1;
            }
        });
    }
}
