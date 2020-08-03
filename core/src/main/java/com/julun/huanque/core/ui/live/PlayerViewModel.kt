package com.julun.huanque.core.ui.live

import android.animation.ValueAnimator
import android.os.SystemClock
import android.text.TextUtils
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRoomService
import com.julun.huanque.common.suger.logger
import com.julun.huanque.core.net.UserService
import com.julun.huanque.common.bean.beans.SingleGame
import com.julun.huanque.common.bean.forms.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.GlobalDataPool
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.BalanceUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.rong.imlib.model.Conversation
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/28 16:07
 *
 *@Description: PlayerViewModel
 *
 */
class PlayerViewModel : BaseViewModel() {

    //发送消息间隔(1秒)
    private val mSendMessageSpace = 1000L

    //上一次发送时间
    private var mLastSendTime = 0L

    //消息发送中标识
    val mMessageSending: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    private val liveService: LiveRoomService by lazy {
        Requests.create(LiveRoomService::class.java)
    }

    private val mUserService: UserService by lazy {
        Requests.create(UserService::class.java)
    }

    //直播间ID
    var programId = 0L

    val roomData: UserEnterRoomRespDto?
        get() = loginSuccessData.value


    //切换房间
    val checkoutRoom: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    //关闭弹窗
    val closeDialog: MutableLiveData<Class<out DialogFragment>> by lazy { MutableLiveData<Class<out DialogFragment>>() }

    //开启弹窗新版调用方式
    val showDialog: MutableLiveData<Class<out DialogFragment>> by lazy { MutableLiveData<Class<out DialogFragment>>() }

    //重置弹窗
    val resetDialog: MutableLiveData<Class<out DialogFragment>> by lazy { MutableLiveData<Class<out DialogFragment>>() }

    //是否是主播
    var isAnchor = false

    //是不是主题房直播
    var isThemeRoom: Boolean = false

    //是不是手机直播
    var isAppShow: Boolean = false

    //是不是在直播
    var isLiving: Boolean = false

    //登录状态
    val loginState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //value为true时 关闭页面
    val finishState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //显示TopDialog
    val topDialog: MutableLiveData<TopDialogBean> by lazy { MutableLiveData<TopDialogBean>() }

    //value为true时  重置礼物Fragment
    val needRefreshAll: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //刷新关注数据
    val modifySubscribe: MutableLiveData<Boolean?> by lazy { MutableLiveData<Boolean?>() }


    //添加主播不在线fragment
//    val notOnLineFragment: MutableLiveData<RecommendPrograms> by lazy { MutableLiveData<RecommendPrograms>() }
    //刷新用户信息
    val userInfo: MutableLiveData<UserInfo> by lazy { MutableLiveData<UserInfo>() }

    //关播相关数据
    val stopLiveData: MutableLiveData<StopBean> by lazy { MutableLiveData<StopBean>() }

    //打开游戏页面
    val openGame: MutableLiveData<SingleGame> by lazy { MutableLiveData<SingleGame>() }

    //打开更多操作
    val openOtherAction: MutableLiveData<MenuActionItem> by lazy { MutableLiveData<MenuActionItem>() }

    //显示余额不足页面
    val notEnoughBalance: MutableLiveData<NotEnoughBalanceBean> by lazy { MutableLiveData<NotEnoughBalanceBean>() }

    //显示停播弹窗
    val closeView: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    val recommendView: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //显示警告弹窗
    val warnAuthorView: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }


    //显示开通守护弹窗
    val guardView: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //显示普通管理弹窗
    val masterView: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //打开相应功能的简单设置（合并一些只需要传Boolean值的设置）
    val actionBeanData: MutableLiveData<BottomActionBean> by lazy { MutableLiveData<BottomActionBean>() }

    //显示分享弹窗
    val shareView: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //显示礼物视图
    val giftView: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //显示幸运转盘
    val luckyPanView: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //默认选中某款礼物
    val openGiftViewWithSelect: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //显示用户信息弹窗
    val userInfoView: MutableLiveData<UserInfoBean> by lazy { MutableLiveData<UserInfoBean>() }


