package com.julun.huanque.common.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.utils.CommonUtil


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/1 10:09
 *
 *@Description: BaseVMFragment 封装一个方便网络状态管理的Fragment基类 包含一个自己专有的ViewModel
 * 支持懒加载
 *
 */
abstract class BaseVMFragment<VM : BaseViewModel> : BaseFragment() {

    private var mHasLoadedOnce: Boolean = false//判断是不是第一次加载
    protected lateinit var mViewModel: VM
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initBaseViewModel()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        lazyLoad()
    }
    private fun initBaseViewModel() {

        mViewModel = ViewModelProvider(this).get(CommonUtil.getClass(this))

        mViewModel.loadState.observe(viewLifecycleOwner, observer)

    }

    abstract fun showLoadState(state: NetState)

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
    private val observer by lazy {
        Observer<NetState> {
            it?.let {
                showLoadState(it)
            }
        }
    }

}