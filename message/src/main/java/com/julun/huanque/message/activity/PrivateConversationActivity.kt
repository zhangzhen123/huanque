package com.julun.huanque.message.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.opengl.Visibility
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.effective.android.panel.PanelSwitchHelper
import com.effective.android.panel.interfaces.ContentScrollMeasurer
import com.effective.android.panel.view.panel.PanelView
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.events.*
import com.julun.huanque.common.bean.message.ExpressionAnimationBean
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.interfaces.EmojiInputListener
import com.julun.huanque.common.interfaces.EventListener
import com.julun.huanque.common.interfaces.SystemMessageClickListener
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.UserHeartManager
import com.julun.huanque.common.manager.audio.AudioPlayerManager
import com.julun.huanque.common.manager.audio.MediaPlayFunctionListener
import com.julun.huanque.common.manager.audio.MediaPlayInfoListener
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.image.ImageActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.common.widgets.emotion.Emotion
import com.julun.huanque.common.widgets.emotion.PrivateChatPanelView
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.MessageAdapter
import com.julun.huanque.message.fragment.*
import com.julun.huanque.message.viewmodel.IntimateDetailViewModel
import com.julun.huanque.message.viewmodel.PrivateAnimationViewModel
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
import kotlinx.android.synthetic.main.act_private_chat.tv_unread_count
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.*
import java.util.concurrent.TimeUnit


/**
 *@创建者   dong
 *@创建时间 2020/6/30 16:55
 *@描述 私聊会话页面
 */
@Route(path = ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY)
class PrivateConversationActivity : BaseActivity() {
    companion object {

        /**
         * @param targetId 会话ID
         * @param nickname 对方昵称
         * @param meetStatus 欢遇标识
         * @param headerPic 头像
         */
        fun newInstance(
            activity: Activity,
            targetId: Long,
            nickname: String = "",
            meetStatus: String = "",
            operation: String = "",
            headerPic: String = "",
            fromPlayer: Boolean = false
        ) {
            val intent = Intent(activity, PrivateConversationActivity::class.java)
            intent.putExtra(ParamConstant.TARGET_USER_ID, targetId)
            intent.putExtra(ParamConstant.NICKNAME, nickname)
            intent.putExtra(ParamConstant.MEET_STATUS, meetStatus)
            intent.putExtra(ParamConstant.OPERATION, operation)
            intent.putExtra(ParamConstant.HeaderPic, headerPic)
            intent.putExtra(ParamConstant.FROM, fromPlayer)
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

    //传送门消息
    private val Send_Room = "Send_Room"

    private val mPrivateConversationViewModel: PrivateConversationViewModel by viewModels()

    private var mIntimateDetailViewModel: IntimateDetailViewModel? = null

    //动画使用的ViewModel
    private val mPrivateAnimationViewModel: PrivateAnimationViewModel by viewModels()

    private val huanQueViewModel = HuanViewModelManager.huanQueViewModel
    private var mHelper: PanelSwitchHelper? = null

    private val mAdapter = MessageAdapter()

    private var mLinearLayoutManager: LinearLayoutManager? = null

    private var mChatSendGiftFragment: PrivateSendGiftFragment? = null

    //动画使用的Fragment
    private var mAnimationFragment: PrivateAnimationFragment? = null

    //余额不足弹窗
    private var mBalanceNotFoundFragment: BaseDialogFragment? = null

    /**
     * 欢遇弹窗
     */
    private var mIntimateDetailFragment: IntimateDetailFragment? = null

    //是否显示过付费弹窗
    private var feeDialogShow = false

    //是否有官方用户（对方或者自己是官方，都是true）
    private var hasManager = false

    override fun getLayoutId() = R.layout.act_private_chat

    //首次进入私聊详情的倒计时
    private var mFirstXiaoQueDisposable: Disposable? = null

    //为回复倒计时
    private var mNoReceiveDisposable: Disposable? = null

    //直播列表
    private val mProgramListChatFragment: ProgramListInChatFragment by lazy { ProgramListInChatFragment() }

    //需要显示键盘的标识位
    private var mNeedShowKeyBoard = false

    private val audioPlayerManager: AudioPlayerManager by lazy { AudioPlayerManager(this) }

    //语音管理对象 是否初始化的标记位
    private var voiceMamagerInited = false

    private val mPrivateOrderFragment: PrivateOrderFragment by lazy { PrivateOrderFragment() }
    override fun isRegisterEventBus() = true

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val bgColor = GlobalUtils.getColor(R.color.color_gray_three)
        StatusBarUtil.setColor(this, bgColor)
        header_view.backgroundColor = bgColor
        initViewModel()
        initRecyclerView()
        feeDialogShow = SharedPreferencesUtils.getBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, false)

        initBasic(intent)
        registerMessageEventProcessor()
        mIntimateDetailViewModel?.friendId = mPrivateConversationViewModel?.targetIdData?.value ?: 0L
        mPrivateConversationViewModel?.fromPlayer = intent?.getBooleanExtra(ParamConstant.FROM, false) ?: false
        EventBus.getDefault().post(QueryUnreadCountEvent(mPrivateConversationViewModel?.fromPlayer ?: false))
//        mPrivateConversationViewModel?.propList()
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
        if (targetID == mPrivateConversationViewModel?.targetIdData?.value) {
            //就是当前用户
            return
        }
        val nickName = intent?.getStringExtra(ParamConstant.NICKNAME) ?: ""
        val meetStatus = intent?.getStringExtra(ParamConstant.MEET_STATUS) ?: ""
        val header = intent?.getStringExtra(ParamConstant.HeaderPic) ?: ""
        mAdapter.otherUserInfo = ChatUser(header)
        showTitleView(nickName, meetStatus, "")
        mPrivateConversationViewModel?.targetIdData?.value = targetID

        //显示草稿
        val draft = mPrivateConversationViewModel?.getDraft() ?: ""
        edit_text.setText(draft)
        if (draft.isNotEmpty()) {
            edit_text.setSelection(edit_text.text.toString().length)
            tv_send.isEnabled = true
            if (mHelper == null) {
                mNeedShowKeyBoard = true
            } else {
                mHelper?.toKeyboardState(true)
            }
        }

        mPrivateConversationViewModel?.operationType =
            intent?.getStringExtra(ParamConstant.OPERATION) ?: ""
        //订单ID
//        mPrivateConversationViewModel?.mFateID = intent?.getStringExtra(ParamConstant.FATE_ID)
        //获取基本数据
        mPrivateConversationViewModel?.chatBasic(targetID ?: return)
//        //获取小鹊语料
//        mPrivateConversationViewModel?.getActiveWord()
        //获取配置相关
        val cb = SPUtils.getObject<ChatBubble>(SPParamKey.PRIVATE_CHAT_BUBBLE, ChatBubble::class.java)
        if (cb == null || cb.bgc.isEmpty()) {
            mPrivateConversationViewModel?.getSetting()
        } else {
            mPrivateConversationViewModel?.bubbleData?.value = cb
        }

        showBackground()
    }


