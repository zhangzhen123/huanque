package com.julun.huanque.common.base

import android.os.Bundle
import android.view.View
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.utils.ULog


/**
 *
 *@author zhangzhen
 *@data 2017/2/28
 * 懒加载的fragment 用于hide show模式 或者 viewpager+fragment主要是延时加载网络请求或耗时操作
 * hide show模式 第一个fragment可能不会懒加载 手动show触发
 *
 **/
abstract class BaseLazyFragment : BaseFragment() {
    var visibleToUser: Boolean = false
    /** Fragment当前状态是否可见  */
    var isPrepared: Boolean = false//是否已经初始化布局文件
    var mHasLoadedOnce: Boolean = false//判断是不是第一次加载
    /**
     * viewpager+fragment会调用
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        ULog.i("setUserVisibleHint:" + isVisibleToUser)
        if (userVisibleHint) {
            visibleToUser = true
            onVisible()
        } else {
            visibleToUser = false
            onInvisible()
        }
    }

    /**
     * hide show模式会调用
     */
    override fun onHiddenChanged(hidden: Boolean) {
        ULog.i("onHiddenChanged:" + hidden)
        if (hidden) {
            visibleToUser = false
            onInvisible()
        } else {
            visibleToUser = true
            onVisible()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isPrepared = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        if (!isHidden) {
//            visibleToUser = true
        lazyLoad()
//        }
    }

    /**
     * 真正去加载数据
     */
    abstract fun lazyLoadData()

    /**
     * 可见
     */
    open fun onVisible() {
        lazyLoad()
    }


    /**
     * 不可见
     */
    private fun onInvisible() {
    }


    /**
     * 延迟加载
     */
    protected fun lazyLoad() {
        if (!isPrepared || !visibleToUser || mHasLoadedOnce) {
            return
        }
        lazyLoadData()
        ULog.i("开始加载数据-------------------------")
        mHasLoadedOnce = true//加载之后改变此标志
    }

}