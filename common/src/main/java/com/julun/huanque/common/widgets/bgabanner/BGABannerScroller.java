package com.julun.huanque.common.widgets.bgabanner;

import android.content.Context;
import android.widget.Scroller;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:15/6/19 下午11:59
 * 描述:
 */
public class BGABannerScroller extends Scroller {
    private int mDuration = 1000;
    public boolean noDuration;
    public BGABannerScroller(Context context, int duration) {
        super(context);
        mDuration = duration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        if(noDuration){
            //界面滑动不需要时间间隔
            super.startScroll(startX, startY, dx, dy, 0);
        }else {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        if(noDuration){
            //界面滑动不需要时间间隔
            super.startScroll(startX, startY, dx, dy, 0);
        }else {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }
}