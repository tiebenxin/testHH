package net.cb.cb.library.base;

import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * @author Liszt
 * @date 2019/8/10
 * Description mvp Fragment 基类
 */
public abstract class BaseMvpFragment<M extends IModel, V extends IView, P extends BasePresenter> extends Fragment implements BaseMvp<M, V, P> {
    protected P presenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        presenter = createPresenter();
        if (presenter != null) {
            presenter.registerModel(createModel());
            presenter.registerView(createView());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }

}
