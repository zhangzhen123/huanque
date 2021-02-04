package com.julun.huanque.core.ui.dynamic

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
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
import com.julun.huanque.common.base.dialog.BottomActionDialog
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.BottomAction
import com.julun.huanque.common.bean.beans.DynamicItemBean
import com.julun.huanque.common.bean.beans.DynamicListInfo
import com.julun.huanque.common.bean.beans.PhotoBean
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
import com.julun.huanque.core.adapter.DynamicListAdapter
import com.julun.huanque.core.ui.publish_dynamic.PublishStateActivity
import com.julun.huanque.core.ui.share.LiveShareActivity
import kotlinx.android.synthetic.main.fragment_user_dynamic_list.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2021/1/13 10:54
 *
 *@Description: UserDynamicFragment
 *
 */
class UserDynamicFragment : BaseVMFragment<UserDynamicViewModel>() {

    companion object {
        fun newInstance(userId:Long): UserDynamicFragment{
            val args = Bundle()
            args.putLong(IntentParamKey.USER_ID.name,userId)
            val fragment = UserDynamicFragment()
            fragment.arguments = args
            return fragment
        }

        const val PublishRequestCode = 0x9836
    }

    private var huanQueViewModel = HuanViewModelManager.huanQueViewModel


    private var currentItem: DynamicItemBean? = null
    private var bottomDialog: BottomActionDialog? = null
    private val bottomDialogListener: BottomActionDialog.OnActionListener by lazy {
        object : BottomActionDialog.OnActionListener {
            override fun operate(action: BottomAction) {
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

    private val dynamicAdapter = DynamicListAdapter()
    override fun isRegisterEventBus(): Boolean {
        return true
    }

    override fun getLayoutId(): Int = R.layout.fragment_user_dynamic_list

    var currentUserId: Long? = null
    private var isMe: Boolean = false
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentUserId = arguments?.getLong(IntentParamKey.USER_ID.name, 0L)
        initViewModel()
        isMe = currentUserId == SessionUtils.getUserId()
        if (isMe) {
            dynamicAdapter.showType = DynamicListAdapter.POST_LIST_ME
//            publish_dynamic.show()
        } else {
            dynamicAdapter.showType = DynamicListAdapter.POST_LIST_OTHER
//            publish_dynamic.hide()
        }
        postList.layoutManager = LinearLayoutManager(requireContext())
        dynamicAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info(" postList.stopScroll()")
            postList.stopScroll()
            logger.info("authorAdapter loadMoreModule 加载更多")
            mViewModel.requestPostList(QueryType.LOAD_MORE, currentUserId)
        }
        (postList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        dynamicAdapter.headerWithEmptyEnable = true
        postList.adapter = dynamicAdapter
        dynamicAdapter.setEmptyView(
            MixedHelper.getEmptyView(
                requireContext(),
                "暂无动态，快去发一条吧~"
            )
        )



        mRefreshLayout.setOnRefreshListener {
            mViewModel.requestPostList(QueryType.REFRESH, currentUserId)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)


    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)

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
//                    if (!item.userAnonymous) {
//                        HomePageActivity.newInstance(this, item.userId)
//                    }

                }
                R.id.btn_action -> {
                    logger.info("关注")
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
                    DynamicDetailActivity.start(requireActivity(), item.postId, true)
                }
                R.id.tv_share_num -> {
                    logger.info("分享")
                    LiveShareActivity.newInstance(requireActivity(), ShareFromType.Share_Dynamic, item.postId)
                }
                R.id.iv_more_action -> {
                    logger.info("更多操作")
                    currentItem = item
                    val actions = arrayListOf<BottomAction>()
                    if (item.userId == SessionUtils.getUserId()) {
                        actions.add(BottomAction(BottomActionCode.DELETE, "删除"))
                        actions.add(BottomAction(BottomActionCode.CANCEL, "取消"))
                    } else {
                        actions.add(BottomAction(BottomActionCode.REPORT, "举报"))
                        actions.add(BottomAction(BottomActionCode.CANCEL, "取消"))
                    }
                    if (bottomDialog == null) {
                        bottomDialog = BottomActionDialog.newInstance(actions = actions)
                    } else {
                        bottomDialog?.setActions(actions)
                    }
                    bottomDialog?.listener = bottomDialogListener
                    bottomDialog?.show(requireActivity(), "bottomDialog")
                }
                R.id.tv_circle_name -> {
                    reportClick(StatisticCode.EnterGroup + StatisticCode.Post)
                    CircleDynamicActivity.start(requireActivity(), item.group?.groupId ?: return@onAdapterChildClickNew)
                }
            }

        }


        publish_dynamic.onClickNew {
            logger.info("跳转到交友")
            reportClick(
                StatisticCode.PubPost + StatisticCode.MyPost
            )
//            ARouter.getInstance().build(ARouterConstant.PUBLISH_STATE_ACTIVITY).navigation(requireActivity(), PublishRequestCode)
            val intent=Intent(requireActivity(),PublishStateActivity::class.java)
            startActivityForResult(intent,PublishRequestCode)
        }

