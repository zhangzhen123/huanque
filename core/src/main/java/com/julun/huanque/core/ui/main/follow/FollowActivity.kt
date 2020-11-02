package com.julun.huanque.core.ui.main.follow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.FollowProgramInfo
import com.julun.huanque.common.bean.beans.MultiBean
import com.julun.huanque.common.bean.beans.ProgramLiveInfo
import com.julun.huanque.common.constant.ProgramItemType
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.ProgramAdapter
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.search.SearchResultAdapter
import kotlinx.android.synthetic.main.activity_follow.*
import kotlinx.android.synthetic.main.activity_follow.mRefreshLayout
import kotlinx.android.synthetic.main.layout_bottom_follow_recommend.view.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/10/29 9:48
 *
 *@Description: 关注页面
 *
 */
class FollowActivity : BaseVMActivity<FollowViewModel>() {


    override fun getLayoutId(): Int = R.layout.activity_follow

    private val authorAdapter = SearchResultAdapter(needLine = true)
    private val recommendAdapter = ProgramAdapter()
    private val bottomLayout: View by lazy {
        LayoutInflater.from(this).inflate(R.layout.layout_bottom_follow_recommend, null)
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        headerPageView.imageViewBack.onClickNew {
            finish()
        }
        headerPageView.textTitle.text = "关注主播"

        bottomLayout.recommendList.layoutManager = GridLayoutManager(this, 2)
        bottomLayout.recommendList.adapter = recommendAdapter
        bottomLayout.recommendList.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(5)))
        recommendAdapter.setOnItemClickListener { adapter, view, position ->
            val item = recommendAdapter.getItemOrNull(position)
            val content = item?.content
            if (item != null && content is ProgramLiveInfo) {
                logger.info("跳转直播间${content.programId}")
                PlayerActivity.start(this, programId = content.programId, prePic = content.coverPic)
            }

        }
        // rv_follows.layoutManager = GridLayoutManager(this, 2)
        // rv_follows.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(5)))
        rv_follows.layoutManager = LinearLayoutManager(this)
        initViewModel()
        authorAdapter.addFooterView(bottomLayout)

        authorAdapter.footerWithEmptyEnable = true
        rv_follows.adapter = authorAdapter

        rv_follows.isNestedScrollingEnabled = false

        authorAdapter.setOnItemClickListener { _, _, position ->
            val item = authorAdapter.getItemOrNull(position)
            if (item != null) {
                logger.info("跳转直播间${item.programId}")
                PlayerActivity.start(this, programId = item.programId, prePic = item.coverPic)
            }
        }

        authorAdapter.loadMoreModule.setOnLoadMoreListener {
            mViewModel.requestProgramList(QueryType.LOAD_MORE)
        }

        mRefreshLayout.setOnRefreshListener {
            mViewModel.requestProgramList(QueryType.REFRESH)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)
        mViewModel.requestProgramList(QueryType.INIT)

    }


    private fun initViewModel() {
        mViewModel.followInfo.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                renderData(it.requireT())
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

    private fun renderData(listData: FollowProgramInfo) {

        if (listData.isPull) {
            val programList = listData.followList.distinct()
            totalList.clear()
            totalList.addAll(programList)
//            val list = mutableListOf<MultiBean>()
//            programList.forEach {
//                list.add(MultiBean(ProgramItemType.NORMAL, it))
//            }

            authorAdapter.setList(programList)

            if (!listData.recomList.isNullOrEmpty()) {
                bottomLayout.title.show()
                bottomLayout.recommendList.show()
                val followList = listData.recomList!!.removeDuplicate(totalList)
                val fList = mutableListOf<MultiBean>()
                followList.forEach {
                    fList.add(MultiBean(ProgramItemType.NORMAL, it))
                }
                recommendAdapter.setList(fList)
            } else {
                bottomLayout.title.hide()
                bottomLayout.recommendList.hide()
            }
            rv_follows.post {
                rv_follows.scrollToPosition(0)
            }
        } else {
            val programList = listData.followList.removeDuplicate(totalList)
            totalList.addAll(programList)
//            val list = mutableListOf<MultiBean>()
//            programList.forEach {
//                list.add(MultiBean(ProgramItemType.NORMAL, it))
//            }
            authorAdapter.addData(programList)
        }
        if (listData.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (listData.followList.isEmpty()) {
                authorAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                authorAdapter.loadMoreModule.loadMoreComplete()
            }
        } else {
            //防止底部没有边距
            authorAdapter.loadMoreModule.loadMoreEnd()
        }
        if (authorAdapter.data.isEmpty()) {
            authorAdapter.setEmptyView(
                MixedHelper.getEmptyView(
                    this,
                    msg = "还没有关注主播，快去认识一些新主播吧。"/* ,
                    btnTex = "前往",
                   onClick = View.OnClickListener {
                        logger.info("跳转到交友")
                        ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                            .withInt(IntentParamKey.TARGET_INDEX.name, 0).navigation()
                    }*/
                ).apply {
                    val lp = if (listData.recomList?.isNotEmpty() == true) {
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(300))
                    } else {
                        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    }

                    this.layoutParams = lp
                }
            )
        }
    }


    override fun showLoadState(state: NetState) {
//        when (state.state) {
//            NetStateType.SUCCESS -> {//showSuccess()
//                state_pager_view.showSuccess()
//                mRefreshLayout.show()
//            }
//            NetStateType.LOADING -> {//showLoading()
//                mRefreshLayout.hide()
//                state_pager_view.showLoading()
//            }
//            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
//                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
//                    mViewModel.requestProgramList(QueryType.INIT)
//                })
//            }
//
//        }
        when (state.state) {
            NetStateType.SUCCESS -> {
                //由于上面在renderData中已经设置了空白页 这里不需要了
//                authorAdapter.setEmptyView(MixedHelper.getEmptyView(this))
            }
            NetStateType.LOADING -> {
                authorAdapter.setEmptyView(MixedHelper.getLoadingView(this))
            }
            NetStateType.ERROR -> {
                authorAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = this,
                        msg = state.message,
                        onClick = View.OnClickListener {
                            mViewModel.queryInfo(QueryType.INIT)
                        })
                )

            }
            NetStateType.NETWORK_ERROR -> {
                authorAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = this,
                        msg = "网络错误",
                        onClick = View.OnClickListener {
                            mViewModel.requestProgramList(QueryType.INIT)
                        })
                )

            }
        }


    }

}