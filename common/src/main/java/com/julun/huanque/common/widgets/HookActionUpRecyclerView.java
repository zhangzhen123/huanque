package com.julun.huanque.common.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.julun.huanque.common.helper.DensityHelper;

public class HookActionUpRecyclerView extends RecyclerView {

    public boolean startScroll = false;
    private float curDownX = 0f;
    private float curDownY = 0f;
    private float scrollMax = DensityHelper.dp2px( 8);

    public HookActionUpRecyclerView(Context context) {
        super(context);
    }

    public HookActionUpRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HookActionUpRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutManager(new LinearLayoutManager(context));
    }

    /**
     * 具体规则查 {@link com.effective.android.panel.view.content.ContentContainerImpl}
     *
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float curX = e.getX();
        float curY = e.getY();
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            startScroll = false;
            curDownX = curX;
            curDownY = curY;
        }

        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            startScroll = Math.abs(curX - curDownX) > scrollMax || Math.abs(curY - curDownY) > scrollMax;
        }

        if (e.getAction() == MotionEvent.ACTION_UP && !startScroll) {
            return false;
        }

        return super.onTouchEvent(e);

    }
}
