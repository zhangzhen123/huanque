package com.julun.huanque.message.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.effective.android.panel.PanelSwitchHelper
import com.effective.android.panel.view.panel.PanelView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.beans.TargetUserObj
import com.julun.huanque.common.bean.events.EventMessageBean
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.onTouch
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.ui.image.ImageActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.emotion.EmotionPagerView
import com.julun.huanque.common.widgets.emotion.Emotions
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.MessageAdapter
import com.julun.huanque.message.fragment.ChatSendGiftFragment
import com.julun.huanque.message.fragment.IntimateDetailFragment
import com.julun.huanque.message.viewmodel.IntimateDetailViewModel
import com.julun.huanque.message.viewmodel.PrivateConversationViewModel
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.rd.PageIndicatorView
import com.rd.utils.DensityUtils
import io.reactivex.rxjava3.core.Observable
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.message.ImageMessage
import kotlinx.android.synthetic.main.act_private_chat.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.imageResource
import java.util.concurrent.TimeUnit


/**
 *@创建者   dong
 *@创建时间 2020/6/30 16:55
 *@描述 私聊会话页面
 */
class PrivateConversationActivity : BaseActivity() {
    companion object {
        const val TARGETID = "TARGETID"
        fun newInstance(activity: Activity, targetId: Long) {
            val intent = Intent(activity, PrivateConversationActivity::class.java)
            intent.putExtra(TARGETID, targetId)
            activity.startActivity(intent)
        }
    }

    //文本消息
    private val Message_Text = "Message_Text"

    //图片消息
    private val Message_Pic = "Message_Pic"

    //送礼消息
    private val Message_Gift = "Message_Gift"

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

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val ivOperation = findViewById<ImageView>(R.id.ivOperation)
        ivOperation.imageResource = R.mipmap.icon_conversation_setting
        ivOperation.show()

        initViewModel()
        initRecyclerView()
        val targetID = intent?.getLongExtra(TARGETID, 0)
        mPrivateConversationViewModel?.targetIdData?.value = targetID
        registerMessageEventProcessor()
        //        mPrivateConversationViewModel?.getMessageList(first = true)
        //获取基本数据
        mPrivateConversationViewModel?.chatBasic(targetID ?: return)
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
                RongIMClient.getInstance().clearMessagesUnreadStatus(Conversation.ConversationType.PRIVATE, "$it")
                EventBus.getDefault().post(EventMessageBean("$it"))
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
                scrollToBottom()
                mPrivateConversationViewModel?.messageChangeState?.value = null
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
                mAdapter.otherUserInfo = it
                mAdapter.notifyDataSetChanged()
            }
        })
        mPrivateConversationViewModel?.basicBean?.observe(this, Observer {
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
    }

    override fun initEvents(rootView: View) {
        findViewById<View>(R.id.ivback).onClickNew {
            finish()
        }
        findViewById<ImageView>(R.id.ivOperation).onClickNew {
            //打开会话设置
            val targetId = mPrivateConversationViewModel?.targetIdData?.value ?: return@onClickNew
            PrivateConversationSettingActivity.newInstance(this, "$targetId")
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
        iv_pic.onClickNew { checkPermissions() }
        tv_meet.onClickNew {
            //显示欢遇弹窗
            mIntimateDetailFragment = mIntimateDetailFragment ?: IntimateDetailFragment.newInstance()
            mIntimateDetailFragment?.show(supportFragmentManager, "MeetDetailFragment")
        }

        iv_phone.onClickNew {
            //跳转语音通话页面
            val dialogShow = SharedPreferencesUtils.getBoolean(SPParamKey.VOICE_FEE_DIALOG_SHOW, false)
            if (!dialogShow) {
                //未显示过价格弹窗，显示弹窗
                MyAlertDialog(this).showAlertWithOKAndCancel(
                    "语音通话${mPrivateConversationViewModel?.basicBean?.value?.voiceFee}鹊币/分钟",
                    MyAlertDialog.MyDialogCallback(onRight = {
                        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_FEE_DIALOG_SHOW, true)
                        judgeBalance()
                    }, onCancel = {
                        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_FEE_DIALOG_SHOW, true)
                    }), "语音通话费用", "发起通话"
                )
            } else {
                judgeBalance()
            }

        }
        iv_gift.onClickNew {
            mChatSendGiftFragment = mChatSendGiftFragment ?: ChatSendGiftFragment()

            mChatSendGiftFragment?.show(this, "ChatSendGiftFragment")
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

        val bundle = Bundle()
        bundle.putString(ParamKey.TYPE, ConmmunicationUserType.CALLING)
        bundle.putSerializable(ParamKey.USER, mPrivateConversationViewModel?.chatInfoData?.value ?: return)
        ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY).with(bundle).navigation()
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
            val tempData = mAdapter.getItem(position)
            when (view.id) {
                R.id.iv_send_fail -> {
                    //重新发送消息
                    val targerId = mPrivateConversationViewModel?.targetIdData?.value ?: return@setOnItemChildClickListener
                    tempData.sentStatus = Message.SentStatus.SENDING
                    adapter.getViewByPosition(position, R.id.iv_send_fail)?.hide()
                    adapter.getViewByPosition(position, R.id.send_progress)?.show()
                    RongCloudManager.send(tempData, "$targerId") {
                        if (it) {
                            ChatUtils.deleteSingleMessage(tempData.messageId)
                            mPrivateConversationViewModel?.deleteSingleMessage(tempData)
                        } else {
                            tempData.sentStatus = Message.SentStatus.FAILED
                            adapter.getViewByPosition(position, R.id.iv_send_fail)
                                ?.show()
                            adapter.getViewByPosition(position, R.id.send_progress)
                                ?.hide()
                        }
                    }
                }
                R.id.sdv_image -> {
                    //查看图片
                    val content = tempData.content
                    if (content is ImageMessage) {
                        ImageActivity.start(this, 0, medias = listOf(StringHelper.getOssImgUrl("${content.remoteUri}")))
                    }
                }
            }
        }
//        mAdapter.isUpFetchEnable = true
//        //预加载2个position
//        mAdapter.setStartUpFetchPosition(2)
//        mAdapter.setUpFetchListener {
//            val currLast = mAdapter.getItem(0)
//            mPrivateConversationViewModel?.getMessageList(
//                currLast?.messageId ?: return@setUpFetchListener
//            )
//            mAdapter.isUpFetching = true
//        }
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
     * @param message 发送文本消息使用
     * @param pic 上传图片的地址
     * @param localPic 图片库中的di
     */
    private fun sendChatMessage(message: String = "", pic: String = "", localPic: String = "", messageType: String = Message_Text) {
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
        if (giftBean == null) {
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
                RongCloudManager.send(message, "${targetChatInfo.userId}", targetUserObj = targetUser) {}
            }
            Message_Pic -> {
                //图片消息
                RongCloudManager.sendMediaMessage("${targetChatInfo.userId}", targetUser, Conversation.ConversationType.PRIVATE, pic, localPic)
            }
            Message_Gift -> {
                //送礼消息
                RongCloudManager.sendCustomMessage(
                    "${targetChatInfo.userId}",
                    targetUser,
                    Conversation.ConversationType.PRIVATE,
                    MessageCustomBeanType.Gift,
                    giftBean ?: return
                )
            }
            else -> {

            }
        }
    }

    private fun checkPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                        goToPictureSelectPager()
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
}