package com.yanlong.im.user.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.ScreenUtils;
import com.yanlong.im.R;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.ImageBean;
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
import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @创建人 shenxin
 * @创建时间 2019/8/23 0023 9:26
 */
public class FeedbackActivity extends AppActivity {
    public static final int SHOW_IMAGE = 9038;
    private HeadView headView;
    private EditText edContent;
    private Button btnCommit;
    private RecyclerView recyclerView;
    private String[] strings = {"拍照", "相册", "取消"};
    private PopupSelectView popupSelectView;
    private CheckPermission2Util permission2Util = new CheckPermission2Util();
    private List<ImageBean> list = new ArrayList<>();
    private FeedbackAdatper adatper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        initView();
        initEvent();
        initData();
    }

    private void initView() {
        headView = findViewById(R.id.headView);
        edContent = findViewById(R.id.ed_content);
        btnCommit = findViewById(R.id.btn_commit);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3,
                ScreenUtils.dip2px(this, 10), false));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adatper = new FeedbackAdatper();
        recyclerView.setAdapter(adatper);
    }

    private void initEvent() {
        headView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });
        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commit();
            }
        });
    }


    private void initData() {
        ImageBean imageBean = new ImageBean();
        imageBean.setType(0);
        list.add(imageBean);
    }


    private void commit() {
        String content = edContent.getText().toString();
        if (TextUtils.isEmpty(content)) {
            ToastUtil.show(FeedbackActivity.this, "请填写问题描叙");
            return;
        }
        if (content.length() < 6) {
            ToastUtil.show(FeedbackActivity.this, "描叙内容不能少于6个字");
            return;
        }
        String imageUrl = "";
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getType() == 1) {
                    imageUrl += list.get(i).getUrl() + ",";
                }
            }
        }

        new UserAction().userOpinion(content, imageUrl, new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                super.onResponse(call, response);
                if (response.body() == null) {
                    return;
                }
                if (response.body().isOk()) {
                    ToastUtil.show(context, "提交成功");
                    finish();
                }
            }
        });

    }


    private void initPopup() {
        popupSelectView = new PopupSelectView(this, strings);
        popupSelectView.showAtLocation(headView, Gravity.BOTTOM, 0, 0);
        popupSelectView.setListener(new PopupSelectView.OnClickItemListener() {
            @Override
            public void onItem(String string, int postsion) {
                switch (postsion) {
                    case 0:
                        permission2Util.requestPermissions(FeedbackActivity.this, new CheckPermission2Util.Event() {
                            @Override
                            public void onSuccess() {
                                PictureSelector.create(FeedbackActivity.this)
                                        .openCamera(PictureMimeType.ofImage())
                                        .compress(true)
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


                        PictureSelector.create(FeedbackActivity.this)
                                .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()
                                .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                                .maxSelectNum(6 - adatper.getNum())
                                .previewImage(false)// 是否可预览图片 true or false
                                .isCamera(false)// 是否显示拍照按钮 ture or false
                                .compress(true)// 是否压缩 true or false
                                .freeStyleCropEnabled(false)
                                .rotateEnabled(false)
                                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

                        break;
                }
                popupSelectView.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    if (null != data) {
                        List<LocalMedia> list = PictureSelector.obtainMultipleResult(data);
                        if (null != list && list.size() > 0) {
                            for (int i = 0; i < list.size(); i++) {
                                final String file = PictureSelector.obtainMultipleResult(data).get(i).getCompressPath();
                                final Uri uri = Uri.fromFile(new File(file));
                                if (!alert.isShown()) {
                                    alert.show();
                                }

                                new UpFileAction().upFile(UpFileAction.PATH.FEEDBACK, getContext(), new UpFileUtil.OssUpCallback() {
                                    @Override
                                    public void success(final String url) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                alert.dismiss();
                                                ImageBean imageBean = new ImageBean();
                                                imageBean.setType(1);
                                                imageBean.setUrl(url);
                                                imageBean.setPath(uri);
                                                adatper.addImage(imageBean);
                                            }
                                        });

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
                        }
                    }

                    break;
                case SHOW_IMAGE:
                    int postion = data.getIntExtra(FeedbackShowImageActivity.POSTION, 0);
                    adatper.remove(postion);
                    break;
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permission2Util.onRequestPermissionsResult();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private class FeedbackAdatper extends RecyclerView.Adapter<FeedbackAdatper.FeedbackViewHolder> {

        public void addImage(ImageBean imageBean) {
            if (list.size() == 6) {
                list.remove(5);
                list.add(list.size(), imageBean);
            } else {
                list.add(list.size() - 1, imageBean);
            }
            recyclerView.getAdapter().notifyDataSetChanged();
        }

        public void remove(int postion) {
            if (list.size() == 6) {
                if (list.get(5).getType() == 0) {
                    list.remove(postion);
                } else {
                    list.remove(postion);
                    ImageBean imageBean = new ImageBean();
                    imageBean.setType(0);
                    list.add(imageBean);
                }
            } else {
                list.remove(postion);
            }
            this.notifyDataSetChanged();
        }


        public int getNum() {
            if (list == null && list.size() >= 0) {
                return 0;
            } else {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getType() == 0) {
                        return list.size() - 1;
                    }
                }
                return list.size();
            }
        }


        @Override
        public FeedbackViewHolder onCreateViewHolder(@android.support.annotation.NonNull ViewGroup viewGroup, int i) {
            View view = inflater.inflate(R.layout.item_feedback, viewGroup, false);
            return new FeedbackViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@android.support.annotation.NonNull FeedbackViewHolder viewHolder, final int i) {

            ImageBean imageBean = list.get(i);
            if (imageBean.getType() == 0) {
                //         viewHolder.imageView.setImageURI("android.resource://" + getPackageName() + "/" + R.mipmap.icon_image_add);
                viewHolder.imageView.setImageResource(R.mipmap.icon_image_add);

                viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showInput(false);
                        initPopup();
                    }
                });
            } else {
                //       viewHolder.imageView.setImageURI(imageBean.getPath());
                Glide.with(context).load(imageBean.getPath())
                        .apply(GlideOptionsUtil.defImageOptions1()).into(viewHolder.imageView);

                viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FeedbackActivity.this, FeedbackShowImageActivity.class);
                        intent.putExtra(FeedbackShowImageActivity.URL, list.get(i).getUrl());
                        intent.putExtra(FeedbackShowImageActivity.POSTION, i);
                        startActivityForResult(intent, SHOW_IMAGE);
                    }
                });


            }

        }

        @Override
        public int getItemCount() {
            if (list != null && list.size() > 0) {
                return list.size();
            }
            return 0;
        }


        class FeedbackViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;

            public FeedbackViewHolder(@android.support.annotation.NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);
            }
        }
    }

    /**
     * 是否关闭键盘
     *
     * @param show
     */
    public void showInput(boolean show) {
        try {
            if (show) {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInputFromInputMethod(this.getCurrentFocus().getApplicationWindowToken(), 0);
            } else {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getApplicationWindowToken(), 0);
            }
        } catch (NullPointerException e1) {

        } catch (Exception e) {
        }
    }

}
