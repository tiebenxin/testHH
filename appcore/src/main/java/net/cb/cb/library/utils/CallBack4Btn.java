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
public abstract class CallBack4Btn<T> extends CallBack<T> {
    public CallBack4Btn(MultiListView listView) {
        super(listView);
    }

    public CallBack4Btn(View btnView) {
        super(btnView);
    }
    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        super.onResponse(call, response);
        onResp(call,response);
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        super.onFailure(call, t);
        onFail(call, t);
    }

    public abstract void onResp(Call<T> call, Response<T> response);
    public void onFail(Call<T> call, Throwable t) {

    };





}
