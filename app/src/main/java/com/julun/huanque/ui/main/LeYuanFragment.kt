package com.julun.huanque.ui.main

import android.os.Bundle
import android.view.View
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseFragment

/**
 *@创建者   dong
 *@创建时间 2020/6/29 18:06
 *@描述 乐园Fragment
 */
class LeYuanFragment : BaseFragment() {
    companion object{
        fun newInstance() = LeYuanFragment()
    }
    override fun getLayoutId() = R.layout.fragment_leyuan

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
    }
}