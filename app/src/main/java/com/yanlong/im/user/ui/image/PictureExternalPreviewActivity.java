package com.yanlong.im.user.ui.image;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.luck.picture.lib.utils.PicSaveUtils;
import com.luck.picture.lib.PictureBaseActivity;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.RxPermissions;
import com.luck.picture.lib.photoview.OnViewTapListener;
import com.luck.picture.lib.photoview.PhotoView;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.luck.picture.lib.tools.ToastManage;
import com.luck.picture.lib.view.PopupSelectView;
import com.luck.picture.lib.view.bigImg.BlockImageLoader;
import com.luck.picture.lib.view.bigImg.LargeImageView;
import com.luck.picture.lib.view.bigImg.factory.FileBitmapDecoderFactory;
import com.luck.picture.lib.widget.PreviewViewPager;
import com.luck.picture.lib.widget.longimage.ImageSource;
import com.luck.picture.lib.widget.longimage.ImageViewState;
import com.luck.picture.lib.widget.longimage.SubsamplingScaleImageView;
import com.luck.picture.lib.zxing.decoding.RGBLuminanceSource;
import com.yalantis.ucrop.util.FileUtils;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.ui.forward.MsgForwardActivity;
import com.yanlong.im.utils.MyDiskCacheUtils;
import com.yanlong.im.utils.QRCodeManage;

import net.cb.cb.library.event.EventFactory;
import net.cb.cb.library.utils.DeviceUtils;
import net.cb.cb.library.utils.DownloadUtil;
import net.cb.cb.library.utils.ImgSizeUtil;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.AlertYesNo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;

/**
 * author：luck
 * project：PictureSelector
 * package：com.luck.picture.ui
 * email：邮箱->893855882@qq.com
 * data：17/01/18
 */
public class PictureExternalPreviewActivity extends PictureBaseActivity implements View.OnClickListener {
    private static String TAG = "PictureExternalPreviewActivity";
    private ImageButton left_back;
    private TextView tv_title;
    private PreviewViewPager viewPager;
    private List<LocalMedia> images = new ArrayList<>();
    private int position = 0;
    private String directory_path;
    private SimpleFragmentAdapter adapter;
    private LayoutInflater inflater;
    private RxPermissions rxPermissions;
    private LoadDataThread loadDataThread;
    //    private String[] strings = {"识别二维码", "保存图片", "取消"};
    private String[] strings = {"发送给朋友", "保存图片", "识别二维码", "取消"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(com.luck.picture.lib.R.layout.picture_activity_external_preview);
        EventBus.getDefault().register(this);
        inflater = LayoutInflater.from(this);
        tv_title = (TextView) findViewById(com.luck.picture.lib.R.id.picture_title);
        left_back = (ImageButton) findViewById(com.luck.picture.lib.R.id.left_back);
        viewPager = (PreviewViewPager) findViewById(com.luck.picture.lib.R.id.preview_pager);
        position = getIntent().getIntExtra(PictureConfig.EXTRA_POSITION, 0);
//        if (DeviceUtils.isViVoAndOppo()) {
//            directory_path = "/Pictures/";
//        } else {
//            directory_path = "/DCIM/Camera/";
//        }
        directory_path = "/DCIM/Camera/";

        images = (List<LocalMedia>) getIntent().getSerializableExtra(PictureConfig.EXTRA_PREVIEW_SELECT_LIST);
        left_back.setOnClickListener(this);
        initAndPermissions();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //检测是否有权限
    }

