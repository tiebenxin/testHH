package com.yanlong.im.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yanlong.im.chat.dao.MsgDao;

import net.cb.cb.library.utils.StringUtil;

public class ImageUtils {
    public static void showImg(Context mContext, String imgHead,ImageView imageView,String gid){
        if (imgHead!=null&&!imgHead.isEmpty()&& StringUtil.isNotNull(imgHead)){
            Glide.with(mContext).load(imgHead)
                    .apply(GlideOptionsUtil.headImageOptions()).into(imageView);
        }else{
            MsgDao msgDao=new MsgDao();
            String url= msgDao.groupHeadImgGet(gid);
            Glide.with(mContext).load(url)
                    .apply(GlideOptionsUtil.headImageOptions()).into(imageView);
        }
    }
}
