package com.julun.huanque.core.ui.main.program

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.listener.GridSpanSizeLookup
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.ProgramAdapter
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.main.follow.FollowViewModel
import kotlinx.android.synthetic.main.fragment_program_tab.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/10/29 9:48
 *
 *@Description: ProgramTabFragment 节目单 根据tab的类型展示不同的主播
 *
 */
class ProgramTabFragment : BaseVMFragment<ProgramTabViewModel>() {

    companion object {
        const val BANNER_POSITION = 6
        fun newInstance(tab: ProgramTab?): ProgramTabFragment {
            return ProgramTabFragment().apply {
                val bundle = Bundle()
                bundle.putSerializable(IntentParamKey.TAB_TYPE.name, tab)
                this.arguments = bundle
            }
        }
    }

    private val followViewModel: FollowViewModel by activityViewModels()

    private var currentTab: ProgramTab? = null
    override fun getLayoutId(): Int = R.layout.fragment_program_tab

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentTab = arguments?.getSerializable(IntentParamKey.TAB_TYPE.name) as? ProgramTab

        authorList.layoutManager = GridLayoutManager(requireContext(), 2)
        initViewModel()


        authorAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info(" authorList.stopScroll()")
            authorList.stopScroll()
            logger.info("authorAdapter loadMoreModule 加载更多")
            mViewModel.requestProgramList(QueryType.LOAD_MORE, currentTab?.typeCode)
        }
        authorAdapter.setGridSpanSizeLookup(GridSpanSizeLookup { gridLayoutManager, viewType, position ->
            return@GridSpanSizeLookup when (viewType) {
                ProgramItemType.BANNER -> 2
                else -> 1
            }
        })
        authorList.adapter = authorAdapter

        authorList.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(5)))
        authorList.isNestedScrollingEnabled = false

        authorAdapter.setOnItemClickListener { _, _, position ->
            val item = authorAdapter.getItemOrNull(position)
            val content = item?.content
            if (item != null && content is ProgramLiveInfo) {
                logger.info("跳转直播间${content.programId}")
                PlayerActivity.start(requireActivity(), programId = content.programId, prePic = content.coverPic)
            }
        }
        mRefreshLayout.setOnRefreshListener {
            mViewModel.requestProgramList(QueryType.REFRESH, currentTab?.typeCode)
            followViewModel.requestProgramList(QueryType.REFRESH)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)

    }

    override fun lazyLoadData() {
        mViewModel.requestProgramList(QueryType.INIT, currentTab?.typeCode)
    }

    private fun initViewModel() {
        mViewModel.dataList.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                renderHotData(it.requireT())
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
            authorAdapter.loadMoreModule.loadMoreFail()
        }
    }

    private val totalList: MutableList<ProgramLiveInfo> = mutableListOf()
    private fun renderHotData(listData: ProgramListInfo) {

        if (listData.isPull) {
            val programList = listData.programList.distinct()
            totalList.clear()
            totalList.addAll(programList)
            val list = mutableListOf<MultiBean>()
            programList.forEach {
                list.add(MultiBean(ProgramItemType.NORMAL, it))
            }

            if (listData.adList != null) {
                if (list.size > BANNER_POSITION) {
                    list.add(BANNER_POSITION, MultiBean(ProgramItemType.BANNER, listData.adList!!))
                } else {
                    list.add(MultiBean(ProgramItemType.BANNER, listData.adList!!))
                }
            }

            authorAdapter.setList(list)

        } else {
            val programList = listData.programList.removeDuplicate(totalList)
            totalList.addAll(programList)
            val list = mutableListOf<MultiBean>()
            programList.forEach {
                list.add(MultiBean(ProgramItemType.NORMAL, it))
            }
            authorAdapter.addData(list)
        }
        if (listData.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (listData.programList.isEmpty()) {
                authorAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                authorAdapter.loadMoreModule.loadMoreComplete()
            }

        } else {
            //防止底部没有边距
            authorAdapter.loadMoreModule.loadMoreEnd()
//            if (listData.isPull) {
//                authorAdapter.loadMoreModule.loadMoreEnd(true)
//            } else {
//                authorAdapter.loadMoreModule.loadMoreEnd()
//            }

        }
        if (authorAdapter.data.isEmpty()) {
            mRefreshLayout.hide()
            state_pager_view.showEmpty(emptyTxt = "没有开播的主播，去交友看看吧", btnTex = "前往",
                onClick = View.OnClickListener {
                    logger.info("跳转到交友")
                    ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                        .withInt(IntentParamKey.TARGET_INDEX.name, 0).navigation()
                })
        }
    }

    //
    fun scrollToTopAndRefresh() {
        authorList.smoothScrollToPosition(0)
        mViewModel.requestProgramList(QueryType.REFRESH, currentTab?.typeCode)
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
                state_pager_view.showSuccess()
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

    private val authorAdapter = ProgramAdapter()

}