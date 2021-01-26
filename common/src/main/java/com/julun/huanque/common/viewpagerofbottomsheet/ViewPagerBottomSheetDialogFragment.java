package com.julun.huanque.common.viewpagerofbottomsheet;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.julun.huanque.common.R;


/**
 * create by Jiang HongLi
 * create on 2019/1/9 0009
 * description 用于viewpager+fragment+recyclerview的bottomsheetdialog
 */
public abstract class ViewPagerBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public FrameLayout bottomSheet;
    private ViewPagerBottomSheetBehavior<FrameLayout> behavior;
    protected View mRootView;
    protected Activity mActivity;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        return new ViewPagerBottomSheetDialog(getContext(), R.style.basedialog_anim_style);
        return new ViewPagerBottomSheetDialog(getContext());
    }

    public void setCanDrag(boolean canDrag) {
        getDialog().setCancelable(canDrag);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(setLayoutId(), container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initView();
        setListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewPagerBottomSheetDialog dialog = (ViewPagerBottomSheetDialog) getDialog();

        bottomSheet = (FrameLayout) dialog.getDelegate().findViewById(R.id.design_bottom_sheet);
        behavior = ViewPagerBottomSheetBehavior.from(bottomSheet);

        // 重写返回键 用于回退
        DialogInterface.OnKeyListener keylistener = new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    return onKeyDown(dialog, keyCode, event);
                }
                return false;

            }
        };

        getDialog().setOnKeyListener(keylistener);
    }

    /**
     * viewpager页面改变了调用 解决不能刷新Fagment页面 造成Recyclerview不能滑动的bug。
     *
     * @param viewPager
     */
    public void onPageChange(ViewPager viewPager) {
        if (viewPager != null && behavior != null) {
            viewPager.post(new Runnable() {
                @Override
                public void run() {
                    behavior.updateScrollingChild();
                }
            });
        }

    }

    /**
     * 用于返回键使用
     *
     * @param dialog
     * @param keyCode
     * @param event
     * @return
     */
    protected boolean onKeyDown(DialogInterface dialog, int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected abstract int setLayoutId();
    /**
     * 初始化数据
     */
    protected void initData() {

    }

    /**
     * view与数据绑定
     */
    protected void initView() {

    }

    /**
     * 设置监听
     */
    protected void setListener() {

    }
}
