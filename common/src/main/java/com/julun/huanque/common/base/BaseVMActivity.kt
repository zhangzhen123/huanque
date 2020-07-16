package com.julun.huanque.common.base

import android.os.Bundle
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
 *@Description: BaseVMActivity 封装一个方便网络状态管理的Activity基类 包含一个自己专有的ViewModel
 *
 */
abstract class BaseVMActivity<VM : BaseViewModel> : BaseActivity() {

    protected lateinit var mViewModel: VM
    override fun onCreate(savedInstanceState: Bundle?) {
        initBaseViewModel()
        super.onCreate(savedInstanceState)

    }

    private fun initBaseViewModel() {

        mViewModel = ViewModelProvider(this).get(CommonUtil.getClass(this))
        mViewModel.loadState.observe(this, observer)

    }

    abstract fun showLoadState(state: NetState)


    private val observer by lazy {
        Observer<NetState> {
            it?.let {
                showLoadState(it)
            }
        }
    }

}