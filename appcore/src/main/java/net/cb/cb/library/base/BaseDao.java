package net.cb.cb.library.base;

/**
 * @author Liszt
 * @date 2019/10/11
 * Description 数据库操作base类
 */
public abstract class BaseDao<T> {
    abstract void updateOrInsert(T t);

    abstract boolean delete(T t);

    abstract T query(Object o);


}
