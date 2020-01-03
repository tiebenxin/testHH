package com.luck.picture.lib;

import com.luck.picture.lib.entity.LocalMedia;

import java.util.List;

/**
 * @author Liszt
 * @date 2019/8/14
 * Description
 */
public interface OnPhotoSelectChangedListener {
    /**
     * 拍照回调
     */
    void onTakePhoto();

    /**
     * 已选Media回调
     *
     * @param selectImages
     */
    void onChange(List<LocalMedia> selectImages);

    /**
     * 图片预览回调
     *
     * @param media
     * @param position
     */
    void onPictureClick(LocalMedia media, int position);
}
