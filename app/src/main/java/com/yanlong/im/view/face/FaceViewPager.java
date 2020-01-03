package com.yanlong.im.view.face;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.yanlong.im.R;
import com.yanlong.im.view.face.adapter.FaceAdapter;
import com.yanlong.im.view.face.bean.FaceBean;

import java.util.ArrayList;

/**
 * 表情视图
 * 
 * @Description TODO
 * @author CodeApe
 * @version 1.0
 * @date 2013-11-23
 * @Copyright: Copyright (c) 2013 Shenzhen Tentinet Technology Co., Ltd. Inc.
 *             All rights reserved.
 * 
 */
public class FaceViewPager extends RelativeLayout {

	/** 上下文 */
	private Context context;
	/** 父视图 */
	private View view_Parent;
	/** 表情列表控件 */
	private GridView mGridView;
	/** 表情列表 */
	public ArrayList<FaceBean> list_FaceBeans = new ArrayList<FaceBean>();
	/** 删除按钮 */
	private ImageButton imgBtn_Del;
	/** 列表适配器 */
	private FaceAdapter adapter;

	/** 表情类型 */
	private int faceType = FaceAdapter.FACE_TYPE_EMOJI;

	public FaceViewPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public FaceViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FaceViewPager(Context context, int faceType) {
		super(context);
		this.faceType = faceType;
		init(context);
	}

	/**
	 * 初始化界面
	 * 
	 * @version 1.0
	 * @createTime 2013-10-20,下午4:51:48
	 * @updateTime 2013-10-20,下午4:51:48
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param context
	 */
	private void init(Context context) {
		this.context = context;

		view_Parent = LayoutInflater.from(this.context).inflate(R.layout.view_face_pager, null);
		this.addView(view_Parent);

		mGridView =  view_Parent.findViewById(R.id.view_face_pager_gridview);
		imgBtn_Del = view_Parent.findViewById(R.id.view_face_pager_btn_delete);
		
		switch (faceType) {
		case FaceAdapter.FACE_TYPE_EMOJI:
			mGridView.setNumColumns(7);
			imgBtn_Del.setVisibility(View.VISIBLE);
			break;
//			case FaceAdapter.FACE_TYPE_ANIMO:
//			case FaceAdapter.FACE_TYPE_CUSTOM:
		default:
			mGridView.setNumColumns(4);
			imgBtn_Del.setVisibility(View.GONE);
			break;
		}
		
		adapter = new FaceAdapter(context, list_FaceBeans);
		mGridView.setAdapter(adapter);

	}

	/**
	 * 设置表情列表
	 * 
	 * @version 1.0
	 * @createTime 2013-11-23,下午4:50:34
	 * @updateTime 2013-11-23,下午4:50:34
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 */
	public void setFaceList(ArrayList<FaceBean> list_FaceBeans) {
		this.list_FaceBeans.clear();
		this.list_FaceBeans.addAll(list_FaceBeans);
		adapter.notifyDataSetChanged();

	}

	/**
	 * 设置列表监听事件
	 * 
	 * @version 1.0
	 * @createTime 2013-11-23,下午4:52:47
	 * @updateTime 2013-11-23,下午4:52:47
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param listener
	 *            列表点击事件监听器
	 */
	public void setOnItemClikListener(final FaceClickListener listener) {
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				listener.OnItemClick(list_FaceBeans.get(position));
			}
		});
	}
	
	/**
	 * 设置列表长按事件监听
	 *
	 * @version 1.0
	 * @createTime 2013-12-30,上午10:50:40
	 * @updateTime 2013-12-30,上午10:50:40
	 * @createAuthor liujingguo
	 * @updateAuthor liujingguo
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 *
	 * @param listener 列表长按事件监听器
	 */
	public void setOnItemLongClickListener(final FaceLongClickListener listener) {
		mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				if(listener!=null){
					listener.onItemLongClick(list_FaceBeans.get(position));
				}
				return false;
			}
		});
	}

	/**
	 * 设置删除按钮监听事件
	 * 
	 * @version 1.0
	 * @createTime 2013-11-23,下午4:55:34
	 * @updateTime 2013-11-23,下午4:55:34
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param listener
	 */
	public void setOnDeleteListener(OnClickListener listener) {
		imgBtn_Del.setOnClickListener(listener);
	}

	/**
	 * 表情列表点击事件回调
	 * 
	 * @Description TODO
	 * @author CodeApe
	 * @version 1.0
	 * @date 2013-11-23
	 * @Copyright: Copyright (c) 2013 Shenzhen Tentinet Technology Co., Ltd.
	 *             Inc. All rights reserved.
	 * 
	 */
	public interface FaceClickListener {

		/**
		 * 表情列表点击时候调用该方法
		 * 
		 * @version 1.0
		 * @createTime 2013-11-23,下午10:54:35
		 * @updateTime 2013-11-23,下午10:54:35
		 * @createAuthor CodeApe
		 * @updateAuthor CodeApe
		 * @updateInfo (此处输入修改内容,若无修改可不写.)
		 * 
		 * @param bean
		 *            返回的表情属性
		 */
		public void OnItemClick(FaceBean bean);

	};
	/**
	 * 表情列表长按事件回调
	 *
	 * @Description TODO
	 * @author liujingguo
	 * @version 1.0
	 * @date 2013-12-30
	 * @Copyright: Copyright (c) 2013 Shenzhen Tentinet Technology Co., Ltd. Inc. All rights reserved.
	 *
	 */
	public interface FaceLongClickListener{
		/**
		 * 表情列表长按时调用该方法
		 *
		 * @version 1.0
		 * @createTime 2013-12-30,上午10:37:36
		 * @updateTime 2013-12-30,上午10:37:36
		 * @createAuthor liujingguo
		 * @updateAuthor liujingguo
		 * @updateInfo (此处输入修改内容,若无修改可不写.)
		 *
		 * @param bean
		 */
		public void onItemLongClick(FaceBean bean);
	
	};

}
