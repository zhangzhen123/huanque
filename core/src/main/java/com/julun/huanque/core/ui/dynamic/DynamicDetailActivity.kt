package com.julun.huanque.core.ui.dynamic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.effective.android.panel.PanelSwitchHelper
import com.effective.android.panel.view.panel.PanelView
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.base.dialog.BottomDialog
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.DynamicChangeResult
import com.julun.huanque.common.bean.beans.DynamicComment
import com.julun.huanque.common.bean.beans.DynamicDetailInfo
import com.julun.huanque.common.bean.beans.PhotoBean
import com.julun.huanque.common.bean.events.ShareSuccessEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.interfaces.EmojiInputListener
import com.julun.huanque.common.interfaces.EventDispatchListener
import com.julun.huanque.common.interfaces.SecondCommentClickListener
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.manager.audio_record.AudioRecordManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.image.ImageActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.common.widgets.emotion.Emotion
import com.julun.huanque.common.widgets.emotion.PrivateChatPanelView
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.DynamicDetailCommentFirstAdapter
import com.julun.huanque.core.adapter.DynamicListAdapter
import com.julun.huanque.core.adapter.DynamicPhotosAdapter
import com.julun.huanque.core.ui.homepage.HomePageActivity
import com.julun.huanque.core.ui.share.LiveShareActivity
import kotlinx.android.synthetic.main.activity_dynamic_details.*
import kotlinx.android.synthetic.main.layout_header_comment.*
import kotlinx.android.synthetic.main.layout_header_dynamic_detail.view.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.*
import kotlin.math.max

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/24 14:26
 *
 *@Description: DynamicDetailActivity 动态详情
 *
 */
@Route(path = ARouterConstant.DYNAMIC_DETAIL_ACTIVITY)
class DynamicDetailActivity : BaseVMActivity<DynamicDetailViewModel>() {

