package com.julun.huanque.common.base


/**
 *
 *@author zhangzhen
 *@data 2017/2/28
 * 懒加载的fragment Androidx的懒加载简单了许多 只需要处理onResume
 *
 **/
abstract class BaseLazyFragment : BaseFragment() {

    private var mHasLoadedOnce: Boolean = false//判断是不是第一次加载

    override fun onResume() {
        super.onResume()
        lazyLoad()
    }

    /**
     * 真正去加载数据
     */
    abstract fun lazyLoadData()


    /**
     * 延迟加载
     */
    private fun lazyLoad() {
        if (mHasLoadedOnce) {
            return
        }
        lazyLoadData()
        logger.info("开始加载数据-------------------------")
        mHasLoadedOnce = true//加载之后改变此标志
    }

}