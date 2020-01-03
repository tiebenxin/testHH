package com.yanlong.im.view.face.adapter;

import java.util.ArrayList;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * viewpager的适配器
 *
 * @Description TODO
 * @author CodeApe
 * @version 1.0
 * @date 2013-11-23
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 *
 */
public class ViewPagerAdapter extends PagerAdapter {

	/** v]iew集合*/
	private ArrayList<View> views;

	public ViewPagerAdapter(ArrayList<View> views) {
		this.views = views;
	}

	@Override
	public int getCount() {
		return views.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(View view, int position) {
		ViewPager viewPage = (ViewPager) view;
		viewPage.addView(views.get(position));
		return views.get(position);
	}

	@Override
	public void destroyItem(ViewGroup group, int position, Object object) {
		ViewPager viewPage = (ViewPager) group;
		viewPage.removeView(views.get(position));
	}

}
