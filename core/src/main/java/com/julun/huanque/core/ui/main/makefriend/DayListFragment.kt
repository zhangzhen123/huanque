package com.julun.huanque.core.ui.main.makefriend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.PlumFlowerViewModel

/**
 *@创建者   dong
 *@创建时间 2020/8/21 20:14
 *@描述 日榜Fragment
 */
class DayListFragment(val type: String) : BaseFragment() {

    companion object {
        //今日
        val TODAY = "TODAY"

        //昨日
        val YESTERDAY = "YESTERDAY"
    }

    //花魁ViewModel
    private val mViewModel: PlumFlowerViewModel by activityViewModels<PlumFlowerViewModel>()

    override fun getLayoutId() = R.layout.fragment_day_list

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        if (type == TODAY) {
            mViewModel.getToadyList()
        } else if (type == YESTERDAY) {
            mViewModel.getYesterdayList()
        }


    }

    private fun initViewModel() {
        mViewModel.listData.observe(this, Observer {
            if (it != null) {
                //显示数据
            }
        })
    }
}