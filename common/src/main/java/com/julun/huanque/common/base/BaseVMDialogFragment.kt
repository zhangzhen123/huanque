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
 *@Description: BaseVMDialogFragment 封装一个方便网络状态管理的DialogFragment基类 包含一个自己专有的ViewModel
 *
 */
abstract class BaseVMDialogFragment<VM : BaseViewModel> : BaseDialogFragment() {

    protected lateinit var mViewModel: VM
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initBaseViewModel()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initBaseViewModel() {

        mViewModel = ViewModelProvider(this).get(CommonUtil.getClass(this))

        mViewModel.loadState.observe(viewLifecycleOwner, observer)

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