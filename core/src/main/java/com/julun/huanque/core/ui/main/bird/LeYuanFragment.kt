package com.julun.huanque.core.ui.main.bird

import android.os.Bundle
import android.view.View
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.core.R

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