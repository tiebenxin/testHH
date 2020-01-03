package com.example.nim_lib.util;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.nim_lib.R;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-10-18
 * @updateAuthor
 * @updateDate
 * @description
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class GlideUtil {

    // glide头像 Options
    public static RequestOptions headImageOptions() {

        RoundedCorners roundedCorners = new RoundedCorners(5);
        RequestOptions mRequestOptions = RequestOptions.bitmapTransform(roundedCorners)
                .error(R.mipmap.ic_info_head)
                .placeholder(R.mipmap.ic_info_head)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .skipMemoryCache(false);
        return mRequestOptions;
    }


    // 普通图片
    public static RequestOptions imageOptions() {
        RoundedCorners roundedCorners = new RoundedCorners(3);
        RequestOptions mRequestOptions = RequestOptions.bitmapTransform(roundedCorners)
                .error(R.mipmap.ic_img_def)
                .placeholder(R.mipmap.ic_img_def)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .fitCenter();
        return mRequestOptions;
    }

    //普通图片 不带圆角有默认图
    public static RequestOptions defImageOptions() {
        RequestOptions mRequestOptions = RequestOptions.centerInsideTransform()
                .error(R.mipmap.ic_img_def)
                .placeholder(R.mipmap.ic_img_def)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .fitCenter();
        return mRequestOptions;
    }

    //普通图片 不带圆角有默认图
    public static RequestOptions defImageOptions1() {
        RequestOptions mRequestOptions = RequestOptions.centerInsideTransform()
                .error(R.mipmap.ic_img_def)
                .placeholder(R.mipmap.ic_img_def)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .centerCrop();
        return mRequestOptions;
    }


    //普通图片不带圆角默认图
    public static RequestOptions notDefImageOptions() {
        RequestOptions mRequestOptions = RequestOptions.centerInsideTransform()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .fitCenter();
        return mRequestOptions;

    }


    //圆形头像
    public static RequestOptions headImageCircleCropOptions() {
        RequestOptions mRequestOptions = RequestOptions.circleCropTransform()
                .error(R.mipmap.ic_info_head)
                .placeholder(R.mipmap.ic_info_head)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .centerCrop();
        return mRequestOptions;
    }
}