    //权限申请和初始化
    private void initAndPermissions() {

        if (rxPermissions == null) {
            rxPermissions = new RxPermissions(PictureExternalPreviewActivity.this);
        }
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            initViewPageAdapterData();
                        } else {
                            ToastManage.s(mContext, getString(com.luck.picture.lib.R.string.picture_jurisdiction));
                            finish();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    String indexPath;

    public void showBigImage(final PhotoView imageView, final TextView tvLookOrigin, final View btnDown, final LargeImageView imgLarge, final String path) {
        tvLookOrigin.setEnabled(false);
        btnDown.setEnabled(false);
        imgLarge.setTag(path);
        boolean isHttp = PictureMimeType.isHttp(path);
        if (isHttp) {
            //TODO 文件下载重新构建
            final String filePath = getExternalCacheDir().getAbsolutePath() + "/Image/";
            final String fileName = path.substring(path.lastIndexOf("/") + 1);
            File fileSave = new File(filePath + "/" + fileName);

            if (fileSave.exists()) {
                long fsize = (long) tvLookOrigin.getTag();
                long fsize2 = fileSave.length();
                boolean broken = fsize2 < fsize;
                if (broken) {//缓存清理
                    fileSave.delete();
                    new File(fileSave.getAbsolutePath() + FileBitmapDecoderFactory.cache_name).delete();
                }
            }

            if (!fileSave.exists()) {//文件是否被缓存
                File fPath = new File(filePath);
                if (!fPath.exists()) {
                    fPath.mkdir();
                }

                //TODO 下载要做取消 9.5
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        final Call download = DownloadUtil.get().download(path, filePath, fileName, new DownloadUtil.OnDownloadListener() {

                            @Override
                            public void onDownloadSuccess(final File file) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        imgLarge.setAlpha(0);
                                        imgLarge.setVisibility(View.VISIBLE);
                                        setDownloadProgress(tvLookOrigin, 100);
                                        btnDown.setEnabled(true);
                                        LogUtil.getLog().d("showBigImage", "showBigImage: " + path);
                                        imgLarge.setImage(new FileBitmapDecoderFactory(file.getAbsolutePath()));
                                        //暂时先不处理放大后的位置
                                    /*  float scale=  imageView.getScale();
                                      int x= imageView.getIndex_x();
                                        int y=imageView.getIndex_y();
                                        imgLarge.smoothScale(scale,x,y);
*/
                                        MyDiskCacheUtils.getInstance().putFileNmae(filePath, fileSave.getAbsolutePath());
                                        //这边要改成已读
                                        msgDao.ImgReadStatSet(path, true);
                                    }
                                });
                            }

                            @Override
                            public void onDownloading(final int progress) {
                                LogUtil.getLog().d(TAG, "onDownloading: " + progress);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setDownloadProgress(tvLookOrigin, progress);
                                    }
                                });

                            }

                            @Override
                            public void onDownloadFailed(Exception e) {
                                new File(filePath + "/" + fileName).delete();
                                new File(filePath + "/" + fileName + FileBitmapDecoderFactory.cache_name).delete();
                                e.printStackTrace();
                            }
                        });
                        imgLarge.setOnDetached(new LargeImageView.Event() {
                            @Override
                            public void onDetach() {
                                download.cancel();
                            }
                        });


                    }
                }).start();


            } else {
                imgLarge.setAlpha(0);
                imgLarge.setVisibility(View.VISIBLE);
                //从缓存中加载
                setDownloadProgress(tvLookOrigin, 100);
                btnDown.setEnabled(true);
                imgLarge.setImage(new FileBitmapDecoderFactory(filePath + "/" + fileName));
                //这边要改成已读
                msgDao.ImgReadStatSet(path, true);
            }

        } else {
            // 有可能本地图片
            try {
                imgLarge.setAlpha(0);
                imgLarge.setVisibility(View.VISIBLE);
                setDownloadProgress(tvLookOrigin, 100);
                btnDown.setEnabled(true);
                LogUtil.getLog().d("showBigImage", "showBigImage: " + path);
                imgLarge.setImage(new FileBitmapDecoderFactory(path));
//                loadImage(imgLarge, path);
                //这边要改成已读
                msgDao.ImgReadStatSet(path, true);

            } catch (Exception e) {


                e.printStackTrace();
            } finally {
                dismissDialog();
            }
        }
    }

    private void loadImage(LargeImageView iv, String url) {
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(PictureExternalPreviewActivity.this)
                .asBitmap()
                .load(url)
                .apply(options)  //480     800
                .into(new SimpleTarget<Bitmap>(800, 800) {
                    /* .into(new SimpleTarget<Bitmap>(ScreenUtils.getScreenWidth(PictureExternalPreviewActivity.this),
                             ScreenUtils.getScreenHeight(PictureExternalPreviewActivity.this)) {*/
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        dismissDialog();
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        dismissDialog();
                        iv.setImage(resource);
                    }
                });
    }
    //当前图片路径

    private MsgDao msgDao = new MsgDao();

    private void initViewPageAdapterData() {
        tv_title.setText(position + 1 + "/" + images.size());
//        adapter = new SimpleFragmentAdapter();
//        viewPager.setAdapter(adapter);
        AdapterPreviewImage mAdapter = new AdapterPreviewImage(this);
        mAdapter.setPopParentView(tv_title);
        mAdapter.bindData(images);
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(position);
        indexPath = images.get(position).getPath();


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tv_title.setText(position + 1 + "/" + images.size());
                indexPath = images.get(position).getPath();

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        //点击返回
        viewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventRefreshChat(EventFactory.ClosePictureEvent event) {
        if (images != null && event != null) {
            for (LocalMedia localMedia : images) {
                if (event.msg_id.equals(localMedia.getMsg_id())) {
                    showDialog(event.name);
                    break;
                }
            }
        }
    }

    private void showDialog(String name) {
        AlertYesNo alertYesNo = new AlertYesNo();
        alertYesNo.init(PictureExternalPreviewActivity.this, null, "\"" + name + "\"" + "撤回了一条消息",
                "确定", null, new AlertYesNo.Event() {
                    @Override
                    public void onON() {

                    }

                    @Override
                    public void onYes() {
                        finish();
                    }
                });
        alertYesNo.show();
    }

    @Override
    public void onClick(View v) {
        finish();
        overridePendingTransition(0, com.luck.picture.lib.R.anim.a3);
    }

    public class SimpleFragmentAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView((View) object);
        }

        @Override
        public void finishUpdate(@NonNull ViewGroup container) {
            try {
                super.finishUpdate(container);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View contentView = inflater.inflate(com.luck.picture.lib.R.layout.picture_image_preview, container, false);
            // 常规图控件
            final PhotoView imageView = contentView.findViewById(com.luck.picture.lib.R.id.preview_image);

            // 长图控件
            final SubsamplingScaleImageView longImg = contentView.findViewById(com.luck.picture.lib.R.id.longImg);
            final LargeImageView imgLarge = contentView.findViewById(com.luck.picture.lib.R.id.img_large);
            final TextView tvLookOrigin = contentView.findViewById(com.luck.picture.lib.R.id.txt_big);//查看原图
            final ImageView ivDownload = contentView.findViewById(com.luck.picture.lib.R.id.iv_download);


            //1.先显示中图

            LocalMedia media = images.get(position);


            final String path = media.getCompressPath();
            boolean isGif = FileUtils.isGif(path);
            boolean isHttp = PictureMimeType.isHttp(path);


            // 可以长按保存并且是网络图片显示一个对话框
           /* if (isHttp) {

                showPleaseDialog(path);
            }*/


            final boolean eqLongImg = PictureMimeType.isLongImg(media);

            imageView.setVisibility(eqLongImg && !isGif ? View.GONE : View.VISIBLE);
            longImg.setVisibility(eqLongImg && !isGif ? View.VISIBLE : View.GONE);
            // 压缩过的gif就不是gif了
            if (isGif && !media.isCompressed()) {
                if (!media.getCutPath().equals(media.getCompressPath())) {

                    Glide.with(getApplicationContext()).load(media.getCutPath()).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            showGif(imageView, tvLookOrigin, path);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            showGif(imageView, tvLookOrigin, path);
                            return false;
                        }
                    }).into(imageView);
                } else {
                    showGif(imageView, tvLookOrigin, path);
                }

            } else {
                if (!media.getCutPath().equals(media.getCompressPath())) {
                    Glide.with(PictureExternalPreviewActivity.this)
                            .asBitmap()
                            .load(media.getCutPath())
                            .into(new SimpleTarget<Bitmap>(800, 800) {
                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                    super.onLoadFailed(errorDrawable);
                                }

                                @Override
                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                    imageView.setImageBitmap(resource);
                                    showImg(imageView, longImg, path, eqLongImg);
                                }
                            });
                } else {
                    showImg(imageView, longImg, path, eqLongImg);
                }
