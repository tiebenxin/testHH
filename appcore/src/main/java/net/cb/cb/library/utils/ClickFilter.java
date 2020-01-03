package net.cb.cb.library.utils;

import android.view.View;

//按钮拦截
public class ClickFilter {
    private static long TIME_FT=1000;//阻挡300ms的点击
    private static long time=0;
    public static void onClick(View view, final View.OnClickListener onclick){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(System.currentTimeMillis()<(time+TIME_FT)){

                    return;
                }

                time=System.currentTimeMillis();
                onclick.onClick(v);
            }
        });
    }



}
