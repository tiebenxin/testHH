package net.cb.cb.library.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/***
 * @author jyj
 * @date 2017/3/15
 */
public class MapViewPage extends ViewPager {
    public MapViewPage(Context context) {
        super(context);
    }

    public MapViewPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if(v.getClass().getName().equals("com.baidu.mapapi.map.MapView")||v.getClass().getName().equals("com.amap.api.maps.MapView")) {
            return true;
        }
        //if(v instanceof MapView){
        //    return true;
        //}
        return super.canScroll(v, checkV, dx, x, y);
    }
}
