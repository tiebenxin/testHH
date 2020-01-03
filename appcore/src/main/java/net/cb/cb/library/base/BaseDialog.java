package net.cb.cb.library.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;


public abstract  class BaseDialog extends Dialog implements OnClickListener{

	
	public BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		initView();
	}

	public BaseDialog(Context context, int theme) {
		super(context, theme);
		initView();
	}

	public BaseDialog(Context context) {
		super(context);
		initView();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		initView();
		initData();
		initEvent();
	}

	public abstract void initView();
	public void initData(){}
	public void initEvent(){}
	public abstract void processClick(View view);
	@Override
	public void onClick(View v) {
		processClick(v);
		
	}
	
}