//                showImage(imageView, longImg, imgLarge, tvLookOrigin, ivDownload, media);
            }
            imgEvent(imageView, longImg, path, media);
            //2.是否是原图
            final String imgpath = media.getPath();
            LogUtil.getLog().d("atg", "----:imgpath " + imgpath);
            imgLarge.setTag(imgpath);
            boolean isOriginal = false;//原图
            isOriginal = StringUtil.isNotNull(imgpath);
            if (isOriginal && (!isGif)) {//是原图,但是不是gif
                //设置文件大小后面用来判断原图是否破损
                tvLookOrigin.setTag(media.getSize());
                //3.是否已读原图
                boolean readStat = msgDao.ImgReadStatGet(imgpath);
                if (readStat) {//原图已读,就显示
                    tvLookOrigin.setVisibility(View.GONE);
                    tvLookOrigin.callOnClick();
                    imgDownloadEvent(ivDownload, null, imgpath, imageView, isOriginal, readStat);
                    imgLargeEvent(imageView, tvLookOrigin, ivDownload, imgLarge, imgpath, media);
                } else {
                    imgDownloadEvent(ivDownload, tvLookOrigin, imgpath, imageView, isOriginal, readStat);
                    tvLookOrigin.setVisibility(View.VISIBLE);
                    tvLookOrigin.setText("查看原图(" + ImgSizeUtil.formatFileSize(images.get(position).getSize()) + ")");
                    initLookOriginEvent(imageView, imgLarge, tvLookOrigin, ivDownload, imgpath);
                }
            } else {
                tvLookOrigin.setVisibility(View.GONE);
                ivDownload.setVisibility(View.VISIBLE);
                imgDownloadEvent(ivDownload, null, path, imageView, isOriginal, false);
            }


            (container).addView(contentView, 0);

            //9.6 预览图加载不出来,不能退出
            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            return contentView;
        }

        /**
         * @param imageView    普通图片显示控件
         * @param longImg      长图显示控件
         * @param imgLarge     原图显示控件
         * @param tvLookOrigin 查看原图
         * @param ivDownload   下载按钮
         * @param media        数据
         *                     显示非gif图片
         */
        private void showImage(PhotoView imageView, SubsamplingScaleImageView longImg, LargeImageView imgLarge, TextView tvLookOrigin, ImageView ivDownload, LocalMedia media) {
            String imgUrl = media.getCompressPath();//缩略图路径
            String originUrl = media.getPath();//原图路径
            boolean isOriginal = StringUtil.isNotNull(originUrl);//是否有原图
            boolean isLongImage = PictureMimeType.isLongImg(media);
            if (isOriginal) {
                tvLookOrigin.setTag(media.getSize());
                boolean readStat = msgDao.ImgReadStatGet(originUrl);
                if (readStat) {//原图已读,就显示
                    tvLookOrigin.setVisibility(View.GONE);
                    tvLookOrigin.callOnClick();
                    imgDownloadEvent(ivDownload, null, originUrl, imageView, isOriginal, readStat);
                    imgLargeEvent(imageView, tvLookOrigin, ivDownload, imgLarge, originUrl, media);//显示原图
                    showImg(imageView, longImg, imgUrl, isLongImage);//先加载缩略图
                    showBigImage(imageView, tvLookOrigin, ivDownload, imgLarge, originUrl);
                } else {
                    tvLookOrigin.setVisibility(View.VISIBLE);
                    tvLookOrigin.setText("查看原图(" + ImgSizeUtil.formatFileSize(images.get(position).getSize()) + ")");
                    imgDownloadEvent(ivDownload, tvLookOrigin, originUrl, imageView, isOriginal, readStat);
                    imgEvent(imageView, longImg, imgUrl, media);
                    initLookOriginEvent(imageView, imgLarge, tvLookOrigin, ivDownload, originUrl);
                    showImg(imageView, longImg, imgUrl, isLongImage);
                }
            } else {
                tvLookOrigin.setVisibility(View.GONE);
                ivDownload.setVisibility(View.VISIBLE);
                imgEvent(imageView, longImg, imgUrl, media);
                imgDownloadEvent(ivDownload, null, imgUrl, imageView, isOriginal, false);
                showImg(imageView, longImg, imgUrl, isLongImage);
            }
        }

        private void imgDownloadEvent(ImageView ivDownload, final View tvLookOrigin, final String imgPath, final ImageView imageView, boolean isOrigin, boolean hasRead) {
            ivDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //这里保存处理
                    if (tvLookOrigin != null) {
//                        saveImage(imgPath);
//                        txtBig.callOnClick();
                        downloadAndSaveImage(imgPath, (TextView) tvLookOrigin, ivDownload);
                    } else {
                        if (isOrigin && hasRead) {
                            String fileName = getFileExt(imgPath);
                            String path = PictureFileUtils.createDir(PictureExternalPreviewActivity.this,
                                    fileName, null);
                            LogUtil.getLog().d(TAG, "showLoadingImage path: " + path);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        PicSaveUtils.saveFileLocl(new URL(imgPath), path);
//                                        if (DeviceUtils.isViVoAndOppo()) {
//                                            String dirPath = PictureFileUtils.createDir(mContext,
//                                                    fileName, "/Pictures");
//                                    PictureFileUtils.copyFile(file.getAbsolutePath(), dirPath);
//                                            LogUtil.getLog().d("a=", "DeviceUtils" + "--保存图片到相册--" + dirPath);
//                                            PicSaveUtils.sendBroadcast(new File(dirPath), mContext);
//                                        } else {
//                                            PicSaveUtils.sendBroadcast(new File(path), mContext);
//                                        }
                                        PicSaveUtils.sendBroadcast(new File(path), mContext);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        ToastUtil.show(PictureExternalPreviewActivity.this, "保存失败");
                                    }
                                }
                            }).start();


                        } else {
                            saveImageImg(imgPath, imageView, ivDownload);
                        }
                    }
                }
            });


        }

        //图片事件
        private void imgEvent(PhotoView imageView, SubsamplingScaleImageView longImg, String path, LocalMedia media) {
            imageView.setOnViewTapListener(new OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    finish();
                    overridePendingTransition(0, com.luck.picture.lib.R.anim.a3);
                }
            });
            longImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    overridePendingTransition(0, com.luck.picture.lib.R.anim.a3);
                }
            });
            imageView.setOnLongClickListener(onLongClick(path, media));

        }

        /*
         * 初始化查看原图按钮点击事件
         * */
        private void initLookOriginEvent(PhotoView photoView, LargeImageView imgLarge, TextView tvLookOrigin, ImageView ivDownload, String path) {
            if (tvLookOrigin != null) {
                tvLookOrigin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setDownloadProgress(tvLookOrigin, 0);
                        showBigImage(photoView, tvLookOrigin, ivDownload, imgLarge, path);
                    }
                });
            }
        }


        //大图事件
        private void imgLargeEvent(final PhotoView imageView, final TextView tvLookOrigin, final View btnDown, final LargeImageView imgLarge, final String imgpath, LocalMedia media) {
            tvLookOrigin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDownloadProgress(tvLookOrigin, 0);
                    showBigImage(imageView, tvLookOrigin, btnDown, imgLarge, imgpath);

                }
            });
            //查看大图------------------------
            imgLarge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // imgLarge.setVisibility(View.GONE);
                    onBackPressed();

                }
            });
            imgLarge.setOnLoadStateChangeListener(new BlockImageLoader.OnLoadStateChangeListener() {
                @Override
                public void onLoadStart(int loadType, Object param) {

                }

                @Override
                public void onLoadFinished(int loadType, Object param, boolean success, Throwable throwable) {


                }
            });
            imgLarge.setOnImageLoadListener(new BlockImageLoader.OnImageLoadListener() {
                @Override
                public void onBlockImageLoadFinished() {

                    imgLarge.setAlpha(1);
                    dismissDialog();
                    setDownloadProgress(tvLookOrigin, 100);
                    // ToastUtil.show(getApplicationContext(),"加载完成");
                }

                @Override
                public void onLoadImageSize(int imageWidth, int imageHeight) {

                }

                @Override
                public void onLoadFail(Exception e) {
                    ToastUtil.show(getApplicationContext(), "加载失败,请重试");
                    dismissDialog();
                }
            });
            imgLarge.setOnLongClickListener(onLongClick(imgpath, media));
        }

        //显示普通图片
        private void showImg(final PhotoView imageView, final SubsamplingScaleImageView longImg, final String path, final boolean eqLongImg) {
            RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            LogUtil.getLog().e("Glide", "显示普通图" + path + "eqLongImg" + eqLongImg);
            Glide.with(PictureExternalPreviewActivity.this)
                    .asBitmap()
                    .load(path)
                    .apply(options)  //480     800
                    .into(new SimpleTarget<Bitmap>(800, 800) {
                        /* .into(new SimpleTarget<Bitmap>(ScreenUtils.getScreenWidth(PictureExternalPreviewActivity.this),
                                 ScreenUtils.getScreenHeight(PictureExternalPreviewActivity.this)) {*/
                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            dismissDialog();
                        }

                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                            dismissDialog();
                            if (eqLongImg) {
                                displayLongPic(resource, longImg);
                            } else {
                                imageView.setImageBitmap(resource);
                            }
                        }
                    });
        }

        //显示gif图片
        private void showGif(final PhotoView imageView, TextView txtBig, String path) {
            LogUtil.getLog().e("Glide", "显示gif图");
            txtBig.setVisibility(View.GONE);
            RequestOptions gifOptions = new RequestOptions()
                    .priority(Priority.LOW)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(PictureExternalPreviewActivity.this)
                    .asGif()
                    .apply(gifOptions)
                    .load(path)
                    .listener(new RequestListener<GifDrawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, final Object model
                                , Target<GifDrawable> target, boolean isFirstResource) {
                            dismissDialog();
                            imageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(getApplicationContext()).asBitmap().load(model).into(imageView);
                                }
                            });

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, Object model
                                , Target<GifDrawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            dismissDialog();
                            return false;
                        }
                    })
                    .into(imageView);
        }


    }

    private View.OnLongClickListener onLongClick(final String path, LocalMedia media) {

        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (rxPermissions == null) {
                    rxPermissions = new RxPermissions(PictureExternalPreviewActivity.this);
                }
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Observer<Boolean>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onNext(Boolean aBoolean) {
                                if (aBoolean) {
                                    showDownLoadDialog(path, media);
                                } else {
                                    ToastManage.s(mContext, getString(com.luck.picture.lib.R.string.picture_jurisdiction));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onComplete() {
                            }
                        });
                return true;
            }
        };
    }

    /**
     * 缩放
     *
     * @param origin
     * @param ratio
     * @return
     */
    private Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        // origin.recycle();
        return newBM;
    }

    /**
     * 加载长图
     *
     * @param bmp
     * @param longImg
     */
    private void displayLongPic(Bitmap bmp, SubsamplingScaleImageView longImg) {
        LogUtil.getLog().i(TAG, "displayLongPic: 显示长图");

        if (bmp.getHeight() > 4000 || bmp.getWidth() > 4000) {
            if (bmp.getHeight() > bmp.getWidth()) {

                float sp = 4000.0f / bmp.getHeight();
                bmp = scaleBitmap(bmp, sp);
            } else {
                float sp = 4000.0f / bmp.getWidth();
                bmp = scaleBitmap(bmp, sp);
            }
        }


        longImg.setQuickScaleEnabled(true);
        longImg.setZoomEnabled(true);
        longImg.setPanEnabled(true);
        longImg.setDoubleTapZoomDuration(100);
        longImg.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
        longImg.setDoubleTapZoomDpi(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
        longImg.setImage(ImageSource.cachedBitmap(bmp), new ImageViewState(0, new PointF(0, 0), 0));
    }

    /**
     * 下载图片提示
     */
    private void showDownLoadDialog(final String path, LocalMedia media) {
        final PopupSelectView popupSelectView = new PopupSelectView(this, strings);
        popupSelectView.setListener(new PopupSelectView.OnClickItemListener() {
            @Override
            public void onItem(String string, int postsion) {
                if (postsion == 0) {//转发
                    String msgId = media.getMsg_id();
                    if (!TextUtils.isEmpty(msgId)) {
                        MsgAllBean msgAllBean = msgDao.getMsgById(msgId);
                        if (msgAllBean != null) {
                            startActivity(new Intent(PictureExternalPreviewActivity.this, MsgForwardActivity.class)
                                    .putExtra(MsgForwardActivity.AGM_JSON, new Gson().toJson(msgAllBean)));
                        }
                    } else {
                        //TODO:无消息id，要不要自己新建一条消息记录，然后发出去？

                    }

                } else if (postsion == 1) {//保存
                    saveImage(path);
                } else if (postsion == 2) {//识别二维码
                    scanningQrImage(path);
                }
                popupSelectView.dismiss();

            }
        });
        popupSelectView.showAtLocation(tv_title, Gravity.BOTTOM, 0, 0);

    }


    private Result scanningQrImage(String path) {
        boolean isHttp = PictureMimeType.isHttp(path);
        if (isHttp) {
            loadDataThread = new LoadDataThread(path, 1, null);
            loadDataThread.start();
        } else {
            LogUtil.getLog().d(TAG, "scanningQrImage: path" + path);
            // 有可能本地图片
            try {
                if (path.toLowerCase().startsWith("file://")) {
                    path = path.replace("file://", "");
                }

                // LogUtil.getLog().d(TAG, "scanningQrImage: dirPath"+dirPath);
                Result result = scanningImage(path);
                QRCodeManage.toZhifubao(this, result);
            } catch (Exception e) {
                ToastUtil.show(mContext, "识别二维码失败");
                dismissDialog();
                e.printStackTrace();
            }
        }


        return null;
    }


    Bitmap scanBitmap;

    /**
     * 扫描二维码图片的方法
     *
     * @param path
     * @return
     */
    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小
//        int sampleSize = (int) (options.outHeight / (float) 200);
//        if (sampleSize <= 0)
//            sampleSize = 1;
        //  options.inSampleSize = sampleSize;
        Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void saveImage(String path) {
        LogUtil.getLog().e("TAG", "------------showLoadingImage$:saveImage " + path);

        boolean isHttp = PictureMimeType.isHttp(path);
        if (isHttp) {
            LogUtil.getLog().e("TAG", "------------showLoadingImage$:saveImage " + "http");
            showPleaseDialog();
            loadDataThread = new LoadDataThread(path, 0, null);
            loadDataThread.start();
        } else {
            if (path.toLowerCase().startsWith("file://")) {
                path = path.replace("file://", "");
            }
            // 有可能本地图片
            try {
                String[] paths = null;
                String spiltPath = null;
                if (path.contains("_below")) {
                    paths = path.split("_below");
                    spiltPath = paths[0];
                    File file = new File(path);
                    file.renameTo(new File(spiltPath));
                    path = spiltPath;
                }
                LogUtil.getLog().e("TAG", "------------showLoadingImage$:saveImage__path__" + path + "--------" + spiltPath);
                String fileName = getFileExt(path);
                String spiltFileName = null;
                if (fileName.contains("_below")) {
                    paths = path.split("_below");
                    spiltFileName = paths[0];
                }
                LogUtil.getLog().e("TAG", "------------showLoadingImage$:saveImage__path__" + fileName);
                //TODO:为什么要copy到相册？相册更新可以是自定义文件路径，执行MediaStore.Images.Media.insertImage会在相册中产生两张图片
                //刷新相册的广播
//                if (DeviceUtils.isViVoAndOppo()) {
//                    String dirPath = PictureFileUtils.createDir(mContext, fileName, "/Pictures");
//                    PictureFileUtils.copyFile(fileName, dirPath);
//                    MediaStore.Images.Media.insertImage(mContext.getContentResolver(), path, fileName, null);
//                    LogUtil.getLog().d("a=", "DeviceUtils" + "--保存图片到相册--" + dirPath);
//                    PicSaveUtils.sendBroadcast(new File(dirPath), getApplicationContext());
//                } else {
//                    PicSaveUtils.sendBroadcast(new File(path), getApplicationContext());
//                    LogUtil.getLog().d("a=", "" + "--保存图片到相册--" + path);
//                }
                PicSaveUtils.sendBroadcast(new File(path), getApplicationContext());
                ToastManage.s(mContext, "保存成功");
                dismissDialog();
            } catch (Exception e) {
                ToastManage.s(mContext, getString(com.luck.picture.lib.R.string.picture_save_error) + "\n" + e.getMessage());
                dismissDialog();
                e.printStackTrace();
            }
        }
        dismissDialog();
    }

    private void saveImageImg(String path, ImageView imageView, ImageView ivDownload) {
        LogUtil.getLog().d("TAG", "------------showLoadingImage$:saveImage " + path);
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        if (null != bitmap) {
            boolean isSuccess = PicSaveUtils.saveImgLoc(this, bitmap, path);
            if (isSuccess) {
                ivDownload.setEnabled(true);
                ivDownload.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.show(PictureExternalPreviewActivity.this, "保存成功");
                    }
                }, 100);

            }
        }
    }

    // 进度条线程
    public class LoadDataThread extends Thread {
        private String path;
        private Object obj;
        private int type;

        public LoadDataThread(String path, int type, Object obj) {
            super();
            LogUtil.getLog().d("TAG", "------------LoadDataThread: " + obj);
            this.path = path;
            this.type = type;
            this.obj = obj;
        }

        @Override
        public void run() {
            try {
                showLoadingImage(path, type, obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 下载图片保存至手机
    public void showLoadingImage(String urlPath, int type, Object obj) {
        try {
            LogUtil.getLog().d(TAG, "showLoadingImage: " + urlPath);
            URL u = new URL(urlPath);
//            //网路图片本地化
            String fileName = PicSaveUtils.getFileExt(urlPath);
            String path = PictureFileUtils.createDir(PictureExternalPreviewActivity.this,
                    fileName, null);
            PicSaveUtils.saveFileLocl(u, path);

            if (type == 0) {
                Message message = handler.obtainMessage();
                message.what = 200;
                message.obj = path;
                handler.sendMessage(message);
            } else if (type == 2) {//显示大图
                Message message = handler.obtainMessage();
                message.what = 400;
                message.obj = obj;
                ((View) obj).setTag(path);

                handler.sendMessage(message);
            } else if (type == 1) {
                Message message = handler.obtainMessage();
                message.what = 300;
                message.obj = path;
                handler.sendMessage(message);
            }


        } catch (IOException e) {
            // ToastManage.s(mContext, getString(com.luck.picture.lib.R.string.picture_save_error) + "\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /***
     * 获取文件后缀
     * @param urlPath
     * @return
     */
    private String getFileExt(String urlPath) {
        String fName = urlPath.trim();
        //http://e7-test.oss-cn-beijing.aliyuncs.com/Android/20190802/fe85b909-0bea-4155-a92a-d78052e8638c.png/below-200k
        int index = fName.lastIndexOf("/");
        if (fName.lastIndexOf(".") > index) {
            return fName.substring(index + 1);
        } else {
            String name = fName.substring(fName.lastIndexOf("/", index - 1) + 1);
            name = name.replace("/", "_");
            return name;
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 200:
                    String path = (String) msg.obj;
                    // ToastManage.s(mContext, getString(com.luck.picture.lib.R.string.picture_save_success) + "\n" + path);
                    //dismissDialog();
                    saveImage(path);
                    break;
                case 300:
                    String qrPath = (String) msg.obj;
                    Result result = scanningImage(qrPath);
                    QRCodeManage.toZhifubao(PictureExternalPreviewActivity.this, result);
                    break;
            }
        }
    };

    /*
     * 更新下载进度
     * */
    public void setDownloadProgress(TextView txtBig, int progress) {
        txtBig.setText("已完成 " + progress + "%");
        if (progress == 100) {
            txtBig.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (loadDataThread != null && handler != null) {
            handler.removeCallbacks(loadDataThread);
            loadDataThread = null;
            handler = null;
        }
//        super.onBackPressed();
        finish();
        overridePendingTransition(0, com.luck.picture.lib.R.anim.a3);
    }

    @Override
    protected void onDestroy() {
        viewPager.setAdapter(null);
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void downloadAndSaveImage(String url, TextView tvLookOrigin, ImageView ivDownLoad) {
        final String filePath = getExternalCacheDir().getAbsolutePath() + "/Image/";
        final String fileName = url.substring(url.lastIndexOf("/") + 1);
        File fileSave = new File(filePath + "/" + fileName);

        if (fileSave.exists()) {
            long fsize = (long) tvLookOrigin.getTag();
            long fsize2 = fileSave.length();
            boolean broken = fsize2 < fsize;

            if (broken) {//缓存清理
                fileSave.delete();
                new File(fileSave.getAbsolutePath() + FileBitmapDecoderFactory.cache_name).delete();
            }

        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    final Call download = DownloadUtil.get().download(url, filePath, fileName, new DownloadUtil.OnDownloadListener() {

                        @Override
                        public void onDownloadSuccess(final File file) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                imgLarge.setAlpha(0);
//                                imgLarge.setVisibility(View.VISIBLE);
                                    setDownloadProgress(tvLookOrigin, 100);
                                    ivDownLoad.setEnabled(true);
                                    LogUtil.getLog().d("showBigImage", "showBigImage: " + url);
//                                imgLarge.setImage(new FileBitmapDecoderFactory(file.getAbsolutePath()));
                                    MyDiskCacheUtils.getInstance().putFileNmae(filePath, fileSave.getAbsolutePath());
                                    //这边要改成已读
                                    msgDao.ImgReadStatSet(url, true);
                                }
                            });
                            saveImage(url);
                        }

                        @Override
                        public void onDownloading(final int progress) {
                            LogUtil.getLog().d(TAG, "onDownloading: " + progress);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setDownloadProgress(tvLookOrigin, progress);
                                }
                            });

                        }

                        @Override
                        public void onDownloadFailed(Exception e) {
                            new File(filePath + "/" + fileName).delete();
                            new File(filePath + "/" + fileName + FileBitmapDecoderFactory.cache_name).delete();
                            e.printStackTrace();
                        }
                    });
//                imgLarge.setOnDetached(new LargeImageView.Event() {
//                    @Override
//                    public void onDetach() {
//                        download.cancel();
//                    }
//                });


                }
            }).start();
        }

    }

}
