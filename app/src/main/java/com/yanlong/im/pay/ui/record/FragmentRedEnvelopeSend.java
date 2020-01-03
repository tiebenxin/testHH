package com.yanlong.im.pay.ui.record;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hm.cxpay.R;
import com.hm.cxpay.bean.RedDetailsBean;
import com.hm.cxpay.bean.RedEnvelopeItemBean;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;

import net.cb.cb.library.base.AbstractRecyclerAdapter;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.MultiListView;

import java.util.ArrayList;
import java.util.List;

//发出的红包
public class FragmentRedEnvelopeSend extends Fragment {
    private View rootView;
    private MultiListView mMtListView;
    int currentPage = 1;
    private AdapterRedEnvelopeSend adapter;
    private List<RedEnvelopeItemBean> mDataList;
    private long totalCount;

    public FragmentRedEnvelopeSend() {

    }


    public static FragmentRedEnvelopeSend newInstance() {
        FragmentRedEnvelopeSend fragment = new FragmentRedEnvelopeSend();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fgm_red_packet_record, null);
        initView();
        getRedEnvelopeDetails();
        return rootView;
    }

    private void initView() {
        mMtListView = rootView.findViewById(R.id.mtListView);
        adapter = new AdapterRedEnvelopeSend(getContext());
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
        PayHttpUtils.getInstance().getRedEnvelopeDetails(currentPage, startTime, 2)
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
                        if (currentPage > 1) {
                            currentPage--;
                        }
                    }
                });
    }

    //时间更新
    public void updateDetails() {
        currentPage = 1;
        getRedEnvelopeDetails();
    }


}
