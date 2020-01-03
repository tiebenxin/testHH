package net.cb.cb.library.utils;

import android.view.View;

import net.cb.cb.library.AppConfig;
import net.cb.cb.library.R;
import net.cb.cb.library.view.MultiListView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/***
 * 统一处理CallBack的错误处理
 *
 * @author jyj
 * @date 2016/12/23
 */
public abstract class CallBack<T> implements Callback<T> {
    MultiListView listView;
    View btnView;

    public CallBack() {
    }

    public CallBack(MultiListView listView) {
        this.listView = listView;
    }

    public CallBack(View btnView) {
        this.btnView = btnView;
        btnView.setEnabled(false);
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (btnView != null) {
            btnView.setEnabled(true);
        }

    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        LogUtil.getLog().e("==响应异常=解析异常==" + t.getMessage());
        if (listView == null) {
            ToastUtil.show(AppConfig.APP_CONTEXT, R.string.app_link_err);
        }

        if (listView != null) {
            listView.getLoadView().setStateNoNet(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 处理空指针问题
                    if (listView.getEvent() != null) {
                        listView.getEvent().onLoadFail();
                    }
                }
            });

        }
        if (btnView != null) {
            btnView.setEnabled(true);
        }

        if (t != null)
            t.printStackTrace();
    }

}
