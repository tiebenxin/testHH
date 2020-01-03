package net.cb.cb.library.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Liszt on 2019/9/18.
 */

public abstract class AbstractViewHolder<T> extends RecyclerView.ViewHolder {


    public AbstractViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bindHolder(T bean);

}