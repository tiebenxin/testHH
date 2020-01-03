package net.cb.cb.library.base;

/**
 * @author Liszt
 * @date 2019/8/10
 * Description mvp interface presenter
 */
public interface IPresenter<M extends IModel, V extends IView> {

    /**
     * 注册Model层
     *
     * @param model
     */
    void registerModel(M model);

    /**
     * 注册View层
     *
     * @param view
     */
    void registerView(V view);

    /**
     * 获取View
     *
     * @return
     */
    V getView();

    /**
     * 销毁动作（如Activity、Fragment的卸载）
     */
    void onDestroy();

    /**
     * create动作（如Activity、Fragment的卸载）
     */
    void onCreate();

}
