package com.julun.huanque.message.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.bean.forms.WatchForm
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.message.R
import com.julun.huanque.message.activity.PrivateConversationActivity
import com.julun.huanque.message.adapter.WatchAdapter
import com.julun.huanque.message.viewmodel.WatchHistoryViewModel
import kotlinx.android.synthetic.main.frag_watch.*

/**
 *@创建者   dong
 *@创建时间 2021/1/18 20:11
 *@描述 访问历史
 */
class WatchFragment : BaseFragment() {
    companion object {
        fun newInstance(type: String): WatchFragment {
            val fragment = WatchFragment()
            fragment.arguments = Bundle().apply { putString(ParamConstant.TYPE, type) }
            return fragment
        }
    }

    private val mViewModel: WatchHistoryViewModel by viewModels()

    //需要刷新的标记位
    private var mNeedRefresh = false

    private val mAdapter = WatchAdapter()
    private var mType = ""
    override fun getLayoutId() = R.layout.frag_watch

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mType = arguments?.getString(ParamConstant.TYPE) ?: ""
        if (mType != WatchForm.HaveSeen) {
            mAdapter.needBlur = true
        }
        initRecyclerView()
        initViewModel()
        mViewModel.queryData(mType, true)

        swipe_refresh.setOnRefreshListener {
            mViewModel.queryData(mType, true)
        }
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        cardView.onClickNew {
            val touchEype = mViewModel.watchExtra.value?.touchType ?: ""
//            jump(touchEype)
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.adapter = mAdapter
        mAdapter.setEmptyView(MixedHelper.getEmptyView(requireContext(), "暂无数据"))
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            val tempHistoryBean = mAdapter.getItemOrNull(position) ?: return@setOnItemChildClickListener
            when (view.id) {
                R.id.tv_private -> {
                    //私信
                    PrivateConversationActivity.newInstance(
                        requireActivity(),
                        tempHistoryBean.userId,
                        tempHistoryBean.nickName,
                        headerPic = tempHistoryBean.headPic
                    )
                }
                R.id.sdv_header -> {
                    //主页
                    val maxCount = mViewModel.watchExtra.value?.viewNum ?: 0
                    if (!mAdapter.needBlur || maxCount == -1 || position < maxCount) {
                        val bundle = Bundle().apply {
                            putLong(ParamConstant.UserId, tempHistoryBean.userId)
                        }
                        ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
                    }
                }
            }

        }

        mAdapter.loadMoreModule.setOnLoadMoreListener {
            mViewModel.queryData(mType)
        }
    }


    override fun onResume() {
        super.onResume()
        if (mNeedRefresh) {
            mViewModel.refreshGuide()
            mNeedRefresh = false
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel.historyData.observe(this, Observer {
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

        mViewModel.watchExtra.observe(this, Observer {
            if (it != null) {
                mAdapter.maxCount = it.viewNum
                mAdapter.notifyDataSetChanged()
                if (it.guideText.isEmpty()) {
                    cardView.hide()
                } else {
                    cardView.show()
                    tv_attention.text = it.guideText
                }
            }
        })
    }

}