package com.yanlong.im.user.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.yanlong.im.R;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.IdCardBean;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.CheckPermission2Util;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.utils.UpFileAction;
import net.cb.cb.library.utils.UpFileUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.HeadView;
import net.cb.cb.library.view.PopupSelectView;

import java.io.File;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Response;

public class UploadIdentityActivity extends AppActivity implements View.OnClickListener {
    private static final int FRONT = 1000;
    private static final int CONTRARY = 2000;

    private HeadView mHeadView;
    private ImageView mIvFront;
    private ImageView mIvContrary;
    private Button mBtnCommit;
    private String[] strings = {"拍照", "相册", "取消"};
    private PopupSelectView popupSelectView;
    private CheckPermission2Util permission2Util = new CheckPermission2Util();
    private String frontUrl;
    private String contraryUrl;
    private TextView mTvAuthStat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_identity);
        initView();
        initEvent();
        initData();
    }

    private void initView() {
        mHeadView = findViewById(R.id.headView);
        mIvFront = findViewById(R.id.iv_front);
        mIvContrary = findViewById(R.id.iv_contrary);
        mBtnCommit = findViewById(R.id.btn_commit);
        mTvAuthStat =  findViewById(R.id.tv_auth_stat);
    }

    private void initEvent() {
        mHeadView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        mIvFront.setOnClickListener(this);
        mIvContrary.setOnClickListener(this);
        mBtnCommit.setOnClickListener(this);
    }

    private void initData(){
        taskIdCardInfo();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_front:
                initPopup(1);
                break;
            case R.id.iv_contrary:
                initPopup(2);
                break;
            case R.id.btn_commit:
                taskUploadCard(frontUrl, contraryUrl);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FRONT:
                    // 图片选择结果回调
                    String file = PictureSelector.obtainMultipleResult(data).get(0).getCompressPath();
                    Uri uri = Uri.fromFile(new File(file));
                    alert.show();
                 //   mIvFront.setImageURI(uri);
                    Glide.with(this).load(uri)
                            .apply(GlideOptionsUtil.notDefImageOptions()).into(mIvFront);


                    new UpFileAction().upFile(UpFileAction.PATH.IMG,getContext(), new UpFileUtil.OssUpCallback() {
                        @Override
                        public void success(String url) {
                            alert.dismiss();
                            frontUrl = url;
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

                    break;
                case CONTRARY:
                    // 图片选择结果回调
                    String file1 = PictureSelector.obtainMultipleResult(data).get(0).getCompressPath();
                    Uri uri1 = Uri.fromFile(new File(file1));
                    alert.show();
                   // mIvContrary.setImageURI(uri1);

                    Glide.with(this).load(uri1)
                            .apply(GlideOptionsUtil.notDefImageOptions()).into(mIvContrary);


                    new UpFileAction().upFile(UpFileAction.PATH.IMG,getContext(), new UpFileUtil.OssUpCallback() {
                        @Override
                        public void success(String url) {
                            alert.dismiss();
                            contraryUrl = url;
                        }

                        @Override
                        public void fail() {
                            alert.dismiss();
                            ToastUtil.show(getContext(), "上传失败!");
                        }

                        @Override
                        public void inProgress(long progress, long zong) {

                        }
                    }, file1);

                    break;
            }
        }
    }

    private void initPopup(final int type) {
        popupSelectView = new PopupSelectView(this, strings);
        popupSelectView.showAtLocation(mIvFront, Gravity.BOTTOM, 0, 0);
        popupSelectView.setListener(new PopupSelectView.OnClickItemListener() {
            @Override
            public void onItem(String string, int postsion) {
                switch (postsion) {
                    case 0:
                        if (type == 1) {
                            permission2Util.requestPermissions(UploadIdentityActivity.this, new CheckPermission2Util.Event() {
                                @Override
                                public void onSuccess() {
                                    PictureSelector.create(UploadIdentityActivity.this)
                                            .openCamera(PictureMimeType.ofImage())
                                            .compress(true)
                                            .forResult(FRONT);
                                }

                                @Override
                                public void onFail() {

                                }
                            }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
                        } else {
                            permission2Util.requestPermissions(UploadIdentityActivity.this, new CheckPermission2Util.Event() {
                                @Override
                                public void onSuccess() {
                                    PictureSelector.create(UploadIdentityActivity.this)
                                            .openCamera(PictureMimeType.ofImage())
                                            .compress(true)
                                            .forResult(CONTRARY);
                                }

                                @Override
                                public void onFail() {

                                }
                            }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
                        }
                        break;
                    case 1:
                        if (type == 1) {
                            PictureSelector.create(UploadIdentityActivity.this)
                                    .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()
                                    .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                                    .previewImage(false)// 是否可预览图片 true or false
                                    .isCamera(false)// 是否显示拍照按钮 ture or false
                                    .compress(true)// 是否压缩 true or false
                                    .forResult(FRONT);//结果回调onActivityResult code
                        } else {
                            PictureSelector.create(UploadIdentityActivity.this)
                                    .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()
                                    .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                                    .previewImage(false)// 是否可预览图片 true or false
                                    .isCamera(false)// 是否显示拍照按钮 ture or false
                                    .compress(true)// 是否压缩 true or false
                                    .forResult(CONTRARY);//结果回调onActivityResult code
                        }
                        break;
                }
                popupSelectView.dismiss();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permission2Util.onRequestPermissionsResult();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void taskUploadCard(String cardBack, String cardFront) {
        if (TextUtils.isEmpty(cardFront)) {
            ToastUtil.show(context, "请上传身份证正面");
            return;
        }
        if (TextUtils.isEmpty(cardFront)) {
            ToastUtil.show(context, "请上传身份证反面");
            return;
        }
        new UserAction().setCardPhoto(cardBack, cardFront, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                ToastUtil.show(context, response.body().getMsg());
                if (response.body().isOk()) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }


    private void taskIdCardInfo() {
        new UserAction().getIdCardInfo(new CallBack<ReturnBean<IdCardBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<IdCardBean>> call, Response<ReturnBean<IdCardBean>> response) {
                if (response.body() == null) {
                    return;
                }
                if (response.body().isOk()) {
                    IdCardBean bean = response.body().getData();
                    if (bean != null) {
                        if(bean.getStat() == 2){
                            mTvAuthStat.setText("身份证照片验证已完成");
                        }else{
                            mTvAuthStat.setText("身份证照片验证未完成");
                        }
                    }
                }
            }
        });
    }


}
