package com.yanlong.im.utils;

import android.view.View;
import android.widget.ImageView;

import com.yanlong.im.R;


/**
 * @创建人 shenxin
 * @创建时间 2019/10/15 0015 11:53
 */
public class ReadDestroyUtil {


    public void setImageViewShow(int destroyTime, ImageView imageView){
        switch (destroyTime){
            case -1:
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.mipmap.icon_colse);
                break;
            case 0:
                imageView.setVisibility(View.GONE);
                break;
            case 5:
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.mipmap.icon_5_s);
                break;
            case 10:
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.mipmap.icon_10_s);
                break;
            case 30:
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.mipmap.icon_30_s);
                break;
            case 60:
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.mipmap.icon_1_min);
                break;
            case 300:
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.mipmap.icon_5_min);
                break;
            case 1800:
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.mipmap.icon_30_min);
                break;
            case 3600:
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.mipmap.icon_1_h);
                break;
            case 21600:
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.mipmap.icon_6_h);
                break;
            case 43200:
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.mipmap.icon_12_h);
                break;
            case 86400:
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.mipmap.icon_1_day);
                break;
            case 604800:
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.mipmap.icon_1_week);
                break;
            default:
                imageView.setVisibility(View.GONE);
                break;
        }
    }



    public String getDestroyTimeContent(int destroyTime){
        switch (destroyTime){
            case -1:
                return "退出即焚";
            case 0:
                return "关闭";
            case 5:
                return "5秒";
            case 10:
                return "10秒";
            case 30:
                return "30秒";
            case 60:
                return "1分钟";
            case 300:
                return "5分钟";
            case 1800:
                return "30分钟";
            case 3600:
                return "1小时";
            case 21600:
                return "6小时";
            case 43200:
                return "12小时";
            case 86400:
                return "1天";
            case 604800:
                return "一个星期";
            default:
                return "关闭";
        }
    }


}
