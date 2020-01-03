package com.yanlong.im.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.disklrucache.DiskLruCache;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.signature.EmptySignature;
import com.luck.picture.lib.glide.CustomGlideModule;
import com.luck.picture.lib.glide.OriginalKey;
import com.yanlong.im.R;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.bean.Session;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.user.bean.UserInfo;

import net.cb.cb.library.AppConfig;
import net.cb.cb.library.utils.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/***
 * 缓存合成图片
 */
public class GroupHeadImageUtil {


    /***
     * 只能合成fresco已经加载过的图
     * @param url
     * @return
     */
    public static File synthesis(Context context, String... url) {
        List<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i < url.length; i++) {
//            FileBinaryResource resource = (FileBinaryResource) Fresco.getImagePipelineFactory()
//                    .getMainFileCache().getResource(new SimpleCacheKey(url[i]));
            //               File file = resource.getFile();
            if (TextUtils.isEmpty(url[i])) {
                Bitmap bt = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_info_head);
                if (bt != null) {
                    bitmaps.add(bt);
                }
            } else {
                File file = getCacheFile2(context, url[i]);
//                File file = getCacheFileDisk(url[i]);
                FileInputStream inputStream = null;
                if (file != null) {
                    try {
                        inputStream = new FileInputStream(file);
                        if (file.exists()) {
//                    BitmapFactory.Options opt = new BitmapFactory.Options();
//                     //这个isjustdecodebounds很重要
//                    opt.inJustDecodeBounds = true;
//                     opt.inSampleSize=300;
                            Bitmap bt = BitmapFactory.decodeStream(inputStream);
                            if (bt != null) {
                                bitmaps.add(bt);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();

                    } finally {
                        try {
                            if (inputStream != null) {
                                inputStream.close();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return save2File(addBitmap(10, bitmaps.toArray(new Bitmap[]{})));
    }


    public static void creatAndSaveImg(Context mContext, String gid) {
        MsgDao msgDao = new MsgDao();
        Group gginfo = msgDao.getGroup4Id(gid);
        if (gginfo != null) {
            int i = gginfo.getUsers().size();
            i = i > 9 ? 9 : i;
            //头像地址
            String url[] = new String[i];
            for (int j = 0; j < i; j++) {
                MemberUser userInfo = gginfo.getUsers().get(j);
//            if (j == i - 1) {
//                name += userInfo.getName();
//            } else {
//                name += userInfo.getName() + "、";
//            }
                url[j] = userInfo.getHead();
            }
            File file = GroupHeadImageUtil.synthesis(mContext, url);
//        Glide.with(this).load(file)
//                .apply(GlideOptionsUtil.headImageOptions()).into(imgHead);
            msgDao.groupHeadImgCreate(gginfo.getGid(), file.getAbsolutePath());
        }
    }


    public static File getCacheFileDisk(String url) {
        OriginalKey originalKey = new OriginalKey(url, EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey = safeKeyGenerator.getSafeKey(originalKey);
        try {
            File storageDirectory = Environment.getExternalStorageDirectory();
            String cachePath = storageDirectory + "/changxin/cache/image/";
            DiskLruCache diskLruCache = DiskLruCache.open(new File(cachePath, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
            DiskLruCache.Value value = diskLruCache.get(safeKey);
            if (value != null) {
                return value.getFile(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static File getCacheFile2(Context context, String url) {
        DataCacheKey dataCacheKey = new DataCacheKey(new GlideUrl(url), EmptySignature.obtain());
        SafeKeyGenerator safeKeyGenerator = new SafeKeyGenerator();
        String safeKey = safeKeyGenerator.getSafeKey(dataCacheKey);
        try {
            int cacheSize = 100 * 1000 * 1000;
//                DiskLruCache diskLruCache = DiskLruCache.open(new File(context.getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, cacheSize);
//                DiskLruCache.Value value = diskLruCache.get(safeKey);
            DiskLruCache.Value value;
            if (CustomGlideModule.hasPermission(context)) {
//            LogUtil.getLog().d("a=", "Glide缓存位置：/com.yanlong.cll/cache/image");
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                //SD卡已装入
                    File storageDirectory = Environment.getExternalStorageDirectory();
                    String cachePath = storageDirectory + "/changxin/cache/image";
//                DiskLruCache diskLruCache = DiskLruCache.open(new File(context.getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, cacheSize);
                    DiskLruCache diskLruCache = DiskLruCache.open(new File(cachePath), 1, 1, cacheSize);

                    value = diskLruCache.get(safeKey);
                } else {
                    DiskLruCache diskLruCache = DiskLruCache.open(new File(context.getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, cacheSize);
                    value = diskLruCache.get(safeKey);
                }
            } else {
//                //设置内存缓存大小,默认缓存位置
////            LogUtil.getLog().d("a=", "Glide缓存位置：默认应用内");
                DiskLruCache diskLruCache = DiskLruCache.open(new File(context.getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR), 1, 1, cacheSize);
                value = diskLruCache.get(safeKey);
            }

            if (value != null) {
                return value.getFile(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File save2File(Bitmap bt) {
        FileOutputStream out = null;
        File file;
        // 获取SDCard指定目录下
        String dir = AppConfig.APP_CONTEXT.getCacheDir().getAbsolutePath() + "/group/";
        File dirFile = new File(dir);  //目录转化成文件夹
        if (!dirFile.exists()) {                //如果不存在，那就建立这个文件夹
            dirFile.mkdirs();
        }                            //文件夹有啦，就可以保存图片啦
        file = new File(dir, System.currentTimeMillis() + ".jpg");// 在SDcard的目录下创建图片文,以当前时间为其命名

        try {
            out = new FileOutputStream(file);
            bt.compress(Bitmap.CompressFormat.JPEG, 90, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.flush();
                out.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        LogUtil.getLog().i("img", "图片路径>>>>>>" + file.getAbsoluteFile());
        return file;

    }


    private static Bitmap addBitmap(int margin, Bitmap... bitmaps) {
        int th = 600;//总宽,高
        int tw = 600;

        int size = 0;//单图片大小
        int sh = 0;//总高度
        int sw = 0;//总宽度
        // int row = 1;//行
        // int col = 0;//列
        //行图
        ArrayList<Bitmap> row0Pic = new ArrayList<>();
        ArrayList<Bitmap> row1Pic = new ArrayList<>();
        ArrayList<Bitmap> row2Pic = new ArrayList<>();

        Bitmap result = Bitmap.createBitmap(th, tw, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.parseColor("#f6f8fa"));
        Paint paint = new Paint();

        switch (bitmaps.length) {
            case 1:

                size = th;
                sh = th;
                sw = tw;

                bitmaps = zoom(bitmaps, size, size);
                row0Pic.add(bitmaps[0]);
                break;
            case 2:

                size = (th - margin) / 2;
                sh = size;
                sw = size * 2 + margin;

                bitmaps = zoom(bitmaps, size, size);
                row0Pic.add(bitmaps[0]);
                row0Pic.add(bitmaps[1]);
                break;
            case 3:


                size = (th - margin) / 2;
                sh = size * 2 + margin;
                sw = size * 2 + margin;

                bitmaps = zoom(bitmaps, size, size);

                row0Pic.add(bitmaps[0]);
                row1Pic.add(bitmaps[1]);
                row1Pic.add(bitmaps[2]);
                break;
            case 4:


                size = (th - margin) / 2;
                sh = size * 2 + margin;
                sw = size * 2 + margin;

                bitmaps = zoom(bitmaps, size, size);

                row0Pic.add(bitmaps[0]);
                row0Pic.add(bitmaps[1]);
                row1Pic.add(bitmaps[2]);
                row1Pic.add(bitmaps[3]);
                break;

            case 5:


                size = (th - margin * 2) / 3;
                sh = size * 2 + margin;
                sw = size * 3 + margin * 2;

                bitmaps = zoom(bitmaps, size, size);

                row0Pic.add(bitmaps[0]);
                row0Pic.add(bitmaps[1]);
                row1Pic.add(bitmaps[2]);
                row1Pic.add(bitmaps[3]);
                row1Pic.add(bitmaps[4]);
                break;
            case 6:


                size = (th - margin * 2) / 3;
                sh = size * 2 + margin;
                sw = size * 3 + margin * 2;

                bitmaps = zoom(bitmaps, size, size);

                row0Pic.add(bitmaps[0]);
                row0Pic.add(bitmaps[1]);
                row0Pic.add(bitmaps[2]);
                row1Pic.add(bitmaps[3]);
                row1Pic.add(bitmaps[4]);
                row1Pic.add(bitmaps[5]);
                break;

            case 7:


                size = (th - margin * 2) / 3;
                sh = size * 3 + margin * 2;
                sw = size * 3 + margin * 2;

                bitmaps = zoom(bitmaps, size, size);

                row0Pic.add(bitmaps[0]);
                row1Pic.add(bitmaps[1]);
                row1Pic.add(bitmaps[2]);
                row1Pic.add(bitmaps[3]);
                row2Pic.add(bitmaps[4]);
                row2Pic.add(bitmaps[5]);
                row2Pic.add(bitmaps[6]);
                break;
            case 8:


                size = (th - margin * 2) / 3;
                sh = size * 3 + margin * 2;
                sw = size * 3 + margin * 2;

                bitmaps = zoom(bitmaps, size, size);

                row0Pic.add(bitmaps[0]);
                row0Pic.add(bitmaps[1]);
                row1Pic.add(bitmaps[2]);
                row1Pic.add(bitmaps[3]);
                row1Pic.add(bitmaps[4]);
                row2Pic.add(bitmaps[5]);
                row2Pic.add(bitmaps[6]);
                row2Pic.add(bitmaps[7]);
                break;
            case 9:


                size = (th - margin * 2) / 3;
                sh = size * 3 + margin * 2;
                sw = size * 3 + margin * 2;

                bitmaps = zoom(bitmaps, size, size);


                row0Pic.add(bitmaps[0]);
                row0Pic.add(bitmaps[1]);
                row0Pic.add(bitmaps[2]);
                row1Pic.add(bitmaps[3]);
                row1Pic.add(bitmaps[4]);
                row1Pic.add(bitmaps[5]);
                row2Pic.add(bitmaps[6]);
                row2Pic.add(bitmaps[7]);
                row2Pic.add(bitmaps[8]);
                break;
        }


        int startx = (tw - (row0Pic.size() - 1) * margin - size * row0Pic.size()) / 2;//第一行起始位置
        int starty = (th - sh) / 2;


        for (int i = 0; i < row0Pic.size(); i++) {
            canvas.drawBitmap(row0Pic.get(i), startx, starty, paint);
            startx += margin + size;
        }
        startx = 0;
        starty += margin + size;
        for (int i = 0; i < row1Pic.size(); i++) {

            canvas.drawBitmap(row1Pic.get(i), startx, starty, paint);
            startx += margin + size;
        }
        startx = 0;
        starty += margin + size;
        for (int i = 0; i < row2Pic.size(); i++) {
            canvas.drawBitmap(row2Pic.get(i), startx, starty, paint);
            startx += margin + size;
        }


        return result;
    }

    private static Bitmap[] zoom(Bitmap[] imgs, int vWidth, int vHeight) {
        Bitmap[] ret = new Bitmap[imgs.length];

        for (int i = 0; i < imgs.length; i++) {
            ret[i] = Bitmap.createScaledBitmap(imgs[i], vWidth, vHeight, true);
        }

        return ret;
    }

    private static Bitmap addBitmaps(int margin, Bitmap... bitmaps) {

        int row = 1;
        int col = 0;
        int width = 0;
        int height = 0;
        int totalHeight = 0;
        int length = bitmaps.length;
        if (length > 3) {
            row = length % 3 == 0 ? length / 3 : length / 3 + 1;
            col = 3;
        } else {
            row = 1;
            col = length;
        }
        for (int i = 0; i < length; i++) {
            height = Math.max(height, bitmaps[i].getHeight());
        }
        totalHeight = height * row;
        totalHeight += (row - 1) * margin;

        for (int i = 0; i < col; i++) {
            width += bitmaps[i].getWidth();
            width += margin;
        }
        width -= margin;
        Bitmap result = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        for (int i = 0; i < row; i++) {
            int left = 0;
            for (int i1 = 0; i1 < col; i1++) {
                if (i * col + i1 >= length) {
                    break;
                }
                if (i > 0) {
                    if (i1 > 0) {
                        left += bitmaps[i * col + i1 - 1].getWidth();
                        left += margin;
                        int top = (height + margin) * i;
                        canvas.drawBitmap(bitmaps[i * col + i1], left, top, null);
                    } else {
                        left = 0;
                        int top = (height + margin) * i;
                        canvas.drawBitmap(bitmaps[i * col + i1], left, top, null);
                    }
                } else {
                    //第1行
                    if (i1 > 0) {
                        left += bitmaps[i1 - 1].getWidth();
                        left += margin;
                        canvas.drawBitmap(bitmaps[i1], left, 0, null);
                    } else {
                        left = 0;
                        canvas.drawBitmap(bitmaps[i1], left, 0, null);
                    }
                }
            }
        }
        return result;
    }
}
