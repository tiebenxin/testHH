package net.cb.cb.library.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.cb.cb.library.R;
import net.cb.cb.library.utils.DensityUtil;
import net.cb.cb.library.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/***
 * 拼音控件
 */
public class PySortView extends LinearLayout {
    private TextView txtSelectView;
    private LinearLayout txtPyIptView;
    private int maxSize = 28;
    private RecyclerView listview;
    //标签,和列号
    private HashMap<String, Integer> tagIndex = new HashMap<>();

    //目标项是否在最后一个可见项之后
    private boolean mShouldScroll;
    //记录目标项位置
    private int mToPosition;
    private LayoutInflater mInflater;
    private float spHeight = 0;
    private int height = 0;
    private int maxheight = 0;
    private LinearLayoutManager mManager;

    private Event mEvent = new Event() {
        @Override
        public void onChange(String type) {
            if (listview == null)
                return;
            if (tagIndex == null)
                return;

            if (tagIndex.containsKey(type)) {
                int i = tagIndex.get(type);

                if (mManager != null) {
                    moveToPosition(mManager, i);
                }
//                else {
//                    smoothMoveToPosition(listview, i);
//                }
            }

        }
    };

    public void setEvent(Event mEvent) {
        this.mEvent = mEvent;
    }


    public void putTag(String tag, int i) {
        if (!tagIndex.containsKey(tag))
            tagIndex.put(tag, i);
    }

    public void clearAllTag() {
        if (tagIndex != null) {
            tagIndex.clear();
        }
    }

    public HashMap<String, Integer> getTagIndex() {
        return tagIndex;
    }

    public void setListView(RecyclerView recyclerView) {
        listview = recyclerView;
        listview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mShouldScroll && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    mShouldScroll = false;
//                    smoothMoveToPosition(listview, mToPosition);
                    if (mManager != null) {
                        moveToPosition(mManager, mToPosition);
                    }
                }
            }
        });

    }

    public void setLinearLayoutManager(LinearLayoutManager linearLayoutmanager) {
        mManager = linearLayoutmanager;
    }

    /**
     * RecyclerView 移动到当前位置，不需要滚动效果
     *
     * @param manager 设置RecyclerView对应的manager
     * @param n       要跳转的位置
     */
    public static void moveToPosition(LinearLayoutManager manager, int n) {
        try {
            manager.scrollToPositionWithOffset(n, 0);
            // TODO 当条数少时，会直接显示到底部
//            manager.setStackFromEnd(true);
        } catch (Exception e) {
        }
    }

    /**
     * 滑动到指定位置 TODO 用户量多的时候滑动起来体验不好
     */
    private void smoothMoveToPosition(RecyclerView mRecyclerView, int position) {
        // 第一个可见位置
        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
        // 最后一个可见位置
        int lastItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));
        if (position < firstItem) {
            // 第一种可能:跳转位置在第一个可见位置之前，使用smoothScrollToPosition
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            // 第二种可能:跳转位置在第一个可见位置之后，最后一个可见项之前
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                int top = mRecyclerView.getChildAt(movePosition).getTop();
                // smoothScrollToPosition 不会有效果，此时调用smoothScrollBy来滑动到指定位置
                mRecyclerView.smoothScrollBy(0, top);
            }
        } else {
            // 第三种可能:跳转位置在最后可见项之后，则先调用smoothScrollToPosition将要跳转的位置滚动到可见位置
            // 再通过onScrollStateChanged控制再次调用smoothMoveToPosition，执行上一个判断中的方法
            mRecyclerView.smoothScrollToPosition(position);
            mToPosition = position;
            mShouldScroll = true;
        }
    }


    public PySortView(final Context context, AttributeSet attrs) {
        super(context, attrs);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View viewRoot = mInflater.inflate(R.layout.view_pysort, this);
        txtSelectView = viewRoot.findViewById(R.id.txt_py_select);
        txtPyIptView = viewRoot.findViewById(R.id.view_py_ipt);
        txtSelectView.setVisibility(GONE);
//        for (int i = 0; i < maxSize; i++) {
//
//            TextView textView = (TextView) inflater.inflate(R.layout.view_pysort_item, null);
//
//            if (i == 0) {
//                textView.setText("↑");
//            }else if(i==(maxSize-1)){
//                textView.setText("#");
//            }else {
//                textView.setText("" + (char) (64 + i));
//            }
//            textView.setTag(textView.getText().toString());
//
//
//            txtPyIptView.addView(textView);
//            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
//            layoutParams.weight = 1;
//        }
    }

    /**
     * 添加首字母视图
     *
     * @param list 首字母列表
     */
    public void addItemView(List<String> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        // 去掉重复值
        List<String> listTemp = new ArrayList<>();
        for (String value : list) {
            if (!listTemp.contains(value) && !"↑".equals(value)) {
                listTemp.add(value);
            }
        }
        txtPyIptView.removeAllViews();
        if (mInflater == null) {
            mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        maxSize = listTemp.size();

        for (int i = 0; i < listTemp.size(); i++) {
            addView(listTemp.get(i));
        }
    }

    public void addView(String value) {
        if ("↑".equals(value)) {
            return;
        }
        TextView txtViewTop = (TextView) mInflater.inflate(R.layout.view_pysort_item, null);
        txtViewTop.setText(value);
        txtViewTop.setTag(txtViewTop.getText().toString());
        txtPyIptView.addView(txtViewTop);
        setLayoutparams(txtViewTop);
    }

    private void setLayoutparams(TextView txtView) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) txtView.getLayoutParams();
        layoutParams.width = DensityUtil.dip2px(getContext(), 25);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        height = getMeasuredHeight();
        if (maxSize > 0) {
            spHeight = getMeasuredHeight() / maxSize;
        }
        maxheight = height - txtSelectView.getMeasuredHeight();
    }


    private String oldTxt = "";

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                txtSelectView.setVisibility(VISIBLE);

                Float idx = event.getY() / spHeight;

                idx = idx < 0 ? 0 : idx;
                idx = idx > maxSize ? maxSize - 1 : idx;
                View view = txtPyIptView.getChildAt(idx.intValue());
                if (view != null) {
                    //设置显示文字
                    String txt = (String) view.getTag();
                    if (StringUtil.isNotNull(txt)) {
                        txtSelectView.setText(txt);
                    }
                    //动态设置文字位置
                    float y = event.getY();
                    y = y < 0 ? 0 : y;
                    y = y > maxheight ? maxheight : y;
                    txtSelectView.setY(y);

                    //处理回掉事件
                    if (mEvent != null) {
                        if (!oldTxt.equals(txt)) {
                            oldTxt = txt;
                            mEvent.onChange(txt);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                txtSelectView.setVisibility(GONE);
                oldTxt = "";
                break;
        }


        return true;
    }

    public interface Event {
        void onChange(String type);
    }
}
