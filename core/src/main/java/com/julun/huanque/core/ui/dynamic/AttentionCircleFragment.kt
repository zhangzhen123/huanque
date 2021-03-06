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
import kotlinx.android.synthetic.main.fragment_attention_circle.state_pager_view

/**
 *@创建者   dong
 *@创建时间 2020/11/24 10:20
 *@描述 关注圈子Fragment
 */
class AttentionCircleFragment : BaseLazyFragment() {
    companion object {
        fun newInstance() = AttentionCircleFragment()
    }

    private var mAttentionCircleAdapter = AttentionCircleAdapter()

    //关注列表的ViewModel
    private val mAttentionCircleViewModel: AttentionCircleViewModel by viewModels()

    private val mCircleViewModel: CircleViewModel by activityViewModels()


    override fun getLayoutId() = R.layout.fragment_attention_circle

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        state_pager_view.showLoading()
        initRecyclerView()
        mAttentionCircleAdapter.hideAction = mCircleViewModel.mType == CircleGroupType.Circle_Choose
        initViewModel()

        swipeRefreshLayout.setOnRefreshListener {
            mAttentionCircleViewModel.requestType = CircleGroupTabType.Follow
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
        mAttentionCircleViewModel.requestType = CircleGroupTabType.Follow
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
                //全部圈子  打开详情
                reportClick(StatisticCode.EnterGroup+StatisticCode.List)
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
                mAttentionCircleViewModel.getCircleGroupInfo(QueryType.LOAD_MORE)
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
                if (it.group.isPull) {
                    //刷新操作,清空列表
                    if (mCircleViewModel.mType == CircleGroupType.Circle_Choose && it.group.list.isEmpty()) {
                        //选择圈子 并且数据为空,选中推荐模块
                        mCircleViewModel.selectRecom.value = true
                    }
                    mAttentionCircleAdapter.setList(null)


                    val myList = it.group.list
                    if (myList.isEmpty()) {
                        //我加入的圈子为空
//                        circleList.add(AttentionCircleAdapter.Circle_Mine_Attention_Empty)
                        if(mCircleViewModel.mType == CircleGroupType.Circle_Choose){
                            circleList.add("还没有加入圈子，快去推荐列表看看")
                        }else{
                            circleList.add("还没加入圈子，快去加入有趣的圈子吧")
                        }
                    } else {
                        circleList.addAll(myList)
                    }
                    val recommendList = it.recommendGroup.list
                    if (recommendList.isNotEmpty()) {
                        circleList.add(AttentionCircleAdapter.Title_Recommend_Circle)
                        circleList.addAll(recommendList)
                    }
                    mAttentionCircleAdapter.setList(circleList)
                    if (recommendList.isNotEmpty()) {
                        //推荐的数据不为空，使用推荐的hasmore字段
                        if (!it.recommendGroup.hasMore) {
                            //没有更多了
                            mAttentionCircleAdapter.loadMoreModule.loadMoreEnd()
                        } else {
                            mAttentionCircleAdapter.loadMoreModule.loadMoreComplete()
                        }
                    } else {
                        //推荐的数据为空，显示推荐的hasmore字段
                        if (!it.group.hasMore) {
                            //没有更多了
                            if (myList.isEmpty() && mCircleViewModel.mType == CircleGroupType.Circle_Choose) {
                                mAttentionCircleAdapter.loadMoreModule.loadMoreEnd(true)
                            } else {
                                mAttentionCircleAdapter.loadMoreModule.loadMoreEnd()
                            }
                        } else {
                            mAttentionCircleAdapter.loadMoreModule.loadMoreComplete()
                        }
                    }
                } else {
                    //加载更多操作
                    val group = it.group
                    val recommendGroup = it.recommendGroup
                    if (group.list.isNotEmpty()) {
                        //追加关注数据
                        circleList.addAll(group.list)
//                        mAttentionCircleAdapter.addData(circleList)
//                        if (!group.hasMore) {
//                            //没有更多了
//                            mAttentionCircleAdapter.loadMoreModule.loadMoreEnd()
//                        } else {
//                            mAttentionCircleAdapter.loadMoreModule.loadMoreComplete()
//                        }
                    }

                    if (recommendGroup.list.isNotEmpty()) {
                        //追加推荐数据
                        circleList.addAll(recommendGroup.list)

//                        if (!recommendGroup.hasMore) {
//                            //没有更多了
//                            mAttentionCircleAdapter.loadMoreModule.loadMoreEnd()
//                        } else {
//                            mAttentionCircleAdapter.loadMoreModule.loadMoreComplete()
//                        }
                    }
                    if (recommendGroup.hasMore || group.hasMore) {
                        mAttentionCircleAdapter.loadMoreModule.loadMoreComplete()
                    } else {
                        mAttentionCircleAdapter.loadMoreModule.loadMoreEnd()
                    }

                    mAttentionCircleAdapter.addData(circleList)
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