package com.julun.huanque.core.ui.main.makefriend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.SingleFamousMonth
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.FlowerFamousMonthAdapter
import com.julun.huanque.core.viewmodel.PlumFlowerViewModel
import kotlinx.android.synthetic.main.fragment_famous_list.*
import kotlinx.android.synthetic.main.fragment_famous_list.recyclerView
import kotlinx.android.synthetic.main.fragment_famous_list.statePage

/**
 *@创建者   dong
 *@创建时间 2020/8/24 16:13
 *@描述 名人榜
 */
class FamousListFragment : BaseFragment() {
    //花魁ViewModel
    private val mViewModel: PlumFlowerViewModel by viewModels<PlumFlowerViewModel>()

    private val mAdapter = FlowerFamousMonthAdapter()

    override fun getLayoutId() = R.layout.fragment_famous_list

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        initRecyclerView()
        mViewModel.getFamousList()
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {

        mViewModel.loadState.observe(this, Observer {
            swipeLayout.isRefreshing = false
            when (it.state) {
                NetStateType.SUCCESS -> {
                }
                NetStateType.ERROR -> {
                    statePage.showError()
                }
            }
        })

        mViewModel.famousListData.observe(this, Observer {
            if (it != null) {
                if (it.inRank == BusiConstant.True) {
                    //本人在名人榜
                    tv_ranking.text = "恭喜你荣登名人榜"
                } else {
                    //本人不在名人榜
                    tv_ranking.text = "很遗憾你没有入榜"
                }

                val tempData = mutableListOf<SingleFamousMonth>()
                tempData.addAll(it.monthList)
                tempData.addAll(it.monthList)
                mAdapter.setList(tempData)
            }
        })
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        swipeLayout.setOnRefreshListener {
            mViewModel.getFamousList()
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter
    }
}