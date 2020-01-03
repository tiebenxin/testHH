package com.yanlong.im.utils;

import android.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import net.cb.cb.library.view.MultiListView;

public class SlideSearchUtil {
    public static void slide(final View viewSearch ,final MultiListView mtListView){
        //需要创建一个相对layout,并且改变控件顺序,设置 mtListView底部对齐
      /*  ViewGroup rootview =(ViewGroup) viewSearch.getParent();
        RelativeLayout relativeLayout=new RelativeLayout(viewSearch.getContext());
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        relativeLayout.addView(mtListView);
        relativeLayout.addView(viewSearch);
        rootview.addView(relativeLayout);
        rootview.removeView(viewSearch);
        rootview.removeView(mtListView);
*/





        mtListView.getListView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                int vset= rv.computeVerticalScrollOffset() ;


                if(viewSearch.getTag()!=null&&vset>(int)viewSearch.getTag()){
                    vset=(int)viewSearch.getTag();
                }

                viewSearch.setTranslationY(-vset);


            }
        });
        final Runnable uiRun=new Runnable() {
            @Override
            public void run() {

                ViewGroup.LayoutParams lp = viewSearch.getLayoutParams();
                int h=viewSearch.getMeasuredHeight();
                if(lp instanceof ViewGroup.MarginLayoutParams){
                    ViewGroup.MarginLayoutParams lps=   ((ViewGroup.MarginLayoutParams) lp);
                    h+=lps.topMargin+lps.bottomMargin;
                    viewSearch.setTag(h);
                }
                mtListView.getListView().setPadding(0,h,0,0);
                //这里marpin设置为-h
                ViewGroup.MarginLayoutParams lp2 = (ViewGroup.MarginLayoutParams) mtListView.getLayoutParams();
                lp2.topMargin=-h;
                mtListView.setLayoutParams(lp2);

                mtListView.getListView().setClipToPadding(false);

                mtListView.getListView().scrollBy(0,-h);

            }
        };

        viewSearch.post(uiRun);
    }
}
