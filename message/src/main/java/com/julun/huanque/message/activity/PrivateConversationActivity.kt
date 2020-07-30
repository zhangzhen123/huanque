package com.julun.huanque.message.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.effective.android.panel.PanelSwitchHelper
import com.effective.android.panel.interfaces.ContentScrollMeasurer
import com.effective.android.panel.view.panel.PanelView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.beans.IntimateBean
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.TargetUserObj
import com.julun.huanque.common.bean.events.ChatBackgroundChangedEvent
import com.julun.huanque.common.bean.events.UserInfoChangeEvent
import com.julun.huanque.common.bean.message.ExpressionAnimationBean
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.interfaces.EmojiInputListener
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.onTouch
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.ui.image.ImageActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.common.widgets.emotion.Emotion
import com.julun.huanque.common.widgets.emotion.Emotions
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.MessageAdapter
import com.julun.huanque.message.fragment.ChatSendGiftFragment
import com.julun.huanque.message.fragment.CopyDialogFragment
import com.julun.huanque.message.fragment.IntimateDetailFragment
import com.julun.huanque.message.fragment.SingleIntimateprivilegeFragment
import com.julun.huanque.message.viewmodel.IntimateDetailViewModel
import com.julun.huanque.message.viewmodel.PrivateConversationViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.message.ImageMessage
import io.rong.message.TextMessage
import kotlinx.android.synthetic.main.act_private_chat.*
import kotlinx.android.synthetic.main.item_header_conversions.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.px2dip
import org.jetbrains.anko.sdk23.listeners.textChangedListener
import java.util.concurrent.TimeUnit


/**
 *@创建者   dong
 *@创建时间 2020/6/30 16:55
 *@描述 私聊会话页面
 */
@Route(path = ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY)
class PrivateConversationActivity : BaseActivity() {
    companion object {

        fun newInstance(activity: Activity, targetId: Long, nickname: String = "", meetStatus: String = "", operation: String = "") {
            val intent = Intent(activity, PrivateConversationActivity::class.java)
            intent.putExtra(ParamConstant.TARGET_USER_ID, targetId)
            intent.putExtra(ParamConstant.NICKNAME, nickname)
            intent.putExtra(ParamConstant.MEET_STATUS, meetStatus)
            intent.putExtra(ParamConstant.OPERATION, operation)
            activity.startActivity(intent)
        }
    }

    //文本消息
    private val Message_Text = "Message_Text"

    //图片消息
    private val Message_Pic = "Message_Pic"

    //送礼消息
    private val Message_Gift = "Message_Gift"

    //特权表情
    private val Message_Privilege = "Message_Privilege"

    //动画表情
    private val Message_Animation = "Message_Animation"

    private var mPrivateConversationViewModel: PrivateConversationViewModel? = null

    private var mIntimateDetailViewModel: IntimateDetailViewModel? = null

    private var mHelper: PanelSwitchHelper? = null

    private val mAdapter = MessageAdapter()

    private var mLinearLayoutManager: LinearLayoutManager? = null

    private var mChatSendGiftFragment: ChatSendGiftFragment? = null

    /**
     * 欢遇弹窗
     */
    private var mIntimateDetailFragment: IntimateDetailFragment? = null

    override fun getLayoutId() = R.layout.act_private_chat

    //首次进入私聊详情的倒计时
    private var mFirstXiaoQueDisposable: Disposable? = null

    //为回复倒计时
    private var mNoReceiveDisposable: Disposable? = null

    override fun isRegisterEventBus() = true

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        header_view.backgroundColor = GlobalUtils.getColor(R.color.color_gray_three)
        header_view.imageOperation.imageResource = R.mipmap.icon_conversation_setting
        header_view.imageOperation.show()
        initViewModel()
        initRecyclerView()

