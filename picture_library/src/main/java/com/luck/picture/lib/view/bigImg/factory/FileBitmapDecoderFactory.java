package com.luck.picture.lib.view.bigImg.factory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.os.Environment;

import com.luck.picture.lib.compress.Luban;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileBitmapDecoderFactory implements BitmapDecoderFactory {
    private String path;
   public static String cache_name = "_cache";

    public FileBitmapDecoderFactory(String filePath) {
        super();
        filePath = filePath.toLowerCase().startsWith("file://") ? filePath.replace("file://", "") : filePath;
        //8.12 华为图片的问题处在分辨率过高,解码系统失败,可以从图片分辨率大小来解决,把分辨率超过4000*4000的重新缓存一遍
        try {
            filePath = getimage(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }


        this.path = filePath;
    }



    public static String getimage(String srcPath) throws IOException {

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        float hh = 4000f;// 这里设置高度为800f
        float ww = 4000f;// 这里设置宽度为480f

        int w = newOpts.outWidth;
        int h = newOpts.outHeight;

        if (w < ww && h < hh) {//如果图片还在正常范围内,不压缩
            return srcPath;
        }
        File file = new File(srcPath + cache_name);

        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        Bitmap btTemp = BitmapFactory.decodeFile(file.getAbsolutePath(),options);

        try{
            btTemp.getWidth();
        }catch(Exception e) {
            e.printStackTrace();
            file.delete();
        }*/

        if (file.exists()) {
            return srcPath + cache_name;
        }

        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        double be = 1;// be=1表示不缩放,最小缩放2倍
        if (w >= h && w >= ww) {// 如果宽度大的话根据宽度固定大小缩放
            be =  (newOpts.outWidth / (ww+0.0f));
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be =  (newOpts.outHeight / (hh+0.0f));
        }

        newOpts.inSampleSize = new Double(Math.ceil(be)).intValue();// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);


        FileOutputStream fos = new FileOutputStream(file);
        Bitmap.CompressFormat fmt = Bitmap.CompressFormat.JPEG;
       /* if(srcPath.toLowerCase().endsWith("png")){
            fmt = Bitmap.CompressFormat.PNG;
        }*/
        bitmap.compress(fmt, 75, fos);
        try {
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            fos.close();

        }
        return srcPath + cache_name;




    }




    public FileBitmapDecoderFactory(File file) {
        super();
        this.path = file.getAbsolutePath();
    }

    @Override
    public BitmapRegionDecoder made() throws IOException {
        return BitmapRegionDecoder.newInstance(path, false);
    }

    @Override
    public int[] getImageInfo() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return new int[]{options.outWidth, options.outHeight};
    }
}