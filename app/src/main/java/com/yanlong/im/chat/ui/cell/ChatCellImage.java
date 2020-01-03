package com.yanlong.im.chat.ui.cell;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.ImageMessage;
import com.yanlong.im.chat.bean.MsgAllBean;

import net.cb.cb.library.utils.DensityUtil;
import net.cb.cb.library.utils.LogUtil;

import static android.view.View.VISIBLE;

/*
 * 图片消息
 * */
public class ChatCellImage extends ChatCellBase {
    //w/h = 3/4
    final int DEFAULT_W = DensityUtil.dip2px(getContext(), 135);
    final int DEFAULT_H = DensityUtil.dip2px(getContext(), 180);
    int width = DEFAULT_W;
    int height = DEFAULT_H;

    private ImageView imageView;
    private ImageMessage imageMessage;
    private ProgressBar progressBar;
    private TextView tv_progress;
    private LinearLayout ll_progress;

    protected ChatCellImage(Context context, View view, ICellEventListener listener, MessageAdapter adapter) {
        super(context, view, listener, adapter);
    }


    @Override
    protected void initView() {
        super.initView();
        imageView = getView().findViewById(R.id.iv_img);
        ll_progress = getView().findViewById(R.id.ll_progress);
        progressBar = getView().findViewById(R.id.progress_bar);
        tv_progress = getView().findViewById(R.id.tv_progress);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void showMessage(MsgAllBean message) {
        super.showMessage(message);
        imageMessage = message.getImage();
        if (imageMessage == null) {
            return;
        }
        loadImage(message);
    }

    private void loadImage(MsgAllBean message) {
        String thumbnail = imageMessage.getThumbnailShow();
        resetSize();
        checkSendStatus();
        RequestOptions rOptions = new RequestOptions();
        rOptions.override(width, height);
        String tag = (String) imageView.getTag(R.id.tag_img);
        if (isGif(thumbnail)) {
            String gif = message.getImage().getPreview();
            if (!TextUtils.equals(tag, gif)) {
                imageView.setTag(R.id.tag_img, gif);
                rOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
                glide(rOptions, gif);
            } else {
                glide(rOptions, tag);
            }

        } else {
//            rOptions.centerCrop();
            rOptions.error(R.mipmap.default_image);
            rOptions.placeholder(R.mipmap.default_image);
            if (!TextUtils.equals(tag, thumbnail)) {
                imageView.setTag(R.id.tag_img, thumbnail);
                glide(rOptions, thumbnail);
            } else {
                glide(rOptions, tag);
            }

        }

    }

    private void glide(RequestOptions rOptions, String gif) {
        Glide.with(getContext())
                .load(gif)
                .apply(rOptions)
//                    .thumbnail(0.2f)
                .into(imageView);
    }


    private boolean isGif(String path) {
        if (!TextUtils.isEmpty(path)) {
            if (path.toLowerCase().endsWith(".gif")) {
                return true;
            }
        }
        return false;
    }

    private void resetSize() {
        int realW = (int) imageMessage.getWidth();
        int realH = (int) imageMessage.getHeight();
        if (realH > 0) {
            double scale = (realW * 1.00) / realH;
            if (realW > realH) {
                width = DEFAULT_W;
                height = (int) (width / scale);
            } else if (realW < realH) {
                height = DEFAULT_H;
                width = (int) (height * scale);
            } else {
                width = height = DEFAULT_H;
            }
        }
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.width = width;
        lp.height = height;
        imageView.setLayoutParams(lp);

        if (ll_progress != null) {
            FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            lp2.width = width;
            lp2.height = height;
            ll_progress.setLayoutParams(lp2);
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == imageView.getId()) {
            if (mCellListener != null && model != null) {
                mCellListener.onEvent(ChatEnum.ECellEventType.IMAGE_CLICK, model, new Object());
            }
        }
    }

    @Override
    public void onBubbleClick() {
        super.onBubbleClick();
        if (mCellListener != null && model != null) {
            mCellListener.onEvent(ChatEnum.ECellEventType.IMAGE_CLICK, model, new Object());
        }
    }

    private void checkSendStatus() {
        if (ll_progress == null) {
            return;
        }
        setSendStatus();
        switch (model.getSend_state()) {
            case ChatEnum.ESendStatus.ERROR:
                ll_progress.setVisibility(View.GONE);
                break;
            case ChatEnum.ESendStatus.PRE_SEND:
                ll_progress.setVisibility(VISIBLE);
                break;
            case ChatEnum.ESendStatus.NORMAL:
                ll_progress.setVisibility(View.GONE);

                break;
            case ChatEnum.ESendStatus.SENDING:
                ll_progress.setVisibility(VISIBLE);
                break;
        }
    }

    public void updateProgress(@ChatEnum.ESendStatus int status, int progress) {
        loadImage(model);
//        LogUtil.getLog().i(ChatCellImage.class.getSimpleName(), "发送状态=" + status + "--发送进度=" + progress);
        if (ll_progress != null && progressBar != null && tv_progress != null) {
            checkSendStatus();
            if (progress > 0 && progress < 100) {
                ll_progress.setVisibility(View.VISIBLE);
//                setSendStatus();
//                tv_progress.setVisibility(VISIBLE);
                tv_progress.setText(progress + "%");
            } else {
                ll_progress.setVisibility(View.GONE);
            }
        }
    }
}
