package com.julun.huanque.core.ui.main.dynamic_square

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.base.dialog.BottomDialog
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.DynamicItemBean
import com.julun.huanque.common.bean.beans.HomeDynamicListInfo
import com.julun.huanque.common.bean.beans.PhotoBean
import com.julun.huanque.common.bean.beans.SquareTab
import com.julun.huanque.common.bean.events.LoginEvent
import com.julun.huanque.common.bean.events.ShareSuccessEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.image.ImageActivity
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.DynamicGroupListAdapter
import com.julun.huanque.core.adapter.DynamicListAdapter
import com.julun.huanque.core.ui.dynamic.CircleActivity
import com.julun.huanque.core.ui.dynamic.CircleDynamicActivity
import com.julun.huanque.core.ui.dynamic.DynamicDetailActivity
import com.julun.huanque.core.ui.homepage.HomePageActivity
import com.julun.huanque.core.ui.share.LiveShareActivity
import kotlinx.android.synthetic.main.fragment_dynamic_tab.*
import kotlinx.android.synthetic.main.layout_header_dynamic.view.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

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
        fun newInstance(tab: SquareTab?, groupId: Long? = null): DynamicTabFragment {
            return DynamicTabFragment().apply {
                val bundle = Bundle()
                bundle.putSerializable(IntentParamKey.TAB_TYPE.name, tab)
                if (groupId != null)
                    bundle.putLong(IntentParamKey.ID.name, groupId)
                this.arguments = bundle
            }
        }
    }

    private var currentTab: SquareTab? = null
    private var currentGroupId: Long? = null
    private val headerLayout: View by lazy {
        LayoutInflater.from(requireContext()).inflate(R.layout.layout_header_dynamic, null)
    }
    private val groupAdapter: DynamicGroupListAdapter by lazy {
        DynamicGroupListAdapter()
    }
    private val dynamicAdapter = DynamicListAdapter()

    private var huanQueViewModel = HuanViewModelManager.huanQueViewModel

    private var bottomDialog: BottomDialog? = null
    private val bottomDialogListener: BottomDialog.OnActionListener by lazy {
        object : BottomDialog.OnActionListener {
            override fun operate(action: BottomDialog.Action) {
                when (action.code) {
                    BottomActionCode.DELETE -> {
                        logger.info("删除动态 ${currentItem?.postId}")
                        MyAlertDialog(requireActivity()).showAlertWithOKAndCancel(
                            "确定删除该动态内容？",
                            MyAlertDialog.MyDialogCallback(onRight = {
                                //删除动态
                                huanQueViewModel.deletePost(currentItem?.postId ?: return@MyDialogCallback)
                            }), "提示", okText = "确定"
                        )
                    }
                    BottomActionCode.REPORT -> {
                        logger.info("举报动态 ${currentItem?.postId}")
                        val extra = Bundle()
                        extra.putLong(ParamConstant.TARGET_USER_ID, currentItem?.userId ?: return)
                        ARouter.getInstance().build(ARouterConstant.REPORT_ACTIVITY).with(extra).navigation()
                    }
                }
            }
        }
    }
    private var currentItem: DynamicItemBean? = null

    override fun getLayoutId(): Int = R.layout.fragment_dynamic_tab

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentTab = arguments?.getSerializable(IntentParamKey.TAB_TYPE.name) as? SquareTab
        currentGroupId = arguments?.getLong(IntentParamKey.ID.name)
        initViewModel()
        if (currentTab?.typeCode == SquareTabType.RECOMMEND) {
            dynamicAdapter.showType = DynamicListAdapter.HOME_RECOM
            groupAdapter.showType = DynamicListAdapter.HOME_RECOM
        } else if (currentTab?.typeCode == SquareTabType.FOLLOW) {
            dynamicAdapter.showType = DynamicListAdapter.HOME_FOLLOW
            groupAdapter.showType = DynamicListAdapter.HOME_FOLLOW
        }
        postList.layoutManager = LinearLayoutManager(requireContext())
        dynamicAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info(" postList.stopScroll()")
            postList.stopScroll()
            logger.info("authorAdapter loadMoreModule 加载更多")
            queryData(QueryType.LOAD_MORE, currentTab?.typeCode)
        }
        if (currentTab?.typeCode == SquareTabType.RECOMMEND || currentTab?.typeCode == SquareTabType.FOLLOW) {
            headerLayout.groupList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            headerLayout.groupList.adapter = groupAdapter
            headerLayout.hide()
            dynamicAdapter.addHeaderView(headerLayout)
            dynamicAdapter.headerWithEmptyEnable = true
        }

        postList.adapter = dynamicAdapter

        (postList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

//        postList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            private var isUp: Boolean = false
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                    val positionFirst = layoutManager.findFirstVisibleItemPosition()
//                    val positionLast = layoutManager.findLastVisibleItemPosition()
//                    logger.info("onScrollStateChanged positionFirst=${positionFirst} positionLast=$positionLast")
//
//                }
//
//            }
//
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//            }
//        })

        mRefreshLayout.setOnRefreshListener {
            queryData(QueryType.REFRESH, currentTab?.typeCode)
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
            logger.info("打开头部圈子：${item.groupName}")
            reportClick(StatisticCode.EnterGroup+StatisticCode.List)
            CircleDynamicActivity.start(requireActivity(), item.groupId)
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
        dynamicAdapter.onAdapterChildClickNew { _, view, position ->
            val item = dynamicAdapter.getItemOrNull(position) ?: return@onAdapterChildClickNew
            when (view.id) {
                R.id.user_info_holder -> {
                    logger.info("打开个人主页")
                    if (!item.userAnonymous)
                        HomePageActivity.newInstance(requireActivity(), item.userId)
                }
                R.id.btn_action -> {
                    logger.info("关注")
                    reportClick(
                        eventCode = StatisticCode.Follow + StatisticCode.Post
                    )
                    huanQueViewModel.follow(item.userId)
                }
                R.id.sdv_photo -> {
                    logger.info("大图")
                    ImageActivity.start(
                        requireActivity(),
                        0,
                        item.pics.map { StringHelper.getOssImgUrl(it) },
                        item.userId
                    )

                }
                R.id.tv_follow_num -> {
                    logger.info("点赞")
                    if (item.hasPraise) {
                        huanQueViewModel.cancelPraise(item.postId)
                    } else {
                        huanQueViewModel.praise(item.postId)
                    }

                }
                R.id.tv_comment_num -> {
                    logger.info("评论")
                    DynamicDetailActivity.start(requireActivity(), item.postId)
                }
                R.id.tv_share_num -> {
                    LiveShareActivity.newInstance(requireActivity(), ShareFromType.Share_Dynamic, item.postId)
                }
                R.id.iv_more_action -> {
                    logger.info("更多操作")
                    currentItem = item
                    val actions = arrayListOf<BottomDialog.Action>()
                    if (item.userId == SessionUtils.getUserId()) {
                        actions.add(BottomDialog.Action(BottomActionCode.DELETE, "删除"))
                        actions.add(BottomDialog.Action(BottomActionCode.CANCEL, "取消"))
                    } else {
                        actions.add(BottomDialog.Action(BottomActionCode.REPORT, "举报"))
                        actions.add(BottomDialog.Action(BottomActionCode.CANCEL, "取消"))
                    }
                    if (bottomDialog == null) {
                        bottomDialog = BottomDialog.newInstance(actions = actions)
                    } else {
                        bottomDialog?.setActions(actions)
                    }
                    bottomDialog?.listener = bottomDialogListener
                    bottomDialog?.show(requireActivity(), "bottomDialog")
                }
                R.id.tv_circle_name -> {
                    reportClick(StatisticCode.EnterGroup+StatisticCode.Post)
                    CircleDynamicActivity.start(requireActivity(), item.group?.groupId ?: return@onAdapterChildClickNew)
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
        queryData(QueryType.INIT, currentTab?.typeCode)
    }

    private fun queryData(queryType: QueryType, typeCode: String?) {
        when (typeCode) {
            SquareTabType.FOLLOW, SquareTabType.RECOMMEND -> {
                mViewModel.requestPostList(queryType, typeCode)
            }
            SquareTabType.HOT, SquareTabType.NEW -> {
                mViewModel.requestGroupPostList(queryType, currentGroupId, typeCode)
            }

        }
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
        mViewModel.postList.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                renderDynamicGroupData(it.requireT())
            } else if (it.state == NetStateType.ERROR) {
                loadDataFail(it.isRefresh())
            }
            mRefreshLayout.isRefreshing = false
        })
        huanQueViewModel.userInfoStatusChange.observe(this, Observer {
            if (it.isSuccess()) {
                val change = it.requireT()
                //处理关注状态
//                val result = dynamicAdapter.data.firstOrNull { item -> item.userId == change.userId } ?: return@Observer
//                result.follow = change.follow == FollowStatus.True
//                val index = dynamicAdapter.data.indexOf(result)
//                logger.info("index=$index")
//                MixedHelper.safeNotifyItem(index, postList, dynamicAdapter)

                val result = dynamicAdapter.data.filter { item -> item.userId == change.userId }.map { bean ->
                    bean.follow = change.follow == FollowStatus.True || change.follow == FollowStatus.Mutual
                }
                dynamicAdapter.notifyDataSetChanged()
            }

        })


        huanQueViewModel.dynamicChangeResult.observe(this, Observer {
            if (it != null) {
                val result = dynamicAdapter.data.firstOrNull { item -> item.postId == it.postId } ?: return@Observer
                if (it.hasDelete) {
                    dynamicAdapter.remove(result)
                    return@Observer
                }
                //处理点赞刷新
                if (it.praise != null) {
                    if (it.praise != result.hasPraise) {
                        result.hasPraise = it.praise!!
                        if (it.praise == true) {
                            result.praiseNum++
                        } else {
                            result.praiseNum--
                        }
                    }


                }
                if (it.comment != null) {
                    result.commentNum = it.comment!!
                }
                val index = dynamicAdapter.data.indexOf(result)
                logger.info("index=$index")
                MixedHelper.safeNotifyItem(index, postList, dynamicAdapter)

            }

        })


    }

    override fun isRegisterEventBus(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun shareSuccess(bean: ShareSuccessEvent) {
        if (bean.commentId == null) {
            val result = dynamicAdapter.data.firstOrNull { item -> item.postId == bean.postId } ?: return
            result.shareNum++
            val index = dynamicAdapter.data.indexOf(result)
            logger.info("share index=$index")
            lifecycleScope.launchWhenResumed {
                logger.info("share safeNotifyItem=$index")
                MixedHelper.safeNotifyItem(index, postList, dynamicAdapter)
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveLoginCode(event: LoginEvent) {
        logger.info("登录事件:${event.result}")
        if (event.result) {
            queryData(QueryType.REFRESH, currentTab?.typeCode)
        }

    }

    private fun loadDataFail(isPull: Boolean) {
        if (isPull) {
            ToastUtils.show2("刷新失败")
        } else {
            dynamicAdapter.loadMoreModule.loadMoreFail()
        }
    }

    private fun renderDynamicGroupData(listData: RootListData<DynamicItemBean>) {

        if (listData.isPull) {
            val programList = listData.list.distinct()
            dynamicAdapter.setList(programList)

        } else {
            val programList = listData.list.removeDuplicate(dynamicAdapter.data)
//            totalList.addAll(programList)
            dynamicAdapter.addData(programList)
        }
        if (listData.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (listData.list.isEmpty()) {
                dynamicAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                dynamicAdapter.loadMoreModule.loadMoreComplete()
            }

        } else {
            //防止底部没有边距
            dynamicAdapter.loadMoreModule.loadMoreEnd()

        }

        if (dynamicAdapter.data.isEmpty()) {
            state_pager_view.showSuccess()
            mRefreshLayout.show()
            dynamicAdapter.setEmptyView(
                MixedHelper.getEmptyView(
                    requireContext(),
                    msg = "暂无动态，快去发布一条吧~"
                )
            )
        } else {
            mRefreshLayout.show()
            state_pager_view.showSuccess()
        }
    }

    private fun renderDynamicData(listData: HomeDynamicListInfo) {

        if (listData.isPull) {
            val programList = listData.postList.distinct()
            dynamicAdapter.setList(programList)


            if (currentTab?.typeCode == SquareTabType.FOLLOW) {
                headerLayout.show()
                headerLayout.tvTitle.text = "我的圈子"
                groupAdapter.setList(listData.groupList)
                if (listData.groupList.isNotEmpty()) {
                    headerLayout.header_layout.show()
//                    headerLayout.bottom_layout.hide()
                } else {
                    headerLayout.header_layout.hide()

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
                if (listData.recom) {
                    headerLayout.bottom_layout.show()
                } else {
                    headerLayout.bottom_layout.hide()
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
            state_pager_view.showEmpty(emptyTxt = "暂无推荐内容")
        } else if (dynamicAdapter.data.isEmpty()) {
            state_pager_view.showSuccess()
            mRefreshLayout.show()
            var message = ""
            if (currentTab?.typeCode == SquareTabType.FOLLOW) {
                message = "暂无关注内容"
            } else if (currentTab?.typeCode == SquareTabType.RECOMMEND) {
                message = "暂无推荐内容"
            }
            dynamicAdapter.setEmptyView(
                MixedHelper.getEmptyView(
                    requireContext(),
                    msg = message
                )
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
            queryData(QueryType.REFRESH, currentTab?.typeCode)
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
                    queryData(QueryType.INIT, currentTab?.typeCode)
                })
            }

        }

    }


}