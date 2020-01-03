package net.cb.cb.library.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.baoyz.widget.PullRefreshLayout;

import net.cb.cb.library.R;

import retrofit2.Response;

import static net.cb.cb.library.view.MlistAdapter.LOAD_STATE_COMPLETE;
import static net.cb.cb.library.view.MlistAdapter.LOAD_STATE_NOMORL;


/***
 * 多功能ListView
 *
 * @author 姜永健
 * @date 2016年3月31日
 */
public class MultiListView extends LinearLayout {
    private static final String TAG = "MultiListView";
    private LoadView loadView;
    private PullRefreshLayout swipeLayout;
    private RecyclerView listView;
    private Context context;
    private MlistAdapter adt;
    private Event event;
    private int resId;
    //页面大小
    private Integer pageSize;
    private YLLinearLayoutManager layoutManager;

    /***
     * 设置事件设置监听
     *
     * @param event
     */
    public void setEvent(Event event) {

        this.event = event;
        if (event != null)
            swipeLayout.setEnabled(true);
    }

    public Event getEvent() {
        return event;
    }

    public RecyclerView getListView() {
        return listView;
    }

    public LoadView getLoadView() {
        return loadView;
    }

    public PullRefreshLayout getSwipeLayout() {
        return swipeLayout;
    }

