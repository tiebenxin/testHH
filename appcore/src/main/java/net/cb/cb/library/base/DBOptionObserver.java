package net.cb.cb.library.base;

import io.reactivex.observers.DefaultObserver;

/**
 * @author Liszt
 * @date 2019/8/10
 * Description  数据库操作观察者
 */

public abstract class DBOptionObserver<T> extends DefaultObserver<T> {


    @Override
    public void onNext(T t) {
        onOptionSuccess(t);
    }

    @Override
    public void onError(Throwable e) {
        onOptionFailed(e);
    }

    @Override
    public void onComplete() {
    }

    //数据库操作失败
    public abstract void onOptionSuccess(T t);

    //数据库操作成功
    public void onOptionFailed(Throwable e) {
        e.printStackTrace();
    }


}