//        postList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    //停止状态，显示发布按钮
//                    showOrHidePublish(true)
//                } else {
//                    //滑动状态，隐藏发布按钮
//                    showOrHidePublish(false)
//                }
//            }
//
//        })
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

//        huanQueViewModel.userInfoStatusChange.observe(this, Observer {
//            if (it.isSuccess()) {
//                //处理关注状态
//                val value = it.requireT()
//                if ((value.follow == FollowStatus.True || value.follow == FollowStatus.Mutual) && value.userId == currentUserId) {
//
//                }
//            }
//
//        })
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

    //隐藏发布按钮动画
    private var mHidePublishAnimation: ObjectAnimator? = null

    //显示发布按钮动画
    private var mShowPublishAnimation: ObjectAnimator? = null


    /**
     * 显示或者隐藏发布按钮
     * @param show  true 显示发布按钮  false 隐藏发布按钮
     */
    private fun showOrHidePublish(show: Boolean) {
        if (!isMe) {
            publish_dynamic.hide()
            return
        }
        if (show) {
            if (mShowPublishAnimation?.isRunning == true) {
                //动画正在执行，什么都不操作
                return
            }
            mShowPublishAnimation = ObjectAnimator.ofFloat(publish_dynamic, "translationX", publish_dynamic.translationX, 0f)
                .apply { duration = 100 }
            mHidePublishAnimation?.cancel()
            mShowPublishAnimation?.cancel()
            mShowPublishAnimation?.start()
        } else {
            if (mHidePublishAnimation?.isRunning == true) {
                //动画正在执行，什么都不操作
                return
            }
            mHidePublishAnimation =
                ObjectAnimator.ofFloat(publish_dynamic, "translationX", publish_dynamic.translationX, dp2pxf(95))
                    .apply { duration = 100 }

            mHidePublishAnimation?.cancel()
            mShowPublishAnimation?.cancel()
            mHidePublishAnimation?.start()
        }

    }


    private fun loadDataFail(isPull: Boolean) {
        if (isPull) {
            ToastUtils.show2("刷新失败")
        } else {
            dynamicAdapter.loadMoreModule.loadMoreFail()
        }
    }

    private fun renderDynamicData(listData: DynamicListInfo<DynamicItemBean>) {

        var isEmpty: Boolean = false
        if (listData.isPull) {
            val list = listData.list.distinct()
            isEmpty = list.isEmpty()
            dynamicAdapter.setList(list)
        } else {
            val programList = listData.list.removeDuplicate(dynamicAdapter.data)
//            totalList.addAll(programList)
            isEmpty = programList.isEmpty()
            dynamicAdapter.addData(programList)
        }
        if (listData.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (isEmpty) {
                dynamicAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                dynamicAdapter.loadMoreModule.loadMoreComplete()
            }

        } else {
            //防止底部没有边距
            dynamicAdapter.loadMoreModule.loadMoreEnd()

        }
        if (dynamicAdapter.data.isEmpty()) {
            if (isMe) {
                dynamicAdapter.setEmptyView(
                    MixedHelper.getEmptyView(
                        requireContext(),
                        msg = "暂无动态，快去发布一条吧~", btnTex = "去发布"
                    )
                )
            } else {
                dynamicAdapter.setEmptyView(
                    MixedHelper.getEmptyView(
                        requireContext(),
                        msg = "暂无动态"
                    )
                )
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun shareSuccess(bean: ShareSuccessEvent) {
        if (bean.commentId == null) {
            val result = dynamicAdapter.data.firstOrNull { item -> item.postId == bean.postId } ?: return
            result.shareNum++
            val index = dynamicAdapter.data.indexOf(result)
            logger.info("share index=$index")
            lifecycleScope.launchWhenResumed {
                MixedHelper.safeNotifyItem(index, postList, dynamicAdapter)
            }
        }
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
                state_pager_view.showSuccess()
            }
            NetStateType.LOADING -> {//showLoading()
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    mViewModel.requestPostList(QueryType.INIT, currentUserId)
                })
            }

        }

    }

    override fun onViewDestroy() {
        super.onViewDestroy()
        mHidePublishAnimation?.cancel()
        mShowPublishAnimation?.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PublishRequestCode && resultCode == Activity.RESULT_OK) {
            //刷新
            mViewModel.requestPostList(QueryType.REFRESH, currentUserId)
        }
    }

    override fun lazyLoadData() {
        mViewModel.requestPostList(QueryType.INIT, currentUserId)
    }

}