    public MultiListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // TODO Auto-generated constructor stub
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.view_re_list, this);
        this.context = context;
        loadView = rootView.findViewById(R.id.loadView);
        swipeLayout = rootView
                .findViewById(R.id.swipeLayout);
        listView = rootView.findViewById(R.id.listView);

        // addView(rootView);
        rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        initView();
    }

    /***
     * 初始化控件
     */
    private void initView() {
        //定义下拉样式
        //swipeLayout.setColorSchemeResources(R.color.green_600);
        swipeLayout.setRefreshDrawable(new MaterialDrawable(context, swipeLayout));

        swipeLayout.setEnabled(false);
        layoutManager = new YLLinearLayoutManager(context);
        listView.setLayoutManager(layoutManager);
        ((SimpleItemAnimator) listView.getItemAnimator()).setSupportsChangeAnimations(false);
        listView.setItemAnimator(null);
        // ------------------------------------------
        // 下拉刷新
        swipeLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (event != null) {
                    Log.i(TAG, "===>onRefresh");
                    event.onRefresh();

                }
                // 让旋转刷新等1s
                /*
                 * swipeLayout.postDelayed(new Runnable() {
                 *
                 * @Override public void run() { // TODO Auto-generated method
                 * stub swipeLayout.setRefreshing(false); } }, 1000);
                 */
            }
        });
        // 加载更多
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isLoad = false;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = getLastVisiBleItem();// ((LinearLayoutManager)
                // listView.getLayoutManager()).findLastVisibleItemPosition();
                int totalItemCount = adt.getItemCounts(); //listView.getLayoutManager().getItemCount();
                if (lastVisibleItem == totalItemCount - 1 && dy > 0) {
                    isLoad = true;
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView,
                                             int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 当滚到最后一行且停止滚动时，执行加载
                if (isLoad && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (event != null) {//有加载更多事件
                        Log.i(TAG, "===>onLoadMore");
                        event.onLoadMore();
                    }
                    isLoad = false;
                }
            }
        });

        // ---------------------------------------
    }

    public LinearLayoutManager getLayoutManager() {
        return layoutManager;
    }

    /**
     * 最后一个的位置
     */
    private int getLastVisiBleItem() {
        RecyclerView.LayoutManager layoutManager = listView.getLayoutManager();
        int lastVisibleItemPosition = -1;

        int layoutManagerType = 0;
        if (layoutManager instanceof LinearLayoutManager) {
            layoutManagerType = 0;
        } else if (layoutManager instanceof GridLayoutManager) {
            layoutManagerType = 1;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            layoutManagerType = 2;
        } else {
            throw new RuntimeException(
                    "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
        }

        switch (layoutManagerType) {
            case 0:
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager)
                        .findLastVisibleItemPosition();
                break;
            case 1:
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager)
                        .findLastVisibleItemPosition();
                break;
            case 2:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                int[] lastPositions = new int[staggeredGridLayoutManager
                        .getSpanCount()];

                staggeredGridLayoutManager
                        .findLastVisibleItemPositions(lastPositions);
                lastVisibleItemPosition = findMax(lastPositions);
                break;
        }
        return lastVisibleItemPosition;
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    /***
     * 1.初始化
     *
     * @param adapter 适配器
     */
    public void init(Adapter adapter) {

        //init(adapter, R.drawable.bg_no_date);
        init(adapter, R.mipmap.ic_nodate);

    }

    /***
     * 1.初始化
     *
     * @param adapter 适配器
     * @param noDataBG 空数据背景图
     */
    public void init(Adapter adapter, int noDataBG) {
        init(adapter, null, noDataBG);
    }

    /***
     * 1.初始化(针对有刷新)
     *
     * @param adapter 适配器
     * @param noDataBG 空数据背景图
     * @param event 列表事件
     *            ,如果不设置则无下拉刷新
     */
    public void init(Adapter adapter, Event event, int noDataBG) {
        init(adapter, event, noDataBG, null, null);
    }

    public void init(Adapter adapter, Integer pageSize, Event event) {
        init(adapter, event, null, pageSize, null);
    }

    public void init(Adapter adapter, Integer column, Integer pageSize, Event event) {
        init(adapter, event, null, pageSize, column);
    }


    public void init(Adapter adapter, Event event, Integer noDataBG, Integer pageSize, Integer column) {
        if (column != null) {
            listView.setLayoutManager(new GridLayoutManager(getContext(), column));
        }
        adt = new MlistAdapter(context, adapter);
        listView.setAdapter(adt);
        resId = noDataBG == null ? R.mipmap.ic_nodate : noDataBG;
        this.pageSize = pageSize;
        setEvent(event);
    }

    int lastDataCount = 0;

    public void notifyDataSetChange() {
        Response response = Response.success(null);
        notifyDataSetChange(response);
    }

    /***
     * 2.改变数据,无论数据是否有变化都得调用
     *
     * @param response
     */
    public void notifyDataSetChange(Response response) {


        swipeLayout.setRefreshing(false);
        adt.notifyDataSetChanged();


        //加载更多数据处理
        if (pageSize != null) {
            int nowCount = adt.getItemCounts();
            if (lastDataCount + pageSize == nowCount) {
                adt.setLoadState(LOAD_STATE_NOMORL);
            } else if (nowCount < pageSize) {
                adt.setLoadState(LOAD_STATE_COMPLETE);
            } else if (nowCount > pageSize && (lastDataCount + pageSize) > nowCount) {
                adt.setLoadState(LOAD_STATE_COMPLETE);
            }
            adt.notifyItemChanged(adt.getItemCounts());
            lastDataCount = nowCount;
        }


        if (adt.getItemCounts() > 0) {// 列表有数据后
            //if (response.code() == 200) {// 正常加载数据
            loadView.setStateNormal();
		/*	} else if (netErr != null
					&& netErr.getCode() == netErr.CODE_ERR_NOT_NET) {// 加载数据后网络不通
				ToastMaker.makeToast(getContext(), "网络不可用，请稍后再试。");
			} else if (netErr != null
					&& netErr.getCode() == netErr.CODE_ERR_LOST_CONNECT) {// 加载数据后网络不通
				ToastMaker.makeToast(context, "失败了，请稍后再试。");
			}*/

        } else {// 列表无数据
            if (response.code() == 200) {// 网络无问题
                loadView.setStateNoData(resId);
            } else {// 网络有问题
               /* loadView.setStateNoNet(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        loadView.setStateLoading();
                        event.onLoadFail();

                    }
                });*/
            }
        }

    }

    /***
     * 事件接口
     *
     * @author 姜永健
     * @date 2016年3月31日
     */
    public interface Event {
        /***
         * 刷新
         */
        void onRefresh();

        /***
         * 加载更多
         */
        void onLoadMore();

        /***
         * 加载失败重试的task放这里
         */
        void onLoadFail();
    }

}
