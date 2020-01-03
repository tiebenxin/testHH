package com.hm.cxpay.rx;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;


import com.hm.cxpay.R;
import com.hm.cxpay.rx.exeception.ApiException;
import com.hm.cxpay.rx.exeception.ExceptionEngine;
import com.hm.cxpay.rx.view.DialogHelper;
import com.hm.cxpay.rx.view.IBaseView;

import net.cb.cb.library.AppConfig;
import net.cb.cb.library.utils.LogUtil;

import java.lang.ref.WeakReference;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class RxSubscriberHelper<T> implements Observer<T> {

    public static class Builder<E> {

        boolean isShowMessage = true;
        boolean isCheckPermission = true;
        boolean isShowLoad = false;
        IBaseView view;

        public void setView(IBaseView view) {
            this.view = view;
        }

        public Builder setShowMessage(boolean showMessage) {
            isShowMessage = showMessage;
            return this;
        }

        public Builder setCheckPermission(boolean checkPermission) {
            isCheckPermission = checkPermission;
            return this;
        }

        public Builder setShowLoad(boolean showLoad) {
            isShowLoad = showLoad;
            return this;
        }
    }

    /**
     * 错误是否弹出错误toast，默认是{@link Builder#isShowMessage}
     */
    protected boolean isShowMessage = true;
    /**
     * 是否自动拦截权限类错误，默认是{@link Builder#isCheckPermission}
     */
    protected boolean isCheckPermission = true;
    /**
     * 是否显示加载，默认不显示{@link Builder#isShowLoad}
     */
    protected boolean isShowLoad = false;

    private final WeakReference<Context> context;

    protected IBaseView view;

    public RxSubscriberHelper() {
        context = null;
    }

    /**
     * @param context
     * @param isShowLoad 是否显示load
     */
    public RxSubscriberHelper(Context context, boolean isShowLoad) {
        this.isShowLoad = isShowLoad;
        this.context = new WeakReference<>(context);
    }

    /**
     * @param context
     * @param builder 配置builder
     */
    public RxSubscriberHelper(Context context, Builder builder) {
        this.context = new WeakReference<>(context);
        if (builder != null) {
            isShowMessage = builder.isShowMessage;
            isCheckPermission = builder.isCheckPermission;
            isShowLoad = builder.isShowLoad;
            view = builder.view;
        }
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (isShowLoad) {
            onShowLoading();
        }
    }

    @Override
    public void onComplete() {
        if (isShowLoad) {
            onDissLoad();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable != null) {
            LogUtil.getLog().e(throwable.getMessage(), throwable.getClass().getSimpleName());
        }
        ApiException apiException = ExceptionEngine.handleException(throwable);
        if (isCheckPermission && apiException.code == ExceptionEngine.ERROR.PERMISSION_ERROR) {
            onPermissionError(apiException);
        } else {
            if (isShowMessage) {
                onShowMessage(apiException);
            }
            _onError(apiException);
        }
    }

    @Override
    public void onNext(T t) {
        _onNext(t);
    }

    public abstract void _onNext(T t);

    public void _onError(ApiException error) {
        if (isShowLoad) {
            onDissLoad();
        }
        if (view != null) {
            view.onLoadedError(error);
        }
    }

    /**
     * 显示加载loading
     */
    protected void onShowLoading() {
        if (context != null) {
            DialogHelper.getInstance().showLodingDialog(context.get());
        }
    }

    /**
     * 关闭加载loading
     */
    protected void onDissLoad() {
        DialogHelper.getInstance().dissLodingDialog();
    }

    /**
     * 权限失效会自动登出, V1版本权限失效主要依据为 401 {@link ExceptionEngine#handleException(Throwable)}
     * 所有需要验证权限的接口，Authorization token 缺失或校验失败都将触发401 {@link }
     *
     * @param apiException
     */
    protected void onPermissionError(ApiException apiException) {
    }

    /**
     * 任何类型的错误都会弹出Toast,重写方式修改
     *
     * @param apiException
     */
    protected void onShowMessage(ApiException apiException) {
        if (apiException != null && !TextUtils.isEmpty(apiException.message)) {
            Toast.makeText(AppConfig.getContext(), apiException.message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AppConfig.getContext(), AppConfig.getContext().getString(R.string.common_net_error_default), Toast.LENGTH_SHORT).show();
        }
    }
}
