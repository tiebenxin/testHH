package net.cb.cb.library.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.cb.cb.library.R;


/***
 * 加载视图
 *
 * @author 姜永健
 * @date 2016年3月1日
 */
public class LoadView extends RelativeLayout {

    private View pb;
    private ImageView imgNoData;
    private ImageView imgNoNet;

    public LoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.view_load_data, null);
        pb = rootView.findViewById(R.id.prog);
        imgNoData = rootView.findViewById(R.id.img_no_data);
        imgNoNet = rootView.findViewById(R.id.img_no_net);

        addView(rootView);
        rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public LoadView(Context context) {
        super(context);

    }

    /***
     * 状态:无数据
     *
     * @param resId 背景图
     */
    public void setStateNoData(int resId) {
        Log.e("noDataRid", "===resId>" + resId);
        imgNoData.setImageResource(resId);

        imgNoData.setVisibility(View.VISIBLE);
        imgNoNet.setVisibility(View.GONE);
        pb.setVisibility(View.GONE);
        this.setVisibility(View.VISIBLE);
    }


    /***
     * 状态:无网络
     */
    public void setStateNoNet(OnClickListener oc) {
        imgNoNet.setVisibility(View.VISIBLE);
        imgNoData.setVisibility(View.GONE);
        this.setOnClickListener(oc);
        pb.setVisibility(View.GONE);
        this.setVisibility(View.VISIBLE);
    }

    /***
     * 状态:加载中
     */
    public void setStateLoading() {
        imgNoNet.setVisibility(View.GONE);
        imgNoData.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);
        this.setVisibility(View.VISIBLE);
    }

    /***
     * 状态:正常
     */
    public void setStateNormal() {
        imgNoNet.setVisibility(View.GONE);
        imgNoData.setVisibility(View.GONE);
        pb.setVisibility(View.GONE);
        this.setVisibility(View.GONE);
    }
}