    companion object {
        fun start(activity: Activity, postId: Long) {
            val intent = Intent(activity, DynamicDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putLong(IntentParamKey.POST_ID.name, postId)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }

    private val commentAdapter = DynamicDetailCommentFirstAdapter()

    private var mHelper: PanelSwitchHelper? = null

    //我的主页 操作弹窗
    private var bottomDialog: BottomDialog? = null
    private val bottomDialogListener: BottomDialog.OnActionListener by lazy {
        object : BottomDialog.OnActionListener {
            override fun operate(action: BottomDialog.Action) {
                val bean = mViewModel.dynamicInfo.value?.post ?: return
                when (action.code) {
                    BottomActionCode.DELETE -> {
                        logger.info("删除动态 ${bean.postId}")
                        MyAlertDialog(this@DynamicDetailActivity).showAlertWithOKAndCancel(
                            "确定删除该动态内容？",
                            MyAlertDialog.MyDialogCallback(onRight = {
                                //删除动态
                                mHuanQueViewModel.deletePost(bean.postId)
                            }), "提示", okText = "确定"
                        )

                    }
                    BottomActionCode.REPORT -> {
                        logger.info("举报动态 ${bean.postId}")
                        val extra = Bundle()
                        extra.putLong(ParamConstant.TARGET_USER_ID, bean.userId)
                        ARouter.getInstance().build(ARouterConstant.REPORT_ACTIVITY).with(extra).navigation()
                    }
                }
            }
        }
    }

    private var mHuanQueViewModel = HuanViewModelManager.huanQueViewModel

    private val headerLayout: View by lazy {
        LayoutInflater.from(this).inflate(R.layout.layout_header_dynamic_detail, null)
    }
    var postId: Long = 0L
    override fun getLayoutId(): Int = R.layout.activity_dynamic_details

    override fun isRegisterEventBus() = true


    override fun setHeader() {
        headerPageView.initHeaderView(titleTxt = "动态", operateImg = R.mipmap.icon_more_black_01)
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        postId = intent.getLongExtra(IntentParamKey.POST_ID.name, 0L)
        mViewModel.mPostId = postId
        if (postId == 0L) {
            ToastUtils.show2("无效的ID")
            finish()
        }
        initViewModel()
        initRecyclerView()
        mRefreshLayout.setOnRefreshListener {
            mViewModel.mOffset = 0
            mViewModel.queryDetail(postId, QueryType.REFRESH)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)

        //计算RecyclerView的高度
        val recyclerHeight = ScreenUtils.getScreenHeight() - dp2px(44 + 60)
        val refreshParams = mRefreshLayout.layoutParams
        refreshParams.height = recyclerHeight
        mRefreshLayout.layoutParams = refreshParams

//        val rvParams = rv_comments.layoutParams
//        rvParams.height = recyclerHeight
//        rv_comments.layoutParams = rvParams

        mViewModel.queryDetail(postId, QueryType.INIT)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initEvents(rootView: View) {
        headerPageView.imageViewBack.onClickNew {
            finish()
        }
        headerPageView.imageOperation.onClickNew {
            //点击更多操作
//            mActionFragment = mActionFragment ?: DynamicDetailActionFragment.newInstance()
//            mActionFragment?.show(supportFragmentManager, "DynamicDetailActionFragment")
            val bean = mViewModel.dynamicInfo.value?.post ?: return@onClickNew
            val actions = arrayListOf<BottomDialog.Action>()
            if (bean.userId == SessionUtils.getUserId()) {
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

        rv_comments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var isUserDo = false
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isUserDo = true
                }
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
//                val viewHead: View? = linearLayoutManager.findViewByPosition(0)
//                val view: View? = linearLayoutManager.findViewByPosition(1)


//                logger.info("viewHead={viewHead?.id}  view={view?.top} firstVisibleItemPosition=$firstVisibleItemPosition lastVisibleItemPosition=$lastVisibleItemPosition ")
//                if (viewHead == null) {
////                    if (!ic_sticky_comment.isVisible() && isUserDo) {
//                        logger.info("此时需要显示粘性布局")
//                        ic_sticky_comment.show()
////                    }
//                    return
//                }
                //                if (viewHead == commentAdapter.headerLayout&&view!=null) {
//                    val top = view.top
//
//                    if (top <= dp2px(40)) {
//                        ic_sticky_comment.show()
//                    } else {
//                        ic_sticky_comment.hide()
//                    }
//                }
                val firstVisibleItemPosition: Int = linearLayoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition: Int = linearLayoutManager.findLastVisibleItemPosition()
                logger.info("firstVisibleItemPosition=$firstVisibleItemPosition lastVisibleItemPosition=$lastVisibleItemPosition ")

                when {
                    firstVisibleItemPosition == 0 && lastVisibleItemPosition == 0 -> {
                        ic_sticky_comment.hide()
                    }
                    firstVisibleItemPosition == 0 -> {
                        val view: View? = linearLayoutManager.findViewByPosition(1)
                        if (view != null) {
                            val top = view.top
                            if (top <= dp2px(40)) {
                                ic_sticky_comment.show()
                            } else {
                                ic_sticky_comment.hide()
                            }
                        }

                    }

                    firstVisibleItemPosition >= 1 -> {
                        if (firstVisibleItemPosition == 1 && lastVisibleItemPosition == 1) {
                            ic_sticky_comment.hide()//这种情况是空白页 隐藏
                        } else {
                            ic_sticky_comment.show()
                            logger.info("此时需要显示粘性布局")
                        }
                    }

                }


            }
        })
        tv_comment.onClickNew {
            ll_input.show()
            edit_text.forceLayout()
            edit_text.performClick()
            edit_text.hint = "我来评论..."
        }

        edit_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s == null) {
                    tv_send.isEnabled = false
                    return
                }
                tv_send.isEnabled = s.toString().isNotEmpty()
                if (s.toString().length > 100) {
                    edit_text.setText(s.toString().substring(0, 100))
                    edit_text.setSelection(100)
                    Toast.makeText(this@DynamicDetailActivity, "输入长度超限", Toast.LENGTH_SHORT).show()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                logger.info("Message onTextChanged")
            }
        })

        headerLayout.btn_action.onClickNew Observer@{
            val curr = mViewModel.dynamicDetailInfo.value?.getT()?.post ?: return@Observer
            mHuanQueViewModel.follow(curr.userId)
        }
        panel_emotion.onClickNew {
            //屏蔽事件
        }
        panel_emotion.mListener = object : EmojiInputListener {
            override fun onClick(type: String, emotion: Emotion) {
                val start: Int = edit_text.selectionStart
                val editable: Editable = edit_text.editableText
                val emotionSpannable: Spannable = EmojiSpanBuilder.buildEmotionSpannable(
                    this@DynamicDetailActivity,
                    emotion.text
                )
                editable.insert(start, emotionSpannable)
            }

            override fun onLongClick(type: String, view: View, emotion: Emotion) {
                //长按,显示弹窗
                showEmojiSuspend(type, view, emotion)
            }

            override fun onActionUp() {
                mEmojiPopupWindow?.dismiss()
            }

            override fun onClickDelete() {
                //点击了删除事件
                deleteInputEmoji()
            }

            override fun showPrivilegeFragment(code: String) {
            }
        }
        tv_send.onClickNew {
            //发表
            val content = edit_text.text.toString()
            edit_text.setText("")
            mViewModel.comment(content)
        }
        panel_switch_layout.onTouch { _, _ ->
            mHelper?.hookSystemBackByPanelSwitcher()
            ll_input.hide()
            false
        }

        rv_comments.mEventDispatchListener = object : EventDispatchListener {
            override fun onDispatch(ev: MotionEvent?): Boolean {
                if (ll_input.visibility == View.VISIBLE && ev?.action == MotionEvent.ACTION_DOWN) {
                    mHelper?.hookSystemBackByPanelSwitcher()
                    ll_input.hide()
                    return false
                }
                return true
            }

        }

        headerLayout.findViewById<View>(R.id.tvSort).onClickNew {
            switchPopupWindow(it ?: return@onClickNew)
        }

        headerLayout.findViewById<View>(R.id.tv_circle_name).onClickNew {
            CircleDynamicActivity.start(this, mViewModel?.dynamicInfo?.value?.post?.group?.groupId ?: return@onClickNew)
        }

        tvSort.onClickNew {
            //时间和热度切换
            switchPopupWindow(it ?: return@onClickNew)
        }

        tv_follow_num.onClickNew {
            //
            val followStatus = mViewModel.dynamicInfo.value?.post?.hasPraise ?: return@onClickNew
            if (followStatus) {
                //取消点赞
                mHuanQueViewModel.cancelPraise(mViewModel.mPostId)
            } else {
                //点赞
                mHuanQueViewModel.praise(mViewModel.mPostId)
            }
        }
        tv_share_num.onClickNew {
            //分享
            LiveShareActivity.newInstance(this, ShareFromType.Share_Dynamic, postId)
        }
        headerLayout.header_pic.onClickNew {
            val info = mViewModel.dynamicInfo.value?.post ?: return@onClickNew
            if (!info.userAnonymous)
                HomePageActivity.newInstance(this, info.userId)
        }
    }

    /**
     * 切换评论类型
     */
    private fun switchCommentType(type: String) {
        if (type == CommentOrderType.Time) {
            //切换到时间样式
            mViewModel.commentType = CommentOrderType.Time
            mViewModel.mOffset = 0
            tvSort.text = "时间"
            headerLayout.findViewById<TextView>(R.id.tvSort).text = "时间"
        } else {
            //切换到热度样式
            mViewModel.commentType = CommentOrderType.Heat
            mViewModel.mOffset = 0
            tvSort.text = "热度"
            headerLayout.findViewById<TextView>(R.id.tvSort).text = "热度"
        }
        mViewModel.commentList()
    }


    //悬浮表情
    private var mEmojiPopupWindow: PopupWindow? = null

    /**
     * 显示表情悬浮效果
     */
    private fun showEmojiSuspend(type: String, view: View, emotion: Emotion) {
        if (mEmojiPopupWindow?.isShowing == true) {
            return
        }
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val content = emotion.text
        var dx = 0
        var dy = 0
        var rootView: View? = null
        when (type) {
            EmojiType.NORMAL -> {
                rootView =
                    LayoutInflater.from(this).inflate(R.layout.fragment_normal_emoji_suspend, null)
                mEmojiPopupWindow = PopupWindow(rootView, dip(50), dip(66))
                val drawable = GlobalUtils.getDrawable(R.drawable.bg_emoji_suspend)
                mEmojiPopupWindow?.setBackgroundDrawable(drawable)
                dx = location[0] + (view.width - dip(50)) / 2
                dy = location[1] - dip(66) + dip(13)
                rootView.findViewById<ImageView>(R.id.iv_emoji)?.imageResource = emotion.drawableRes
            }
            else -> {

            }
        }

        if (rootView == null) {
            return
        }


        val name = content.substring(content.indexOf("[") + 1, content.indexOf("]"))
        rootView.findViewById<TextView>(R.id.tv_emoji)?.text = name

        mEmojiPopupWindow?.isOutsideTouchable = false
        mEmojiPopupWindow?.showAtLocation(view, Gravity.TOP or Gravity.LEFT, dx, dy)
    }


    // 删除光标所在前一位(不考虑切换到emoji时的光标位置，直接删除最后一位)
    private fun deleteInputEmoji() {
        val keyCode = KeyEvent.KEYCODE_DEL
        val keyEventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val keyEventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        edit_text.onKeyDown(keyCode, keyEventDown)
        edit_text.onKeyUp(keyCode, keyEventUp)
    }

    private fun initViewModel() {

        mViewModel.dynamicDetailInfo.observe(this, Observer {
            if (it.isSuccess()) {
                renderData(it.requireT())
            } else {
//                loadDataFail(it.isRefresh())
                val errorCode = it.error?.busiCode ?: return@Observer
                if (errorCode == 501) {
                    MyAlertDialog(this).showAlertWithOK(
                        it.error?.busiMessage ?: "当前内容被删除，无法查看",
                        MyAlertDialog.MyDialogCallback(onRight = {
                            //删除动态
//                        mViewModel.deletePost()
                            finish()
                        }, onCancel = {
                            finish()
                        }), "提示", okText = "确定"
                    )
                }

            }
            mRefreshLayout.isRefreshing = false
        })
        mViewModel.commentSuccessData.observe(this, Observer {
            if (it != null) {
                //评论成功
                val firstCommentId = it.firstCommentId
                if (firstCommentId == 0L) {
                    //1级评论
                    commentAdapter.addData(0, it)
                } else {
                    //2级评论
                    var notifyIndex = -1
                    commentAdapter.data.forEachIndexed { index, adapterItem ->
                        if (adapterItem.commentId == it.firstCommentId) {
                            adapterItem.secondComments.add(0, it)
                            adapterItem.commentNum += 1
                            notifyIndex = index
                            return@forEachIndexed
                        }
                    }
                    if (notifyIndex >= 0) {
                        commentAdapter.notifyDataSetChanged()
                    }
                }
                mHelper?.hookSystemBackByPanelSwitcher()
            }
        })
        mViewModel.commentListResult.observe(this, Observer {
            if (it != null) {
                if (it.isPull) {
                    commentAdapter.setList(it.list)
                } else {
                    commentAdapter.addData(it.list)
                }

                if (it.hasMore) {
                    //有更多
                    commentAdapter.loadMoreModule.loadMoreComplete()
                } else {
                    //没有更多
                    commentAdapter.loadMoreModule.loadMoreEnd()
                }

            }
        })

        mViewModel.secondCommentListResult.observe(this, Observer {
            if (it != null) {
                var notifyIndex = -1
                val commentList = it.list
                if (commentList.isNotEmpty()) {
                    val parentId = commentList[0].parentCommentId
                    commentAdapter.data.forEachIndexed { index, adapterItem ->
                        if (adapterItem.commentId == parentId) {
                            //去重之后再添加
                            val secondCommentIds = mutableListOf<Long>()
                            adapterItem.secondComments.forEach { sc ->
                                secondCommentIds.add(sc.commentId)
                            }
                            val realComment = mutableListOf<DynamicComment>()
                            commentList.forEach { needAddComment ->
                                if (!secondCommentIds.contains(needAddComment.commentId)) {
                                    realComment.add(needAddComment)
                                }

                            }
                            adapterItem.secondComments.addAll(realComment)
                            notifyIndex = index
                            adapterItem.hasMore = it.hasMore
                            return@forEachIndexed
                        }
                    }
                    if (notifyIndex >= 0) {
                        commentAdapter.notifyDataSetChanged()
                    }
                }
            }
        })

//        mViewModel.deleteFlag.observe(this, Observer {
//            if (it != null) {
//                MyAlertDialog(this).showAlertWithOKAndCancel(
//                    "确定删除该动态内容？",
//                    MyAlertDialog.MyDialogCallback(onRight = {
//                        //删除动态
////                        mViewModel.deletePost()
//                        mHuanQueViewModel.deletePost(mViewModel.mPostId)
//                    }), "提示", okText = "确定"
//                )
//            }
//        })
//        mViewModel.deletedData.observe(this, Observer {
//            if (it == true) {
//                //动态已经删除
//                ToastUtils.show("内容删除成功")
//                finish()
//            }
//        })

        mViewModel.commentPraiseResult.observe(this, Observer {
            if (it != null) {
                commentAdapter.notifyDataSetChanged()
            }
        })
        mViewModel.commentNumData.observe(this, Observer {
            if (it != null) {
                val commentConetnt = if (it == 0L) {
                    "评论"
                } else {
                    "$it"
                }
                tv_comment_num.text = commentConetnt
            }
        })
        mViewModel.commentDeleted.observe(this, Observer {
            if (it != null) {
                if (it.firstCommentId == 0L) {
                    //删除的是1级评论
                    commentAdapter.remove(it)
                } else {
                    //删除的是2级评论
                    var tempIndex = -1
                    commentAdapter.data.forEachIndexed { index, dynamicComment ->
                        if (it.firstCommentId == dynamicComment.commentId) {
                            //找到对应的1级评论
                            dynamicComment.secondComments.remove(it)
                            val commentNum = max(0, dynamicComment.commentNum - 1)
                            dynamicComment.commentNum = commentNum
                            tempIndex = index
                            return@forEachIndexed
                        }
                    }
                    if (tempIndex >= 0) {
                        commentAdapter.notifyDataSetChanged()
                    }
                }
            }
        })

        mHuanQueViewModel.dynamicChangeResult.observe(this, Observer {
            if (it != null) {
                if (mViewModel.dynamicDetailInfo.value?.isSuccess() != true) {
                    return@Observer
                }
                val bean = mViewModel.dynamicInfo.value?.post ?: return@Observer
                if (it.postId == mViewModel.mPostId && it.hasDelete) {
                    finish()
                    return@Observer
                }
                if (it.praise == true) {
                    //点赞成功
                    bean.hasPraise = true
                    bean.praiseNum += 1
                } else {
                    //取消点赞成功
                    bean.hasPraise = false
                    bean.praiseNum = max(0, bean.praiseNum - 1)
                }
                val followContent = if (bean.praiseNum == 0L) {
                    "点赞"
                } else {
                    "${bean.praiseNum}"
                }
                tv_follow_num.text = followContent
                tv_follow_num.isActivated = bean.hasPraise
                mViewModel.dynamicChangeFlag = true
            }
        })
        mHuanQueViewModel.userInfoStatusChange.observe(this, Observer {
            if (it.isSuccess()) {
                //处理关注状态
                val curr = mViewModel.dynamicDetailInfo.value?.getT()?.post ?: return@Observer
                val value = it.requireT()
                if ((value.follow == FollowStatus.True || value.follow == FollowStatus.Mutual) && value.userId == curr.userId) {
//                    headerPageView.textOperation.hide()
                    headerLayout.btn_action.hide()
                }
            }

        })

    }

    private fun initRecyclerView() {
        headerLayout.hide()
        commentAdapter.addHeaderView(headerLayout)
        commentAdapter.headerWithEmptyEnable = true
        commentAdapter.mSecondCommentClickListener = object : SecondCommentClickListener {
            override fun secondCommentClick(secondComment: DynamicComment) {
                mViewModel.replyingComment = secondComment
                ll_input.show()
                edit_text.forceLayout()
                edit_text.performClick()
                edit_text.hint = "回复${secondComment.nickname}"
            }

            override fun praise(secondComment: DynamicComment) {
                //点赞或者取消点赞
                if (secondComment.hasPraise) {
                    //取消点赞
                    mViewModel.cancelPraiseComment(secondComment)
                } else {
                    //点赞
                    mViewModel.praiseComment(secondComment)
                }
            }

            override fun secondLongCommentClick(view: View, secondComment: DynamicComment) {
                //我的动态
                showActionPopupWindow(view, secondComment)

            }
        }

        //预留一个很大的底部空白
        val foot = LayoutInflater.from(this).inflate(R.layout.view_bottom_holder, null)
        val lp = foot.findViewById<View>(R.id.view_id).layoutParams
        lp.height = ScreenUtils.getScreenHeight()
        commentAdapter.addFooterView(foot)

//        if (commentAdapter.data.isEmpty()) {
//            commentAdapter.setEmptyView(
//                MixedHelper.getEmptyView(
//                    this,
//                    msg = "暂无评论，快去抢沙发吧～",
//                    isImageHide = true
//                ).apply {
//                    val ll = this as LinearLayout
//                    val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(150))
//                    this.layoutParams = lp
//                }
//            )
//        }

        commentAdapter.footerWithEmptyEnable = true
        rv_comments.layoutManager = LinearLayoutManager(this)
        rv_comments.adapter = commentAdapter
        commentAdapter.setOnItemClickListener { adapter, view, position ->
            //回复的是1级评论
            val tempData = adapter.getItemOrNull(position) as? DynamicComment ?: return@setOnItemClickListener
            mViewModel.replyingComment = tempData.apply { firstCommentId = commentId }
            ll_input.show()
            edit_text.forceLayout()
            edit_text.performClick()
            edit_text.hint = "回复${tempData.nickname}"
        }
        commentAdapter.setOnItemLongClickListener { adapter, view, position ->
            //长按操作
            val tempData = adapter.getItemOrNull(position) as? DynamicComment
            showActionPopupWindow(view, tempData ?: return@setOnItemLongClickListener true)
            return@setOnItemLongClickListener true

        }
        commentAdapter.setOnItemChildClickListener { adapter, view, position ->
            val tempData = adapter.getItemOrNull(position) as? DynamicComment ?: return@setOnItemChildClickListener
            when (view.id) {
                R.id.ll_comment_more -> {
                    mViewModel.secondCommentList(tempData.commentId, 3)
                }
                R.id.tv_share_num -> {
                    //点击了分享
                    LiveShareActivity.newInstance(this, ShareFromType.Share_Comment, postId, tempData.commentId)
                }
                R.id.tv_praise, R.id.iv_praise -> {
                    if (tempData.hasPraise) {
                        mViewModel.cancelPraiseComment(tempData)
                    } else {
                        mViewModel.praiseComment(tempData)
                    }
                }
                R.id.sdv_header -> {
                    //跳转主页
                    CommonInit.getInstance().getCurrentActivity()?.let { act ->
                        HomePageActivity.newInstance(act, tempData.userId)
                    }
                }
            }
        }
        commentAdapter.loadMoreModule.setOnLoadMoreListener {
            mViewModel.commentList()
        }
    }

    private var mSwitchPopupWindow: PopupWindow? = null

    /**
     * 显示热度和时间切换的PopupWindow
     */
    private fun switchPopupWindow(parentView: View) {
        val view = LayoutInflater.from(this).inflate(R.layout.view_dynamic_switch, null)
        if (mSwitchPopupWindow == null) {
            mSwitchPopupWindow = PopupWindow(view, dp2px(94), dp2px(42 * 2 + 4))
            val drawable = GlobalUtils.getDrawable(R.mipmap.icon_dynamic_long_click)
            mSwitchPopupWindow?.setBackgroundDrawable(drawable)
            mSwitchPopupWindow?.isOutsideTouchable = true
            val tv_time = view.findViewById<View>(R.id.tv_time)
            val tv_hot = view.findViewById<View>(R.id.tv_hot)

            tv_time.onClickNew {
                //时间排序
                if (mViewModel.commentType == CommentOrderType.Time) {
                    mSwitchPopupWindow?.dismiss()
                    return@onClickNew
                }
                switchCommentType(CommentOrderType.Time)
                mSwitchPopupWindow?.dismiss()
            }
            tv_hot.onClickNew {
                //热度排序
                if (mViewModel.commentType == CommentOrderType.Heat) {
                    mSwitchPopupWindow?.dismiss()
                    return@onClickNew
                }
                switchCommentType(CommentOrderType.Heat)
                mSwitchPopupWindow?.dismiss()
            }
        }

//        val tv_time = view.findViewById<View>(R.id.tv_time)
//        val tv_hot = view.findViewById<View>(R.id.tv_hot)
//        if (mViewModel.commentType == CommentOrderType.Time) {
//            tv_time.backgroundColor = GlobalUtils.formatColor("#F5F5F5")
//            tv_hot.backgroundColor = Color.WHITE
//        } else {
//            tv_hot.backgroundColor = GlobalUtils.formatColor("#F5F5F5")
//            tv_time.backgroundColor = Color.WHITE
//        }

        mSwitchPopupWindow?.showAsDropDown(parentView)

    }


    //长按评论的操作PopupWindow
    private var mLongActionPopupWindow: PopupWindow? = null

    /**
     * 显示操作PopupWindow
     */
    private fun showActionPopupWindow(parentView: View, comment: DynamicComment) {
        val view = LayoutInflater.from(this).inflate(R.layout.view_dynamic_long_click, null)
        var tempHeight = if (comment.deleteAuth != BusiConstant.True || comment.userId == SessionUtils.getUserId()) {
            //只有2个操作按钮
            42 * 2 + 4
        } else {
            //非本人回复
            42 * 3 + 4
        }
        mLongActionPopupWindow = PopupWindow(view, dp2px(94), dp2px(tempHeight))
        val drawable = GlobalUtils.getDrawable(R.mipmap.icon_dynamic_long_click)
        mLongActionPopupWindow?.setBackgroundDrawable(drawable)
        mLongActionPopupWindow?.isOutsideTouchable = true
        val tv_copy = view.findViewById<View>(R.id.tv_copy)
        val tv_del = view.findViewById<View>(R.id.tv_del)
        val tv_report = view.findViewById<View>(R.id.tv_report)
        if (comment.userId == SessionUtils.getUserId()) {
            tv_report.hide()
        } else {
            tv_report.show()
        }
        if (comment.deleteAuth != BusiConstant.True) {
            tv_del.hide()
        } else {
            tv_del.show()
        }

        tv_copy.onClickNew {
            //复制
            GlobalUtils.copyToSharePlate(this, comment.content, "内容复制成功")
            mLongActionPopupWindow?.dismiss()
        }
        tv_del.onClickNew {
            //删除
            MyAlertDialog(this).showAlertWithOKAndCancel(
                "确定删除这条回复吗？",
                MyAlertDialog.MyDialogCallback(onRight = {
                    //删除回复
                    mViewModel.deleteComment(comment)
                }), "提示", okText = "确定"
            )
            mLongActionPopupWindow?.dismiss()
        }
        tv_report.onClickNew {
            //举报
            val extra = Bundle()
            extra.putLong(ParamConstant.TARGET_USER_ID, comment.userId)
            ARouter.getInstance().build(ARouterConstant.REPORT_ACTIVITY).with(extra).navigation()
        }

//        val pTop = parentView.top + dp2px(tempHeight + 61)
//        mLongActionPopupWindow?.showAtLocation(parentView, Gravity.TOP or Gravity.LEFT, px2dp(ScreenUtils.getScreenWidth() / 2f) - 47, pTop)
        val locationView = parentView.findViewById<View>(R.id.tv_content)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            mLongActionPopupWindow?.showAsDropDown(locationView, 26, 5, Gravity.BOTTOM or Gravity.RIGHT)
//        } else {
        mLongActionPopupWindow?.showAsDropDown(locationView)
//        }

    }

    private fun loadDataFail(isPull: Boolean) {
        if (isPull) {
            ToastUtils.show2("刷新失败")
        } else {
            commentAdapter.loadMoreModule.loadMoreFail()
        }
    }

    private var isCommentMode: Boolean = false
    private fun renderData(info: DynamicDetailInfo) {

        if (info.post?.userAnonymous == true && info.post?.deleteAuth == true || info.post?.userId == SessionUtils.getUserId()) {
            headerPageView.textTitle.text = "我的动态"
//            headerPageView.imageOperation.show()
            headerPageView.imageOperation.imageResource = R.mipmap.icon_more_black_01
        } else {
            headerPageView.textTitle.text = "Ta的动态"
//            headerPageView.imageOperation.hide()
        }
        if (info.post?.follow == true) {
            headerLayout.btn_action.hide()
        } else {
            if (info.post?.userAnonymous == true || info.post?.userId == SessionUtils.getUserId()) {
                headerLayout.btn_action.hide()
            } else {
                headerLayout.btn_action.show()
            }

        }
        val posterInfo = info.post
        if (posterInfo != null) {
            headerLayout.show()
            ImageHelper.setDefaultHeaderPic(headerLayout.header_pic, posterInfo.sex)
            headerLayout.header_pic.loadImage(posterInfo.headPic + BusiConstant.OSS_160, 46f, 46f)
            val name = if (posterInfo.nickname.length > 10) {
                "${posterInfo.nickname.substring(0, 10)}…"
            } else {
                posterInfo.nickname
            }
            val authTag = headerLayout.sd_auth_tag
            if (posterInfo.authMark.isNotEmpty()) {
                authTag.show()
                ImageUtils.loadImageWithHeight_2(authTag, posterInfo.authMark, dp2px(16))
            } else {
                authTag.hide()
            }
            headerLayout.tv_mkf_name.text = name
            headerLayout.tv_time.text = posterInfo.postTime
            headerLayout.tv_location.text = " · ${posterInfo.city}"
            if (posterInfo.city.isEmpty()) {
                headerLayout.tv_location.hide()
            } else {
                headerLayout.tv_location.show()
            }
            val emotionSpannable: Spannable = EmojiSpanBuilder.buildEmotionSpannable(
                this, posterInfo.content
            )
            headerLayout.tv_dyc_content.text = emotionSpannable
            if (posterInfo.group == null) {
                headerLayout.tv_circle_name.hide()
            } else {
                headerLayout.tv_circle_name.show()
                headerLayout.tv_circle_name.text = posterInfo.group!!.groupName
            }
            val sex = headerLayout.tv_sex

            if (posterInfo.userAnonymous) {
                sex.hide()
            } else {
                sex.text = "${posterInfo.age}"
                when (posterInfo.sex) {//Male、Female、Unknow

                    Sex.FEMALE -> {
                        val drawable = ContextCompat.getDrawable(this, R.mipmap.icon_sex_female)
                        if (drawable != null) {
                            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                            sex.setCompoundDrawables(drawable, null, null, null)
                        }
                        sex.textColor = Color.parseColor("#FF9BC5")
                        sex.backgroundResource = R.drawable.bg_shape_mkf_sex_female
                    }
                    else -> {
                        val drawable = ContextCompat.getDrawable(this, R.mipmap.icon_sex_male)
                        if (drawable != null) {
                            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                            sex.setCompoundDrawables(drawable, null, null, null)
                        }
                        sex.textColor = Color.parseColor("#58CEFF")
                        sex.backgroundResource = R.drawable.bg_shape_mkf_sex_male
                    }
                }

            }
            val rl = posterInfo.pics.map { PhotoBean(url = it) }.toMutableList()
            val list = if (rl.size > 4) {
                rl.subList(0, 4)
            } else {
                rl
            }
            when {
                list.isNotEmpty() -> {

                    if (list.size == 1) {
                        headerLayout.rv_photos.hide()
                        headerLayout.sdv_photo.show()
                        val sgSdv = headerLayout.sdv_photo
                        val rvLp = sgSdv.layoutParams as ConstraintLayout.LayoutParams
                        val pic = list[0].url
                        val map = StringHelper.parseUrlParams(pic)
                        var h = map["h"]?.toIntOrNull() ?: DynamicListAdapter.SINGLE_PHOTO_DEFAULT
                        var w = map["w"]?.toIntOrNull() ?: DynamicListAdapter.SINGLE_PHOTO_DEFAULT
                        if (h > w) {
                            if (h > DynamicListAdapter.SINGLE_PHOTO_MAX_HEIGHT) {
                                w = w * DynamicListAdapter.SINGLE_PHOTO_MAX_HEIGHT / h
                                h = DynamicListAdapter.SINGLE_PHOTO_MAX_HEIGHT
                            } else if (h < DynamicListAdapter.SINGLE_PHOTO_MINI_SIZE) {
                                //最小不能小于最小网格
//                            w = w * SINGLE_PHOTO_MINI_SIZE / h
//                            h = SINGLE_PHOTO_MINI_SIZE
                                w = DynamicListAdapter.SINGLE_PHOTO_MINI_SIZE
                                h = DynamicListAdapter.SINGLE_PHOTO_MINI_SIZE
                            }
                        } else {
                            if (w > DynamicListAdapter.SINGLE_PHOTO_MAX_WIDTH) {
                                h = DynamicListAdapter.SINGLE_PHOTO_MAX_WIDTH * h / w
                                w = DynamicListAdapter.SINGLE_PHOTO_MAX_WIDTH
                            } else if (h < DynamicListAdapter.SINGLE_PHOTO_MINI_SIZE) {
                                //最小不能小于最小网格
//                            w = w * SINGLE_PHOTO_MINI_SIZE / h
//                            h = SINGLE_PHOTO_MINI_SIZE
                                w = DynamicListAdapter.SINGLE_PHOTO_MINI_SIZE
                                h = DynamicListAdapter.SINGLE_PHOTO_MINI_SIZE
                            }
                        }
                        rvLp.height = h
                        rvLp.width = w
                        sgSdv.requestLayout()
                        sgSdv.loadImageInPx(pic, w, height = h)
                        sgSdv.onClickNew {
                            ImageActivity.start(
                                this,
                                0,
                                list.map { StringHelper.getOssImgUrl(it.url) },
                                posterInfo.userId
                            )

                        }
                    } else {
                        headerLayout.rv_photos.show()
                        headerLayout.sdv_photo.hide()
                        val rv = headerLayout.rv_photos
                        rv.setHasFixedSize(true)
                        val rvLp = rv.layoutParams as ConstraintLayout.LayoutParams
                        if (list.size >= 4) {
                            rvLp.width = DynamicListAdapter.Width_4
                        } else {
                            rvLp.width = 0
                        }
                        val spanCount = when (list.size) {
//                        1 -> {
//                            1
//                        }
                            2, 3 -> {
                                3
                            }
                            4 -> {

                                2
                            }
                            else -> 3
                        }
                        rv.layoutManager = GridLayoutManager(this, spanCount)
                        if (rv.itemDecorationCount <= 0) {
                            rv.addItemDecoration(
                                GridLayoutSpaceItemDecoration2(DynamicListAdapter.space)
                            )
                        }

                        val mPhotosAdapter = DynamicPhotosAdapter()
                        rv.adapter = mPhotosAdapter

                        mPhotosAdapter.setList(list)
                        mPhotosAdapter.totalList = rl
                        mPhotosAdapter.setOnItemClickListener { adapter, view, position ->
                            logger.info("点击了第几个图片：$position")
                            ImageActivity.start(
                                this,
                                position,
                                list.map { StringHelper.getOssImgUrl(it.url) },
                                posterInfo.userId
                            )
                        }
                    }

                }
                else -> {
                    headerLayout.rv_photos.hide()
                    headerLayout.sdv_photo.hide()
                }
            }


            val shareContent = if (posterInfo.shareNum == 0L) {
                "分享"
            } else {
                "${posterInfo.shareNum}"
            }
            tv_share_num.text = shareContent

            val commentConetnt = if (posterInfo.commentNum == 0L) {
                "评论"
            } else {
                "${posterInfo.commentNum}"
            }
            tv_comment_num.text = commentConetnt

            val followContent = if (posterInfo.praiseNum == 0L) {
                "点赞"
            } else {
                "${posterInfo.praiseNum}"
            }
            tv_follow_num.isActivated = posterInfo.hasPraise
            tv_follow_num.text = followContent

        } else {
            headerLayout.hide()
        }


        val list = info.comments.distinct()
        commentAdapter.setList(list)

        rv_comments.post {
            rv_comments.scrollToPosition(0)
        }

        if (info.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (list.isEmpty()) {
                commentAdapter.loadMoreModule.loadMoreEnd()
            } else {
                commentAdapter.loadMoreModule.isEnableLoadMore = true
                commentAdapter.loadMoreModule.loadMoreComplete()
            }
        } else {
            //防止底部没有边距
            commentAdapter.loadMoreModule.loadMoreEnd()
        }

    }

    override fun onStart() {
        super.onStart()
        if (mHelper == null) {
            mHelper = PanelSwitchHelper.Builder(this).addKeyboardStateListener {
                onKeyboardChange { visible, height ->
                    //可选实现，监听输入法变化
                }
            }.addEditTextFocusChangeListener {
                onFocusChange { _, hasFocus ->
                    //可选实现，监听输入框焦点变化
                    if (hasFocus) {
//                        scrollToBottom()
                    }
                }
            }
                .addViewClickListener {
                    onClickBefore { view ->
                        //可选实现，监听触发器的点击
                        if (view?.id == R.id.iv_emoji) {
//                            scrollToBottom()
                        }
                    }
                }.addPanelChangeListener {
                    onKeyboard {
                        //可选实现，输入法显示回调
                        iv_emoji.isSelected = false
//                        scrollToBottom()
                    }
                    onNone {
                        //可选实现，默认状态回调
                        iv_emoji.isSelected = false
                        ll_input.hide()
                        mViewModel.replyingComment = null
                    }
                    onPanel { view ->
                        //可选实现，面板显示回调
                        if (view is PrivateChatPanelView) {
                            iv_emoji.isSelected = view.id == R.id.panel_emotion
//                            scrollToBottom()
                        }
                    }
                    onPanelSizeChange { panelView, _, _, _, width, height ->
                        //可选实现，输入法动态调整时引起的面板高度变化动态回调
                        if (panelView is PanelView) {
                            when (panelView.id) {
//                            R.id.panel_emotion -> {
//                                val pagerView = findViewById<EmotionPagerView>(R.id.view_pager)
//                                val viewPagerSize: Int = height - DensityHelper.dpToPx(30)
//                                pagerView.buildEmotionViews(
//                                    findViewById<PageIndicatorView>(R.id.pageIndicatorView),
//                                    edit_text,
//                                    Emotions.getEmotions(),
//                                    width,
//                                    viewPagerSize
//                                )
//                            }
                            }
                        }
                    }
                }    //可选模式，默认true，当面板实现时内容区域是否往上滑动
//                .logTrack(true)                 //可选，默认false，是否开启log信息输出
//                .addContentScrollMeasurer(object : ContentScrollMeasurer {
//                    override fun getScrollDistance(defaultDistance: Int) = 0
//
//                    override fun getScrollViewId() = R.id.iv_background
//                })
//                .addContentScrollMeasurer(object : ContentScrollMeasurer {
//                    override fun getScrollDistance(defaultDistance: Int) =
//                        defaultDistance - unfilledHeight
//
//                    override fun getScrollViewId() = R.id.recyclerview
//                })
                .build(false)                      //可选，默认false，是否默认打开输入法

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        AudioRecordManager.destroy()
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {
                //由于上面在renderData中已经设置了空白页 这里不需要了
//                commentAdapter.setEmptyView(MixedHelper.getEmptyView(this))
                if (commentAdapter.data.isEmpty()) {
                    commentAdapter.setEmptyView(
                        MixedHelper.getEmptyView(
                            this,
                            msg = "暂无评论，快去抢沙发吧～",
                            isImageHide = true
                        ).apply {
                            val ll = this as LinearLayout
                            val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(150))
                            this.layoutParams = lp
                        }
                    )
                }
            }
            NetStateType.LOADING -> {
//                commentAdapter.setEmptyView(MixedHelper.getLoadingView(this))
            }
            NetStateType.ERROR -> {
                commentAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = this,
                        msg = state.message,
                        onClick = View.OnClickListener {
                            mViewModel.queryDetail(postId, QueryType.INIT)
                        })
                )

            }
            NetStateType.NETWORK_ERROR -> {
                commentAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = this,
                        msg = "网络错误",
                        onClick = View.OnClickListener {
                            mViewModel.queryDetail(postId, QueryType.INIT)
                        })
                )

            }
        }

    }

    override fun onViewDestroy() {
        super.onViewDestroy()

        if (mViewModel.dynamicChangeFlag) {
            //详情有修改操作，需要通知外界更新数据
            val changeBean = DynamicChangeResult(mViewModel.mPostId)
//            if (mViewModel.deleteFlag.value == true) {
//                changeBean.hasDelete = true
//            } else {
            //传递最新数据
            val postInfo = mViewModel.dynamicInfo.value?.post ?: return
//                changeBean.praise = postInfo.hasPraise
//                changeBean.share = postInfo.shareNum.toInt()
            changeBean.comment = postInfo.commentNum.toLong()
//            }
            //这里只刷新评论数 附带其他参数会导致重复+1-1
            mHuanQueViewModel.dynamicChangeResult.value = changeBean
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun shareSuccess(bean: ShareSuccessEvent) {
        val postId = bean.postId
        if (postId == mViewModel.mPostId) {


            //就是当前动态的分享
            val commentId = bean.commentId
            if (commentId == null) {
                //评论ID为空，分享的是动态
                val shareNum = (mViewModel.dynamicInfo.value?.post?.shareNum ?: 0) + 1
                mViewModel.dynamicInfo.value?.post?.shareNum = shareNum

                val shareContent = if (shareNum == 0L) {
                    "分享"
                } else {
                    "${shareNum}"
                }
                tv_share_num.text = shareContent
            } else {
                //评论ID不为空，分享的是评论
                var notifyIndex = -1
                commentAdapter.data.forEachIndexed { index, adapterItem ->
                    if (adapterItem.commentId == commentId) {
                        adapterItem.shareNum = adapterItem.shareNum + 1
                        notifyIndex = index
                        return@forEachIndexed
                    }
                }
                if (notifyIndex >= 0) {
                    commentAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}