    /**
     * 显示标题
     */
    private fun showTitleView(nickname: String, meetStatus: String, userType: String, statusConent: String = "") {
        val title = tvTitle
        if (nickname.isEmpty()) {
            title.text = "欢鹊宝宝"
        } else {
            title.text = nickname
        }
        tv_online_content.text = statusConent

        if (userType == UserType.Manager) {
            //有官方标识 隐藏亲密度和欢遇标识
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icon_manager, 0)
            title.compoundDrawablePadding = 5
            iv_intimate.hide()
            tv_intimate.hide()
        } else {
            //非官方
            iv_intimate.show()
            tv_intimate.show()

            val meetResource = ImageHelper.getMeetStatusResource(meetStatus)

            if (SessionUtils.getSex() == Sex.FEMALE && meetResource > 0) {
                //女性 有欢遇标识
                title.setCompoundDrawablesWithIntrinsicBounds(0, 0, meetResource, 0)
                title.compoundDrawablePadding = 5
            } else {
                title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                title.compoundDrawablePadding = 0
            }
        }
    }


    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
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
                mAdapter.setList(it)
                scrollToBottom(true)
                showXiaoQueAuto()
            }
        })

        mPrivateConversationViewModel?.addMessageData?.observe(this, Observer {
            if (it != null) {
                addSingleMessage(it)
                mPrivateConversationViewModel?.addMessageData?.value = null
            }
        })

        mPrivateConversationViewModel?.mProgramInfo?.observe(this, Observer {
            if (it != null) {
                con_living.show()
                sdv_anchor_header.loadImage("${StringHelper.getOssImgUrl(it.coverPic)}${BusiConstant.OSS_160}")
                if (it.living == BusiConstant.True) {
                    sdv_living.show()
                    ImageUtils.loadGifImageLocal(sdv_living, R.mipmap.living_private_conversation)
                } else {
                    sdv_living.hide()
                }
            } else {
                con_living.hide()
            }
        })

        mPrivateConversationViewModel?.programListData?.observe(this, Observer {
            if (it != null && it.isNotEmpty()) {
                val firstData = it[0]
                if (firstData.programId != mPrivateConversationViewModel?.mProgramInfo?.value?.programId) {
                    val bean = ProgramInfoInConversation(
                        firstData.coverPic,
                        firstData.living,
                        firstData.programId,
                        mPrivateConversationViewModel?.mProgramInfo?.value?.viewMore ?: BusiConstant.False
                    )
                    mPrivateConversationViewModel?.mProgramInfo?.value = bean
                }
                mProgramListChatFragment.show(supportFragmentManager, "ProgramListInChatFragment")
            }
        })
        mPrivateConversationViewModel.finishState.observe(this, Observer {
            if (it == true) {
                finish()
            }
        })

