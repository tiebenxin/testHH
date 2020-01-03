package com.yanlong.im.chat.ui.view;

import android.database.DataSetObserver;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;


public class ControllerLinearList {

    private LinearLayout _pnlValues;
    private BaseAdapter _adapter;
    private OnClickListener mListener;

    public ControllerLinearList(LinearLayout linearLayout) {
        _pnlValues = linearLayout;
    }

    public void setAdapter(BaseAdapter adapter) {
        _adapter = adapter;
        _adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateListValues();
            }
        });
        updateListValues();
    }

    private void updateListValues() {
        _pnlValues.removeAllViews();

        for (int i = 0; i < _adapter.getCount(); i++) {
            _pnlValues.addView(_adapter.getView(i, null, null));
        }
    }

    public void setOnClickListener(OnClickListener listener){
        mListener = listener;
    }

    public interface OnClickListener<T> {
        void onItemClick(T t);
    }
}
