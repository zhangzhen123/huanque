package com.julun.huanque.core.ui.dynamic

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.base.BaseLazyFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.CircleGroup
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.reportClick
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.AttentionCircleAdapter
import com.julun.huanque.core.viewmodel.AttentionCircleViewModel
import com.julun.huanque.core.viewmodel.CircleViewModel
import kotlinx.android.synthetic.main.fragment_attention_circle.*

/**
 *@创建者   dong
 *@创建时间 2020/11/24 10:20
 *@描述 关注圈子Fragment
 */
class RecommendCircleFragment : BaseLazyFragment() {
    companion object {
        fun newInstance() = RecommendCircleFragment()
    }

    private val mAttentionCircleViewModel: AttentionCircleViewModel by viewModels()
    private val mCircleViewModel: CircleViewModel by activityViewModels()
    private var mAttentionCircleAdapter = AttentionCircleAdapter()

    override fun getLayoutId() = R.layout.fragment_attention_circle

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        state_pager_view.showLoading()
        initRecyclerView()
        mAttentionCircleAdapter.hideAction = mCircleViewModel.mType == CircleGroupType.Circle_Choose
        initViewModel()
        swipeRefreshLayout.setOnRefreshListener {
            mAttentionCircleViewModel.requestType = CircleGroupTabType.Recom
            mAttentionCircleViewModel.mOffset = 0
            if (mCircleViewModel.mType == CircleGroupType.Circle_Choose) {
                mAttentionCircleViewModel.getChooseCircle(QueryType.REFRESH)
            } else {
                mAttentionCircleViewModel.getCircleGroupInfo(QueryType.REFRESH)
            }
        }
        MixedHelper.setSwipeRefreshStyle(swipeRefreshLayout)
    }

    override fun lazyLoadData() {
        mAttentionCircleViewModel.requestType = CircleGroupTabType.Recom
        if (mCircleViewModel.mType == CircleGroupType.Circle_Choose) {
            mAttentionCircleViewModel.getChooseCircle(QueryType.INIT)
        } else {
            mAttentionCircleViewModel.getCircleGroupInfo(QueryType.INIT)
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAttentionCircleAdapter
        mAttentionCircleAdapter.setEmptyView(MixedHelper.getEmptyView(requireContext(), "已加入所有圈子，快去关注列表看看"))

        mAttentionCircleAdapter.setOnItemChildClickListener { adapter, view, position ->
            val tempData = adapter.getItemOrNull(position)
            if (tempData is CircleGroup) {
                if (tempData.joined == BusiConstant.True) {
                    //退出圈子
                    MyAlertDialog(requireActivity()).showAlertWithOKAndCancel(
                        "退出圈子将错过精彩内容和动态推送",
                        MyAlertDialog.MyDialogCallback(onCancel = {
                            mAttentionCircleViewModel.groupQuit(tempData.groupId)
                        }), "提示", "继续关注", "残忍退出"
                    )
                } else {
                    //加入圈子
                    mAttentionCircleViewModel.groupJoin(tempData.groupId)
                }
            }
        }
        mAttentionCircleAdapter.setOnItemClickListener { adapter, view, position ->
            val tempData = adapter.getItemOrNull(position) as? CircleGroup ?: return@setOnItemClickListener
            if (mCircleViewModel.mType == CircleGroupType.Circle_All) {
                reportClick(StatisticCode.EnterGroup + StatisticCode.List)
                //全部圈子  打开详情
                CircleDynamicActivity.start(requireActivity(), tempData.groupId)
            } else if (mCircleViewModel.mType == CircleGroupType.Circle_Choose) {
                //选择圈子，返回选中的圈子数据
                val act = requireActivity()
                val intent = act.intent
                intent.putExtra(PublicStateCode.CIRCLE_DATA, tempData)
                act.setResult(Activity.RESULT_OK, intent)
                act.finish()
            }
        }
        mAttentionCircleAdapter.loadMoreModule.setOnLoadMoreListener {
            if (mCircleViewModel.mType == CircleGroupType.Circle_Choose) {
                mAttentionCircleViewModel.getChooseCircle(QueryType.LOAD_MORE)
            } else {
                var joinCount = 0
                mAttentionCircleAdapter.data.forEach {
                    if (it is CircleGroup && it.joined == BusiConstant.True) {
                        joinCount++
                    }
                }
                mAttentionCircleViewModel.getCircleGroupInfo(QueryType.LOAD_MORE, joinCount)
            }
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mAttentionCircleViewModel.myGroupData.observe(this, Observer {
            if (it != null) {
//                val circleList = mutableListOf<Any>()
                val recommendGroup = if (mCircleViewModel.mType == CircleGroupType.Circle_Choose) {
                    it.group
                } else {
                    it.recommendGroup
                }
                if (recommendGroup.isPull) {
                    //刷新操作,清空列表
                    mAttentionCircleAdapter.setList(null)
                    val recommendList = recommendGroup.list
//                    if (recommendList.isNotEmpty()) {
//                        circleList.addAll(recommendList)
//                    }
                    mAttentionCircleAdapter.setList(recommendList)
                    if (recommendList.isNotEmpty()) {
                        //推荐的数据不为空，使用推荐的hasmore字段
                        if (!recommendGroup.hasMore) {
                            //没有更多了
                            mAttentionCircleAdapter.loadMoreModule.loadMoreEnd()
                        } else {
                            mAttentionCircleAdapter.loadMoreModule.loadMoreComplete()
                        }
                    } else {
                        //推荐的数据为空，显示推荐的hasmore字段
                        if (!it.group.hasMore) {
                            //没有更多了
                            mAttentionCircleAdapter.loadMoreModule.loadMoreEnd()
                        } else {
                            mAttentionCircleAdapter.loadMoreModule.loadMoreComplete()
                        }
                    }
                } else {
                    //加载更多操作

//                    if (recommendGroup.list.isNotEmpty()) {
//                        //追加推荐数据
//                        circleList.addAll(recommendGroup.list)
//                    }
                    val localData = mAttentionCircleAdapter.data

                    val localIdList = mutableListOf<Long>()
                    localData.forEach {
                        if (it is CircleGroup) {
                            localIdList.add(it.groupId)
                        }
                    }

                    val realAddData = mutableListOf<CircleGroup>()
                    recommendGroup.list.forEach {
                        if (!localIdList.contains(it.groupId)) {
                            realAddData.add(it)
                        }
                    }

                    mAttentionCircleAdapter.addData(realAddData)
                    if (!recommendGroup.hasMore) {
                        //没有更多了
                        mAttentionCircleAdapter.loadMoreModule.loadMoreEnd()
                    } else {
                        mAttentionCircleAdapter.loadMoreModule.loadMoreComplete()
                    }

                }

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
        mAttentionCircleViewModel.joinedGroupIdData.observe(this, Observer {
            if (it != null) {
                var joinedIndex = -1
                mAttentionCircleAdapter.data.forEachIndexed { index, any ->
                    if (any is CircleGroup && any.groupId == it) {
                        any.joined = BusiConstant.True
                        joinedIndex = index
                        return@forEachIndexed
                    }
                }
                if (joinedIndex >= 0) {
                    mAttentionCircleAdapter.notifyItemChanged(joinedIndex)
                }
            }
        })
        mAttentionCircleViewModel.quitGroupIdData.observe(this, Observer {
            if (it != null) {
                var joinedIndex = -1
                mAttentionCircleAdapter.data.forEachIndexed { index, any ->
                    if (any is CircleGroup && any.groupId == it) {
                        any.joined = ""
                        joinedIndex = index
                        return@forEachIndexed
                    }
                }
                if (joinedIndex >= 0) {
                    mAttentionCircleAdapter.notifyItemChanged(joinedIndex)
                }
            }
        })
    }

}