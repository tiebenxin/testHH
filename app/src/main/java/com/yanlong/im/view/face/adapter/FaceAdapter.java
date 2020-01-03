package com.yanlong.im.view.face.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.yanlong.im.R;
import com.yanlong.im.view.face.bean.FaceBean;

import java.util.ArrayList;

/**
 * 表情列表适配器
 * 
 * @Description TODO
 * @author CodeApe
 * @version 1.0
 * @date 2013-11-16
 * @Copyright: Copyright (c) 2013 Shenzhen Tentinet Technology Co., Ltd. Inc.
 *             All rights reserved.
 * 
 */
public class FaceAdapter extends BaseAdapter {

	/** emoji系列表情 */
	public static final int FACE_TYPE_EMOJI = 0;
	/** 传情动漫 */
	public static final int FACE_TYPE_ANIMO = 1;
	/** 自定义表情 */
	public static final int FACE_TYPE_CUSTOM = 2;

	/** 上下文环境 */
	private Context context;
	/** 表情属性列表 */
	private ArrayList<FaceBean> list_FaceBeans;
	/** 异步加载图片对象 */
//	private AsyncImageloader asyncLoadPicture;

	public FaceAdapter(Context context, ArrayList<FaceBean> list_FaceBeans) {
		this.context = context;
		this.list_FaceBeans = list_FaceBeans;
//		asyncLoadPicture = new AsyncImageloader(1, R.drawable.image_default_picture, 0, 35);

	}

	@Override
	public int getCount() {
		return list_FaceBeans.size();
	}

	@Override
	public Object getItem(int position) {
		return list_FaceBeans.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ItemHolder holder;
		if (null == convertView) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_face, null);
			holder = new ItemHolder();
			holder.image_thum = (ImageView) convertView.findViewById(R.id.item_face_image_emoji);
			holder.image_big = (ImageView) convertView.findViewById(R.id.item_face_image_big);
			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		FaceBean bean = list_FaceBeans.get(position);
		if (bean.getGroup().equals("custom")) {// 自定义表情
			holder.image_thum.setVisibility(View.GONE);
			holder.image_big.setVisibility(View.VISIBLE);
//			holder.image_big.setImageBitmap(asyncLoadPicture.loadImageBitmap(context, position, bean.getPath(), new ImageCallback() {
//
//				@Override
//				public void imageLoaded(int position, Bitmap bitmap, String imagePath) {
//					if (bitmap != null) {
//						holder.image_thum.setImageBitmap(bitmap);
//					}
//					notifyDataSetChanged();
//				}
//			}));
		} else if (bean.getGroup().equals("animo")) {
			holder.image_thum.setVisibility(View.GONE);
			holder.image_big.setVisibility(View.VISIBLE);
			holder.image_big.setImageResource(list_FaceBeans.get(position).getResId());
		} else {
			holder.image_thum.setVisibility(View.VISIBLE);
			holder.image_big.setVisibility(View.GONE);
			holder.image_thum.setImageResource(list_FaceBeans.get(position).getResId());
		}

		return convertView;
	}

	/**
	 * 内部容器类
	 * 
	 * @Description TODO
	 * @author CodeApe
	 * @version 1.0
	 * @date 2013-11-23
	 * @Copyright: Copyright (c) 2013 Shenzhen Tentinet Technology Co., Ltd.
	 *             Inc. All rights reserved.
	 * 
	 */
	private class ItemHolder {
		private ImageView image_thum;
		private ImageView image_big;
	}

}
