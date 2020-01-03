package com.hm.cxpay.rx;

import android.content.Context;


import com.hm.cxpay.rx.data.BaseResponse;
import com.hm.cxpay.rx.exeception.ApiException;
import com.hm.cxpay.rx.exeception.ExceptionEngine;

import java.io.IOException;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import retrofit2.adapter.rxjava2.HttpException;

/**
 * Created by Liszt on 2018/4/11
 */
public abstract class FXRxSubscriberHelper<T> extends RxSubscriberHelper<T> {

    String flag;

//    StatePresenter statePresenter;

    public FXRxSubscriberHelper() {
        super();
    }

//    public FXRxSubscriberHelper(StatePresenter statePresenter) {
//        super();
//        this.statePresenter = statePresenter;
//    }

    public FXRxSubscriberHelper(Context context, boolean isShowLoad) {
        super(context, isShowLoad);
    }

    /**
     * @param context
     * @param builder 配置builder
     */
    public FXRxSubscriberHelper(Context context, Builder builder) {
        super(context, builder);
    }

    @Override
    public void onSubscribe(Disposable d) {
        super.onSubscribe(d);
//        if (statePresenter != null) {
//            flag = statePresenter.updateFlag();
//        }
    }

    @Override
    public final void onNext(T t) {
//        if (statePresenter != null && !statePresenter.checkFlag(flag)) {
//            return;
//        }
        super.onNext(t);
    }

    private void autoThrowable(Throwable throwable) {
        /**
         * 输出错误日志
         */
        if (throwable != null) {
            if (throwable instanceof CompositeException) {
                List<Throwable> throwables = ((CompositeException) throwable).getExceptions();
                if (throwables != null) {
                    for (Throwable throwableTemp : throwables) {
                        parseThrowable(throwableTemp);
                    }
                }
            } else {
                parseThrowable(throwable);
            }
        }
    }

    private void parseThrowable(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            try {
                String[] datas = JHttpLoggingInterceptor.getHttpLog(
                        httpException.response().raw().request(),
                        httpException.response().raw(),
                        0
                );
                log(datas);
                // TODO 错误日志收集
            } catch (IOException e) {
//                DLog.e(e, ThrowableUtil.getMessage(e));
            }
        } else {
//            DLog.e(throwable, ThrowableUtil.getMessage(throwable));
        }
    }

    void log(String... messages) {
//        DLog.e(messages);
    }

    @Override
    public void onError(Throwable throwable) {
//        if (statePresenter != null && !statePresenter.checkFlag(flag)) {
//            return;
//        }

//        autoThrowable(throwable);

        ApiException apiException = ExceptionEngine.handleException(throwable);
        boolean normalError = !BaseResponse.OFFLINE.equals("" + apiException.status)
                || !BaseResponse.BACKSTAGECLERAR.equals("" + apiException.status);
        if (isShowMessage && normalError) {
            onShowMessage(apiException);
        }
        if (isShowLoad) {
            onDissLoad();
        }
        /**
         * 升级检查
         */
        if (apiException.object != null &&
                (BaseResponse.SUCCESS_CODE_UPGRADE.equals("" + apiException.status)
                        || BaseResponse.SUCCESS_CODE_UPGRADE_FOUCE.equals("" + apiException.status))) {
            if (onUpgradeError(apiException)) {

            }
            return;
        }
        /**
         * token权限校验 SUCCESS_CODE_ERROR_TOKEN_1
         */
        if (isCheckPermission &&
                (apiException.code == ExceptionEngine.ERROR.PERMISSION_ERROR
                        || apiException.status == ExceptionEngine.ERROR.PERMISSION_ERROR)) {
            onPermissionError(apiException);
            return;
        }
        /**
         * 单点登录
         */
        else if (BaseResponse.OFFLINE.equals("" + apiException.status)) {
            if (onSSOError(apiException)) {

            }
            return;
        }
        /**
         * 其他错误
         */
        else {
            _onError(apiException);
        }
    }

    protected boolean onUpgradeError(ApiException apiException) {
        return true;
    }


    protected boolean onSSOError(ApiException apiException) {
        return true;
    }

    /**
     * 权限失效会自动登出, V1版本权限失效主要依据为 401 {@link ExceptionEngine#handleException(Throwable)}
     * 所有需要验证权限的接口，Authorization token 缺失或校验失败都将触发401 {@link }
     *
     * @param apiException
     */
    @Override
    protected void onPermissionError(ApiException apiException) {
        // TODO 登出
//        AccountManager.getInstance().logout();
    }
}
