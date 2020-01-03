package com.yanlong.im.pay.ui.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hm.cxpay.net.FGObserver;
import com.hm.cxpay.net.PayHttpUtils;
import com.hm.cxpay.rx.RxSchedulers;
import com.hm.cxpay.rx.data.BaseResponse;
import com.hm.cxpay.bean.GrabEnvelopeBean;
import com.hm.cxpay.bean.OpenEnvelopeBean;
import com.hm.cxpay.widget.RedAmina;
import com.yanlong.im.R;
import com.yanlong.im.chat.bean.RedEnvelopeMessage;
import com.yanlong.im.utils.GlideOptionsUtil;

import net.cb.cb.library.utils.ToastUtil;

public class RedPacketDialog extends DialogFragment {

    private ImageView imgCls;
    private ImageView imgUhead;
    private TextView txtUname;
    private TextView txtRbInfo;
    private ImageView imgOpen;
    private TextView txtMore;

    private int type = 0;
    private String headUrl;
    private String uname;
    private String info;
    private View.OnClickListener onk;
    private GrabEnvelopeBean grabEnvelopeBean;
    private RedEnvelopeMessage envelopeMessage;

    //自动寻找控件
    private void findViews(View rootView) {
        imgCls = rootView.findViewById(R.id.img_cls);
        imgUhead = rootView.findViewById(R.id.img_uhead);
        txtUname = rootView.findViewById(R.id.txt_uname);
        txtRbInfo = rootView.findViewById(R.id.txt_rb_info);
        imgOpen = rootView.findViewById(R.id.img_open);
        txtMore = rootView.findViewById(R.id.txt_more);
    }

    public void setGrabEnvelopeBean(GrabEnvelopeBean grabBean, RedEnvelopeMessage message) {
        grabEnvelopeBean = grabBean;
        envelopeMessage = message;
    }

    private void updateUI(int envelopeStatus) {
        if (envelopeStatus == 1) {//正常，可以抢
            imgOpen.setVisibility(View.VISIBLE);
            if (envelopeMessage != null) {
                txtRbInfo.setText(envelopeMessage.getComment());
            } else {
                txtRbInfo.setText("恭喜发财，大吉大利");
            }
        } else if (envelopeStatus == 2) {//已经领完
            imgOpen.setVisibility(View.GONE);
            txtRbInfo.setText("手慢了，红包已经派完");

        } else if (envelopeStatus == 3) {//已经过期
            imgOpen.setVisibility(View.GONE);
        }
    }


    //自动生成的控件事件
    private void initEvent() {
        imgCls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        //  imgUhead.setImageURI(Uri.parse(headUrl));
        Glide.with(this).load(headUrl)
                .apply(GlideOptionsUtil.headImageCircleCropOptions()).into(imgUhead);
        txtUname.setText(uname);

        if (type == 0) {
            txtRbInfo.setText(info);
            imgOpen.setVisibility(View.VISIBLE);
            imgOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playAnim(onk);
                }
            });
            txtMore.setVisibility(View.GONE);
        } else {
            txtMore.setVisibility(View.VISIBLE);
            imgOpen.setVisibility(View.GONE);
            txtMore.setOnClickListener(onk);
        }


    }

    public void show4open(FragmentManager manager, String headUrl, String uname, String info, final View.OnClickListener onk) {
        type = 0;
        this.headUrl = headUrl;
        this.uname = uname;
        this.info = info;
        this.onk = onk;
        if (this.isAdded()) {
            initEvent();
        } else {
            show(manager, "redTag");
        }


    }

    public void show4opened(FragmentManager manager, String headUrl, String uname, String info, final View.OnClickListener onkMore) {
        type = 1;
        this.headUrl = headUrl;
        this.uname = uname;
        this.info = info;
        this.onk = onkMore;
        if (this.isAdded()) {
            initEvent();
        } else {
            show(manager, "redTag");
        }

    }

    /***
     * 动画处理
     */
    private void playAnim(final View.OnClickListener onk) {
        // ObjectAnimator
        RedAmina amina = new RedAmina();

        imgOpen.startAnimation(amina);

        amina.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onk.onClick(imgOpen);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //  amina.onCreate();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fgm_redpacket_dialog, container, false);
        this.setCancelable(false);
        findViews(v);
        initEvent();
        return v;
    }

    //拆红包，获取token
    public void openRedEnvelope() {
        if (grabEnvelopeBean == null || envelopeMessage == null) {
            return;
        }
        PayHttpUtils.getInstance().openRedEnvelope(envelopeMessage.getTraceId(), grabEnvelopeBean.getAccessToken())
                .compose(RxSchedulers.<BaseResponse<OpenEnvelopeBean>>compose())
                .compose(RxSchedulers.<BaseResponse<OpenEnvelopeBean>>handleResult())
                .subscribe(new FGObserver<BaseResponse<OpenEnvelopeBean>>() {
                    @Override
                    public void onHandleSuccess(BaseResponse<OpenEnvelopeBean> baseResponse) {
                        if (baseResponse.isSuccess()) {
                            OpenEnvelopeBean bean = baseResponse.getData();
                            if (bean != null) {
                            }
                        } else {
                            ToastUtil.show(getContext(), baseResponse.getMessage());
                        }

                    }

                    @Override
                    public void onHandleError(BaseResponse baseResponse) {
                        super.onHandleError(baseResponse);
                        if (baseResponse.getCode() == -21000) {
                        } else {
                            ToastUtil.show(getContext(), baseResponse.getMessage());
                        }
                    }
                });
    }
}
