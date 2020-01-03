package net.cb.cb.library.utils;

import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Response;

/***
 *
 *
 * @author jyj
 * @date 2017/12/18
 */
public class SynCallBackUtil<T> {
    private CountDownLatch signal = new CountDownLatch(1);
    private Response resp;
    private CallBack callBack = new CallBack() {
        @Override
        public void onResponse(Call call, Response response) {
            resp = response;
            signal.countDown();
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            signal.countDown();
        }
    };

    public CallBack<T> getCallBack() {

        return callBack;
    }

    public Response<T> getResponse() {
        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return resp;
    }
}
