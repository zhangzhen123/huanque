package com.julun.huanque.core.ui.homepage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.CircleGroup
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.AttentionCircleAdapter
import com.julun.huanque.core.viewmodel.AttentionCircleViewModel
import kotlinx.android.synthetic.main.act_home_page.*
import kotlinx.android.synthetic.main.fragment_attention_circle.*
import kotlinx.android.synthetic.main.fragment_attention_circle.state_pager_view

/**
 *@创建者   dong
 *@创建时间 2020/11/24 10:20
 *@描述 关注圈子Fragment
 */
class AttentionCircleFragment : BaseFragment() {
    companion object {
        fun newInstance() = AttentionCircleFragment()
    }

    private var mAttentionCircleAdapter = AttentionCircleAdapter()

    //关注列表的ViewModel
    private val mAttentionCircleViewModel: AttentionCircleViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_attention_circle

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        state_pager_view.showLoading()
        initRecyclerView()
        initViewModel()
        mAttentionCircleViewModel.getCircleGroupInfo()

        swipeRefreshLayout.setOnRefreshListener {
            mAttentionCircleViewModel.getCircleGroupInfo()
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAttentionCircleAdapter

        mAttentionCircleAdapter.setOnItemChildClickListener { adapter, view, position ->
            val tempData = adapter.getItemOrNull(position)
            if (tempData is CircleGroup) {
                mAttentionCircleViewModel.groupJoin(tempData.groupId)
            }
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mAttentionCircleViewModel.myGroupData.observe(this, Observer {
            if (it != null) {
                val circleList = mutableListOf<Any>()
                mAttentionCircleAdapter.setList(null)
                val myList = it.myGroup
                if (myList.isEmpty()) {
                    //我加入的圈子为空
                    circleList.add(AttentionCircleAdapter.Circle_Mine_Attention_Empty)
                } else {
                    circleList.addAll(myList)
                }
                val recommendList = it.recommendGroup
                if (recommendList.isNotEmpty()) {
                    circleList.add(AttentionCircleAdapter.Title_Recommend_Circle)
                    circleList.addAll(recommendList)
                }
                mAttentionCircleAdapter.setList(circleList)
            }
        })
        mAttentionCircleViewModel.loadState.observe(this, Observer {
            if (it != null) {
                swipeRefreshLayout.isRefreshing = false
                when (it.state) {
                    NetStateType.SUCCESS -> {
                        state_pager_view.showSuccess()
                    }
                    NetStateType.NETWORK_ERROR -> {
                        state_pager_view.showError()
                    }
                    NetStateType.LOADING -> {
//                        state_pager_view.showLoading()
                    }


                }
            }
        })
    }

}