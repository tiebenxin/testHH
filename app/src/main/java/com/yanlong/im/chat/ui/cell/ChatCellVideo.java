package com.yanlong.im.chat.ui.cell;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.MsgAllBean;

public class ChatCellVideo extends ChatCellBase {

    protected ChatCellVideo(Context context, View view, ICellEventListener listener, MessageAdapter adapter) {
        super(context, view, listener, adapter);
    }


    public void updateProgress(@ChatEnum.ESendStatus int status, int progress) {
        loadImage(model);
////        LogUtil.getLog().i(ChatCellImage.class.getSimpleName(), "发送状态=" + status + "--发送进度=" + progress);
//        if (ll_progress != null && progressBar != null && tv_progress != null) {
//            checkSendStatus();
//            if (progress > 0 && progress < 100) {
//                ll_progress.setVisibility(View.VISIBLE);
////                setSendStatus();
////                tv_progress.setVisibility(VISIBLE);
//                tv_progress.setText(progress + "%");
//            } else {
//                ll_progress.setVisibility(View.GONE);
//            }
//        }
    }



    private void loadImage(MsgAllBean message) {
//        String thumbnail = imageMessage.getThumbnailShow();
//        RequestOptions rOptions = new RequestOptions();
//        rOptions.override(width, height);
//        String tag = (String) imageView.getTag(R.id.tag_img);
//        if (isGif(thumbnail)) {
//            String gif = message.getImage().getPreview();
//            if (!TextUtils.equals(tag, gif)) {
//                imageView.setTag(R.id.tag_img, gif);
//                rOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
//                glide(rOptions, gif);
//            } else {
//                glide(rOptions, tag);
//            }
//
//        } else {
////            rOptions.centerCrop();
//            rOptions.error(R.mipmap.default_image);
//            rOptions.placeholder(R.mipmap.default_image);
//            if (!TextUtils.equals(tag, thumbnail)) {
//                imageView.setTag(R.id.tag_img, thumbnail);
//                glide(rOptions, thumbnail);
//            } else {
//                glide(rOptions, tag);
//            }
//
//        }

    }

}
