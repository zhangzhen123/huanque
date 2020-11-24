package com.julun.huanque.core.ui.main.dynamic_square

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.MainPageIndexConst
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.removeDuplicate
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.DynamicGroupListAdapter
import com.julun.huanque.core.adapter.DynamicListAdapter
import com.julun.huanque.core.manager.VideoPlayerManager
import com.julun.huanque.core.ui.dynamic.DynamicDetailActivity
import com.julun.huanque.core.ui.main.follow.FollowViewModel
import kotlinx.android.synthetic.main.fragment_dynamic_tab.*
import kotlinx.android.synthetic.main.fragment_program_tab.*
import kotlinx.android.synthetic.main.fragment_program_tab.mRefreshLayout
import kotlinx.android.synthetic.main.fragment_program_tab.state_pager_view
import kotlinx.android.synthetic.main.layout_header_dynamic.view.*
import kotlin.random.Random

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/20 17:34
 *
 *@Description: DynamicTabFragment
 *
 */
class DynamicTabFragment : BaseVMFragment<DynamicTabViewModel>() {

    companion object {
        fun newInstance(tab: SquareTab?): DynamicTabFragment {
            return DynamicTabFragment().apply {
                val bundle = Bundle()
                bundle.putSerializable(IntentParamKey.TAB_TYPE.name, tab)
                this.arguments = bundle
            }
        }
    }

    private val followViewModel: FollowViewModel by activityViewModels()

    private var currentTab: SquareTab? = null

    private val headerLayout: View by lazy {
        LayoutInflater.from(requireContext()).inflate(R.layout.layout_header_dynamic, null)
    }
    private val groupAdapter: DynamicGroupListAdapter by lazy {
        DynamicGroupListAdapter()
    }

    override fun getLayoutId(): Int = R.layout.fragment_dynamic_tab

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentTab = arguments?.getSerializable(IntentParamKey.TAB_TYPE.name) as? SquareTab
        initViewModel()


        postList.layoutManager = LinearLayoutManager(requireContext())
        dynamicAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info(" postList.stopScroll()")
            postList.stopScroll()
            logger.info("authorAdapter loadMoreModule 加载更多")
            mViewModel.requestProgramList(QueryType.LOAD_MORE, currentTab?.typeCode)
        }

        headerLayout.groupList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        headerLayout.groupList.adapter = groupAdapter

        dynamicAdapter.addHeaderView(headerLayout)
        dynamicAdapter.headerWithEmptyEnable = true
        postList.adapter = dynamicAdapter

        postList.isNestedScrollingEnabled = false

        dynamicAdapter.setOnItemClickListener { _, _, position ->
            val item = dynamicAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            DynamicDetailActivity.start(requireActivity(), item.postId)
        }
        mRefreshLayout.setOnRefreshListener {
            mViewModel.requestProgramList(QueryType.REFRESH, currentTab?.typeCode)
            followViewModel.requestProgramList(QueryType.REFRESH, isNullOffset = true)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)

    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
    }

    override fun onResume() {
        logger.info("onResume =${currentTab?.typeName}")
        super.onResume()
    }

    override fun onPause() {
        logger.info("onPause =${currentTab?.typeName}")
        super.onPause()
    }

    /**
     * 该方法供父容器在显隐变化时调用
     */
    fun onParentHiddenChanged(hidden: Boolean) {
        logger.info("当前的界面开始显隐=${currentTab?.typeName} hide=$hidden")
    }

    override fun lazyLoadData() {
        mViewModel.requestProgramList(QueryType.INIT, currentTab?.typeCode)
    }

    private fun initViewModel() {
        mViewModel.dataList.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                renderDynamicData(it.requireT())
            } else if (it.state == NetStateType.ERROR) {
                loadDataFail(it.isRefresh())
            }
            mRefreshLayout.isRefreshing = false
        })
    }

    private fun loadDataFail(isPull: Boolean) {
        if (isPull) {
            ToastUtils.show2("刷新失败")
        } else {
            dynamicAdapter.loadMoreModule.loadMoreFail()
        }
    }

    //    private val totalList: MutableList<DynamicItemBean> = mutableListOf()
    private fun renderDynamicData(listData: HomeDynamicListInfo) {

        if (listData.isPull) {
            val programList = listData.postList.distinct()
//            totalList.clear()
//            totalList.addAll(programList)
            dynamicAdapter.setList(programList)
            if (listData.groupList.isNotEmpty()) {
                headerLayout.groupList.show()
                groupAdapter.setList(listData.groupList)
            } else {
                headerLayout.groupList.hide()
            }

        } else {
            val programList = listData.postList.removeDuplicate(dynamicAdapter.data)
//            totalList.addAll(programList)
            dynamicAdapter.addData(programList)
        }
        if (listData.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (listData.postList.isEmpty()) {
                dynamicAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                dynamicAdapter.loadMoreModule.loadMoreComplete()
            }

        } else {
            //防止底部没有边距
            dynamicAdapter.loadMoreModule.loadMoreEnd()

        }
        if (dynamicAdapter.data.isEmpty()) {
            mRefreshLayout.hide()
            state_pager_view.showEmpty(emptyTxt = "没有动态，洗洗睡吧", btnTex = "前往",
                onClick = View.OnClickListener {
                    logger.info("跳转到交友")
                    ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                        .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX).navigation()
                })
        } else {
            state_pager_view.showSuccess()
        }
    }

    //
    fun scrollToTopAndRefresh() {
        postList.scrollToPosition(0)
        postList.postDelayed({
            mViewModel.requestProgramList(QueryType.REFRESH, currentTab?.typeCode)
        }, 100)
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
//                state_pager_view.showSuccess()
                mRefreshLayout.show()
            }
            NetStateType.LOADING -> {//showLoading()
                mRefreshLayout.hide()
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    mViewModel.requestProgramList(QueryType.INIT, currentTab?.typeCode)
                })
            }

        }

    }

    private val dynamicAdapter = DynamicListAdapter()

}