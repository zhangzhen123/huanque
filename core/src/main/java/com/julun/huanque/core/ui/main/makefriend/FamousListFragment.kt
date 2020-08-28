package com.julun.huanque.core.ui.main.makefriend

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.SingleFamousMonth
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.FlowerFamousMonthAdapter
import com.julun.huanque.core.viewmodel.PlumFlowerViewModel
import kotlinx.android.synthetic.main.fragment_famous_list.*
import kotlinx.android.synthetic.main.fragment_famous_list.recyclerView
import kotlinx.android.synthetic.main.fragment_famous_list.statePage
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textSizeDimen

/**
 *@创建者   dong
 *@创建时间 2020/8/24 16:13
 *@描述 名人榜
 */
class FamousListFragment : BaseFragment() {
    //花魁ViewModel
    private val mViewModel: PlumFlowerViewModel by viewModels<PlumFlowerViewModel>()

    private val mAdapter = FlowerFamousMonthAdapter()

    //头部
    private var mHeaderTextView: TextView? = null

    override fun getLayoutId() = R.layout.fragment_famous_list

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        initRecyclerView()
        statePage.showLoading()
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
                NetStateType.NETWORK_ERROR -> {
                    statePage.showError(btnClick = View.OnClickListener {
                        statePage.showLoading()
                        mViewModel.getFamousList()
                    })
                }
            }
        })

        mViewModel.famousListData.observe(this, Observer {
            if (it != null) {
                if (it.inRank == BusiConstant.True) {
                    //本人在名人榜
                    mHeaderTextView?.text = "恭喜你荣登名人榜"
                } else {
                    //本人不在名人榜
                    mHeaderTextView?.text = "很遗憾你没有入榜"
                }

//                val tempData = mutableListOf<SingleFamousMonth>()
//                tempData.addAll(it.monthList)
//                tempData.addAll(it.monthList)
//                tempData.addAll(it.monthList)
//                tempData.addAll(it.monthList)
//                tempData.addAll(it.monthList)
//                tempData.addAll(it.monthList)
                if (it.monthList.isNotEmpty()) {
                    statePage.showSuccess()
                }
                mAdapter.setList(it.monthList)
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
        mHeaderTextView = TextView(context).apply {
            textColor = GlobalUtils.formatColor("#FF5757")
            textSizeDimen = R.dimen.sp_12
            gravity = Gravity.CENTER
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter
        mHeaderTextView?.let {
            mAdapter.addHeaderView(it)
            val params = it.layoutParams
            params?.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.setPadding(0, dp2px(10), 0, dp2px(10))
            it.layoutParams = params
        }
    }
}