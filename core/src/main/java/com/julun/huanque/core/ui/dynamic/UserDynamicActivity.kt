package com.julun.huanque.core.ui.dynamic

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.base.dialog.BottomDialog
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
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
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.DynamicListAdapter
import com.julun.huanque.core.ui.homepage.HomePageActivity
import com.julun.huanque.core.ui.publish_dynamic.PublishStateActivity
import com.julun.huanque.core.ui.share.LiveShareActivity
import kotlinx.android.synthetic.main.activity_dynamic_details.mRefreshLayout
import kotlinx.android.synthetic.main.activity_dynamic_list.*
import kotlinx.android.synthetic.main.fragment_dynamic_tab.postList
import kotlinx.android.synthetic.main.fragment_dynamic_tab.state_pager_view
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundResource

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/24 14:26
 *
 *@Description: 用户动态列表
 *
 */
@Route(path = ARouterConstant.USER_DYNAMIC_ACTIVITY)
class UserDynamicActivity : BaseVMActivity<UserDynamicViewModel>() {

    companion object {
    }

    private var huanQueViewModel = HuanViewModelManager.huanQueViewModel


    private var currentItem: DynamicItemBean? = null
    private var bottomDialog: BottomDialog? = null
    private val bottomDialogListener: BottomDialog.OnActionListener by lazy {
        object : BottomDialog.OnActionListener {
            override fun operate(action: BottomDialog.Action) {
                when (action.code) {
                    BottomActionCode.DELETE -> {
                        logger.info("删除动态 ${currentItem?.postId}")
                        MyAlertDialog(this@UserDynamicActivity).showAlertWithOKAndCancel(
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

    override fun getLayoutId(): Int = R.layout.activity_dynamic_list

    var currentUserId: Long? = null
    private var isMe: Boolean = false
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentUserId = intent.getLongExtra(IntentParamKey.USER_ID.name, 0L)
        initViewModel()
        isMe = currentUserId == SessionUtils.getUserId()
        if (isMe) {
            headerPageView.initHeaderView(titleTxt = "我的动态")
            dynamicAdapter.showType = DynamicListAdapter.POST_LIST_ME
        } else {
            headerPageView.initHeaderView(titleTxt = "Ta的动态")
            dynamicAdapter.showType = DynamicListAdapter.POST_LIST_OTHER
        }
        headerPageView.imageViewBack.onClickNew {
            finish()
        }

        postList.layoutManager = LinearLayoutManager(this)
        dynamicAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info(" postList.stopScroll()")
            postList.stopScroll()
            logger.info("authorAdapter loadMoreModule 加载更多")
            mViewModel.requestPostList(QueryType.LOAD_MORE, currentUserId)
        }
        (postList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        dynamicAdapter.headerWithEmptyEnable = true
        postList.adapter = dynamicAdapter
        dynamicAdapter.setEmptyView(MixedHelper.getEmptyView(this, "暂无动态，快去发一条吧~", btnTex = "去发布", onClick = View.OnClickListener {
            ARouter.getInstance().build(ARouterConstant.PUBLISH_STATE_ACTIVITY).navigation()
        }))

        postList.isNestedScrollingEnabled = false


        mRefreshLayout.setOnRefreshListener {
            mViewModel.requestPostList(QueryType.REFRESH, currentUserId)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)

        mViewModel.requestPostList(QueryType.INIT, currentUserId)

    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)

        dynamicAdapter.onAdapterClickNew { _, _, position ->
            val item = dynamicAdapter.getItemOrNull(position) ?: return@onAdapterClickNew
            DynamicDetailActivity.start(this, item.postId)
        }
        dynamicAdapter.mOnItemAdapterListener = object : DynamicListAdapter.OnItemAdapterListener {
            override fun onPhotoClick(index: Int, position: Int, list: MutableList<PhotoBean>) {
                logger.info("index=$index position=$position ")
                val item = dynamicAdapter.getItemOrNull(index)
                ImageActivity.start(
                    this@UserDynamicActivity,
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
                    if (!item.userAnonymous) {
                        HomePageActivity.newInstance(this, item.userId)
                    }

                }
                R.id.btn_action -> {
                    logger.info("关注")
                    huanQueViewModel.follow(item.userId)
                }
                R.id.sdv_photo -> {
                    logger.info("大图")
                    ImageActivity.start(
                        this,
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
                    DynamicDetailActivity.start(this, item.postId)
                }
                R.id.tv_share_num -> {
                    logger.info("分享")
                    LiveShareActivity.newInstance(this, ShareFromType.Share_Dynamic, item.postId)
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
                    bottomDialog?.show(this, "bottomDialog")
                }
                R.id.tv_circle_name -> {
                    CircleDynamicActivity.start(this, item.group?.groupId ?: return@onAdapterChildClickNew)
                }
            }

        }

        headerPageView.textOperation.onClickNew {
            huanQueViewModel.follow(currentUserId ?: return@onClickNew)
        }

        publish_dynamic.onClickNew {
            logger.info("跳转到交友")
            ARouter.getInstance().build(ARouterConstant.PUBLISH_STATE_ACTIVITY).navigation()
        }

        postList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //停止状态，显示发布按钮
                    showOrHidePublish(true)
                } else {
                    //滑动状态，隐藏发布按钮
                    showOrHidePublish(false)
                }
            }

        })
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

        huanQueViewModel.userInfoStatusChange.observe(this, Observer {
            if (it.isSuccess()) {
                //处理关注状态
                val value = it.requireT()
                if ((value.follow == FollowStatus.True || value.follow == FollowStatus.Mutual) && value.userId == currentUserId) {
                    headerPageView.textOperation.hide()
                }
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

    //隐藏发布按钮动画
    private var mHidePublishAnimation: ObjectAnimator? = null

    //显示发布按钮动画
    private var mShowPublishAnimation: ObjectAnimator? = null


    /**
     * 显示或者隐藏发布按钮
     * @param show  true 显示发布按钮  false 隐藏发布按钮
     */
    private fun showOrHidePublish(show: Boolean) {

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
            mHidePublishAnimation = ObjectAnimator.ofFloat(publish_dynamic, "translationX", publish_dynamic.translationX, dp2pxf(95))
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
        if (currentUserId != SessionUtils.getUserId()) {
            if (listData.extData?.follow == true) {
                headerPageView.textOperation.hide()
            } else {
                headerPageView.textOperation.show()
                headerPageView.textOperation.text = "关注"
                headerPageView.textOperation.height = dp2px(24)
                headerPageView.textOperation.width = dp2px(52)
                headerPageView.textOperation.backgroundResource = R.drawable.bg_solid_btn1
            }

        }
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
            mRefreshLayout.hide()
            if (isMe) {
                state_pager_view.showEmpty(emptyTxt = "暂无动态，快去发一条吧~", onClick = View.OnClickListener {
                    //跳转发布页面
                    val intent = Intent(this, PublishStateActivity::class.java)
                    if (ForceUtils.activityMatch(intent)) {
                        startActivity(intent)
                    }
                })
            } else {
                state_pager_view.showEmpty(emptyTxt = "暂无动态")
            }

        } else {
            mRefreshLayout.show()
            state_pager_view.showSuccess()
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
//                state_pager_view.showSuccess()
//                mRefreshLayout.show()
            }
            NetStateType.LOADING -> {//showLoading()
                mRefreshLayout.hide()
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

}