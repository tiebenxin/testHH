package com.yanlong.im.user.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.utils.PicSaveUtils;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.yanlong.im.R;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.EventMyUserInfo;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.utils.GlideOptionsUtil;
import com.yanlong.im.utils.GroupHeadImageUtil;

import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.CheckPermission2Util;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.UpFileAction;
import net.cb.cb.library.utils.UpFileUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;
import net.cb.cb.library.view.PopupSelectView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Response;


public class ImageHeadActivity extends AppActivity {
    public final static String IMAGE_HEAD = "imageHead";
    private HeadView mHeadView;
    private ImageView mSdImageHead;
    private PopupSelectView popupSelectView;
    private PopupSelectView saveImagePopup;
    private String[] strings = {"拍照", "相册", "取消"};
    private String[] saveImages = {"保存图片", "取消"};
    private String imageHead;
    private CheckPermission2Util permission2Util = new CheckPermission2Util();
    private Button mBtnImageHead;

    private boolean isAdmin = false;
    private boolean isGroup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_head);
        initView();
        initEvent();
    }


    private String urlImg = null;
    private String gGroupid;
    private void initView() {
        imageHead = getIntent().getStringExtra(IMAGE_HEAD);
        gGroupid= getIntent().getStringExtra("gid");
        isAdmin = getIntent().getBooleanExtra("admin", false);
        isGroup = getIntent().getBooleanExtra("groupSigle", false);
        mHeadView = findViewById(R.id.headView);
        if (isGroup) {
            mHeadView.setTitle("群头像");
        }
        mSdImageHead = findViewById(R.id.sd_image_head);

        if (imageHead != null && !imageHead.isEmpty() && StringUtil.isNotNull(imageHead)) {
            urlImg = imageHead;
            Glide.with(this).load(imageHead)
                    .apply(GlideOptionsUtil.headImageOptions()).into(mSdImageHead);
        } else {
            if (isGroup) {
                MsgDao msgDao = new MsgDao();
                String url = msgDao.groupHeadImgGet(gGroupid);
                if(!StringUtil.isNotNull(url)){
                    //头像为空 创建一次
                    GroupHeadImageUtil.creatAndSaveImg(context,gGroupid);
                    url = msgDao.groupHeadImgGet(gGroupid);
                }
                urlImg = url;
                Glide.with(this).load(url)
                        .apply(GlideOptionsUtil.headImageOptions()).into(mSdImageHead);
            }
        }
        LogUtil.getLog().e("=头像==urlImg=="+urlImg);

        mHeadView.getActionbar().getBtnRight().setImageResource(R.mipmap.ic_chat_more);
        mHeadView.getActionbar().getBtnRight().setVisibility(View.VISIBLE);
        mBtnImageHead = findViewById(R.id.btn_image_head);
        if (isGroup) {
            mBtnImageHead.setText("更换群头像");
        }
        if (!isAdmin && isGroup) {
            mBtnImageHead.setVisibility(View.INVISIBLE);
            mBtnImageHead.setClickable(false);
            mHeadView.getActionbar().getBtnRight().setVisibility(View.GONE);
        }
    }


    private void initEvent() {
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                initPopup();
            }
        });
        mBtnImageHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopup();
            }
        });

        mSdImageHead.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                initSaveImage();
                return false;
            }
        });

    }

    private void initSaveImage() {
        saveImagePopup = new PopupSelectView(this, saveImages);
        saveImagePopup.showAtLocation(mSdImageHead, Gravity.BOTTOM, 0, 0);
        saveImagePopup.setListener(new PopupSelectView.OnClickItemListener() {
            @Override
            public void onItem(String string, int postsion) {
                switch (postsion) {
                    case 0:
                        saveImageToGallery(((BitmapDrawable) mSdImageHead.getDrawable()).getBitmap(), urlImg);
                        break;
                }
                saveImagePopup.dismiss();
            }
        });
    }

    /**
     * 保存图片到图库
     *
     * @param bmp
     */
    public void saveImageToGallery(Bitmap bmp, String bitName) {
        // 首先保存图片
        bitName = SystemClock.currentThreadTimeMillis() + "";
        PicSaveUtils.saveImgLoc(this, bmp, bitName);
    }


    private void initPopup() {
        popupSelectView = new PopupSelectView(this, strings);
        popupSelectView.showAtLocation(mSdImageHead, Gravity.BOTTOM, 0, 0);
        popupSelectView.setListener(new PopupSelectView.OnClickItemListener() {
            @Override
            public void onItem(String string, int postsion) {
                switch (postsion) {
                    case 0:
                        permission2Util.requestPermissions(ImageHeadActivity.this, new CheckPermission2Util.Event() {
                            @Override
                            public void onSuccess() {
                                PictureSelector.create(ImageHeadActivity.this)
                                        .openCamera(PictureMimeType.ofImage())
                                        .compress(true)
                                        .enableCrop(true)
                                        .withAspectRatio(1, 1)
                                        .freeStyleCropEnabled(false)
                                        .rotateEnabled(false)
                                        .forResult(PictureConfig.CHOOSE_REQUEST);
                            }

                            @Override
                            public void onFail() {

                            }
                        }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
                        break;
                    case 1:
                        PictureSelector.create(ImageHeadActivity.this)
                                .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()
                                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                                .previewImage(false)// 是否可预览图片 true or false
                                .isCamera(false)// 是否显示拍照按钮 ture or false
                                .compress(true)// 是否压缩 true or false
                                .enableCrop(true)
                                .withAspectRatio(1, 1)
                                .freeStyleCropEnabled(false)
                                .rotateEnabled(false)
                                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

                        break;
                }
                popupSelectView.dismiss();
            }
        });
    }

    private UpFileAction upFileAction = new UpFileAction();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    final String file = PictureSelector.obtainMultipleResult(data).get(0).getCompressPath();
                    // 例如 LocalMedia 里面返回两种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    Uri uri = Uri.fromFile(new File(file));

                    alert.show();
                    //mSdImageHead.setImageURI(uri);
                    Glide.with(this).load(uri)
                            .apply(GlideOptionsUtil.headImageOptions()).into(mSdImageHead);

                    if (isGroup) {
                        upFileAction.upFile(gGroupid,UpFileAction.PATH.HEAD_GROUP_CHANGE, getContext(), new UpFileUtil.OssUpCallback() {
                            @Override
                            public void success(String url) {
                                alert.dismiss();
                                String gid = getIntent().getExtras().getString("gid");
                                taskGroupInfoSet(gid, url, null, null);
                            }

                            @Override
                            public void fail() {
                                alert.dismiss();
                                ToastUtil.show(getContext(), "上传失败!");
                            }

                            @Override
                            public void inProgress(long progress, long zong) {

                            }
                        }, file);
                    } else {
                        upFileAction.upFile(UserAction.getMyId()+"",UpFileAction.PATH.HEAD, getContext(), new UpFileUtil.OssUpCallback() {
                            @Override
                            public void success(String url) {
                                alert.dismiss();
                                taskUserInfoSet(null, url, null, null);
                            }

                            @Override
                            public void fail() {
                                alert.dismiss();
                                ToastUtil.show(getContext(), "上传失败!");
                            }

                            @Override
                            public void inProgress(long progress, long zong) {

                            }
                        }, file);
                    }
                    break;
            }
        }
    }

    private void taskGroupInfoSet(String gid, final String url, Object o1, Object o2) {
        new MsgAction().changeGroupHead(gid, url, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                if (response.body().isOk()) {
//                    MessageManager.getInstance().updateCacheGroupAvatar(gid, url);

                    new MsgDao().updateGroupHead(gid, url);

                    MessageManager.getInstance().setMessageChange(true);
                    MessageManager.getInstance().notifyRefreshMsg(CoreEnum.EChatType.GROUP, -1L, gid, CoreEnum.ESessionRefreshTag.SINGLE, null);
                }
                imageHead = url;
                ToastUtil.show(ImageHeadActivity.this, response.body().getMsg());
            }

            @Override
            public void onFailure(Call<ReturnBean> call, Throwable t) {
//                super.onFailure(call, t);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permission2Util.onRequestPermissionsResult();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void taskUserInfoSet(String imid, final String avatar, String nickname, Integer gender) {
        new UserAction().myInfoSet(imid, avatar, nickname, gender, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                imageHead = avatar;
                if (avatar != null) {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setHead(avatar);
                    EventBus.getDefault().post(new EventMyUserInfo(userInfo, EventMyUserInfo.ALTER_HEAD));
                }
                ToastUtil.show(ImageHeadActivity.this, response.body().getMsg());
            }
        });
    }

}