//        mPrivateConversationViewModel?.propListData?.observe(this, Observer {
//            //道具列表
//            if (it != null) {
//                if (ForceUtils.isIndexNotOutOfBounds(0, it)) {
//                    val firstData = it[0]
//                    sdv_first_prop.show()
////                    sdv_first_prop.loadImage(firstData.goodsPic, 38f, 24f)
//                    sdv_first_prop.imageResource = R.mipmap.icon_msg_small
//                    tv_first_prop_count.show()
//                    tv_first_prop_count.text = "${firstData.count}"
//                } else {
//                    sdv_first_prop.hide()
//                    tv_first_prop_count.hide()
//                }
//
//                if (ForceUtils.isIndexNotOutOfBounds(1, it)) {
//                    val secondData = it[1]
//                    sdv_second_prop.show()
//                    sdv_second_prop.loadImage(secondData.goodsPic, 38f, 24f)
//                    tv_second_prop_count.show()
//                    tv_second_prop_count.text = "${secondData.count}"
//                } else {
//                    sdv_second_prop.hide()
//                    tv_second_prop_count.hide()
//                }
//            } else {
//                sdv_first_prop.hide()
//                tv_first_prop_count.hide()
//
//                sdv_second_prop.hide()
//                tv_second_prop_count.hide()
//            }
//        })

        mPrivateConversationViewModel?.messageChangeState?.observe(this, Observer {
            if (it != null) {
                val messageList = mPrivateConversationViewModel?.changeMessageList ?: return@Observer

                if (mPrivateConversationViewModel.noMoreState) {
                    showTargetUserInformation()
                }
                if (it) {
                    //添加新消息
                    mAdapter.addData(messageList)
                    scrollToBottom()
                } else {
                    //添加历史消息
                    mAdapter.addData(0, messageList)
                    mAdapter.upFetchModule.isUpFetching = false
                    mAdapter.upFetchModule.isUpFetchEnable = mPrivateConversationViewModel?.noMoreState != true
                }
                mPrivateConversationViewModel?.messageChangeState?.value = null
                mPrivateConversationViewModel?.changeMessageList?.clear()
                showXiaoQueAuto()
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
                mAdapter.otherUserInfo = it
                mAdapter.notifyDataSetChanged()
            }
        })
        mPrivateConversationViewModel?.basicBean?.observe(this, Observer {
            if (it != null) {
                showTargetUserInformation()
                //更新用户信息
                SessionUtils.setUserType(it.usr.userType)
                hasManager = it.friendUser.userType == UserType.Manager || it.usr.userType == UserType.Manager
                showTitleView(it.friendUser.nickname, it.meetStatus, it.friendUser.userType, it.friendUser.onlineStatusText)
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
                tv_intimate.text = "lv.${it.intimate.intimateLevel}"

            } else {
                mIntimateDetailViewModel?.basicBean?.value = it
            }

            if (it.guideInfo != null) {
                mPrivateOrderFragment.show(supportFragmentManager, "PrivateOrderFragment")
            }
        })

        mPrivateConversationViewModel?.propData?.observe(this, Observer {
            if (it != null) {
                //道具
                //语音券数量
                val voiceTicketCount = it.voiceTicketCnt
                //聊天券数量
                val chatTicketCount = it.chatTicketCnt
                updatePropView(voiceTicketCount, chatTicketCount)
            }
        })

        //获取余额
        mPrivateConversationViewModel?.balance?.observe(this, Observer { })

        mPrivateConversationViewModel?.sendGiftSuccessData?.observe(this, Observer {
            if (it != null) {
                mPrivateConversationViewModel?.startAnimationData?.value = it
                //送礼成功.发送自定义消息
                sendChatMessage(messageType = Message_Gift)
            }
        })
        mPrivateConversationViewModel?.msgFeeData?.observe(this, Observer {
            if (it != null) {
                showHint()
//                if (it == 0L && (SessionUtils.getSex() == Sex.MALE || (SessionUtils.getSex() == Sex.FEMALE && mPrivateConversationViewModel?.chatInfoData?.value?.sex == Sex.FEMALE))) {
//                    //免费(男性  或者  自己和对方都是女性  显示标识)
//                    tv_free.hide()
//                    edit_text.hint = "聊天免费，不消耗聊天券和鹊币"
//                } else {
//                    //不免费
//                    tv_free.hide()
//                    edit_text.hint = "聊点什么吧..."
//                }
            }
        })

        mPrivateConversationViewModel?.balanceNotEnoughFlag?.observe(this, Observer {
            if (it == true) {
//                ToastUtils.show("余额不足")
                val bundle = Bundle()
                bundle.putString(ParamConstant.TYPE, BalanceNotEnoughType.Normal)
                mBalanceNotFoundFragment =
                    mBalanceNotFoundFragment ?: ARouter.getInstance().build(ARouterConstant.BalanceNotEnoughFragment).with(bundle)
                        .navigation() as? BaseDialogFragment
                mBalanceNotFoundFragment?.show(supportFragmentManager, "BalanceNotEnoughFragment")
            }
        })

        mPrivateConversationViewModel?.sendRoomIndoData?.observe(this, Observer {
            if (it != null) {
                sendChatMessage(messageType = Send_Room)
            }
        })
        mPrivateConversationViewModel?.sendGiftShowFlag?.observe(this, Observer {
            val placeParams = view_place.layoutParams
            if (it == true) {
                placeParams.height = dip(324)
            } else {
                placeParams.height = dip(104)
            }
            scrollToBottom()
            view_place.layoutParams = placeParams
        })
        mPrivateConversationViewModel?.startAnimationData?.observe(this, Observer {
            if (it != null) {
                //将礼物添加到待播放列表
                mPrivateAnimationViewModel.giftList.add(it)
                //开始消费礼物动画
                mPrivateAnimationViewModel.startConsumeGift(mAnimationFragment)
                mPrivateConversationViewModel?.startAnimationData?.value = null
            }
        })

        mPrivateAnimationViewModel.giftData.observe(this, Observer {
            if (it != null) {
                //开始消费动画
                mAnimationFragment = mAnimationFragment ?: PrivateAnimationFragment()
                mAnimationFragment?.show(supportFragmentManager, "PrivateAnimationFragment")
            }
        })

        mPrivateConversationViewModel?.bubbleData?.observe(this, Observer {
            if (it != null) {
                val intimateLevel = mPrivateConversationViewModel?.basicBean?.value?.intimate?.intimateLevel ?: 0
                if (intimateLevel >= 4) {
                    //亲密度达到4级，有气泡权限
                    RongCloudManager.updateChatBubble(it)
                } else {
                    RongCloudManager.updateChatBubble(null)
                }
            }
        })

        huanQueViewModel.userInfoStatusChange.observe(this, Observer {
            if (it != null && it.isSuccess()) {
                val follow = it.requireT()
                if (follow.userId == mPrivateConversationViewModel.targetIdData.value) {
                    //当前会话，陌生人状态变化
                    mPrivateConversationViewModel.basicBean.value?.stranger = follow.stranger
//            RongCloudManager.strangerChange(event.stranger)
                }
            }
        })
    }

    /**
     * 设置EditText提示文案
     */
    private fun showHint() {
        val price = mPrivateConversationViewModel?.msgFeeData?.value ?: return
//        val chatFree = mPrivateConversationViewModel.basicBean.value?.chatFree ?: ""
//        if (chatFree == BusiConstant.True) {
//            //免费
//            iv_msg_card.hide()
//            tv_msg_card_count.hide()
//            edit_text.setPadding(dp2px(7), dp2px(7), dp2px(15), dp2px(7))
//        } else {
//            //收费
//            iv_msg_card.show()
//            edit_text.setPadding(dp2px(41), dp2px(7), dp2px(15), dp2px(7))
//        }

        if (price == 0L) {
            //免费
            iv_msg_card.hide()
            tv_msg_card_count.hide()
            edit_text.setPadding(dp2px(7), dp2px(7), dp2px(15), dp2px(7))
            edit_text.hint = "聊点什么吧"
            tv_msg_card_count.hide()
        } else {
            //不免费
            iv_msg_card.show()
            edit_text.setPadding(dp2px(41), dp2px(7), dp2px(15), dp2px(7))
            val ticketCount = mPrivateConversationViewModel?.propData?.value?.chatTicketCnt ?: 0
            if (ticketCount > 0) {
                edit_text.hint = "聊天券剩余${ticketCount}次"
            } else {
                edit_text.hint = "对方设置聊天收费，30鹊币/条"
            }
        }
    }


    /**
     * 更新道具视图
     */
    private fun updatePropView(voiceCount: Int, chatCount: Int) {
        showHint()
        val price = mPrivateConversationViewModel?.msgFeeData?.value ?: return
//        val chatFree = mPrivateConversationViewModel.basicBean.value?.chatFree ?: ""
        if (chatCount > 0 && iv_msg_card.visibility == View.VISIBLE && price > 0) {
            //有聊天券，并且不免费
            tv_msg_card_count.show()
            tv_msg_card_count.text = "$chatCount"
        } else {
            tv_msg_card_count.hide()
        }

        if (voiceCount > 0) {
            //有语音券
            tv_voice_card_count.show()
            tv_voice_card_count.text = "$voiceCount"
        } else {
            tv_voice_card_count.hide()
        }
//        sdv_first_prop.hide()
//        tv_first_prop_count.hide()
//        sdv_second_prop.hide()
//        tv_second_prop_count.hide()
//
//        if (voiceCount > 0) {
//            //有语音券
//            sdv_first_prop.show()
//            sdv_first_prop.imageResource = R.mipmap.icon_voice_small
//            tv_first_prop_count.show()
//            tv_first_prop_count.text = "$voiceCount"
//        }
//
//        if (chatCount > 0) {
//            //有聊天券
//            val iv: ImageView
//            val tvCount: TextView
//            if (voiceCount <= 0) {
//                //使用First
//                iv = sdv_first_prop
//                tvCount = tv_first_prop_count
//            } else {
//                //使用second
//                iv = sdv_second_prop
//                tvCount = tv_second_prop_count
//            }
//            iv.show()
//            iv.imageResource = R.mipmap.icon_msg_small
//            tvCount.show()
//            tvCount.text = "$chatCount"
//        }
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
//                panel_emotion.setIntimate(emojiLevel, currentLent, hasManager)
                panel_emotion.setIntimate(0, currentLent, hasManager)
                return
            }
        }
    }


    override fun initEvents(rootView: View) {
        ivback.onClickNew {
            finish()
        }
        tv_unread_count.onClickNew {
            finish()
        }
        ivOperation.onClickNew {
            //打开会话设置
            val chatUserBean =
                mPrivateConversationViewModel?.chatInfoData?.value ?: return@onClickNew
            PrivateConversationSettingActivity.newInstance(
                this,
                chatUserBean.userId,
                chatUserBean.sex
            )
        }

        bottom_action.onTouch { v, event ->
            return@onTouch true
        }

        tv_send.onClickNew {
            //发送按钮
            val msgFee = mPrivateConversationViewModel?.msgFeeData?.value
            if (msgFee != null && msgFee > 0 && !feeDialogShow && (mPrivateConversationViewModel?.propData?.value?.chatTicketCnt ?: 0) <= 0) {
                //消息需要付费
                showMessageFeeDialog(Message_Text, msgFee)
                return@onClickNew
            }

            val message = edit_text.text.toString()
            sendChatMessage(message)
            edit_text.setText("")
        }

        edit_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s == null) {
                    tv_send.isEnabled = false
                    return
                }
                tv_send.isEnabled = s.toString().isNotEmpty()
                if (s.toString().length > 60) {
                    edit_text.setText(s.toString().substring(0, 60))
                    edit_text.setSelection(60)
                    Toast.makeText(this@PrivateConversationActivity, "输入长度超限", Toast.LENGTH_SHORT).show()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                logger.info("Message onTextChanged")
            }
        })


        iv_pic.onClickNew {
            val msgFee = mPrivateConversationViewModel?.msgFeeData?.value

            if (msgFee != null && msgFee > 0 && !feeDialogShow && (mPrivateConversationViewModel?.propData?.value?.chatTicketCnt ?: 0) <= 0) {
                //消息需要付费
                showMessageFeeDialog(Message_Pic, msgFee)
                return@onClickNew
            }
            checkPicPermissions()
        }
        iv_intimate.onClickNew {
            //显示欢遇弹窗
            val targetId = mPrivateConversationViewModel?.targetIdData?.value ?: 0L
            if (targetId > 0) {
                RNPageActivity.start(
                    this,
                    RnConstant.INTIMATE_LEVEL_PAGE,
                    Bundle().apply { putLong("friendId", targetId) })
            }
        }

        iv_phone.onClickNew {
            //跳转语音通话页面
            //检查权限
            checkRecordPermissions()
        }

        iv_gift.onClickNew {
            mHelper?.hookSystemBackByPanelSwitcher()
            mChatSendGiftFragment = mChatSendGiftFragment ?: PrivateSendGiftFragment()

            mChatSendGiftFragment?.show(this, "PrivateSendGiftFragment")
            Observable.timer(300, TimeUnit.MILLISECONDS)
                .bindUntilEvent(this, ActivityEvent.DESTROY)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    smoothScrollToBottom()
                }, {})
        }