//    //显示在线列表视图
//    val onlineView: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //跳转到贡献榜
    val scoreView: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //打开广场直播
    val squareView: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //显示私聊页面
    val privateMessageView: MutableLiveData<PrivateMessageBean> by lazy { MutableLiveData<PrivateMessageBean>() }

    //跳转H5页面
    val gotoWeb: MutableLiveData<GoToUrl> by lazy { MutableLiveData<GoToUrl>() }

    //获取身份（是否是主播 其他布局使用）
    val getAnchor: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //返回身份（其他布局获取身份数据）  true为主播，false为普通用户
    val anchor: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //加入直播间成功
    val loginSuccessData: MutableLiveData<UserEnterRoomRespDto> by lazy { MutableLiveData<UserEnterRoomRespDto>() }


    //私信体验相关数据
    val mEnterExtSmsData: MutableLiveData<ExperienceSms> by lazy { MutableLiveData<ExperienceSms>() }

    //快捷发言数据
    val mQuickWordsData: MutableLiveData<List<SingleWord>> by lazy { MutableLiveData<List<SingleWord>>() }

    //游戏数据
    val gameListData: MutableLiveData<MutableList<SingleGame>> by lazy { MutableLiveData<MutableList<SingleGame>>() }

    //魔法礼物赠送奖励列表
    val eggResultData: MutableLiveData<EggHitSumResult> by lazy { MutableLiveData<EggHitSumResult>() }


    //主播直播间ID
    val anchorProgramId: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    //推流设置相关
    //反转
    val rotate: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //闪光灯
    val flashLight: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //镜面效果
    val mirror: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //静音
    val sound: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //页面跳转
    val jumpBean: MutableLiveData<JumpActivityBean> by lazy { MutableLiveData<JumpActivityBean>() }

    //显示星球霸主标识位
    var mShowPlanet = false

    //出题按钮是否可用
    val questionEnable: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //清屏标志位
    val clearScreen: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //进入直播间之前的基础信息
    val baseData: MutableLiveData<UserEnterRoomRespBase> by lazy { MutableLiveData<UserEnterRoomRespBase>() }

    //进入直播间异常状态 basic异常1  chat异常2 enter异常3
    val errorState: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val contributionSum: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    //是否是首充
    val firstRecharge: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //余额变动
    val balanceChange: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //确定弹窗文本
    val alertViewMsg: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val dismissStatus: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //打开红包弹窗
    val openRPFragment: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }


    //每日任务红点的显示与隐藏的标识位
    val guideToFollow: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }
    val guideToSpeak: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val liansongTime: MutableLiveData<Float> by lazy { MutableLiveData<Float>() }

    val guideSendMessage: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //切换粉丝榜单tab
    val changeFansTab: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //传递粉丝团页面信息
    val pullFansInfo: MutableLiveData<FansRankInfo> by lazy { MutableLiveData<FansRankInfo>() }

    //是否礼物面板显示
    val sendGiftHigh: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //粉丝团操作事件
    val fansClubEvent: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //粉丝团icon参数
    val fansIconParams: MutableLiveData<ArrayList<Int>> by lazy { MutableLiveData<ArrayList<Int>>() }

    //获取粉丝图标参数
    val getFansIconParams: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //获取粉丝拯救信息
    val fansRescueData: MutableLiveData<PkMicSalvationStartInfo> by lazy { MutableLiveData<PkMicSalvationStartInfo>() }

    //引导提示公聊区显示加入粉丝团
    val guideToFansJoin: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //锦鲤池的runwayCache
    val runwayCache: MutableLiveData<RunwayCache> by lazy { MutableLiveData<RunwayCache>() }

    //是否展示粉丝打卡气泡
    val isShowFansBubbleLayout: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //是否展示猜字谜气泡
    val isShowGuessWordsBubbleLayout: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //一元礼包
    val oneYuanInfo: MutableLiveData<OneYuanInfo> by lazy { MutableLiveData<OneYuanInfo>() }


    //回归礼包
    val backGiftPackList: MutableLiveData<ArrayList<ReturnGiftBean>> by lazy { MutableLiveData<ArrayList<ReturnGiftBean>>() }

    //是否是一元首充
    val firstOneYuanRecharge: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //是否展示一元首充tips
    val oneYuanTips: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //接收打开卡牌通知刷新翻牌配置
    val refreshCardsConfig: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }


    //猜字谜奖励
    val wordAwardData: MutableLiveData<WordPuzzleAward> by lazy { MutableLiveData<WordPuzzleAward>() }

    //通知刷新小主回馈页
    val noticeRefreshFeedback: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //签到成功展示奖励弹窗
