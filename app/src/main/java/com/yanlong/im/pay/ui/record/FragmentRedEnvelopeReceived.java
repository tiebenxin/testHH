package com.yanlong.im.pay.ui.record;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hm.cxpay.bean.RedEnvelopeItemBean;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;
import com.hm.cxpay.bean.RedDetailsBean;
import com.yanlong.im.R;

import net.cb.cb.library.base.AbstractRecyclerAdapter;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.MultiListView;

import java.util.List;

/**
 * @author Liszt
 * @date 2019/12/2
 * Description 接收到的红包
 */
public class FragmentRedEnvelopeReceived extends Fragment {

    private View rootView;
    private MultiListView mMtListView;
    int currentPage = 1;
    private AdapterRedEnvelopeReceived adapter;
    private long totalCount;
    private List<RedEnvelopeItemBean> mDataList;

    public static FragmentRedEnvelopeReceived newInstance() {
        FragmentRedEnvelopeReceived fragment = new FragmentRedEnvelopeReceived();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_content, null);
        initView();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRedEnvelopeDetails();
    }

    private void initView() {
        mMtListView = rootView.findViewById(com.hm.cxpay.R.id.mtListView);
        adapter = new AdapterRedEnvelopeReceived(getActivity());
        adapter.setItemClickListener(new AbstractRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Object bean) {
                if (bean instanceof RedEnvelopeItemBean) {
                    RedEnvelopeItemBean b = (RedEnvelopeItemBean) bean;
                    Intent intent = SingleRedPacketDetailsActivity.newIntent(getActivity(), b.getTradeId(), 1);
                    startActivity(intent);
                }
            }
        });
        mMtListView.init(adapter);

        mMtListView.setEvent(new MultiListView.Event() {
            @Override
            public void onRefresh() {
                currentPage = 1;
                getRedEnvelopeDetails();
            }

            @Override
            public void onLoadMore() {
                if (totalCount > 0 && currentPage * 20 >= totalCount) {
                    return;
                }
                currentPage++;
                getRedEnvelopeDetails();
            }

            @Override
            public void onLoadFail() {

            }
        });
    }


    /**
     * 获取收到红包记录
     */
    private void getRedEnvelopeDetails() {
        long startTime = ((RedEnvelopeRecordActivity) getActivity()).getCurrentCalendar();
        PayHttpUtils.getInstance().getRedEnvelopeDetails(currentPage, startTime, 7)
                .compose(RxSchedulers.<BaseResponse<RedDetailsBean>>compose())
                .compose(RxSchedulers.<BaseResponse<RedDetailsBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<RedDetailsBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<RedDetailsBean> baseResponse) {
                        if (baseResponse.isSuccess()) {
                            RedDetailsBean details = baseResponse.getData();
                            if (details != null) {
                                totalCount = details.getTotal();
                                if (((RedEnvelopeRecordActivity) getActivity()).getCurrentTab() == 0) {
                                    ((RedEnvelopeRecordActivity) getActivity()).initDetails(details, true);
                                }
                                if (details.getItems() != null) {
                                    if (currentPage == 1) {
                                        mDataList = details.getItems();
                                    } else {
                                        mDataList.addAll(details.getItems());
                                    }
                                }
                            }
                            adapter.bindData(mDataList);
                            mMtListView.notifyDataSetChange();
                        } else {
                            ToastUtil.show(getContext(), baseResponse.getMessage());
                        }

                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
                        super.onHandleError(baseResponse);
                        ToastUtil.show(getActivity(), baseResponse.getMessage());
                    }
                });
    }

    public void updateDetails() {
        currentPage = 1;
        getRedEnvelopeDetails();
    }


}
