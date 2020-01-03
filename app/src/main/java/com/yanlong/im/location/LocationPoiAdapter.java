package com.yanlong.im.location;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.View;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.yanlong.im.R;
import com.yanlong.im.chat.bean.LocationMessage;
import com.yanlong.im.listener.BaseListener;
import java.util.List;


/**
 * Created by zgd on 2017/7/20.
 * 定位 周围位置
 */
public class LocationPoiAdapter extends BaseQuickAdapter<LocationMessage, BaseViewHolder> {
    private Context context;
    private BaseListener listener;
    public int position=0;

    public LocationPoiAdapter(@LayoutRes int layoutResId, @Nullable Context context, @Nullable List<LocationMessage> data) {
        super(layoutResId, data);
    }

    public LocationPoiAdapter(@Nullable Context context, @Nullable List<LocationMessage> data) {
        this(R.layout.item_location_poi, context, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, final LocationMessage item) {

        helper.setText(R.id.name_tv,item.getAddress());
        helper.setText(R.id.addr_tv,item.getAddressDescribe());

        if(position==helper.getAdapterPosition()){
            helper.getView(R.id.iamge_iv).setVisibility(View.VISIBLE);
        }else {
            helper.getView(R.id.iamge_iv).setVisibility(View.INVISIBLE);
        }

        helper.getView(R.id.item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position=helper.getAdapterPosition();

                if(listener!=null){
                    listener.onSuccess(item);
                }
                notifyDataSetChanged();
            }
        });
    }

    public void setListener(BaseListener baseListener){
        listener=baseListener;
    }
}
