package com.julun.huanque.message.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.effective.android.panel.PanelSwitchHelper
import com.effective.android.panel.view.panel.PanelView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.events.EventMessageBean
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.onTouch
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ChatUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.emotion.EmotionPagerView
import com.julun.huanque.common.widgets.emotion.Emotions
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.MessageAdapter
import com.julun.huanque.message.viewmodel.PrivateConversationViewModel
import com.rd.PageIndicatorView
import com.rd.utils.DensityUtils
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import kotlinx.android.synthetic.main.act_private_chat.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.imageResource


/**
 *@创建者   dong
 *@创建时间 2020/6/30 16:55
 *@描述 私聊会话页面
 */
class PrivateConversationActivity : BaseActivity() {
    companion object {
        const val TARGETID = "TARGETID"
        fun newInstance(activity: Activity, targetId: String) {
            val intent = Intent(activity, PrivateConversationActivity::class.java)
            intent.putExtra(TARGETID, targetId)
            activity.startActivity(intent)
        }
    }

    private var mPrivateConversationViewModel: PrivateConversationViewModel? = null

    private var mHelper: PanelSwitchHelper? = null

    private val mAdapter = MessageAdapter()

    private var mLinearLayoutManager: LinearLayoutManager? = null

    override fun getLayoutId() = R.layout.act_private_chat

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val ivOperation = findViewById<ImageView>(R.id.ivOperation)
        ivOperation.imageResource = R.mipmap.icon_conversation_setting
        ivOperation.show()

        initViewModel()
        initRecyclerView()
        mPrivateConversationViewModel?.targetIdData?.value = intent?.getStringExtra(TARGETID)
        registerMessageEventProcessor()
        //        mPrivateConversationViewModel?.getMessageList(first = true)
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mPrivateConversationViewModel =
                ViewModelProvider(this).get(PrivateConversationViewModel::class.java)

        mPrivateConversationViewModel?.targetIdData?.observe(this, Observer {
            if (it != null) {
                mPrivateConversationViewModel?.messageListData?.value?.clear()
                mPrivateConversationViewModel?.getMessageList(first = true)
                RongIMClient.getInstance()
                        .clearMessagesUnreadStatus(Conversation.ConversationType.PRIVATE, it)
                EventBus.getDefault().post(EventMessageBean(it))
            }
        })

        mPrivateConversationViewModel?.messageListData?.observe(this, Observer {
            if (it != null) {
                mAdapter.setNewData(it)
                scrollToBottom(true)
            }
        })
        mPrivateConversationViewModel?.messageChangeState?.observe(this, Observer {
            if (it == true) {
                mAdapter.notifyDataSetChanged()
                mPrivateConversationViewModel?.messageChangeState?.value = null
            }
        })

        mPrivateConversationViewModel?.noMoreState?.observe(this, Observer {
            if (it == true) {
                mAdapter.isUpFetchEnable = false
            }
        })

