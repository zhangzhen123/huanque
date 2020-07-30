package com.julun.huanque.core.ui.live.manager

import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.ConnectMicroOperate
import com.julun.huanque.common.constant.LiveBgType
import com.julun.huanque.common.constant.PKAction
import com.julun.huanque.common.constant.PKType
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.sortList
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.viewmodel.ConnectMicroViewModel
import com.julun.huanque.common.viewmodel.VideoChangeViewModel
import com.julun.huanque.common.viewmodel.VideoViewModel
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.viewmodel.LiveFollowListViewModel
import com.julun.huanque.core.viewmodel.PKViewModel

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/28 20:43
 *
 *@Description: PlayerTransformManager 所有的数据(融云消息数据以及网络http数据)层逻辑都在这里去中转处理 activity不要处理逻辑中转
 *
 */
class PlayerTransformManager(val act: PlayerActivity) {


    //播放页面主ViewModel
    private val mPlayerViewModel: PlayerViewModel by act.viewModels()

    //播放器使用的ViewModel
    private val mVideoViewModel: VideoViewModel by act.viewModels()

    private val liveFollowListViewModel: LiveFollowListViewModel by act.viewModels()

    //新版PK
    private val pKViewModel: PKViewModel by act.viewModels()

    //    private var mBasePlayerViewModel: BasePlayerViewModel? = null
//    private lateinit var anchorNoLiveViewModel: AnchorNoLiveViewModel
    private val videoPlayerViewModel: VideoChangeViewModel by act.viewModels()

    //    private val mConfigViewModel: PlayerConfigViewModel by lazy { ViewModelProviders.of(context).get(PlayerConfigViewModel::class.java) }
    //连麦ViewModel
    private val connectMicroViewModel: ConnectMicroViewModel by act.viewModels()
    //ZEGO推流相关
//    private lateinit var mZegoPublishViewModel: ZegoPublishViewModel

    //    private lateinit var liveFollowListViewModel: LiveFollowListViewModel


    init {
        initViewModels()
    }

    /**
     * 设置ViewModel的数据传递
     */
    private fun initViewModels() {

        mPlayerViewModel.baseData.observe(act, Observer {
            mVideoViewModel.baseData.value = it
            liveFollowListViewModel.requestFollowLivingList(mPlayerViewModel.programId, true)
        })

        mPlayerViewModel.loginSuccessData.observe(act, Observer { dto ->
            //用户进入直播间成功

            dto?.let { it ->
                it.roomMicInfo?.joinList?.let { list ->
                    logger("连麦状态 5 true")
                    connectMicroViewModel.inMicro.value = true
                    connectMicroViewModel.inPk.value = null
                    addVideoPlayer(list)
                    doWithMic(it.roomMicInfo?.joinList, GuardAgainst.Multi)
                }

            }
        })
        mPlayerViewModel.loginState.observe(act, Observer {
            mVideoViewModel.loginState.value = it
        })


        //VideoViewModel - > PlayerViewModel
        mVideoViewModel.checkoutRoom.observe(act, Observer {
            mPlayerViewModel.checkoutRoom.value = it
        })
        mVideoViewModel.loginSuccessData.observe(act, Observer {
            if (it == mPlayerViewModel.loginSuccessData.value) {
                //不接受相同的数据
                return@Observer
            }
            mPlayerViewModel.loginSuccessData.value = it
        })
        mVideoViewModel.finishState.observe(act, Observer {
            mPlayerViewModel.finishState.value = it
        })
        mVideoViewModel.anchorProgramId.observe(act, Observer {
            mPlayerViewModel.anchorProgramId.value = it
        })
        mVideoViewModel.actionBeanData.observe(act, Observer {
            mPlayerViewModel.actionBeanData.value = it
        })

        //当pk开始时统一处理
        pKViewModel.pkStarting.observe(act, Observer {
            if (it != null) {
                logger("连麦状态 6 null")
                addPkVideoPlayer(it)
                connectMicroViewModel.inPk.value = true
                connectMicroViewModel.inMicro.value = null
                pKViewModel.openPropWindowData.value = it?.openPropWindow
            }

        })

        pKViewModel.remotoPkData.observe(act, Observer {
            if (it != null && mPlayerViewModel.isAnchor) {
                //主播身份，切换重推
                it.detailList?.forEach { user ->
                    if (user.programId == mPlayerViewModel.programId) {
                        connectMicroViewModel.inPk.value = true
                        connectMicroViewModel.inMicro.value = null
                        mVideoViewModel?.switchPublishData?.value = SwitchPublishBaseData(
                            user.pushUrl,
                            user.sdkParams,
                            user.sdkProvider,
                            GuardAgainst.Multi,
                            user.programId
                        )
                        return@Observer
                    }
                }
            }
        })

    }

