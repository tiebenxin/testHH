package com.yanlong.im.wight.avatar;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;


public interface IAvatarDrawable {

     void setImageDrawable(Drawable drawable);

     void setImageBitmap(Bitmap bm);

     ImageView getImageView();

     void setProgress(int value, int max);

     ProgressBar getProgressBar();

     void setGifFlag(boolean value);

     void setPressesStateVisibility(boolean value);

     void setVisibility(int visibility);

     int getVisibility();

     void setOnClickListener(View.OnClickListener onClickListener);

     void setOnLongClickListener(View.OnLongClickListener onLongClickListener);

     void setLayoutParams(ViewGroup.LayoutParams layoutParams);
}