        mPrivateConversationViewModel?.firstSuccessState?.observe(this, Observer {
            if (it == true) {
                scrollToBottom()
                mPrivateConversationViewModel?.firstSuccessState?.value = null
            }
        })
    }

    override fun initEvents(rootView: View) {
        findViewById<View>(R.id.ivback).onClickNew {
            finish()
        }
        findViewById<ImageView>(R.id.ivOperation).onClickNew {
            //打开会话设置
            val targetId = mPrivateConversationViewModel?.targetIdData?.value ?: return@onClickNew
            PrivateConversationSettingActivity.newInstance(this, targetId)
        }

        bottom_action.onTouch { v, event ->
            return@onTouch true
        }

        tv_send.onClickNew {
            //发送按钮
            val message = edit_text.text.toString()
            sendChatMessage(message)
            edit_text.setText("")
        }
    }

    /**
     * 注册消息相关
     */
    private fun registerMessageEventProcessor() {
        // 私聊消息
        MessageProcessor.privateTextProcessor = object : MessageProcessor.PrivateMessageReceiver {
            override fun processMessage(msg: Message) {
                if (msg.targetId == mPrivateConversationViewModel?.targetIdData?.value) {
                    //就是当前的消息，直接显示
                    mPrivateConversationViewModel?.addMessage(msg)
                    scrollToBottom()
                }
            }
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
                        scrollToBottom()
                    }
                }
            }.addViewClickListener {
                onClickBefore { view ->
                    //可选实现，监听触发器的点击
                    if (view?.id == R.id.iv_emoji) {
                        scrollToBottom()
                    }
                }
            }.addPanelChangeListener {
                onKeyboard {
                    //可选实现，输入法显示回调
                    iv_emoji.isSelected = false
                    scrollToBottom()
                }
                onNone {
                    //可选实现，默认状态回调
                    iv_emoji.isSelected = false
                }
                onPanel { view ->
                    //可选实现，面板显示回调
                    if (view is PanelView) {
                        iv_emoji.isSelected = view.id == R.id.panel_emotion
                        scrollToBottom()
                    }
                }
                onPanelSizeChange { panelView, _, _, _, width, height ->
                    //可选实现，输入法动态调整时引起的面板高度变化动态回调
                    if (panelView is PanelView) {
                        when (panelView.id) {
                            R.id.panel_emotion -> {
                                val pagerView = findViewById<EmotionPagerView>(R.id.view_pager)
                                val viewPagerSize: Int = height - DensityUtils.dpToPx(30)
                                pagerView.buildEmotionViews(
                                        findViewById<PageIndicatorView>(R.id.pageIndicatorView),
                                        edit_text,
                                        Emotions.getEmotions(),
                                        width,
                                        viewPagerSize
                                )
                            }
                        }
                    }

                }
            }.contentCanScrollOutside(false)    //可选模式，默认true，当面板实现时内容区域是否往上滑动
                    .logTrack(true)                 //可选，默认false，是否开启log信息输出
                    .build(false)                      //可选，默认false，是否默认打开输入法
        }
    }


    private fun initRecyclerView() {
        mLinearLayoutManager = LinearLayoutManager(this)
        recyclerview.layoutManager = mLinearLayoutManager
        recyclerview.adapter = mAdapter
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            val tempData = mAdapter.getItem(position) ?: return@setOnItemChildClickListener
            val conent = tempData.content
            //自定义消息
            //            var customBean: MessageActionBean? = null
            //            if (conent is CustomMessage) {
            //                //自定义消息
            //                try {
            //                    customBean = JsonUtil.deserializeAsObject<MessageActionBean>(conent.context,
            //                                                                                 MessageActionBean::class.java)
            //                } catch (e: Exception) {
            //                    e.printStackTrace()
            //                }
            //            }
            when (view.id) {
                R.id.iv_send_fail -> {
                    //重新发送消息
                    val targerId = mPrivateConversationViewModel?.targetIdData?.value
                            ?: return@setOnItemChildClickListener
                    tempData.sentStatus = Message.SentStatus.SENDING
                    adapter.getViewByPosition(recyclerview, position, R.id.iv_send_fail)?.hide()
                    adapter.getViewByPosition(recyclerview, position, R.id.send_progress)?.show()
                    RongCloudManager.send(tempData, targerId) {
                        if (it) {
                            ChatUtils.deleteSingleMessage(tempData.messageId)
                            mPrivateConversationViewModel?.deleteSingleMessage(tempData)
                        } else {
                            tempData.sentStatus = Message.SentStatus.FAILED
                            adapter.getViewByPosition(recyclerview, position, R.id.iv_send_fail)
                                    ?.show()
                            adapter.getViewByPosition(recyclerview, position, R.id.send_progress)
                                    ?.hide()
                        }
                    }
                }
            }
        }
        mAdapter.isUpFetchEnable = true
        //预加载2个position
        mAdapter.setStartUpFetchPosition(2)
        mAdapter.setUpFetchListener {
            val currLast = mAdapter.getItem(0)
            mPrivateConversationViewModel?.getMessageList(
                    currLast?.messageId ?: return@setUpFetchListener
            )
            mAdapter.isUpFetching = true
        }
    }

    private fun scrollToBottom(firstLoad: Boolean = false) {
        if (firstLoad) {
            if (mAdapter.itemCount > 0) {
                recyclerview?.scrollToPosition(mAdapter.itemCount - 1)
            }
        } else {
            edit_text.post { mLinearLayoutManager?.scrollToPosition(mAdapter.itemCount - 1) }
        }

    }


    /**
     * 发送消息
     */
    private fun sendChatMessage(message: String) {
        if (TextUtils.isEmpty(message) || message.isBlank()) {
            ToastUtils.show("输入不能为空")
            return
        }
        val targetID = mPrivateConversationViewModel?.targetIdData?.value ?: return
        RongCloudManager.send(message, targetID) {}
    }


    override fun onBackPressed() {
        //用户按下返回键的时候，如果显示面板，则需要隐藏
        if (mHelper != null && mHelper?.hookSystemBackByPanelSwitcher() == true) {
            return
        }
        super.onBackPressed();
    }

    override fun onViewDestroy() {
        super.onViewDestroy()
        MessageProcessor.privateTextProcessor = null
    }
}