//        iv_xiaoque.onClickNew {
//            mFirstXiaoQueDisposable?.dispose()
//            //点击小鹊助手
//            showXiaoQueView(true)
//            //显示文案
//            mPrivateConversationViewModel?.let { vModel ->
//                vModel.xiaoqueCountDown()
//                val wordList = vModel.wordList
//                if (wordList.isEmpty()) {
//                    return@onClickNew
//                }
//                vModel.wordPosition++
//                val position = vModel.wordPosition % wordList.size
//                if (ForceUtils.isIndexNotOutOfBounds(position, wordList)) {
//                    val word = wordList[position]
//                    vModel.currentActiveWord = word
//                    tv_active_content.text = "${word.wordType}，“${word.content}”"
//                }
//                if (vModel.wordPosition >= wordList.size - 1) {
//                    //数据已经使用光，从后台获取新的数据
//                    vModel.getActiveWord()
//                }
//            }
//        }

//        tv_send_exactly.onClickNew {
//            //小鹊助手，直接发送
//            showXiaoQueView(false)
//            sendChatMessage(getActiveWord(), "", "")
//        }
//        view_xiaoque.onClickNew {
//            //小鹊助手，编辑
//            showXiaoQueView(false)
//            val text = getActiveWord()
//            edit_text.setText(text)
//            //唤起键盘
//            edit_text.requestFocus()
//            //修改光标位置
//            edit_text.setSelection(text.length);
//        }

        iv_que_close.onClickNew {
            showXiaoQueView(false)
        }

        iv_share.onClickNew {
            //传送门
            val result = hasManager || judgeIntimate("CSM", "亲密等级达到lv3才能发送传送门哦")
            if (result) {
                //有权限
                val programId = UserHeartManager.getProgramId() ?: 0
                if (programId <= 0) {
                    //不在直播间内
                    ToastUtils.show("您当前不在直播间内，无法发送传送门哦")
                } else {
                    //在直播间内，调用接口
                    mPrivateConversationViewModel?.sendRoom(programId)
                }
            }
        }

        iv_game.onClickNew {
            //显示小鹊游戏
            (ARouter.getInstance().build(ARouterConstant.BIRD_DIALOG_FRAGMENT).navigation() as? BaseDialogFragment)
                ?.show(supportFragmentManager, "BIRD_DIALOG_FRAGMENT")
        }

        recyclerview.mEventListener = object : EventListener {
            override fun onDispatch(ev: MotionEvent?) {
                if (ev?.action == MotionEvent.ACTION_DOWN) {
                    mHelper?.hookSystemBackByPanelSwitcher()
                }
            }

        }

        panel_emotion.setListener(object : EmojiInputListener {
            override fun onClick(type: String, emotion: Emotion) {
                //单击
                when (type) {
                    EmojiType.NORMAL -> {
                        //普通表情
                        val currentLength = edit_text.text.length
                        val emojiLength = emotion.text.length
                        if (currentLength + emojiLength > 60) {
                            Toast.makeText(this@PrivateConversationActivity, "输入长度超限", Toast.LENGTH_SHORT).show()
                            return
                        }
                        val start: Int = edit_text.selectionStart
                        val editable: Editable = edit_text.editableText
                        val emotionSpannable: Spannable = EmojiSpanBuilder.buildEmotionSpannable(
                            this@PrivateConversationActivity,
                            emotion.text
                        )
                        editable.insert(start, emotionSpannable)
                    }
                    EmojiType.PREROGATIVE -> {
                        //特权表情,直接发送
                        val msgFee = mPrivateConversationViewModel?.msgFeeData?.value

                        if (msgFee != null && msgFee > 0 && !feeDialogShow && (mPrivateConversationViewModel?.propData?.value?.chatTicketCnt
                                ?: 0) <= 0
                        ) {
                            //消息需要付费
                            MyAlertDialog(this@PrivateConversationActivity).showAlertWithOKAndCancel(
                                "私信消息${msgFee}鹊币/条",
                                MyAlertDialog.MyDialogCallback(onRight = {
                                    SharedPreferencesUtils.commitBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, true)
                                    feeDialogShow = true
                                    sendChatMessage(message = emotion.text, messageType = Message_Privilege)
                                }, onCancel = {
                                    SharedPreferencesUtils.commitBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, true)
                                    feeDialogShow = true
                                }, onDismiss = {
                                    SharedPreferencesUtils.commitBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, true)
                                    feeDialogShow = true
                                }), "私信消息费用", "继续发送"
                            )
                            return
                        }

                        sendChatMessage(message = emotion.text, messageType = Message_Privilege)
                    }
                    EmojiType.ANIMATION -> {
                        //动画表情
                        //随机结果
                        val result =
                            mPrivateConversationViewModel?.calcuteAnimationResult(emotion.text)
                                ?: ""

                        val msgFee = mPrivateConversationViewModel?.msgFeeData?.value
                        if (msgFee != null && msgFee > 0 && !feeDialogShow && (mPrivateConversationViewModel?.propData?.value?.chatTicketCnt
                                ?: 0) <= 0
                        ) {
                            //消息需要付费
                            MyAlertDialog(this@PrivateConversationActivity).showAlertWithOKAndCancel(
                                "私信消息${msgFee}鹊币/条",
                                MyAlertDialog.MyDialogCallback(onRight = {
                                    SharedPreferencesUtils.commitBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, true)
                                    feeDialogShow = true
                                    sendChatMessage(
                                        message = emotion.text,
                                        animationResult = result,
                                        messageType = Message_Animation
                                    )
                                }, onCancel = {
                                    SharedPreferencesUtils.commitBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, true)
                                    feeDialogShow = true
                                }, onDismiss = {
                                    SharedPreferencesUtils.commitBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, true)
                                    feeDialogShow = true
                                }), "私信消息费用", "继续发送"
                            )
                            return
                        }

                        sendChatMessage(
                            message = emotion.text,
                            animationResult = result,
                            messageType = Message_Animation
                        )
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
//                val intimate = mPrivateConversationViewModel?.basicBean?.value?.intimate ?: return
//                val currentLevel = intimate.intimateLevel
//                IntimateUtil.intimatePrivilegeList.forEach {
//                    if (it.key == "ZSBQ") {
//                        SingleIntimateprivilegeFragment.newInstance(it, currentLevel)
//                            .show(supportFragmentManager, "SingleIntimateprivilegeFragment")
//                        return
//                    }
//                }
            }
        })

        sdv_first_prop.onClickNew {
            //显示弹窗
            val propBean = mPrivateConversationViewModel?.propData?.value ?: return@onClickNew
            if (propBean.voiceTicketCnt > 0) {
                PropFragment.newInstance(true).show(supportFragmentManager, "PropFragment")
            } else {
                PropFragment.newInstance(false).show(supportFragmentManager, "PropFragment")
            }
        }
        sdv_second_prop.onClickNew {
            PropFragment.newInstance(false).show(supportFragmentManager, "PropFragment")
        }

        iv_msg_card.onClickNew {
            //打开消息券弹窗
            PropFragment.newInstance(false, mPrivateConversationViewModel.propData.value?.chatTicketCnt ?: 0)
                .show(supportFragmentManager, "PropFragment")
        }

        con_living.onClickNew {
            //直播信息视图点击
            val programInfo = mPrivateConversationViewModel?.mProgramInfo?.value ?: return@onClickNew
            if (programInfo.viewMore == BusiConstant.True) {
                //显示弹窗
                mPrivateConversationViewModel?.chatRecomList()
            } else {
                //跳转直播间
                val bundle = Bundle()
                bundle.putLong(IntentParamKey.PROGRAM_ID.name, programInfo.programId)
                bundle.putString(ParamConstant.FROM, PlayerFrom.Chat)
                val targetId = mPrivateConversationViewModel?.targetIdData?.value ?: 0L
                bundle.putString(ParamConstant.ShareUserId, "${targetId}")

                ARouter.getInstance().build(ARouterConstant.PLAYER_ACTIVITY).with(bundle).navigation()
            }

        }

        mAdapter.mSystemMessageClickListener = object : SystemMessageClickListener {
            override fun onClick(touchType: String) {
                if (touchType == IntimateTouchBean.NetCall) {
                    //语音通话
                    iv_phone.performClick()
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
    private fun showMessageFeeDialog(messageType: String, fee: Long) {
        if (!feeDialogShow) {
            //未显示过价格弹窗，显示弹窗
            MyAlertDialog(this).showAlertWithOKAndCancel(
                "私信消息${fee}鹊币/条",
                MyAlertDialog.MyDialogCallback(onRight = {
                    SharedPreferencesUtils.commitBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, true)
                    feeDialogShow = true
                    when (messageType) {
                        Message_Pic -> {
                            iv_pic.performClick()
                        }
                        Message_Text -> {
                            tv_send.performClick()
                        }
                        else -> {
                        }
                    }

                }, onCancel = {
                    SharedPreferencesUtils.commitBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, true)
                    feeDialogShow = true
                }, onDismiss = {
                    SharedPreferencesUtils.commitBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, true)
                    feeDialogShow = true
                }), "私信消息费用", "继续发送"
            )
        } else {
            when (messageType) {
                Message_Pic -> {
                    iv_pic.performClick()
                }
                Message_Text -> {
                    tv_send.performClick()
                }
                else -> {
                }
            }
        }
    }


    //悬浮表情
    private
    var mEmojiPopupWindow: PopupWindow? = null


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
            EmojiType.PREROGATIVE -> {
                rootView = LayoutInflater.from(this)
                    .inflate(R.layout.fragment_privilege_emoji_suspend, null)
                mEmojiPopupWindow = PopupWindow(rootView, dip(94), dip(116))
                val drawable = GlobalUtils.getDrawable(R.drawable.bg_expression_privilege_suspend)
                mEmojiPopupWindow?.setBackgroundDrawable(drawable)
                dx = location[0] + (view.width - dip(94)) / 2
                dy = location[1] - dip(116) + dip(13)
                val sdvEmoji = rootView.findViewById<SimpleDraweeView>(R.id.sdv_emoji)
                sdvEmoji?.loadImage(GlobalUtils.getPrivilegeUrl(content), 36f, 36f)
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

        CopyDialogFragment.newInstance(content, localParams)
            .show(supportFragmentManager, "CopyDialogFragment")
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
    private fun judgeIntimate(key: String, toastContent: String = ""): Boolean {
        val intimate = mPrivateConversationViewModel?.basicBean?.value?.intimate
        if (intimate == null) {
            ToastUtils.show(toastContent)
            return false
        }
        val currentLevel = intimate.intimateLevel
        IntimateUtil.intimatePrivilegeList.forEach {
            if (it.key == key) {
                val needLevel = it.minLevel
                return if (needLevel <= currentLevel) {
                    //亲密度允许
                    true
                } else {
                    //亲密度等级不足
                    if (toastContent.isNotEmpty()) {
                        ToastUtils.show(toastContent)
                    } else {
                        SingleIntimateprivilegeFragment.newInstance(it, currentLevel)
                            .show(this, "SingleIntimateprivilegeFragment")
                    }

                    false
                }

            }
        }
        return false
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
            bundle.putLong(
                ParamConstant.UserId,
                mPrivateConversationViewModel?.targetIdData?.value ?: return
            )
            ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY).with(bundle)
                .navigation()
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
                    //判断是否是送礼消息
                    val content = msg.content
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && content is CustomMessage) {
                        if (content.type == MessageCustomBeanType.Gift) {
                            if (!MessageUtils.getAnimationStarted(msg)) {
                                //需要播放动画
                                MessageUtils.setAnimationStarted(msg)
                                try {
                                    val str = content.context
                                    if (str.isNotEmpty()) {
                                        val chatGift = JsonUtil.deserializeAsObject<ChatGift>(str, ChatGift::class.java)
                                        mPrivateConversationViewModel?.startAnimationData?.value = chatGift
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                    addSingleMessage(msg)
                    mPrivateConversationViewModel?.doWithReceiveMessage(msg)
//                    scrollToBottom()
                }
            }
        }
        //用户经验变动消息
        MessageProcessor.registerEventProcessor(this, object : MessageProcessor.UserExpChangeMessageProcessor {
            override fun process(data: UserExpChangeEvent) {
                mPrivateConversationViewModel?.userExpChangeEvent?.value = data
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
                        if (view is PrivateChatPanelView) {
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
                .addContentScrollMeasurer(object : ContentScrollMeasurer {
                    override fun getScrollDistance(defaultDistance: Int) = 0

                    override fun getScrollViewId() = R.id.iv_background
                })
                .addContentScrollMeasurer(object : ContentScrollMeasurer {
                    override fun getScrollDistance(defaultDistance: Int) =
                        defaultDistance - unfilledHeight

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

        if (mNeedShowKeyBoard) {
            mHelper?.toKeyboardState(true)
            mNeedShowKeyBoard = false
        }
    }

    private
    var unfilledHeight = 0

    private fun initRecyclerView() {
        mLinearLayoutManager = LinearLayoutManager(this)
        recyclerview.layoutManager = mLinearLayoutManager
        recyclerview.adapter = mAdapter
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            val tempData = mAdapter.getItem(position)
            val content = tempData.content
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

                    if (content is ImageMessage) {
                        ImageActivity.start(
                            this,
                            0,
                            medias = listOf(StringHelper.getOssImgUrl("${content.remoteUri}")),
                            userId = tempData.senderUserId.toLongOrNull(),
                            operate = ImageActivityOperate.REPORT,
                            from = ImageActivityFrom.CHAT
                        )
                    }
                }
                R.id.sdv_header -> {
                    //跳转他人主页
                    if (tempData.senderUserId == "${SessionUtils.getUserId()}") {
                        //本人发送消息
                        val bundle = Bundle().apply {
                            putLong(ParamConstant.UserId, SessionUtils.getUserId())
                        }
                        ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
                    } else {
                        //对方发送消息
                        try {
                            val bundle = Bundle().apply {
                                putLong(ParamConstant.UserId, tempData.targetId.toLong())
                            }
                            ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                R.id.tv_content -> {
                    if (content is CustomSimulateMessage) {
                        if (content.type == MessageCustomBeanType.Voice_Conmmunication_Simulate) {
                            //点击的是语音消息,直接拨打电话
                            iv_phone.performClick()
                        }
                    }
                }
                R.id.con_send_room -> {
                    //传送门消息
                    if (content is CustomMessage && content.type == MessageCustomBeanType.SendRoom) {
                        try {
                            val extra = content.context
                            if (extra.isEmpty()) {
                                return@setOnItemChildClickListener
                            }
                            val info = JsonUtil.deserializeAsObject<SendRoomInfo>(extra, SendRoomInfo::class.java)
                            val bundle = Bundle()
                            bundle.putLong(IntentParamKey.PROGRAM_ID.name, info.programId)
                            bundle.putString(ParamConstant.FROM, PlayerFrom.SendRoom)
                            bundle.putString(ParamConstant.ShareUserId, tempData.senderUserId)


                            ARouter.getInstance().build(ARouterConstant.PLAYER_ACTIVITY).with(bundle).navigation()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                R.id.view_bg_gift -> {
                    //送礼消息
                    val started =
                        tempData.senderUserId == "${SessionUtils.getUserId()}" || MessageUtils.getAnimationStarted(tempData)
                    if (!started) {
                        //播放动画
                        MessageUtils.setAnimationStarted(tempData)
                        if (content is CustomMessage) {
                            if (content.type == MessageCustomBeanType.Gift) {
                                try {
                                    val str = content.context
                                    if (str.isNotEmpty()) {
                                        val chatGift = JsonUtil.deserializeAsObject<ChatGift>(str, ChatGift::class.java)
                                        mPrivateConversationViewModel?.startAnimationData?.value = chatGift
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        mAdapter.notifyItemChanged(position)
                    }
                }
                R.id.con_post_share -> {
                    //打开动态详情
                    try {
                        if (content is CustomMessage && content.type == MessageCustomBeanType.PostShare) {
                            val str = content.context
                            if (str.isEmpty()) {
                                return@setOnItemChildClickListener
                            }
                            val postShareBean = JsonUtil.deserializeAsObject<PostShareBean>(str, PostShareBean::class.java)
                            val bundle = Bundle().apply {
                                putLong(IntentParamKey.POST_ID.name, postShareBean.postId)
                            }
                            ARouter.getInstance().build(ARouterConstant.DYNAMIC_DETAIL_ACTIVITY).with(bundle).navigation()
                        }


                    } catch (e: Exception) {
                        e.printStackTrace()
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
                        showCopyView(view, content.content ?: "")
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
            if (currLast?.senderUserId == BusiConstant.System_Message) {
                //系统提示
                return@setOnUpFetchListener
            }
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
                        sendChatMessage(content.content ?: "", messageType = Message_Text)
                    }
                    is CustomMessage -> {
                        //自定义消息重发
                        when (content.type) {
                            MessageCustomBeanType.Expression_Privilege -> {
                                //特权表情
                                val extra = JsonUtil.deserializeAsObject<String>(
                                    content.context,
                                    String::class.java
                                )
                                sendChatMessage(message = extra, messageType = Message_Privilege)
                            }
                            MessageCustomBeanType.Expression_Animation -> {
                                //动画表情
                                val bean = JsonUtil.deserializeAsObject<ExpressionAnimationBean>(
                                    content.context,
                                    ExpressionAnimationBean::class.java
                                )
                                //重新生成结果
                                val result =
                                    mPrivateConversationViewModel?.calcuteAnimationResult(bean.name)
                                        ?: ""
                                sendChatMessage(
                                    message = bean.name,
                                    animationResult = result,
                                    messageType = Message_Animation
                                )
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
                                mPrivateConversationViewModel?.sendMessageFail(
                                    msg,
                                    MessageFailType.RONG_CLOUD
                                )
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
                                JsonUtil.serializeAsString(
                                    GlobalUtils.addExtra(
                                        msg.extra ?: "",
                                        ParamConstant.MSG_ANIMATION_STARTED,
                                        false
                                    )
                                )
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
     * 平滑滑动
     */
    private fun smoothScrollToBottom() {
        if (mAdapter.itemCount > 0) {
            recyclerview?.smoothScrollToPosition(mAdapter.itemCount - 1)
        }
    }


    /**
     * 显示小鹊提示视图
     */
    private fun showXiaoQueView(show: Boolean) {
//        if (show) {
//            //显示助手文案视图
//            view_xiaoque.show()
//            iv_arrow.show()
//            iv_que_close.show()
//            tv_active_content.show()
//        } else {
//            //隐藏助手文案视图
//            view_xiaoque.hide()
//            iv_arrow.hide()
//            iv_que_close.hide()
//            tv_active_content.hide()
//        }

    }

    //他人基本信息
    private var mHeaderView: View? = null

    /**
     * 显示对方用户信息
     */
    private fun showTargetUserInformation() {
        if (mPrivateConversationViewModel.noMoreState) {
            //已经获取全部消息
            val friendUser = mPrivateConversationViewModel.basicBean.value?.friendUser ?: return
            if (mHeaderView == null) {
                mHeaderView = LayoutInflater.from(this).inflate(R.layout.layout_chat_header, null)
                mAdapter.addHeaderView(mHeaderView!!)
                //增加点击事件
                val tv_audio_time = mHeaderView?.findViewById<TextView>(R.id.tv_audio_time) ?: return
                tv_audio_time.setTFDinAltB()

                val videoView = mHeaderView?.findViewById<View>(R.id.ll_audio)
                videoView?.onClickNew {
                    if (SharedPreferencesUtils.getBoolean(SPParamKey.VOICE_ON_LINE, false)) {
                        ToastUtils.show("正在语音通话，请稍后再试")
                        return@onClickNew
                    }
                    initVoiceManager()
                    //播放语音
                    if (audioPlayerManager.musicType == -1 || audioPlayerManager.mediaPlayer == null) {
                        //未设置音频地址
                        audioPlayerManager.setNetPath(
                            StringHelper.getOssAudioUrl(
                                mPrivateConversationViewModel.basicBean.value?.friendUser?.voiceUrl ?: ""
                            )
                        )
                        audioPlayerManager.start(false)
                    } else {
                        //已设置音频地址
                        if (audioPlayerManager.isPlaying) {
                            audioPlayerManager.pause()
                        } else {
                            audioPlayerManager.resume()
                        }
                    }
                }
            }
            mHeaderView?.onClickNew {
                val bundle = Bundle().apply {
                    putLong(ParamConstant.UserId, mPrivateConversationViewModel.targetIdData.value ?: return@onClickNew)
                }
                ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
            }

            mHeaderView?.findViewById<SimpleDraweeView>(R.id.sdv_header)?.loadImage("${friendUser.headPic}${BusiConstant.OSS_160}", 70f, 70f)
            mHeaderView?.findViewById<TextView>(R.id.tv_nickname)?.text = friendUser.nickname
            val tv_sex = mHeaderView?.findViewById<TextView>(R.id.tv_sex) ?: return
            val tv_location = mHeaderView?.findViewById<TextView>(R.id.tv_location) ?: return
            val tv_weight_height = mHeaderView?.findViewById<TextView>(R.id.tv_weight_height) ?: return
            val tv_sign = mHeaderView?.findViewById<TextView>(R.id.tv_sign) ?: return

            val sex = friendUser.sex
            var sexDrawable: Drawable? = null
            //性别
            when (sex) {
                Sex.MALE -> {
                    tv_sex.backgroundResource = R.drawable.bg_shape_mkf_sex_male
                    sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_male)
                    tv_sex.textColor = Color.parseColor("#58CEFF")
                }
                Sex.FEMALE -> {
                    tv_sex.backgroundResource = R.drawable.bg_shape_mkf_sex_female
                    sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_female)
                    tv_sex.textColor = Color.parseColor("#FF9BC5")
                }
                else -> sexDrawable = null
            }
            if (sexDrawable != null) {
                sexDrawable.setBounds(0, 0, sexDrawable.minimumWidth, sexDrawable.minimumHeight)
                tv_sex.setCompoundDrawables(sexDrawable, null, null, null)
            } else {
                tv_sex.setCompoundDrawables(null, null, null, null)
            }
            //性别+星座
            val sexContent = "${friendUser.age} | ${friendUser.constellation}"
            val sexStringSpannable = SpannableStringBuilder(sexContent)
            val sexStartIndex = "${friendUser.age} ".length
            val lightColor = if (sex == Sex.FEMALE) {
                GlobalUtils.formatColor("#FCE5EB")
            } else {
                GlobalUtils.formatColor("#58CEFF")
            }
            sexStringSpannable.setSpan(
                ForegroundColorSpan(lightColor),
                sexStartIndex,
                sexStartIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            tv_sex.text = sexStringSpannable


            if (friendUser.weight == 0 || friendUser.height == 0) {
                tv_weight_height.hide()
            } else {
                if (friendUser.weight != 0 && friendUser.height != 0) {
                    val weightContent = "${friendUser.height}cm | ${friendUser.weight}kg"
                    val weightStringSpannable = SpannableStringBuilder(weightContent)
                    val weightStartIndex = "${friendUser.height}cm ".length
                    weightStringSpannable.setSpan(
                        ForegroundColorSpan(GlobalUtils.formatColor("#DAE8F3")),
                        weightStartIndex,
                        weightStartIndex + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );

                    tv_weight_height.text = weightStringSpannable

                } else if (friendUser.weight != 0 && friendUser.height == 0) {
                    tv_weight_height.text = "${friendUser.weight}kg"
                } else {
                    tv_weight_height.text = "${friendUser.height}cm"
                }
            }


            val city = friendUser.city
            if (city.isEmpty()) {
                tv_location.hide()
            } else {
                tv_location.show()
                tv_location.text = city
            }

            tv_sign.text = friendUser.mySign

            val params = mHeaderView?.layoutParams
            val videoView = mHeaderView?.findViewById<View>(R.id.ll_audio)
            val view_bg = mHeaderView?.findViewById<View>(R.id.view_bg)
            if (friendUser.voiceLength == 0) {
                //没有语音，隐藏语音相关布局
                videoView?.hide()
                params?.height = dp2px(111)
                view_bg?.backgroundResource = R.mipmap.bg_user_info_chat_small
            } else {
                videoView?.show()
                params?.height = dp2px(145)
                view_bg?.backgroundResource = R.mipmap.bg_user_info_chat

                //显示语音相关布局
                val tv_audio_time = mHeaderView?.findViewById<TextView>(R.id.tv_audio_time) ?: return
                tv_audio_time.text = "${friendUser.voiceLength}\""

                val sdv_audio_state = mHeaderView?.findViewById<SimpleDraweeView>(R.id.sdv_audio_state) ?: return
                ImageUtils.loadImageLocal(sdv_audio_state, R.mipmap.anim_audio_playing)


            }
            mHeaderView?.layoutParams = params
        }
        scrollToBottom()
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

        if (messageType == Message_Text && (TextUtils.isEmpty(message) || message.isBlank() || message.trim().isEmpty())) {
            //文本消息判断
            ToastUtils.show("不能发送空白消息哦")
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

        val sendRoomBean = mPrivateConversationViewModel?.sendRoomIndoData?.value
        if (messageType == Send_Room && sendRoomBean == null) {
            return
        }
        mPrivateConversationViewModel?.sendRoomIndoData?.value = null

        val chatInfo = mPrivateConversationViewModel?.chatInfoData?.value
        if (chatInfo == null) {
            ToastUtils.show(mPrivateConversationViewModel?.errorMessage ?: "")
            return
        }

        val targetChatInfo = mPrivateConversationViewModel?.chatInfoData?.value ?: return
        val targetUser = TargetUserObj().apply {
            headPic = targetChatInfo.headPic
            nickname = targetChatInfo.nickname
            intimateLevel =
                mPrivateConversationViewModel?.basicBean?.value?.intimate?.intimateLevel ?: 0
            meetStatus = targetChatInfo.meetStatus
            userId = targetChatInfo.userId
            stranger = GlobalUtils.getStrangerString(targetChatInfo.stranger)
            userType = targetChatInfo.userType
            strangerToOther = targetChatInfo.strangerToOther
        }
        if (mPrivateConversationViewModel.noMoreState && mAdapter.data.size == 1 && (mAdapter.getItemOrNull(0)?.senderUserId == BusiConstant.System_Message)) {
            //没有聊天记录(存在一条提示消息)
            targetUser.showMsgFee = BusiConstant.True
        }

        if (targetChatInfo.userId == SessionUtils.getUserId()) {
            ToastUtils.show("不能给自己发消息")
            return
        }
        when (messageType) {
            Message_Text -> {
                //文本消息
                val msg = RongCloudManager.obtainTextMessage(
                    message,
                    "${targetChatInfo.userId}",
                    targetUser
                )
                msg.sentStatus = Message.SentStatus.SENDING
                mPrivateConversationViewModel?.addMessage(msg)
                mPrivateConversationViewModel?.sendMsg(
                    targetChatInfo.userId,
                    message,
                    targetUser,
                    localMsg = msg
                )
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
                mPrivateConversationViewModel?.sendMsg(
                    targetChatInfo.userId,
                    message,
                    targetUser,
                    Message_Privilege,
                    msg
                )
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
                        mPrivateConversationViewModel?.sendFinger(
                            targetChatInfo.userId,
                            animationResult,
                            msg
                        )
                    }
                    "[骰子]" -> {
                        mPrivateConversationViewModel?.sendDice(
                            targetChatInfo.userId,
                            animationResult,
                            msg
                        )
                    }
                    else -> {
                    }
                }

            }
            Send_Room -> {
                //传送门消息
                val localMsg = RongCloudManager.obtainCustomMessage(
                    "${targetChatInfo.userId}",
                    targetUser,
                    Conversation.ConversationType.PRIVATE,
                    MessageCustomBeanType.SendRoom,
                    sendRoomBean ?: return
                )
                localMsg.senderUserId = "${SessionUtils.getUserId()}"
                localMsg.sentStatus = Message.SentStatus.SENDING
                mPrivateConversationViewModel?.addMessage(localMsg)
                //发送融云消息
                RongCloudManager.sendCustomMessage(localMsg) { result, message ->
                    if (!result) {
                        //发送失败
                        localMsg.messageId = message.messageId
                        mPrivateConversationViewModel?.sendMessageFail(localMsg, MessageFailType.RONG_CLOUD)
                    } else {
                        localMsg.sentStatus = message.sentStatus
                        mPrivateConversationViewModel?.msgData?.value = localMsg
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
                        mPrivateConversationViewModel?.sendPic(
                            upLoader,
                            imageMessage.targetId.toLong(),
                            "$picUrl"
                        )
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
                    (imageMessage.content as? ImageMessage)?.remoteUri =
                        (msg.content as? ImageMessage)?.remoteUri
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
            .requestEachCombined(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                        //判断亲密特权
//                        val result = hasManager
//                        if (result) {
                        goToPictureSelectPager()
//                        }
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
     * 增加单条消息
     */
    private fun addSingleMessage(msg: Message) {
        mAdapter.addData(msg)
        mAdapter.upFetchModule.isUpFetching = false
        mAdapter.upFetchModule.isUpFetchEnable = mPrivateConversationViewModel?.noMoreState != true
        scrollToBottom(true)
        showXiaoQueAuto()
    }

    /**
     * 初始化语音设置
     */
    private fun initVoiceManager() {
        if (voiceMamagerInited) {
            return
        }
        voiceMamagerInited = true
        //半秒回调一次
        audioPlayerManager.setSleep(500)
        audioPlayerManager.setMediaPlayFunctionListener(object : MediaPlayFunctionListener {
            override fun prepared() {
                logger.info("prepared")
            }

            override fun start() {
                logger.info("start 总长=${audioPlayerManager.getDuration()}")
//                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_play_home_page)
//                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//                tv_time.setCompoundDrawables(drawable, null, null, null)
                //不使用实际的值
//                currentPlayHomeRecomItem?.introduceVoiceLength = (audioPlayerManager.getDuration() / 1000)+1
//                AliplayerManager.soundOff()
                val iv_audio_play = mHeaderView?.findViewById<ImageView>(R.id.iv_audio_play)
                iv_audio_play?.isActivated = true
                val sdv_audio_state = mHeaderView?.findViewById<SimpleDraweeView>(R.id.sdv_audio_state) ?: return
                ImageUtils.loadGifImageLocal(sdv_audio_state, R.mipmap.anim_audio_playing)
            }

            override fun resume() {
                logger.info("resume")
                //todo 暂停直播间声音
//                AliPlayerManager.soundOff()
//                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_play_home_page)
//                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//                tv_time.setCompoundDrawables(drawable, null, null, null)
                val iv_audio_play = mHeaderView?.findViewById<ImageView>(R.id.iv_audio_play)
                iv_audio_play?.isActivated = true
                val sdv_audio_state = mHeaderView?.findViewById<SimpleDraweeView>(R.id.sdv_audio_state) ?: return
                ImageUtils.loadGifImageLocal(sdv_audio_state, R.mipmap.anim_audio_playing)
            }

            override fun pause() {
                logger.info("pause")
//                AliPlayerManager.soundOn()
                //todo 恢复直播间声音
//                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_pause_home_page)
//                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//                tv_time.setCompoundDrawables(drawable, null, null, null)
                val iv_audio_play = mHeaderView?.findViewById<ImageView>(R.id.iv_audio_play)
                iv_audio_play?.isActivated = false
                val sdv_audio_state = mHeaderView?.findViewById<SimpleDraweeView>(R.id.sdv_audio_state) ?: return
                ImageUtils.loadImageLocal(sdv_audio_state, R.mipmap.anim_audio_playing)
            }

            override fun stop() {
                logger.info("stop")
                //todo 恢复直播间声音
//                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_pause_home_page)
//                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//                tv_time.setCompoundDrawables(drawable, null, null, null)
                val iv_audio_play = mHeaderView?.findViewById<ImageView>(R.id.iv_audio_play)
                iv_audio_play?.isActivated = false
                val sdv_audio_state = mHeaderView?.findViewById<SimpleDraweeView>(R.id.sdv_audio_state) ?: return
                ImageUtils.loadImageLocal(sdv_audio_state, R.mipmap.anim_audio_playing)
            }


        })
        audioPlayerManager.setMediaPlayInfoListener(object : MediaPlayInfoListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int) {
                logger.info("onError mediaPlayer=${mp.hashCode()} what=$what extra=$extra")
            }

            override fun onCompletion(mediaPlayer: MediaPlayer?) {
                logger.info("onCompletion mediaPlayer=${mediaPlayer.hashCode()}")
                val tv_audio_time = mHeaderView?.findViewById<TextView>(R.id.tv_audio_time) ?: return
                tv_audio_time.text = "${mPrivateConversationViewModel.basicBean.value?.friendUser?.voiceLength ?: 0}\""
            }

            override fun onBufferingUpdate(mediaPlayer: MediaPlayer?, i: Int) {
                logger.info("onBufferingUpdate mediaPlayer=${mediaPlayer.hashCode()} i=$i")
            }

            override fun onSeekComplete(mediaPlayer: MediaPlayer?) {
                logger.info("onSeekComplete mediaPlayer=${mediaPlayer.hashCode()} ")
            }

            override fun onSeekBarProgress(progress: Int) {
//                logger.info("onSeekBarProgress progress=${progress / 1000}")
                val tv_audio_time = mHeaderView?.findViewById<TextView>(R.id.tv_audio_time) ?: return
                val voiceLength = mPrivateConversationViewModel.basicBean.value?.friendUser?.voiceLength ?: return
                tv_audio_time.text = "${voiceLength - progress / 1000}s"
            }
        })
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
                        bundle.putLong(
                            ParamConstant.UserId,
                            mPrivateConversationViewModel?.targetIdData?.value ?: return@subscribe
                        )
                        ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY)
                            .with(bundle).navigation()
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
                    sendChatMessage(pic = path, localPic = path, messageType = Message_Pic)
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

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun strangerChange(event: UserInfoChangeEvent) {
//        if (event.userId == mPrivateConversationViewModel?.targetIdData?.value) {
//            //当前会话，陌生人状态变化
//            mPrivateConversationViewModel?.basicBean?.value?.stranger = event.stranger
////            RongCloudManager.strangerChange(event.stranger)
//        }
//    }

    /**
     * 亲密度变化消息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun intimateChange(data: IntimateBean) {
        if (data.intimateLevel >= 4) {
            //亲密度达到4级，有气泡权限
            RongCloudManager.updateChatBubble(mPrivateConversationViewModel?.bubbleData?.value)
        }
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
        val stranger = data.stranger[targetId] ?: false
        huanQueViewModel.userInfoStatusChange.value = UserInfoChangeResult(userId = targetId, stranger = stranger).convertRtData()
//        EventBus.getDefault().post(UserInfoChangeEvent(targetId, stranger))
    }


    /**
     * 显示背景
     */
    private fun showBackground() {
        val key =
            GlobalUtils.getBackgroundKey(mPrivateConversationViewModel?.targetIdData?.value ?: 0)
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun unReadCount(bean: UnreadCountEvent) {
        if (bean.player == mPrivateConversationViewModel?.fromPlayer) {
            val count = bean.unreadCount
            if (count <= 0) {
                tv_unread_count.hide()
            } else {
                tv_unread_count.show()
            }
            if (count < 100) {
                tv_unread_count.text = "$count"
            } else {
                tv_unread_count.text = "99+"
            }
        }
        EventBus.getDefault().removeStickyEvent(bean)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshVoiceCard(event: RefreshVoiceCardEvent) {
        mPrivateConversationViewModel?.propList()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        initBasic(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        MessageProcessor.removeProcessors(this)
        //草稿数据
        val draft = edit_text.text.toString()
//        if (draft.isNotEmpty()) {
        //保存草稿数据
        mPrivateConversationViewModel?.saveDraft(draft)
//        }

        if (draft != mPrivateConversationViewModel?.mDraft) {
            //草稿数据有变化，需要刷新列表
            EventBus.getDefault().post(DraftEvent(mPrivateConversationViewModel?.targetIdData?.value ?: return))
        }
    }
}