package com.julun.huanque.core.ui.main.dynamic_square

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.image.ImageActivity
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.DynamicGroupListAdapter
import com.julun.huanque.core.adapter.DynamicListAdapter
import com.julun.huanque.core.ui.dynamic.DynamicDetailActivity
import com.julun.huanque.core.ui.homepage.CircleActivity
import com.julun.huanque.core.ui.share.LiveShareActivity
import kotlinx.android.synthetic.main.fragment_dynamic_tab.*
import kotlinx.android.synthetic.main.fragment_program_tab.mRefreshLayout
import kotlinx.android.synthetic.main.fragment_program_tab.state_pager_view
import kotlinx.android.synthetic.main.layout_header_dynamic.view.*

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

    private var currentTab: SquareTab? = null

    private val headerLayout: View by lazy {
        LayoutInflater.from(requireContext()).inflate(R.layout.layout_header_dynamic, null)
    }
    private val groupAdapter: DynamicGroupListAdapter by lazy {
        DynamicGroupListAdapter()
    }
    private val dynamicAdapter = DynamicListAdapter()

    override fun getLayoutId(): Int = R.layout.fragment_dynamic_tab

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentTab = arguments?.getSerializable(IntentParamKey.TAB_TYPE.name) as? SquareTab
        initViewModel()


        postList.layoutManager = LinearLayoutManager(requireContext())
        dynamicAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info(" postList.stopScroll()")
            postList.stopScroll()
            logger.info("authorAdapter loadMoreModule 加载更多")
            mViewModel.requestPostList(QueryType.LOAD_MORE, currentTab?.typeCode)
        }

        headerLayout.groupList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        headerLayout.groupList.adapter = groupAdapter
        headerLayout.hide()
        dynamicAdapter.addHeaderView(headerLayout)
        dynamicAdapter.headerWithEmptyEnable = true
        postList.adapter = dynamicAdapter

        postList.isNestedScrollingEnabled = false


        mRefreshLayout.setOnRefreshListener {
            mViewModel.requestPostList(QueryType.REFRESH, currentTab?.typeCode)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)

    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        //头部相关
        headerLayout.header_layout.onClickNew {
            logger.info("打开全部圈子")
            CircleActivity.newInstance(requireActivity(), CircleGroupTabType.Follow)
        }
        groupAdapter.onAdapterClickNew { _, _, position ->
            val item = groupAdapter.getItemOrNull(position) ?: return@onAdapterClickNew
            //todo
            logger.info("打开头部圈子：${item.groupName}")
        }


        dynamicAdapter.onAdapterClickNew { _, _, position ->
            val item = dynamicAdapter.getItemOrNull(position) ?: return@onAdapterClickNew
            DynamicDetailActivity.start(requireActivity(), item.postId)
        }
        dynamicAdapter.mOnItemAdapterListener = object : DynamicListAdapter.OnItemAdapterListener {
            override fun onPhotoClick(index: Int, position: Int, list: MutableList<PhotoBean>) {
                logger.info("index=$index position=$position ")
                val item = dynamicAdapter.getItemOrNull(index)
                ImageActivity.start(
                    requireActivity(),
                    position,
                    list.map { StringHelper.getOssImgUrl(it.url) },
                    item?.userId
                )

            }

        }
        dynamicAdapter.onAdapterChildClickNew { adapter, view, position ->
            val tempData = adapter?.getItemOrNull(position) as? DynamicItemBean ?: return@onAdapterChildClickNew
            when (view.id) {
                R.id.btn_action -> {
                    logger.info("关注")
                }
                R.id.sdv_photo -> {
                    logger.info("大图")
                    val item = dynamicAdapter.getItemOrNull(position) ?: return@onAdapterChildClickNew
                    ImageActivity.start(
                        requireActivity(),
                        0,
                        item.pics.map { StringHelper.getOssImgUrl(it) },
                        item.userId
                    )

                }
                R.id.tv_follow_num -> {
                    logger.info("点赞")
                }
                R.id.tv_comment_num -> {
                    logger.info("评论")
                }
                R.id.tv_share_num -> {
                    LiveShareActivity.newInstance(requireActivity(), ShareFromType.Dynamic, tempData.postId)
                }
                R.id.iv_more_action -> {
                    logger.info("更多操作")
                }
            }
        }
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
        mViewModel.requestPostList(QueryType.INIT, currentTab?.typeCode)
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

    private fun renderDynamicData(listData: HomeDynamicListInfo) {

        if (listData.isPull) {
            val programList = listData.postList.distinct()
            dynamicAdapter.setList(programList)


            if (currentTab?.typeCode == SquareTabType.Follow) {
                headerLayout.show()
                headerLayout.tvTitle.text = "我的圈子"
                groupAdapter.setList(listData.groupList)
                if (listData.groupList.isNotEmpty()) {
                    headerLayout.header_layout.show()
                    headerLayout.bottom_layout.hide()
                } else {
                    headerLayout.header_layout.hide()
                    headerLayout.bottom_layout.show()
                    groupAdapter.setEmptyView(
                        MixedHelper.getEmptyView(requireContext(),
                            msg = "还没加入圈子，快去加入有趣的圈子吧",
                            isImageHide = true,
                            btnTex = "去推荐看看",
                            onClick = View.OnClickListener {
                                logger.info("跳转到推荐")
                                CircleActivity.newInstance(requireActivity(), CircleGroupTabType.Recom)
                            })
                    )
                }

            } else if (currentTab?.typeCode == SquareTabType.RECOMMEND) {
                if (listData.groupList.isNotEmpty()) {
                    headerLayout.tvTitle.text = "热门圈子"
                    headerLayout.show()
                    groupAdapter.setList(listData.groupList)
                } else {
                    headerLayout.hide()
                }
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
        if (currentTab?.typeCode == SquareTabType.RECOMMEND && dynamicAdapter.data.isEmpty() && listData.groupList.isEmpty()) {
            mRefreshLayout.hide()
            state_pager_view.showEmpty(emptyTxt = "没有动态，洗洗睡吧", btnTex = "前往",
                onClick = View.OnClickListener {
                    logger.info("跳转到交友")
                    ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                        .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX).navigation()
                })
        } else if (dynamicAdapter.data.isEmpty()) {
            state_pager_view.showSuccess()
            mRefreshLayout.show()
            dynamicAdapter.setEmptyView(
                MixedHelper.getEmptyView(requireContext(),
                    msg = "没有动态数据",
                    isImageHide = true,
                    btnTex = "去推荐看看",
                    onClick = View.OnClickListener {
                        logger.info("跳转到推荐")

                    })
            )
        } else {
            mRefreshLayout.show()
            state_pager_view.showSuccess()
        }
    }

    //
    fun scrollToTopAndRefresh() {
        postList.scrollToPosition(0)
        postList.postDelayed({
            mViewModel.requestPostList(QueryType.REFRESH, currentTab?.typeCode)
        }, 100)
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
//                state_pager_view.showSuccess()
//                mRefreshLayout.show()
            }
            NetStateType.LOADING -> {//showLoading()
                mRefreshLayout.hide()
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    mViewModel.requestPostList(QueryType.INIT, currentTab?.typeCode)
                })
            }

        }

    }


}