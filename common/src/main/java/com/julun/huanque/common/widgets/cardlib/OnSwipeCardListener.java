package com.julun.huanque.common.widgets.cardlib;

import androidx.recyclerview.widget.RecyclerView;

import javax.annotation.Nullable;

/**
 * Created by linchen on 2018/2/6.
 * mail: linchen@sogou-inc.com
 */

public interface OnSwipeCardListener<T> {


    void onSwiping(RecyclerView.ViewHolder viewHolder, float dx, float dy, int direction, float ratio);


    void onSwipedOut(RecyclerView.ViewHolder viewHolder, T t, int direction);

    /**
     * 滑动不可移除 反弹回去的回调
     * @param selected
     */
    void onSwipedOutUnable(@Nullable RecyclerView.ViewHolder selected);

    void onSwipedClear();

}
