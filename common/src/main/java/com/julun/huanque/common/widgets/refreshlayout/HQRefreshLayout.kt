package com.julun.huanque.common.widgets.refreshlayout

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import com.julun.huanque.common.R
import com.julun.huanque.common.widgets.svgaView.SVGAPlayerView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable

import java.util.concurrent.TimeUnit


/**
 * Created by dong on 2018/3/29.
 */
class HuanQueRefreshLayout(context: Context, attrs: AttributeSet?) : SuperSwipeRefreshLayout(context, attrs) {
    private var svga: SVGAPlayerView? = null
    private var isStart = false
    private var disposable: Disposable? = null
    var onRefreshListener: RefreshListener? = null

    init {
        //不需要加载更多
        setLoadMore(false)
        setHeaderViewBackgroundColor(Color.TRANSPARENT)
        // add headerView
        createHeaderView()
//        setHeaderView(createHeaderView())
        setHeaderViewBackgroundDrawable(resources.getDrawable(R.drawable.bitmap_bg_refresh_header))
        isTargetScrollWithLayout = true
        setOnPullRefreshListener(object : OnPullRefreshListener {

            override fun onRefresh() {
                if (!isStart) {
                    svga?.startAnimation()
                    isStart = true
                }
                onRefreshListener?.onRefresh()
//                Handler().postDelayed({ setRefreshing(false) }, 5000)
            }

            override fun onPullDistance(distance: Int) {
                // pull distance
            }

            override fun onPullEnable(enable: Boolean) {
                if (enable) {
                    if (!isStart) {
                        svga?.startAnimation()
                        isStart = true
                    }
                } else {
                    svga?.stepToFrame(0, false)
                    isStart = false
                }
            }
        })
    }

    fun setRefreshFinish() {
        disposable = Flowable.timer(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    isRefreshing = false
                    svga?.stepToFrame(1, false)
                    disposable?.dispose()
                }
        isStart = false
    }

    fun setRefreshing() {
        //设置开始刷新
        super.setRefreshing(true)
        if (!isStart) {
            svga?.startAnimation()
            isStart = true
        }
    }

    fun getRefreshState() = isRefreshing

    private fun createHeaderView() {
        //因为直接inflate有几步耗时操作：1、io加载xml文件。2、递归解析XML。3、通过反射去创建view。
        //在界面创建时这很影响性能和用户体验，所以优化使用系统API异步加载view。
        //AsyncLayoutInflater的问题，一次最多允许10个线程同时执行
        //二是不支持设置 LayoutInflater.Factory 或者 LayoutInflater.Factory2
        AsyncLayoutInflater(context).inflate(R.layout.view_layout_header_svga, null) { view, _, _ ->
            svga = view?.findViewById(R.id.svga)
            svga?.stepToFrame(1, false)
            setHeaderView(view)
        }
//        val headerView = LayoutInflater.from(context)
//                .inflate(R.layout.view_layout_header_svga, null)
//        svga = headerView.findViewById(R.id.svga)
//        svga?.stepToFrame(1, false)
//        return headerView
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isRefreshing = false
        svga?.stepToFrame(1, false)
        disposable?.dispose()
    }

}

interface RefreshListener {
    //开始刷新
    fun onRefresh()
}