    /**
     * 注册消息
     */
    fun registerMessage() {
        // 用户经验变化
        MessageProcessor.registerEventProcessor(object : MessageProcessor.UserExpChangeMessageProcessor {
            override fun process(data: UserExpChangeEvent) {
//                mPlayerViewModel.userExpChangeEvent.value = data
            }
        })

        /**
         * 本场贡献榜变化消息
         */
        MessageProcessor.registerEventProcessor(object : MessageProcessor.ContributionProcess {
            override fun process(data: ContributionEvent) {
                if (!act.isFinishing) {
//                    mPlayerViewModel.contributionSum.postValue(data.score)
                }
            }
        })

        // 豪车大亨消息
        MessageProcessor.registerTxtProcessor(object : MessageProcessor.HuxuryCarMessageReceiver {
            override fun processMessage(messageList: List<TplBean>) {
                logger("DXC  收到豪车消息")
//                playerDinosaurViewModel.carMessage.value = messageList
            }
        })

//        /**
//         * 任务完成消息  FinishMission
//         * 现在代表总任务
//         */
//        MessageProcessor.registerEventProcessor(object : MessageProcessor.FinishMissionMessageProcessor {
//            override fun process(data: FinishMissionBean) {
//                taskViewModel.showReceive.value = true
//            }
//
//        })


//        /**
//         * 异步红包变更通知
//         */
//        MessageProcessor.registerEventProcessor(object : MessageProcessor.SyncRPChangeMessageProcessor {
//            override fun process(redpacket: YearRedPackageResult) {
//                redPackageViewModel.disposeMsg(redpacket)
//            }
//        })


        /**
         * 刷新段位赛通知
         */
        MessageProcessor.registerEventProcessor(object : MessageProcessor.PkRanRefreshProcessor {
            override fun process(data: VoidResult) {
//                mPlayerViewModel.resetDialog.value = DialogTypes.DIALOG_PK_RANK
//                mPlayerViewModel.resetDialog.value = PkRankMainDialogFragment::class.java
            }
        })

        /**
         * 最高分用户变更事件
         */
        MessageProcessor.registerEventProcessor(object : MessageProcessor.PlanetCasualTopRefreshProcessor {
            override fun process(data: TopUserInfo) {
//                mPlanetViewModel.mTopUserInRelaxation.value = data
            }
        })

        /**
         * 直播间Banner变化消息
         */
        MessageProcessor.registerEventProcessor(object : MessageProcessor.RoomBannerChangeProcessor {
            override fun process(data: RoomBannerChangeBean) {
                val tempRoomBanner = data.bannerInfo ?: return
//                val bannerList = playerBannerViewModel.roomBannerData.value
//                if (data.remove) {
//                    //移除
//                    var bannerIndex = -1
//                    bannerList?.forEachIndexed { index, roomBanner ->
//                        if (roomBanner.adCode == tempRoomBanner.adCode) {
//                            //找到对应的Banner
//                            bannerIndex = index
//                            return@forEachIndexed
//                        }
//                    }
//                    if (bannerIndex >= 0) {
//                        bannerList?.removeAt(bannerIndex)
//                    }
//                } else {
//                    //新增
//                    var realIndex = 0
//
//                    bannerList?.forEachIndexed { index, roomBanner ->
//                        if (roomBanner.adCode == tempRoomBanner.adCode) {
//                            //已经存在对应的banner
//                            return
//                        }
//                        if (roomBanner.orderNum < tempRoomBanner.orderNum) {
//                            realIndex = index
//                        }
//                    }
//                    bannerList?.add(realIndex, tempRoomBanner)
//                }
//                playerBannerViewModel.roomBannerData.value = bannerList
            }
        })


        //直播间数据变化
        MessageProcessor.registerEventProcessor(object : MessageProcessor.GetDanmuCardProcessor {
            override fun process(data: PlayerDataChanged) {
//                val enterData = mPlayerViewModel.loginSuccessData.value ?: return
//                enterData.user?.danMuCard = data.danmuCardNum
//                act?.chatInputView.setFreeDanMu(enterData.user?.hasFreeDanMu ?: false, data.danmuCardNum)
            }

        })


        //刷新用户信息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.RefreshUserMessageProcessor {
            override fun process(data: VoidResult) {
//                mPlayerViewModel.refreshUserInfoData()
            }
        })

        /**
         * 粉丝特权变化
         */
        MessageProcessor.registerEventProcessor(object : MessageProcessor.UserFansLevelChangeProcessor {
            override fun process(data: VoidResult) {
                //刷新用户信息
//                mPlayerViewModel.refreshUserInfoData()
            }

        })
        //主播嘉宾名单变更消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.GuestStatusProcessor {
            override fun process(data: VoidResult) {
                //重新请求嘉宾列表
//                mPlayerViewModel.queryGuestList(false)
            }
        })

        /**
         * 横竖屏切换消息
         */
        MessageProcessor.registerEventProcessor(object : MessageProcessor.RoomCutoverScreenTypeProcessor {
            override fun process(data: SwitchScreen) {
//                if (data.programId != mPlayerViewModel.programId) {
//                    //不是当前直播间消息，直接返回
//                    return
//                }
//                if (mPlayerViewModel.isAnchor) {
//                    //主播身份，直接返回
//                    return
//                }
//                if (mPlayerViewModel.isAppShow) {
//                    //主播处于横屏模式
//                    mConfigViewModel.screenTypeData.value = data.cutoverScreenType
//                    mPlayerViewModel.baseData.value?.screenType = data.cutoverScreenType
//                } else {
//                    //主播处于竖屏模式
//                    mConfigViewModel.screenTypeData.value = BusiConstant.ScreenType.SP
//                    mPlayerViewModel.baseData.value?.screenType = BusiConstant.ScreenType.SP
//                }
            }

        })

        //CDN切换成功消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.ToggleCDNSuccProcessor {
            override fun process(data: PlayInfoBean) {
                //如果是主题房切换的  逻辑处理
                if (mPlayerViewModel.isThemeRoom) {
                    //重新请求嘉宾列表
//                    mPlayerViewModel.queryGuestList(false)
                    //如果下一个上场的主播没有流 关闭正在播的流
                    if (data.playInfo == null) {
                        mVideoViewModel?.stopAllStreamState?.value = true
                    } else {
                        mVideoViewModel?.playerData?.value = LiveBean().apply {
                            programPoster = data.prePic
                            programId = data.themeSessionId
                            isAppShow = mPlayerViewModel.isAppShow
                            playinfo = data.playInfo
                        }
                    }
                    //修改聊天赠送id
//                    chatInputView.acceptProgramId = data.themeShowingProgramId
                } else if (data.programId == mPlayerViewModel.programId) {
                    mVideoViewModel?.playInfoData?.value = data.playInfo

                }
            }
        })

        //连麦操作事件
        MessageProcessor.registerEventProcessor(object : MessageProcessor.MicOperateProcessor {
            override fun process(data: MicOperateBean) {
                when (data.action) {
                    ConnectMicroOperate.Create -> {
                        //创建连麦
                        connectMicroViewModel.notarizeShowState.value = data
                    }
                    ConnectMicroOperate.Reject -> {
                        //拒绝连麦
                        if (data.micId == connectMicroViewModel.notarizeShowState.value?.micId) {
                            //拒绝连麦  切换单流
                            connectMicroViewModel.notarizeShowState.value = null
                            ToastUtils.show(act.resources.getString(R.string.connect_micro_fail))
                        }
                    }
                    ConnectMicroOperate.Timeout -> {
                        //连麦超时
                        if (data.micId == connectMicroViewModel.notarizeShowState.value?.micId) {
                            //连麦超时  切换连麦
                            connectMicroViewModel.notarizeShowState.value = null
                            ToastUtils.show(act.resources.getString(R.string.connect_micro_timeout))
                        }
                    }
                    ConnectMicroOperate.Accept -> {
                        //接收连麦
                        connectMicroViewModel.notarizeShowData.value = data
                    }
                }
            }

        })
        //连麦开始
        MessageProcessor.registerEventProcessor(object : MessageProcessor.MicStartingProcessor {
            override fun process(data: MicActionBean) {
                logger("连麦状态 1 true")
                connectMicroViewModel.inMicro.value = true
                connectMicroViewModel.inPk.value = null
                if (mPlayerViewModel.isAnchor) {
                    ToastUtils.show(act.resources.getString(R.string.connect_micro_success))
                }
                addVideoPlayer(data.micInfo.joinList)
                connectMicroViewModel.notarizeShowState.value = null
            }
        })
        //连麦中切换CDN消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.MicToggleProviderProcessor {
            override fun process(data: MicActionBean) {
                addVideoPlayer(data.micInfo.joinList)
                connectMicroViewModel.notarizeShowState.value = null
            }
        })
        //连麦断开
        MessageProcessor.registerEventProcessor(object : MessageProcessor.MicDisconnectProcessor {
            override fun process(data: MicActionBean) {
                logger("连麦状态 2 null")
                connectMicroViewModel.inMicro.value = null
                //连麦断开  切换单流
                if (mPlayerViewModel.isAnchor) {
                    ToastUtils.show("${data.programName}挂断了连麦")
                }
                removeVideoPlayer(data.micInfo.joinList)
            }

        })
        //连麦结束
        MessageProcessor.registerEventProcessor(object : MessageProcessor.MicFinishProcessor {
            override fun process(data: MicActionBean) {
                logger("连麦状态 3 null")
                connectMicroViewModel.inMicro.value = null
                //连麦结束 切换单流
                if (mPlayerViewModel.isAnchor) {
                    ToastUtils.show("${data.programName}挂断了连麦")
                }
                removeVideoPlayer(data.micInfo.joinList)
            }

        })
        //PK开始
        MessageProcessor.registerEventProcessor(object : MessageProcessor.PKStartMessageProcess {
            override fun process(data: PKStartEvent) {
                if (mPlayerViewModel.isAnchor) {
                    ToastUtils.show(act.resources.getString(R.string.connect_pk_success))
                }
                mPlayerViewModel.loginSuccessData.value?.pking = true
                connectMicroViewModel.notarizePKShowState.value = null
                if (data.pkInfo != null) {
                    //
                    pKViewModel.setPkStart(data.pkInfo ?: return)
                }
            }
        })
        //PK过程中切换CDN消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.PkToggleProviderMessageProcess {
            override fun process(data: PKStartEvent) {
                addPkVideoPlayer(data.pkInfo ?: return, false)
            }
        })
        //pk邀请消息处理
        MessageProcessor.registerEventProcessor(object : MessageProcessor.PKCreateMessageProcess {
            override fun process(data: PKCreateEvent) {
                dealWithPkCreate(data)
            }
        })

        //PK结束
        MessageProcessor.registerEventProcessor(object : MessageProcessor.PKFinishMessageProcess {
            override fun process(data: PKFinishEvent) {
                logger("PKFinishMessage:${data.programIds.size}")
                connectMicroViewModel.inPk.value = null
                if (mPlayerViewModel.isAnchor) {
                    ToastUtils.show("PK已结束")
                }
                //防止界面暂停时 pk都结束了pkStarting还未分发
                pKViewModel.pkStarting.value = null
//               animationFragment.closePk()
                pKViewModel.pkState.postValue(3)
                //移除多余的流
                removePkVideoPlayer(data)

            }
        })

        if (mPlayerViewModel.isAnchor) {
            //主播端特有消息
            //单流切换CDN消息
            MessageProcessor.registerEventProcessor(object : MessageProcessor.ToggleCDNProcessor {
                override fun process(data: SwitchPublishBaseData) {
                    mVideoViewModel?.toggleCDNState?.value = true
                    mVideoViewModel?.switchPublishData?.value = data
                }
            })//PK开始消息
            MessageProcessor.registerEventProcessor(object : MessageProcessor.AnchorPkStartingProcessor {
                override fun process(data: AnchorPKStartEvent) {
                    doWithPK(data.pkInfo?.detailList, GuardAgainst.Multi, true)
                }
            })
            //PK过程中切换CDN消息
            MessageProcessor.registerEventProcessor(object : MessageProcessor.AnchorPkToggleProviderProcessor {
                override fun process(data: AnchorPKStartEvent) {
                    doWithPK(data.pkInfo?.detailList, GuardAgainst.Multi, true)
                }
            })

            //PK结束消息
            MessageProcessor.registerEventProcessor(object : MessageProcessor.AnchorPkFinishProcessor {
                override fun process(data: AnchorPKStartEvent) {
                    doWithPK(data.pkInfo?.detailList, GuardAgainst.Single, false)
                }
            })
            //连麦开始消息
            MessageProcessor.registerEventProcessor(object : MessageProcessor.AnchorMicStartingProcessor {
                override fun process(data: MicActionBean) {
                    doWithMic(data.micInfo.joinList, GuardAgainst.Multi)
                }
            })
            //连麦过程中CDN切换消息
            MessageProcessor.registerEventProcessor(object : MessageProcessor.AnchorMicToggleProviderProcessor {
                override fun process(data: MicActionBean) {
                    doWithMic(data.micInfo.joinList, GuardAgainst.Multi)
                }
            })
            //连麦结束消息
            MessageProcessor.registerEventProcessor(object : MessageProcessor.AnchorMicFinishedProcessor {
                override fun process(data: MicActionBean) {
                    doWithMic(data.micInfo.joinList, GuardAgainst.Single, null)
                }
            })
            //连麦断开消息
            MessageProcessor.registerEventProcessor(object : MessageProcessor.AnchorMicDisconnectProcessor {
                override fun process(data: MicActionBean) {
                    doWithMic(data.micInfo.joinList, GuardAgainst.Single, null)
                }
            })
        }


    }

    //新增播放流
    fun addVideoPlayer(playerList: List<MicAnchor>) {
        videoPlayerViewModel.checkExit()
        playerList.forEach {
            it.isAnchor = mPlayerViewModel.isAnchor
        }
        videoPlayerViewModel.addPlayer(playerList)

    }

    //移除播放流
    fun removeVideoPlayer(playerList: List<MicAnchor>) {
        //收到移除连麦时 同时把basic的连麦字段清空  防止收到开播信号后 依然展示原来的连麦
        val baseData = mPlayerViewModel.baseData.value
        baseData?.roomMicInfo?.joinList?.clear()
        //
        playerList.forEach {
            it.isAnchor = mPlayerViewModel.isAnchor
        }
        videoPlayerViewModel.removePlayer(playerList)
    }


    //移除PK连麦
    private fun removePkVideoPlayer(data: PKFinishEvent) {
        //
        val micList: ArrayList<MicAnchor> = arrayListOf()
        data.pkInfo?.detailList?.forEach { userInfo ->
            micList.add(MicAnchor().apply {
                this.programId = userInfo.programId
                this.playInfo = userInfo.playInfo
            })
        }
        videoPlayerViewModel.removePlayer(micList)
    }

    /**
     * PK相关处理
     */
    private fun doWithPK(
        detailList: MutableList<SingleAnchorPkInfo>?,
        type: String = GuardAgainst.Single,
        inPk: Boolean? = true
    ) {
        connectMicroViewModel.inPk.value = inPk
        if (inPk == true) {
            connectMicroViewModel.inMicro.value = null
        }

        detailList?.forEach {
            if (it.programId == mPlayerViewModel.programId) {
                mVideoViewModel?.switchPublishData?.value =
                    SwitchPublishBaseData(it.pushUrl, it.sdkParams, it.sdkProvider, type, mPlayerViewModel.programId)
                return
            }
        }
    }

    /**
     * 连麦相关处理
     */
    fun doWithMic(joinList: ArrayList<MicAnchor>?, type: String = GuardAgainst.Single, inMic: Boolean? = true) {
        logger("连麦状态 4 $inMic")
        connectMicroViewModel.inMicro.value = inMic
        if (inMic == true) {
            connectMicroViewModel.inPk.value = null
        }

        joinList?.forEach {
            if (it.programId == mPlayerViewModel.programId) {
                mVideoViewModel.switchPublishData?.value =
                    SwitchPublishBaseData(it.pushUrl, it.sdkParams, it.sdkProvider, type, it.programId)
                videoPlayerViewModel.refreshLayout?.value = true
                return
            }
        }
    }

    /**
     * 新增PK连麦
     * @param refreshView 是否需要刷新视图。PK过程当中切换CDN，不需要刷新（传值为false）
     */
    private fun addPkVideoPlayer(pkInfo: PKInfoBean, refreshView: Boolean = true) {
        videoPlayerViewModel.checkExit()
        val micList: ArrayList<MicAnchor> = arrayListOf()
        pkInfo.detailList?.forEach { anchor ->
            micList.add(MicAnchor().apply {
                this.isAnchor = mPlayerViewModel.isAnchor
                this.prePic = anchor.prePic ?: ""
                this.programId = anchor.programId
                this.headPic = anchor.headPic
                this.programName = anchor.nickname
                this.isPK = true
                this.showInfo = !refreshView
                this.playInfo = anchor.playInfo
//                this.landlord = anchor.landlord
//                this.pkType = pkInfo.pkType
            })
        }
        when (pkInfo.pkType) {
            PKType.LANDLORD -> {
                if (micList.isNotEmpty()) {
                    //斗地主 排序，最左边是当前主播，当前主播如果不是地主那么地主会在右上
                    micList.sortList(kotlin.Comparator { o1, o2 ->
                        val a1 = if (o1.programId == mPlayerViewModel.programId) 1 else 0
                        val a2 = if (o2.programId == mPlayerViewModel.programId) 1 else 0
                        var result = a2 - a1
//                        if (result == 0) {
//                            val b1 = if (o1.landlord == true) 1 else 0
//                            val b2 = if (o2.landlord == true) 1 else 0
//                            result = b2 - b1
//                        }
                        result
                    })
                }
            }
            else -> {
                //普通PK
            }
        }

        if (micList.size > 0)
            videoPlayerViewModel.addPlayer(micList)
        if (refreshView) {
            when (pkInfo.pkType) {
                PKType.LANDLORD -> {
                    mPlayerViewModel.bgChange.value = LiveBgType.PK_LANDLORD
                }
                else -> {
                    if (micList.size == 2) {
                        mPlayerViewModel.bgChange.value = LiveBgType.PK_TWO
                    } else if (micList.size == 3) {
                        mPlayerViewModel.bgChange.value = LiveBgType.PK_THREE
                    }
                }
            }
        }
    }
    fun dealWithPkCreate(data: PKCreateEvent) {
        if (!data.userIds.contains(SessionUtils.getUserId())) {
            //用户列表不包含自己，表示消息发送错误
            return
        }
//        var message = ""
//        val name = if (data.userId == SessionUtils.getUserId()) context.getString(R.string.you) else data.nickname
        when (data.action) {
            PKAction.CREATE -> {
//                if (data.userId != SessionUtils.getUserId()) {
//                    //如果此处创建界面正好打开 直接请求刷新
//                    if (createPkFragment != null && createPkFragment!!.isVisible) {
//                        createPkFragment!!.refreshState()
//                        message = context.getString(R.string.pk_inviting, data.nickname)
//                        showTopDialog(message, R.mipmap.done_icon)
//                    } else {
//                        val msg = context.getString(R.string.pk_inviting, data.nickname)
//                        MyAlertDialog(context).showAlertWithOKAndCancel(msg, MyAlertDialog.MyDialogCallback(
//                                onCancel = {
//                                    //
//                                    logger.info("拒绝PK")
//                                    viewModel.rejectPk(RejectPkForm(data.pkId))
//                                },
//                                onRight = {
//                                    openCreatePKView()
//                                }), noText = context.getString(R.string.reject), okText = context.getString(R.string.see_detail), title = context.getString(R.string.reminder))
//                    }
//                }
                connectMicroViewModel.notarizePKShowState.value = data
            }
            PKAction.ACCEPT -> {
//                message = context.getString(R.string.pk_accepted, name)
//                showTopDialog(message, R.mipmap.done_icon)
//                if (createPkFragment != null && createPkFragment!!.isVisible) {
//                    createPkFragment!!.refreshState()
//                }
                connectMicroViewModel.notarizePKShowData.value = data
            }
            PKAction.REJECT -> {//
                //拒绝PK
//                message = context.getString(R.string.pk_rejected, name)
//                showTopDialog(message, R.mipmap.attention_icon)
//                if (createPkFragment != null && createPkFragment!!.isVisible) {
//                    createPkFragment!!.dismiss()
//                }
                //拒绝PK 切换单流
                if (data.pkId == connectMicroViewModel.notarizePKShowState.value?.pkId) {
                    connectMicroViewModel.notarizePKShowState.value = null
                    ToastUtils.show(act.resources.getString(R.string.connect_pk_fail))
                }
            }
            PKAction.CANCEL -> {
                if (data.pkId == connectMicroViewModel.notarizePKShowState.value?.pkId) {
                    connectMicroViewModel.notarizePKShowState.value = null
                    ToastUtils.show(act.resources.getString(R.string.connect_pk_cancel))
                }
            }
            PKAction.TIMEOUT -> {
//                message = context.getString(R.string.pk_timeout, name)
//                showTopDialog(message, R.mipmap.attention_icon)
                if (data.pkId == connectMicroViewModel.notarizePKShowState.value?.pkId) {
                    //连麦超时 切换单流
                    connectMicroViewModel.notarizePKShowState.value = null
                    ToastUtils.show(act.resources.getString(R.string.connect_pk_timeout))
                }
            }
        }
    }


}