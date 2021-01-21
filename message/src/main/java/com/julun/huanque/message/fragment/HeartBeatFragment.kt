package com.julun.huanque.message.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.message.R
import com.julun.huanque.message.activity.PrivateConversationActivity
import com.julun.huanque.message.adapter.HeartBeatAdapter
import com.julun.huanque.message.adapter.WatchAdapter
import com.julun.huanque.message.viewmodel.HeartBeatViewModel
import com.julun.huanque.message.viewmodel.WatchHistoryViewModel
import kotlinx.android.synthetic.main.frag_watch.*

/**
 *@创建者   dong
 *@创建时间 2021/1/18 20:11
 *@描述 访问历史
 */
class HeartBeatFragment : BaseFragment() {
    companion object {
        fun newInstance(type: String): HeartBeatFragment {
            val fragment = HeartBeatFragment()
            fragment.arguments = Bundle().apply { putString(ParamConstant.TYPE, type) }
            return fragment
        }
    }

    private val mViewModel: HeartBeatViewModel by viewModels()

    private val mAdapter = HeartBeatAdapter()
    private var mType = ""
    override fun getLayoutId() = R.layout.frag_heart_beat

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mType = arguments?.getString(ParamConstant.TYPE) ?: ""
        initRecyclerView()
        initViewModel()
        mViewModel.queryData(mType, true)

        swipe_refresh.setOnRefreshListener {
            mViewModel.queryData(mType, true)
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.adapter = mAdapter
        mAdapter.setEmptyView(MixedHelper.getEmptyView(requireContext(), "暂无数据"))

        mAdapter.loadMoreModule.setOnLoadMoreListener {
            mViewModel.queryData(mType)
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel.heartBeatData.observe(this, Observer {
            if (it != null) {
                swipe_refresh.isRefreshing = false
                if (it.isPull) {
                    //刷新
                    mAdapter.setList(it.list)
                } else {
                    //加载更多
                    mAdapter.addData(it.list)
                }
                if (it.hasMore) {
                    mAdapter.loadMoreModule.loadMoreComplete()
                } else {
                    mAdapter.loadMoreModule.loadMoreEnd()
                }
            }
        })
    }

}