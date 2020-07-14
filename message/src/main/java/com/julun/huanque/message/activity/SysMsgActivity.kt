package com.julun.huanque.message.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.SysMsgBean
import com.julun.huanque.common.bean.beans.SysMsgContent
import com.julun.huanque.common.constant.ActivityCodes
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.MessageConstants
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.SysMsgAdapter
import com.julun.huanque.message.viewmodel.SysMsgViewModel
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import kotlinx.android.synthetic.main.activity_sys_msg.*

/**
 * 官方系统消息
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/13
 */
class SysMsgActivity : BaseActivity() {

    private var mViewModel: SysMsgViewModel? = null
    private val mAdapter: SysMsgAdapter by lazy { SysMsgAdapter() }

    override fun getLayoutId(): Int = R.layout.activity_sys_msg

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val targetId = intent.getStringExtra(IntentParamKey.SYS_MSG_ID.name) ?: ""

        MixedHelper.setSwipeRefreshStytle(rlRefreshView, this)
        rvList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvList.adapter = mAdapter
        mAdapter.loadMoreModule.isEnableLoadMore = true

        prepareViewModel()
        queryData(true, targetId)
        registerMessageEventProcessor()

        //清除该会话所有未读消息
        RongIMClient.getInstance()
            .clearMessagesUnreadStatus(Conversation.ConversationType.PRIVATE, targetId, null)
    }

    override fun initEvents(rootView: View) {
        ivback.onClickNew {
            onBackPressed()
        }
        rlRefreshView.setOnRefreshListener {
            queryData(true, mViewModel?.targetId ?: return@setOnRefreshListener)
        }
        //添加加载更多的监听
        mAdapter.loadMoreModule.setOnLoadMoreListener {
            val lastItem = mAdapter.data.last() as? Message
            queryData(
                false,
                mViewModel?.targetId ?: return@setOnLoadMoreListener,
                lastItem?.messageId ?: return@setOnLoadMoreListener
            )
        }
//        mAdapter.onAdapterClickNew { _, view, _ ->
//        }
        mAdapter.onAdapterChildClickNew { _, view, _ ->
            when (view?.id) {
                R.id.llSysRootView -> {
                    val item = view.getTag(R.id.sys_msg_bean_id) as? SysMsgContent
                    customAction(item?.context ?: return@onAdapterChildClickNew)
                }
            }
        }
    }

    private fun prepareViewModel() {
        mViewModel = ViewModelProvider(this).get(SysMsgViewModel::class.java)
        mViewModel?.getSysMsgList?.observe(this, Observer {
            it ?: return@Observer
            if (it.list.isNotEmpty()) {
                if (it.isPull) {
                    mAdapter.setList(it.list)
                } else {
                    mAdapter.addData(it.list)
                }
                if (!it.hasMore) {
                    mAdapter.loadMoreModule.loadMoreEnd()
                } else {
                    mAdapter.loadMoreModule.loadMoreComplete()
                }
                hideCommonView()
            } else {
                isShowError(true, isError = false)
            }
        })
        mViewModel?.refreshErrorStats?.observe(this, Observer {
            it ?: return@Observer
            if (mAdapter.data.isEmpty()) {
                isShowError(isShow = true, isError = true)
                return@Observer
            }
            ToastUtils.show("网络异常，请重试~！")
        })
        mViewModel?.loadMoreErrorStats?.observe(this, Observer {
            it ?: return@Observer
            mAdapter.loadMoreModule.loadMoreFail()
        })
    }

    /**
     * 注册消息相关
     */
    private fun registerMessageEventProcessor() {
        // 私聊消息
        MessageProcessor.privateTextProcessor = object : MessageProcessor.PrivateMessageReceiver {
            override fun processMessage(msg: Message) {
                if (msg.targetId == mViewModel?.targetId) {
                    //就是当前的消息，直接显示
                    mAdapter.addData(0, msg)
                    scrollToTop()
                    //获取会话消息直接置为已读
                    RongIMClient.getInstance()
                        .clearMessagesUnreadStatus(
                            Conversation.ConversationType.PRIVATE,
                            msg.targetId,
                            null
                        )
                }
            }
        }
    }

    private fun queryData(isPull: Boolean, targetId: String, lastMessageId: Int = -1) {
        isShowLoading(true)
        if (isPull) {
            mViewModel?.getHistoryMessages(targetId)
        } else {
            mViewModel?.getHistoryMessages(targetId, lastMessageId)
        }
    }

    private fun customAction(info: SysMsgBean) {
        when (info.touchType) {
            MessageConstants.ACTION_URL -> {
                //H5
                ToastUtils.show("打开H5页")
            }
            MessageConstants.ACTION_None -> {
            }
            else -> {
                ToastUtils.show("没有记录的action类型 -> ${info.touchType}")
            }
        }
    }

    private fun scrollToTop() {
        if (mAdapter.itemCount > 0) {
            rvList?.post {
                rvList?.smoothScrollToPosition(0)
            }
        }
    }

    private fun isShowLoading(isShow: Boolean) {
        if (isShow) {
            commonView.show()
            commonView.showLoading("加载中")
        } else {
            commonView.hide()
        }
    }

    private fun isShowError(isShow: Boolean, isError: Boolean) {
        if (isShow) {
            commonView.show()
            if (isError) {
                commonView.showError(
                    errorTxt = "网络异常，请重试~！",
                    showBtn = true,
                    btnClick = View.OnClickListener {
                        queryData(true, mViewModel?.targetId ?: return@OnClickListener)
                    })
            } else {
                commonView.showEmpty(emptyTxt = "空数据~！")
            }
        } else {
            commonView.hide()
        }
    }

    private fun hideCommonView() {
        if (commonView.isVisible()) {
            commonView.hide()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MessageProcessor.privateTextProcessor = null
        setResult(ActivityCodes.RESPONSE_CODE_NORMAL)
    }

    override fun onBackPressed() {
        setResult(ActivityCodes.RESPONSE_CODE_NORMAL)
        super.onBackPressed()
    }
}