//    val showSigninSuccessDialog: MutableLiveData<SigninAward> by lazy { MutableLiveData<SigninAward>() }

    //刷新礼物面板
    val refreshGift: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //获取广告弹窗相关配置
    val getAdConfig: MutableLiveData<ArrayList<RoomBanner>> by lazy { MutableLiveData<ArrayList<RoomBanner>>() }

    //打开广告弹窗
    val openADFragment: MutableLiveData<ArrayList<RoomBanner>> by lazy { MutableLiveData<ArrayList<RoomBanner>>() }

    //通知获取粉丝团成员人数
    val fansMemberCount: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //通知粉丝团免费加入倒计时
    val fansFreeTime: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    //通知粉丝团加入成功
    val fansJoinSuccess: MutableLiveData<JoinFansResult> by lazy { MutableLiveData<JoinFansResult>() }


    //贵族在线弹窗 or 普通在线弹窗列表人数
    val updateOnLineCount: MutableLiveData<HashMap<String, String>> by lazy { MutableLiveData<HashMap<String, String>>() }

    //    //开启弹窗
//    val openDialog: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    //开启弹窗
    val openOnlineDialog: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //打开锦鲤池
    val openWishKoiDialog: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //打开节目单界面 true代表自动弹出需要排序  false代表用户手动打开
    val openThemeProgram: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //贵族数量变更
    val updateRoyalCount: MutableLiveData<RoyalMessageBean> by lazy { MutableLiveData<RoyalMessageBean>() }

    //体验守护
    val experienceGuard: MutableLiveData<ExperienceGuard> by lazy { MutableLiveData<ExperienceGuard>() }

    //主题房相关
    val themeRoomVO: MutableLiveData<ThemeRoomVO> by lazy { MutableLiveData<ThemeRoomVO>() }

    //是否主题房显示引导
    var showThemeGuide: Boolean = false

    //跳转守护弹窗并在退出时展示守护bubble
    val goGuardAndShowBubbleDialog: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //唤起守护入口气泡
    val showGuardBubble: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //执行体验守护倒计时
    val experienceGuardTime: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    //送礼弹窗关闭标识位
    val sendGiftFragmentDismissState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //聊天模式标记位
    val chatModeState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //聊天模式退出 标记位
    val chatModeExitState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }


    //增加一条公聊消息
    val addCustomMsgData: MutableLiveData<Any> by lazy { MutableLiveData<Any>() }

    //三重礼包是否可以领取
    var mReturnCanReceive = false

    //余额数据
    val balance: LiveData<Long> by lazy { BalanceUtils.getBalance() }

    //余额使用的Disposable
    private var mBalanceDisposable: Disposable? = null

    //弹出特权弹窗
    val showPrerogative: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //游戏红点标识
    val gameRedPoint: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }


    //关注来源 埋点用
    var subscribeSource: String? = null

    //直播间当前所属类型 埋点用
    var roomTypeCode: String? = null

    //打开携手闯关
    val openPassLevel: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //打开礼物面板并切换到背包
    val openGiftAndSelPack: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //刷新礼物背包
    val refreshGiftPackage: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //公聊设置的用户数据
    var roomUserChatExtra: RoomUserChatExtra? = null

    private var loginStateDispoosable: Disposable? = null


    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    val userExpChangeEvent: MutableLiveData<UserExpChangeEvent> by lazy { MutableLiveData<UserExpChangeEvent>() }

    //记录当前切换的节目列表
    val switchList: MutableLiveData<ArrayList<SwitchBean>> by lazy { MutableLiveData<ArrayList<SwitchBean>>() }

    //处理直播间背景在pk时的切换 0初始样式 1 二人pk 2 三人pk 3斗地主
    val bgChange: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    fun getLivRoomBase(programId: Long) {
        logger("getLivRoomBase")
        viewModelScope.launch {
            request({
                val result = liveService.getLivRoomBase(ProgramIdForm(programId)).dataConvert(intArrayOf(1201, 1202))
                baseData.value = result
            }, error = {
                errorState.value = 1
                if (it is ResponseError) {
                    ToastUtils.show(it.busiMessage)
                    when (it.busiCode) {
                        1201 -> {//节目不存在
                        }
                        1202 -> {//没有进入直播间权限
                        }
                        else -> {
                            finishState.postValue(true)
                        }
                    }

                }

            })

        }
    }

    //是否需要刷新切换列表的标志 一般需要刷新 只有上下切换不需要
    var needRefreshSwitchList = true

    /**
     * 获取上下切换列表
     *
     * @param fromType 来源类型 可选项：Social、Follow、Guard、Manager、Watched
     * @param programId
     * @param typeCode 分类
     *
     */
    fun querySwitchList(programId: Long) {
        if (!needRefreshSwitchList) {
            needRefreshSwitchList = true
            return
        }
        logger("刷新切换列表$programId")
        viewModelScope.launch {
            request({
                val fromType: String? = GlobalDataPool.fromType
                val typeCode: String = GlobalDataPool.typeCode
                GlobalDataPool.typeCode = ""
                switchList.value = liveService.switchList(SwitchForm(fromType, programId, typeCode)).dataConvert()
            })
        }


    }

    fun refreshUserInfoData() {
        val roomForm = UserEnterRoomForm(programId)
        //todo
//        liveService.getRoomUserInfo(roomForm)
//            .handleResponse(makeSubscriber {
//                userInfo.value = it
//            })

    }

    fun enterLivRoom(form: UserEnterRoomForm) {
        logger("enterLivRoom")
        viewModelScope.launch {
            request({
                val result = liveService.enterLivRoom(form).dataConvert(intArrayOf(-1))
                loginSuccessData.value = result
                gameListData.value = result.gameList
                getAdConfig.value = result.poppuAds
                updateRoyalCount.value = RoyalMessageBean(royalCount = result.honorCount, guardCount = result.guardCount)
            }, error = {
                it.printStackTrace()
                errorState.value = 3
                if (it is ResponseError) {
                    when (it.busiCode) {
                        -1 -> alertViewMsg.value =
                            CommonInit.getInstance().getContext().resources.getString(R.string.system_error)
                    }
                } else {
                    ToastUtils.show(it.message)
                    finishState.value = true
                }
            })
        }
    }

    fun leave(programId: Long) {
        logger("leave")
    }

    fun closeAllDelayTime() {
        logger("closeAllDelayTime")
    }

    /**
     * 关注主播
     */
    fun follow() {
        viewModelScope.launch {
            request({
                liveService.queryFollow(ProgramIdForm(programId)).dataConvert()
                modifySubscribe.value = true
            }, error = {
                modifySubscribe.value = null
            })
        }
    }

    /**
     * 取消关注主播
     */
    fun unFollow() {
        viewModelScope.launch {
            request({
                liveService.queryUnFollow(ProgramIdForm(programId)).dataConvert()
                modifySubscribe.value = false
            }, error = {
                modifySubscribe.value = null
            })
        }
    }

    /**
     * 统一处理点击事件
     *
     * 主播不能跳转 不打开礼物
     */
    fun processOnTouch(tplBean: TplBean) {
        when (tplBean.textTouch) {
            //默认没有该参数时 弹出个人信息
            null -> {
                val info = tplBean.userInfo
//                if ((info?.userLevel ?: 0) > 0) {
                info?.let {
                    val userInfo = UserInfoBean(info.userId, baseData.value?.programId == info.userId, info.royalLevel, "")
                    userInfoView.value = userInfo
                }
//                }
            }

            TextTouch.OpenDeepSeaGame -> {
                openGame.value = SingleGame(gameCode = GameType.DeepSea)
            }
            TextTouch.OpenGameView -> {
                actionBeanData.value = BottomActionBean(ClickType.GAME)
            }
            TextTouch.OpenLiveRoom -> {
                val messageContext = tplBean.context       //此时当前的消息不能为空
                if (!isAnchor && messageContext != null) {
                    checkoutRoom.value = messageContext.roomId
                }
            }
            TextTouch.OpenSendGiftView -> {
                if (!isAnchor) {
                    actionBeanData.value = BottomActionBean(ClickType.GIFT)
                }
            }
            TextTouch.OpenShareView -> {
                shareView.value = true
            }

            TextTouch.OpenUrlView -> {
                val messageContext = tplBean.context       //需要url
                if (messageContext != null) {
                    val needLogin = messageContext.needLogin == "T"
                    gotoWeb.value = GoToUrl(needLogin, messageContext.url)
                }
            }
            TextTouch.OpenUserCard -> {
                val context = tplBean.context
                context?.let {
                    if (context.userId != 0L) {
                        val royalLevel = tplBean.textParams["\${royalLevel}"]?.toInt()
                        logger("当前的贵族等级：$royalLevel")
                        val userInfo = UserInfoBean(
                            context.userId, false, royalLevel = royalLevel
                                ?: -1
                        )
                        userInfoView.value = userInfo
                    }
                }
            }
            TextTouch.OpenWishView -> {
                if (isAnchor) {
                    return
                }
                //打开锦鲤池
                openWishKoiDialog.value = true
            }
            TextTouch.OPEN_BIG_WHEEL -> {
                //打开幸运转盘
                if (isAnchor) {
                    return
                }
                luckyPanView.value = true
            }
            TextTouch.OPEN_PK_RANK -> {
                //打开PK段位赛规则页面
//                gotoWeb.value = GoToUrl(false, LMUtils.getDomainName(NativeUrl.PK_RANK_URL))
            }
            TextTouch.OPEN_BAG -> {
                //打开礼物面板并切换到背包
                openGiftAndSelPack.value = true
            }
            else -> {
                logger("注意 ！！该点击事件没有执行")
            }
        }
    }

    private var timeAnimator: ValueAnimator? = null

    fun timeInternal(duration: Long) {
        timeAnimator?.cancel()
        timeAnimator = ValueAnimator.ofFloat(0f, 1.0f)
        timeAnimator?.duration = duration
        timeAnimator?.addUpdateListener {
            liansongTime.value = it.animatedValue as Float
        }
        timeAnimator?.start()
    }


    /**
     * 发送消息
     */
    fun sendMessage(msg: String) {
        if (SystemClock.elapsedRealtime() - mLastSendTime < mSendMessageSpace) {
            ToastUtils.show("发言太快了，歇会儿吧。")
            return
        }
        mMessageSending.value = true
        viewModelScope.launch {
            request({
                val result = liveService.sendPubMessage(ValidateForm(msg, programId.toLong())).dataConvert()

                when (result?.resultCode) {
                    ValidateResult.PASS -> {
                        //验证通过
                        realSendMessage(msg)
                    }
                    ValidateResult.ONLY -> {
                        //仅自己可见
                        val newMessage = result.newContent ?: msg
                        RongCloudManager.addUnRealMessage(
                            newMessage,
                            MessageProcessor.TextMessageType.PUBLIC_MESSAGE,
                            null,
                            "$programId",
                            Conversation.ConversationType.CHATROOM
                        )
                        mMessageSending.value = false
                        mLastSendTime = SystemClock.elapsedRealtime()
                    }
                    ValidateResult.RESEND -> {
                        //替换文案发送
                        result?.newContent?.let {
                            realSendMessage(it)
                        }
                    }
                    else -> {
                    }
                }

            }, { mMessageSending.value = false })
        }

    }

    /**
     * 调用融云，发送消息
     */
    private fun realSendMessage(content: String) {
        RongCloudManager.sendPublicTextMessage(content, "$programId") {
            mMessageSending.value = false
            mLastSendTime = SystemClock.elapsedRealtime()
        }
    }
}