        initBasic(intent)
        registerMessageEventProcessor()
    }

    /**
     * 初始化基础数据
     */
    private fun initBasic(intent: Intent?) {
        //之前是否加入过私聊
        val joined = SharedPreferencesUtils.getBoolean(SPParamKey.JOINED_PRIVATE_CHAT, false)
        if (!joined) {
            mFirstXiaoQueDisposable?.dispose()
            SharedPreferencesUtils.commitBoolean(SPParamKey.JOINED_PRIVATE_CHAT, true)
            mFirstXiaoQueDisposable = Observable.timer(3, TimeUnit.SECONDS)
                .bindUntilEvent(this, ActivityEvent.DESTROY)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (mPrivateConversationViewModel?.enableShowXiaoQue == true) {
                        iv_xiaoque.performClick()
                    }
                }, { it.printStackTrace() })
        }


        val targetID = intent?.getLongExtra(ParamConstant.TARGET_USER_ID, 0)
        val nickName = intent?.getStringExtra(ParamConstant.NICKNAME) ?: ""
        val meetStatus = intent?.getStringExtra(ParamConstant.MEET_STATUS) ?: ""
        showTitleView(nickName, meetStatus)
        mPrivateConversationViewModel?.targetIdData?.value = targetID

        mPrivateConversationViewModel?.operationType = intent?.getStringExtra(ParamConstant.OPERATION) ?: ""
        //获取基本数据
        mPrivateConversationViewModel?.chatBasic(targetID ?: return)
        //获取小鹊语料
        mPrivateConversationViewModel?.getActiveWord()
        showBackground()
    }


    /**
     * 显示标题
     */
    private fun showTitleView(nickname: String, meetStatus: String) {
        val title = header_view.textTitle
        if (nickname.isEmpty()) {
            title.text = "欢鹊"
        } else {
            title.text = nickname
        }
        val meetResource = ImageHelper.getMeetStatusResource(meetStatus)

        if (meetResource > 0) {
            //有欢遇标识
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, meetResource, 0)
            title.compoundDrawablePadding = 5
        } else {
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            title.compoundDrawablePadding = 0
        }


    }


    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mPrivateConversationViewModel = ViewModelProvider(this).get(PrivateConversationViewModel::class.java)
        mIntimateDetailViewModel = ViewModelProvider(this).get(IntimateDetailViewModel::class.java)

        mPrivateConversationViewModel?.targetIdData?.observe(this, Observer {
            if (it != null) {
                mPrivateConversationViewModel?.messageListData?.value?.clear()
                mPrivateConversationViewModel?.getMessageList(first = true)

                mPrivateConversationViewModel?.clearUnreadCount("$it")

            }
        })

        mPrivateConversationViewModel?.msgData?.observe(this, Observer {
            if (it != null) {
                //修改发送状态
                mAdapter.updateSentStatus(it)
                mPrivateConversationViewModel?.msgData?.value = null
            }
        })

        mPrivateConversationViewModel?.messageListData?.observe(this, Observer {
            if (it != null) {
                mAdapter.setNewData(it)
                scrollToBottom(true)
                showXiaoQueAuto()
            }
        })

        mPrivateConversationViewModel?.addMessageData?.observe(this, Observer {
            if (it != null) {
                mAdapter.addData(it)
                scrollToBottom(true)
                showXiaoQueAuto()
                mPrivateConversationViewModel?.addMessageData?.value = null
            }
        })

        mPrivateConversationViewModel?.messageChangeState?.observe(this, Observer {
            if (it == true) {
                mAdapter.notifyDataSetChanged()
                scrollToBottom()
                mPrivateConversationViewModel?.messageChangeState?.value = null
                showXiaoQueAuto()
            }
        })

        mPrivateConversationViewModel?.noMoreState?.observe(this, Observer {
            if (it == true) {
                mAdapter.upFetchModule.isUpFetchEnable = false
            }
        })

        mPrivateConversationViewModel?.firstSuccessState?.observe(this, Observer {
            if (it == true) {
                scrollToBottom()
                mPrivateConversationViewModel?.firstSuccessState?.value = null
            }
        })
        mPrivateConversationViewModel?.chatInfoData?.observe(this, Observer {
            if (it != null) {
                showTitleView(it.nickname, it.meetStatus)
                mAdapter.otherUserInfo = it
                mAdapter.notifyDataSetChanged()
            }
        })
        mPrivateConversationViewModel?.basicBean?.observe(this, Observer {
            if (it != null) {
                when (mPrivateConversationViewModel?.operationType) {
                    OperationType.OPEN_GIFT -> {
                        //打开礼物
                        iv_gift.performClick()
                    }
                    OperationType.CALL_PHONE -> {
                        //拨打电话
                        iv_phone.performClick()
                    }
                    else -> {
                    }
                }
                mPrivateConversationViewModel?.operationType = ""
                //刷新特权表情
                refreshPrivilegeEmoji(it.intimate)
            }
            mIntimateDetailViewModel?.basicBean?.value = it
        })

        //获取余额
        mPrivateConversationViewModel?.balance?.observe(this, Observer { })

        mPrivateConversationViewModel?.sendGiftSuccessData?.observe(this, Observer {
            if (it != null) {
                //送礼成功.发送自定义消息
                sendChatMessage(messageType = Message_Gift)
            }
        })
        mPrivateConversationViewModel?.msgFeeData?.observe(this, Observer {
            if (it != null) {
                if (it > 0 || SessionUtils.getSex() == Sex.FEMALE) {
                    //不免费
                    tv_free.hide()
                } else {
                    //免费
                    tv_free.show()
                }
            }
        })
    }

    /**
     * 刷新特权表情
     */
    private fun refreshPrivilegeEmoji(bean: IntimateBean) {
        //当前亲密度等级
        val currentLent = bean.intimateLevel
        IntimateUtil.intimatePrivilegeList.forEach {
            if (it.key == "ZSBQ") {
                val emojiLevel = it.minLevel
                panel_emotion.setIntimate(emojiLevel, currentLent)
                return
            }
        }
    }


    override fun initEvents(rootView: View) {
        header_view.imageViewBack.onClickNew {
            finish()
        }
        header_view.imageOperation.onClickNew {
            //打开会话设置
            val chatUserBean = mPrivateConversationViewModel?.chatInfoData?.value ?: return@onClickNew
            PrivateConversationSettingActivity.newInstance(this, chatUserBean.userId, chatUserBean.sex)
        }

        bottom_action.onTouch { v, event ->
            return@onTouch true
        }

        tv_send.onClickNew {
            //发送按钮
            val msgFee = mPrivateConversationViewModel?.msgFeeData?.value
            val dialogShow = SharedPreferencesUtils.getBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, false)
            if (msgFee != null && msgFee > 0 && !dialogShow) {
                //消息需要付费
                showMessageFeeDialog(true, msgFee)
                return@onClickNew
            }

            val message = edit_text.text.toString()
            sendChatMessage(message)
            edit_text.setText("")
        }

        edit_text.textChangedListener {
            afterTextChanged {
//                val editable: Editable = edit_text.editableText
//                edit_text.setText(EmojiSpanBuilder.buildEmotionSpannable(this@PrivateConversationActivity, editable.toString()))
                tv_send.isEnabled = it.toString().isNotEmpty()
            }
        }

        iv_pic.onClickNew {
            val msgFee = mPrivateConversationViewModel?.msgFeeData?.value
            val dialogShow = SharedPreferencesUtils.getBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, false)
            if (msgFee != null && msgFee > 0 && !dialogShow) {
                //消息需要付费
                showMessageFeeDialog(true, msgFee)
                return@onClickNew
            }
            checkPicPermissions()
        }
        iv_intimate.onClickNew {
            //显示欢遇弹窗
            mIntimateDetailFragment = mIntimateDetailFragment ?: IntimateDetailFragment.newInstance()
            mIntimateDetailFragment?.show(supportFragmentManager, "IntimateDetailFragment")
        }

        iv_phone.onClickNew {
            //跳转语音通话页面
            //检查权限
            checkRecordPermissions()
        }

        iv_gift.onClickNew {
            mChatSendGiftFragment = mChatSendGiftFragment ?: ChatSendGiftFragment()

            mChatSendGiftFragment?.show(this, "ChatSendGiftFragment")
        }

        iv_xiaoque.onClickNew {
            mFirstXiaoQueDisposable?.dispose()
            //点击小鹊助手
            showXiaoQueView(true)
            //显示文案
            mPrivateConversationViewModel?.let { vModel ->
                vModel.xiaoqueCountDown()
                val wordList = vModel.wordList
                if (wordList.isEmpty()) {
                    return@onClickNew
                }
                vModel.wordPosition++
                val position = vModel.wordPosition % wordList.size
                if (ForceUtils.isIndexNotOutOfBounds(position, wordList)) {
                    val word = wordList[position]
                    vModel.currentActiveWord = word
                    tv_active_content.text = "${word.wordType},\"${word.content}\""
                }
                if (vModel.wordPosition >= wordList.size - 1) {
                    //数据已经使用光，从后台获取新的数据
                    vModel.getActiveWord()
                }
            }
        }

        tv_send_exactly.onClickNew {
            //小鹊助手，直接发送
            showXiaoQueView(false)
            sendChatMessage(getActiveWord(), "", "")
        }
        tv_edit.onClickNew {
            //小鹊助手，编辑
            showXiaoQueView(false)
            val text = getActiveWord()
            edit_text.setText(text)
            //唤起键盘
            edit_text.requestFocus()
            //修改光标位置
            edit_text.setSelection(text.length);
        }

        iv_que_close.onClickNew {
            showXiaoQueView(false)
        }

        panel_emotion.mListener = object : EmojiInputListener {
            override fun onClick(type: String, emotion: Emotion) {
                //单击
                when (type) {
                    EmojiType.NORMAL -> {
                        //普通表情
                        val start: Int = edit_text.selectionStart
                        val editable: Editable = edit_text.editableText
                        val emotionSpannable: Spannable = EmojiSpanBuilder.buildEmotionSpannable(this@PrivateConversationActivity, emotion.text)
                        editable.insert(start, emotionSpannable)
                    }
                    EmojiType.PREROGATIVE -> {
                        //特权表情,直接发送
                        sendChatMessage(message = emotion.text, messageType = Message_Privilege)
                    }
                    EmojiType.ANIMATION -> {
                        //动画表情
                        //随机结果
                        val result = mPrivateConversationViewModel?.calcuteAnimationResult(emotion.text) ?: ""
                        sendChatMessage(message = emotion.text, animationResult = result, messageType = Message_Animation)
                    }
                }
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
                //显示特权弹窗
                val intimate = mPrivateConversationViewModel?.basicBean?.value?.intimate ?: return
                val currentLevel = intimate.intimateLevel
                IntimateUtil.intimatePrivilegeList.forEach {
                    if (it.key == "ZSBQ") {
                        SingleIntimateprivilegeFragment.newInstance(it, currentLevel).show(supportFragmentManager, "SingleIntimateprivilegeFragment")
                        return
                    }
                }
            }
        }
    }

    // 删除光标所在前一位(不考虑切换到emoji时的光标位置，直接删除最后一位)
    private fun deleteInputEmoji() {
        val keyCode = KeyEvent.KEYCODE_DEL
        val keyEventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val keyEventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        edit_text.onKeyDown(keyCode, keyEventDown)
        edit_text.onKeyUp(keyCode, keyEventUp)
//        var start = Selection.getSelectionStart(edit_text.text)
//        var end = Selection.getSelectionEnd(edit_text.text)
//        // 光标在第一位
//        if (start == 0 && end == 0) return
//        var sourceTxt = edit_text.text
//        if (sourceTxt.length == 0) return
//
//        // 光标后面的消息串
//        var lastMsg: String = ""
//        if (end != sourceTxt.length) {
//            lastMsg = sourceTxt.substring(end)
//        }
//        // 结束光标默认为当前光标前一位
//        var endIndex = end - 1
//        if (sourceTxt.contains(EmojiUtil.emojiRegex.toRegex())) {
//            // 光标前一位，要删除的字符
//            // 最后一位是“]”结尾，判断是否是emoji
//            var delText = sourceTxt[end - 1].toString()
//            if (delText == "]") {
//                // 光标前半段消息
//                var tempText = sourceTxt.substring(0, end)
//                var matcher = Pattern.compile(EmojiUtil.emojiRegex).matcher(tempText)
//                while (matcher.find()) {
//                    if (matcher.end() == tempText.length) {
//                        var emojiTxt = matcher.group()
//                        if (EmojiUtil.EmojiTextArray.contains(emojiTxt)) {
//                            endIndex = matcher.start()
//                        }
//                    }
//                }
//            }
//            // 0到光标之前的消息 + 删除前光标后面剩余的消息串
//            var newContent = sourceTxt.substring(0, endIndex) + lastMsg
//            // 删除后的消息串是否还包含emoji图标
//            if (newContent.contains(EmojiUtil.emojiRegex.toRegex())) {
//                chatInputEt.text = EmojiUtil.message2emoji(newContent)
//            } else {
//                chatInputEt.setText(newContent)
//            }
//        } else {
//            // 聊天消息中不包含emoji，直接删除最后一位
//            chatInputEt.setText(sourceTxt.substring(0, endIndex) + lastMsg)
//        }
//        // 设置新的光标
//        Selection.setSelection(chatInputEt.text, endIndex)
    }


    /**
     * 显示消息付费弹窗
     * @param text true 标识文字   false   标识图片
     * @param fee 价格
     */
    private fun showMessageFeeDialog(text: Boolean, fee: Long) {
        val dialogShow = SharedPreferencesUtils.getBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, false)
        if (!dialogShow) {
            //未显示过价格弹窗，显示弹窗
            MyAlertDialog(this).showAlertWithOKAndCancel(
                "私信消息${fee}鹊币/条",
                MyAlertDialog.MyDialogCallback(onRight = {
                    SharedPreferencesUtils.commitBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, true)
                    if (text) {
                        tv_send.performClick()
                    } else {
                        iv_pic.performClick()
                    }
                }, onCancel = {
                    SharedPreferencesUtils.commitBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, true)
                }), "私信消息费用", "继续发送"
            )
        } else {
            if (text) {
                tv_send.performClick()
            } else {
                iv_pic.performClick()
            }
        }
    }


    //悬浮表情
    private var mEmojiPopupWindow: PopupWindow? = null


    /**
     * 显示表情悬浮效果
     */
    private fun showEmojiSuspend(type: String, view: View, emotion: Emotion) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        var dx = 0
        var dy = 0
        var rootView: View? = null
        when (type) {
            EmojiType.NORMAL -> {
                rootView = LayoutInflater.from(this).inflate(R.layout.fragment_normal_emoji_suspend, null)
                mEmojiPopupWindow = PopupWindow(rootView, dip(50), dip(66))
                val drawable = GlobalUtils.getDrawable(R.drawable.bg_emoji_suspend)
                mEmojiPopupWindow?.setBackgroundDrawable(drawable)
                dx = location[0] + (view.width - dip(50)) / 2
                dy = location[1] - dip(66) + dip(13)
            }
            EmojiType.PREROGATIVE -> {
                rootView = LayoutInflater.from(this).inflate(R.layout.fragment_privilege_emoji_suspend, null)
                mEmojiPopupWindow = PopupWindow(rootView, dip(94), dip(116))
                val drawable = GlobalUtils.getDrawable(R.drawable.bg_expression_privilege_suspend)
                mEmojiPopupWindow?.setBackgroundDrawable(drawable)
                dx = location[0] + (view.width - dip(94)) / 2
                dy = location[1] - dip(116) + dip(13)
            }
            else -> {

            }
        }

        if (rootView == null) {
            return
        }

        rootView.findViewById<ImageView>(R.id.iv_emoji)?.imageResource = emotion.drawableRes
        val content = emotion.text
        val name = content.substring(content.indexOf("[") + 1, content.indexOf("]"))
        rootView.findViewById<TextView>(R.id.tv_emoji)?.text = name

        mEmojiPopupWindow?.isOutsideTouchable = false
        mEmojiPopupWindow?.showAtLocation(view, Gravity.TOP or Gravity.LEFT, dx, dy)
    }

    /**
     * 显示复制视图
     */
    private fun showCopyView(view: View, content: String) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)

        val localParams = IntArray(4)
        localParams[0] = location[0]
        localParams[1] = location[1]
        localParams[2] = view.width
        localParams[3] = view.height

        CopyDialogFragment.newInstance(content, localParams).show(supportFragmentManager, "CopyDialogFragment")
    }

    /**
     * 获取小鹊助手的文案
     */
    private fun getActiveWord(): String {
        mPrivateConversationViewModel?.let { vModel ->
            val wordList = vModel.wordList
            if (wordList.isEmpty()) {
                return ""
            }
            val word = vModel.currentActiveWord
            if (word != null) {
                return word.content
            } else {
                return ""
            }
        }
        return ""
    }

    /**
     * 被动显示小鹊判断
     */
    private fun showXiaoQueAuto() {
        //小鹊提示相关
        val message = mAdapter.data.lastOrNull() ?: return
        if (message.senderUserId != "${SessionUtils.getUserId()}") {
            //对方消息
            val content = message.content
            if (content is TextMessage || content is ImageMessage) {
                //最后一条消息是对方回复的文本消息,开始倒计时
                if (mNoReceiveDisposable?.isDisposed == false) {
                    //正在倒计时
                    return
                }
                mNoReceiveDisposable = Observable.timer(10, TimeUnit.SECONDS)
                    .bindUntilEvent(this, ActivityEvent.DESTROY)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (mPrivateConversationViewModel?.enableShowXiaoQue == true) {
                            iv_xiaoque.performClick()
                        }
                    }, {})
            } else {
                mNoReceiveDisposable?.dispose()
            }
        } else {
            mNoReceiveDisposable?.dispose()
        }
    }

    /**
     * 判断当前亲密等级是否可以发送图片
     */
    private fun judgeIntimateLevelForPic() {
        val intimate = mPrivateConversationViewModel?.basicBean?.value?.intimate
        if (intimate == null) {
            Toast.makeText(this, "缺少亲密度等级数据", Toast.LENGTH_SHORT).show()
            return
        }
        val currentLevel = intimate.intimateLevel
        IntimateUtil.intimatePrivilegeList.forEach {
            if (it.key == "FSTP") {
                val needLevel = it.minLevel
                if (needLevel <= currentLevel) {
                    //亲密度允许
                    goToPictureSelectPager()
                } else {
                    //亲密度等级不足
                    SingleIntimateprivilegeFragment.newInstance(it, currentLevel).show(this, "SingleIntimateprivilegeFragment")
                }
                return
            }
        }
    }


    /**
     * 判断余额
     */
    private fun judgeBalance() {
        //发起通话
        //余额
        val balance = mPrivateConversationViewModel?.balance?.value ?: 0
        //单价
        val price = mPrivateConversationViewModel?.basicBean?.value?.voiceFee ?: 0

        if (balance < price * 2) {
            //余额不足,显示余额不足弹窗
            MyAlertDialog(this).showAlertWithOK(
                "您的鹊币余额不足",
                MyAlertDialog.MyDialogCallback(onRight = {
                    //跳转充值页面
                }), "余额不足", "去充值"
            )
            return
        }

        if (mPrivateConversationViewModel?.basicBean?.value?.answer == true) {
            val bundle = Bundle()
            bundle.putString(ParamConstant.TYPE, ConmmunicationUserType.CALLING)
            bundle.putLong(ParamConstant.UserId, mPrivateConversationViewModel?.targetIdData?.value ?: return)
            ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY).with(bundle).navigation()
        } else {
            ToastUtils.show("对方关闭了语音通话服务")
        }

    }

    /**
     * 注册消息相关
     */
    private fun registerMessageEventProcessor() {
        // 私聊消息
        MessageProcessor.privateTextProcessor = object : MessageProcessor.PrivateMessageReceiver {
            override fun processMessage(msg: Message) {
                val targetId = mPrivateConversationViewModel?.targetIdData?.value
                if (msg.targetId == "$targetId") {
                    //就是当前的消息，直接显示
                    mPrivateConversationViewModel?.addMessage(msg)
//                    scrollToBottom()
                }
            }
        }

        //亲密度变化消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.IntimateChangeProcessor {
            override fun process(data: IntimateBean) {
                val userIds = data.userIds
                val targetId = mPrivateConversationViewModel?.targetIdData?.value ?: 0
                if (userIds.contains(SessionUtils.getUserId()) && userIds.contains(targetId)) {
                    //当前两个人亲密度发生变化
                    //更新数据库标识
                    var updateDataBase = false
                    val basicData = mPrivateConversationViewModel?.basicBean?.value
                    basicData?.intimate?.apply {
                        if (data.intimateLevel != intimateLevel) {
                            //亲密度等级发生变化，需要更新数据库
                            updateDataBase = true
                        }
                        intimateLevel = data.intimateLevel
                        nextIntimateLevel = data.nextIntimateLevel
                        intimateNum = data.intimateNum
                        nextIntimateNum = data.nextIntimateNum
                    }
                    mPrivateConversationViewModel?.basicBean?.value = basicData
                    if (updateDataBase) {
                        //需要更新数据库
                        val stranger = data.stranger[targetId] ?: false
                        mPrivateConversationViewModel?.updateIntimate(data.intimateLevel, stranger)
                    }
                    if (data.msgFree) {
                        //消息免费
                        mPrivateConversationViewModel?.msgFeeData?.value = 0L
                    }
                }
            }
        })
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
            }
                .addViewClickListener {
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
//                            R.id.panel_emotion -> {
//                                val pagerView = findViewById<EmotionPagerView>(R.id.view_pager)
//                                val viewPagerSize: Int = height - DensityUtils.dpToPx(30)
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
                .addContentScrollMeasurer(object : ContentScrollMeasurer {
                    override fun getScrollDistance(defaultDistance: Int) = 0

                    override fun getScrollViewId() = R.id.iv_background
                })
                .addContentScrollMeasurer(object : ContentScrollMeasurer {
                    override fun getScrollDistance(defaultDistance: Int) = defaultDistance - unfilledHeight

                    override fun getScrollViewId() = R.id.recyclerview
                })
                .build(false)                      //可选，默认false，是否默认打开输入法

            recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager
                    if (layoutManager is LinearLayoutManager) {
                        val childCount = recyclerView.childCount
                        if (childCount > 0) {
                            val lastChildView = recyclerView.getChildAt(childCount - 1)
                            val bottom = lastChildView.bottom
                            val listHeight: Int = recyclerview.height - recyclerview.paddingBottom
                            unfilledHeight = listHeight - bottom
                        }
                    }
                }
            })
        }
    }

    private var unfilledHeight = 0

    private fun initRecyclerView() {
        mLinearLayoutManager = LinearLayoutManager(this)
        recyclerview.layoutManager = mLinearLayoutManager
        recyclerview.adapter = mAdapter
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            val tempData = mAdapter.getItem(position)
            when (view.id) {
                R.id.iv_send_fail -> {
                    //重新发送消息
                    MyAlertDialog(this).showAlertWithOKAndCancel(
                        "确定重发该消息吗？",
                        MyAlertDialog.MyDialogCallback(onRight = {
                            resendMessage(tempData)
                        }), hasTitle = false, okText = "确定"
                    )
                }
                R.id.sdv_image -> {
                    //查看图片
                    val content = tempData.content
                    if (content is ImageMessage) {
                        //todo 添加本地地址查看
                        ImageActivity.start(this, 0, medias = listOf(StringHelper.getOssImgUrl("${content.remoteUri}")))
                    }
                }
                R.id.sdv_header -> {
                    //跳转他人主页
                    if (tempData.senderUserId == "${SessionUtils.getUserId()}") {
                        //本人发送消息
                        RNPageActivity.start(
                            this,
                            RnConstant.PERSONAL_HOMEPAGE,
                            Bundle().apply { putLong("userId", SessionUtils.getUserId()) })
                    } else {
                        //对方发送消息
                        try {
                            RNPageActivity.start(
                                this,
                                RnConstant.PERSONAL_HOMEPAGE,
                                Bundle().apply { putLong("userId", tempData.targetId.toLong()) })
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                R.id.tv_content -> {
                    val content = tempData.content
                    if (content is CustomSimulateMessage) {
                        if (content.type == MessageCustomBeanType.Voice_Conmmunication_Simulate) {
                            //点击的是语音消息,直接拨打电话
                            iv_phone.performClick()
                        }
                    }
                }
            }
        }

        mAdapter.setOnItemChildLongClickListener { adapter, view, position ->
            val tempData = mAdapter.getItem(position)
            when (view.id) {
                R.id.tv_content -> {
                    val content = tempData.content
                    if (content is TextMessage) {
                        //显示复制弹窗
                        showCopyView(view, content.content)
                    }
                }
                else -> {

                }
            }
            return@setOnItemChildLongClickListener true
        }

        mAdapter.upFetchModule.isUpFetchEnable = true
        //预加载2个position
        mAdapter.upFetchModule.startUpFetchPosition = 2
        mAdapter.upFetchModule.setOnUpFetchListener {
            val currLast = mAdapter.getItemOrNull(0)
            mPrivateConversationViewModel?.getMessageList(
                currLast?.messageId ?: return@setOnUpFetchListener
            )
            mAdapter.upFetchModule.isUpFetching = true
        }
    }

    /**
     * 重发消息
     */
    private fun resendMessage(msg: Message) {
        if (msg.messageId != 0) {
            mPrivateConversationViewModel?.deleteSingleMessage(msg.messageId)
        }
        var position = -1
        mAdapter.data.forEachIndexed { index, message ->
            if (message === msg) {
                //找到对应消息
                position = index
            }
        }
        if (position > 0) {
            mAdapter.removeAt(position)
        } else {
            mAdapter.remove(msg)
        }

        val content = msg.content

        if (content is ImageMessage) {
            //图片消息,重试
            sendPicMessage(msg)
            return
        }

        var extraMap: HashMap<String, Any>? = null
        try {
            extraMap = JsonUtil.deserializeAsObject(msg.extra ?: "", HashMap::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val failType = "${extraMap?.get(ParamConstant.MSG_FAIL_TYPE)}"
            if (failType != MessageFailType.RONG_CLOUD) {
                //服务端校验未通过
                when (content) {
                    is TextMessage -> {
                        //重发文本消息
                        sendChatMessage(content.content, messageType = Message_Text)
                    }
                    is CustomMessage -> {
                        //自定义消息重发
                        when (content.type) {
                            MessageCustomBeanType.Expression_Privilege -> {
                                //特权表情
                                val extra = JsonUtil.deserializeAsObject<String>(content.context, String::class.java)
                                sendChatMessage(message = extra, messageType = Message_Privilege)
                            }
                            MessageCustomBeanType.Expression_Animation -> {
                                //动画表情
                                val bean = JsonUtil.deserializeAsObject<ExpressionAnimationBean>(content.context, ExpressionAnimationBean::class.java)
                                //重新生成结果
                                val result = mPrivateConversationViewModel?.calcuteAnimationResult(bean.name) ?: ""
                                sendChatMessage(message = bean.name, animationResult = result, messageType = Message_Animation)
                            }
                        }
                    }
//                    is ImageMessage -> {
//                        //图片消息
//                        val mRoomUserChatExtra = JsonUtil.deserializeAsObject<RoomUserChatExtra>(content.extra, RoomUserChatExtra::class.java)
//                        var targetUserObj = mRoomUserChatExtra.targetUserObj
//
//                        sendChatMessage(localPic = targetUserObj?.localPic ?: return, messageType = Message_Pic)
//                    }
                    else -> {

                    }
                }

            } else {
                //融云未发送成功
                val targerId = msg.targetId
                msg.sentStatus = Message.SentStatus.SENDING
                //重置消息ID(不重置ID，融云会沿用数据库中内容，上一步存在删除消息的操作，异步会出现问题)
                msg.messageId = 0
                //添加模拟消息
                mPrivateConversationViewModel?.addMessage(msg)
                when (content) {
                    is TextMessage -> {
                        //直接发送
                        RongCloudManager.send(msg, targerId) { result, message ->
                            if (!result) {
                                //发送不成功
                                msg.messageId = message.messageId
                                mPrivateConversationViewModel?.sendMessageFail(msg, MessageFailType.RONG_CLOUD)
                            } else {
                                msg.sentStatus = message.sentStatus
                                mPrivateConversationViewModel?.msgData?.value = msg
                            }
                        }
                    }
                    is CustomMessage -> {
                        //自定义消息重发
                        if (content.type == MessageCustomBeanType.Expression_Animation) {
                            //动画消息，重置 播放动画标识
                            msg.extra =
                                JsonUtil.seriazileAsString(GlobalUtils.addExtra(msg.extra ?: "", ParamConstant.MSG_ANIMATION_STARTED, false))
                        }
                        msg.sentStatus = Message.SentStatus.SENDING
                        sendCustomMessage(msg)
                    }
                    else -> {
                        //直接发送
                        sendCustomMessage(msg)
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
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
     * 显示小鹊提示视图
     */
    private fun showXiaoQueView(show: Boolean) {
        if (show) {
            //显示助手文案视图
            view_xiaoque.show()
            tv_send_exactly.show()
            iv_que_close.show()
            tv_edit.show()
            iv_eye.show()
            tv_active_content.show()
            view_xiaoque_top.show()
        } else {
            //隐藏助手文案视图
            view_xiaoque.hide()
            tv_send_exactly.hide()
            iv_que_close.hide()
            tv_edit.hide()
            iv_eye.hide()
            tv_active_content.hide()
            view_xiaoque_top.hide()
        }

    }


    /**
     * 发送消息
     * @param message 发送文本消息使用
     * @param pic 上传图片的地址
     * @param localPic 图片库中的di
     * @param animationResult 动画表情结果
     */
    private fun sendChatMessage(
        message: String = "",
        pic: String = "",
        localPic: String = "",
        animationResult: String = "",
        messageType: String = Message_Text
    ) {
        if (messageType == Message_Text && (TextUtils.isEmpty(message) || message.isBlank())) {
            //文本消息判断
            ToastUtils.show("输入不能为空")
            return
        }
        if (messageType == Message_Pic && pic.isEmpty()) {
            //图片消息判断
            return
        }
        val giftBean = mPrivateConversationViewModel?.sendGiftSuccessData?.value
        if (messageType == Message_Gift && giftBean == null) {
            //礼物消息判断
            return
        }
        if (giftBean != null) {
            mPrivateConversationViewModel?.sendGiftSuccessData?.value = null
        }


        val targetChatInfo = mPrivateConversationViewModel?.chatInfoData?.value ?: return
        val targetUser = TargetUserObj().apply {
            headPic = targetChatInfo.headPic
            nickname = targetChatInfo.nickname
            intimateLevel = mPrivateConversationViewModel?.basicBean?.value?.intimate?.intimateLevel ?: 0
            meetStatus = targetChatInfo.meetStatus
            userId = targetChatInfo.userId
        }

        if (targetChatInfo.userId == SessionUtils.getUserId()) {
            ToastUtils.show("不能给自己发消息")
            return
        }
        when (messageType) {
            Message_Text -> {
                //文本消息
                val msg = RongCloudManager.obtainTextMessage(message, "${targetChatInfo.userId}", targetUser)
                msg.sentStatus = Message.SentStatus.SENDING
                mPrivateConversationViewModel?.addMessage(msg)
                mPrivateConversationViewModel?.sendMsg(targetChatInfo.userId, message, targetUser, localMsg = msg)
            }

            Message_Pic -> {
                //图片消息
                val imageMessage = RongCloudManager.obtainImageMessage(
                    "${targetChatInfo.userId}",
                    localPic,
                    targetUser.apply {
                        fee = mPrivateConversationViewModel?.msgFeeData?.value ?: 0
                        this.localPic = localPic
                    },
                    Conversation.ConversationType.PRIVATE
                )

                imageMessage.senderUserId = "${SessionUtils.getUserId()}"
                imageMessage.sentStatus = Message.SentStatus.SENDING
                sendPicMessage(imageMessage)

            }
            Message_Gift -> {
                //送礼消息
                val msg = RongCloudManager.obtainCustomMessage(
                    "${targetChatInfo.userId}",
                    targetUser,
                    Conversation.ConversationType.PRIVATE,
                    MessageCustomBeanType.Gift,
                    giftBean ?: return
                )
                msg.senderUserId = "${SessionUtils.getUserId()}"
                msg.sentStatus = Message.SentStatus.SENDING
                mPrivateConversationViewModel?.addMessage(msg)
                sendCustomMessage(msg)
            }
            Message_Privilege -> {
                //特权表情消息
                val msg = RongCloudManager.obtainCustomMessage(
                    "${targetChatInfo.userId}",
                    targetUser,
                    Conversation.ConversationType.PRIVATE,
                    MessageCustomBeanType.Expression_Privilege,
                    message
                )
                msg.senderUserId = "${SessionUtils.getUserId()}"
                msg.sentStatus = Message.SentStatus.SENDING
                mPrivateConversationViewModel?.addMessage(msg)
                mPrivateConversationViewModel?.sendMsg(targetChatInfo.userId, message, targetUser, Message_Privilege, msg)
            }
            Message_Animation -> {
                //动画表情
                val bean = ExpressionAnimationBean(message, animationResult)
                val msg = RongCloudManager.obtainCustomMessage(
                    "${targetChatInfo.userId}",
                    targetUser,
                    Conversation.ConversationType.PRIVATE,
                    MessageCustomBeanType.Expression_Animation,
                    bean
                )

                msg.senderUserId = "${SessionUtils.getUserId()}"
                msg.sentStatus = Message.SentStatus.SENDING
                mPrivateConversationViewModel?.addMessage(msg)
                when (message) {
                    "[猜拳]" -> {
                        mPrivateConversationViewModel?.sendFinger(targetChatInfo.userId, animationResult, msg)
                    }
                    "[骰子]" -> {
                        mPrivateConversationViewModel?.sendDice(targetChatInfo.userId, animationResult, msg)
                    }
                    else -> {
                    }
                }

            }
            else -> {

            }
        }
    }


    /**
     * 发送自定义消息
     * 只发送，不添加模拟消息
     */
    private fun sendCustomMessage(message: Message) {
        RongCloudManager.sendCustomMessage(message) { result, msg ->
            message.messageId = msg.messageId
            message.sentStatus = msg.sentStatus
            mPrivateConversationViewModel?.msgData?.value = message
        }
    }


    /**
     * 发送图片
     */
    private fun sendPicMessage(imageMessage: Message) {
        imageMessage.messageId = 0
        imageMessage.sentStatus = Message.SentStatus.SENDING
        mPrivateConversationViewModel?.addMessage(imageMessage)

        RongCloudManager.sendMediaMessage(imageMessage) { msg, upLoader, picUrl ->
            imageMessage.messageId = msg?.messageId ?: 0
            when (msg?.sentStatus) {
                Message.SentStatus.SENDING -> {
                    //发送中
                    try {
                        mPrivateConversationViewModel?.sendPic(upLoader, imageMessage.targetId.toLong(), "$picUrl")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Message.SentStatus.FAILED -> {
                    //发送失败
                    imageMessage.sentStatus = Message.SentStatus.FAILED
                    mPrivateConversationViewModel?.msgData?.value = imageMessage
                }
                Message.SentStatus.SENT -> {
                    //发送成功
                    imageMessage.sentStatus = Message.SentStatus.SENT
                    (imageMessage.content as? ImageMessage)?.remoteUri = (msg.content as? ImageMessage)?.remoteUri
                    mPrivateConversationViewModel?.msgData?.value = imageMessage
                }
                else -> {
                }
            }

        }
    }

    /**
     * 检查图片权限
     */
    private fun checkPicPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                        //判断亲密特权
                        judgeIntimateLevelForPic()
                    }
                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                        ToastUtils.show("权限无法获取")
                    else -> {
                        logger.info("获取权限被永久拒绝")
                        val message = "无法获取到相机/存储权限，请手动到设置中开启"
                        ToastUtils.show(message)
                    }
                }

            }
    }

    /**
     * 检查录音权限
     */
    private fun checkRecordPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(Manifest.permission.RECORD_AUDIO)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                        val bundle = Bundle()
                        bundle.putString(ParamConstant.TYPE, ConmmunicationUserType.CALLING)
                        bundle.putLong(ParamConstant.UserId, mPrivateConversationViewModel?.targetIdData?.value ?: return@subscribe)
                        ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY).with(bundle).navigation()
                    }
                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                        ToastUtils.show("权限无法获取")
                    else -> {
                        logger.info("获取权限被永久拒绝")
                        val message = "无法获取到相机/存储权限，请手动到设置中开启"
                        ToastUtils.show(message)
                    }
                }

            }
    }

    /**
     *
     */
    private fun goToPictureSelectPager() {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
            .theme(R.style.picture_me_style_single)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
            .minSelectNum(1)// 最小选择数量
            .imageSpanCount(4)// 每行显示个数
            .selectionMode(PictureConfig.SINGLE)
            .previewImage(true)// 是否可预览图片
            .isCamera(true)// 是否显示拍照按钮
            .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
            .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
            //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
            .enableCrop(false)// 是否裁剪
            .compress(true)// 是否压缩
            .synOrAsy(true)//同步true或异步false 压缩 默认同步
            //.compressSavePath(getPath())//压缩图片保存地址
            .glideOverride(120, 120)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
            .isGif(false)// 是否显示gif图片
//                    .selectionMedia(selectList)// 是否传入已选图片
            .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
            //.cropCompressQuality(90)// 裁剪压缩质量 默认100
            .minimumCompressSize(100)// 小于100kb的图片不压缩
//            .cropWH(200, 200)// 裁剪宽高比，设置如果大于图片本身宽高则无效
//            .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            .hideBottomControls(true)// 是否显示uCrop工具栏，默认不显示
//            .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
            .isDragFrame(false)
//            .circleDimmedLayer(true)// 是否圆形裁剪
//            .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
//            .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
//            .rotateEnabled(false) // 裁剪是否可旋转图片
//            .scaleEnabled(true)// 裁剪是否可放大缩小图片
            .forResult(PictureConfig.CHOOSE_REQUEST)

        //结果回调onActivityResult code
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                logger.info("收到图片")
                val selectList = PictureSelector.obtainMultipleResult(data)
                for (media in selectList) {
                    Log.i("图片-----》", media.path)
                }
                if (selectList.size > 0) {
                    val media = selectList[0]
                    val path: String?
                    path = if (media.isCut && !media.isCompressed) {
                        // 裁剪过
                        media.cutPath
                    } else if (media.isCompressed || media.isCut && media.isCompressed) {
                        // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                        media.compressPath
                    } else {
                        media.path
                    }
                    logger.info("DXC  收到图片:$path，media.path = ${media.path}")
                    //media.path
                    sendChatMessage(pic = path, localPic = media.path, messageType = Message_Pic)
//                    if(!mLoadingDialog.isShowing){
//                        mLoadingDialog.showDialog()
//                    }
//                    mViewModel?.uploadHead(path)
//                    RongCloudManager.setMediaMessage()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.info("图片返回出错了")
        }
    }

    override fun onBackPressed() {
        //用户按下返回键的时候，如果显示面板，则需要隐藏
        if (mHelper != null && mHelper?.hookSystemBackByPanelSwitcher() == true) {
            return
        }
        super.onBackPressed()
    }

    override fun onViewDestroy() {
        super.onViewDestroy()
        MessageProcessor.privateTextProcessor = null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun backgroundChange(event: ChatBackgroundChangedEvent) {
        if (event.friendId == mPrivateConversationViewModel?.targetIdData?.value ?: 0) {
            //当前页面背景变化
            showBackground()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun strangerChange(event: UserInfoChangeEvent) {
        if (event.userId == mPrivateConversationViewModel?.targetIdData?.value) {
            //当前会话，陌生人状态变化
            mPrivateConversationViewModel?.basicBean?.value?.stranger = event.stranger
//            RongCloudManager.strangerChange(event.stranger)
        }
    }


    /**
     * 显示背景
     */
    private fun showBackground() {
        val key = GlobalUtils.getBackgroundKey(mPrivateConversationViewModel?.targetIdData?.value ?: 0)
        val picSource = SharedPreferencesUtils.getString(key, "")
        if (picSource.isNotEmpty()) {
            ImageUtils.loadNativeFilePath(
                iv_background,
                picSource,
                px2dip(ScreenUtils.screenWidthFloat.toInt()),
                px2dip(ScreenUtils.screenHeightFloat.toInt())
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        initBasic(intent)
    }
}