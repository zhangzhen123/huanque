package com.julun.huanque.common.widgets.cardlib;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by linchen on 2018/2/6.
 * mail: linchen@sogou-inc.com
 */

public interface OnSwipeCardListener<T> {


    void onSwiping(RecyclerView.ViewHolder viewHolder, float dx, float dy, int direction, float ratio);


    void onSwipedOut(RecyclerView.ViewHolder viewHolder, T t, int direction);


    void onSwipedClear();

}
