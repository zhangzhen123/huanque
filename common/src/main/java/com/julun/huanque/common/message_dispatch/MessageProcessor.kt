package com.julun.huanque.common.message_dispatch

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.MessageUtil
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.events.EventMessageBean
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.constant.SystemTargetId
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.reflect.ReflectUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

import io.rong.imlib.model.Message
import org.greenrobot.eventbus.EventBus

/**
 * Created by djp on 2016/12/20.
 * 一些额外消息统一在执行发送时切换到主线程
 */
object MessageProcessor {

    private val logger = ULog.getLogger("MessageProcess")
    const val EVENT_CODE = "eventCode"
    const val EVENT_CONTEXT = "context"
    private val textProcessors: MutableList<MessageReceiver> = mutableListOf()
    private val eventProcessors: MutableList<EventMessageProcessor<*>> = mutableListOf()

    //专门放置全局事件 不会被清除
//    private val globalEventProcessors: MutableList<EventMessageProcessor<*>> = mutableListOf()

    //私聊会话使用
    var privateTextProcessor: PrivateMessageReceiver? = null

    /**
     * 清空全部订阅消息 [total]是否清空全部 true代表把包括全局的事件处理订阅一起清理掉
     */

    fun clearProcessors(total: Boolean) {
        if (total) {
            textProcessors.clear()
            eventProcessors.clear()
        } else {
            textProcessors.removeAll(textProcessors.filter { !it.isGlobal() })
            eventProcessors.removeAll(eventProcessors.filter { !it.isGlobal() })
        }

    }

    fun registerTxtProcessor(processor: MessageReceiver): Unit {
        textProcessors.add(processor)
    }

    fun processTextMessage(beanList: List<TplBean>, messageType: TextMessageType) {
        try {
            beanList.forEach {
                it.preProcess()
            }
            textProcessors.forEach {
                if (it.getMessageType() == messageType) {
                    it.processMessage(beanList)
                }

            }
        } catch (e: Exception) {
            reportCrash(e)
            e.printStackTrace()
        }
    }

    /**
     * 处理私聊消息
     */
    private fun processPrivateMessage(msg: Message) {
        try {
            val targetId = msg.targetId
            if ((targetId == SystemTargetId.systemNoticeSender || targetId == SystemTargetId.friendNoticeSender) && msg.content is CustomSimulateMessage) {
                //模拟插入的系统消息或者好友通知(不做通知处理)
            } else {
                EventBus.getDefault().post(
                    EventMessageBean(
                        msg.targetId ?: "",
                        GlobalUtils.getStrangerType(msg)
                    )
                )
            }
            privateTextProcessor?.processMessage(msg)
        } catch (e: Exception) {
            reportCrash(e)
            e.printStackTrace()
        }
    }

    /**
     *处理指令消息
     * msgId 有些类型消息需要
     */
    fun parseTextMessage(jsonString: String, msgId: String) {

        val bean = MessageUtil.decodeMessageContent(jsonString)
        bean.preProcess()

        val displayTypes = bean.display.split(",")
        if (displayTypes.size == 1) {
            checkDisplayType(bean, displayTypes[0], msgId)
        } else if (displayTypes.size > 1) {

            displayTypes.forEachIndexed { index, s ->
                //第一次的用原始副本 有多个的使用克隆的
                if (index == 0) {
                    checkDisplayType(bean, displayTypes[0], msgId)
                } else {
                    val beanC = MessageUtil.decodeMessageContent(jsonString)
                    beanC.preProcess()
                    checkDisplayType(beanC, s, msgId)
                }
            }
        }

    }

    /**
     *
     * Runway SystemNotice Welcome Public这些原来的消息保留原有的格式  后续新消息统一只须用[TextMessageType]类型
     *
     */
    private fun checkDisplayType(bean: TplBean, displayType: String, msgId: String) {
        when (displayType) {
            DisplayType.Runway.name -> { //跑道
                bean.context?.msgId = msgId
                processTextMessageOnMain(arrayListOf(bean), TextMessageType.RUNWAY_MESSAGE)
            }
            DisplayType.SystemNotice.name -> { //系统通知
                processTextMessageOnMain(arrayListOf(bean), TextMessageType.SYSTEM_NOTICE_MESSAGE)
            }
            DisplayType.Welcome.name -> {//入场
                //入场特效
                processTextMessageOnMain(arrayListOf(bean), TextMessageType.WELCOME_MESSAGE)
            }
            //显示公聊
            DisplayType.Public.name -> {
                MessageReceptor.putTextMessageWithData(bean)
            }
            else -> {
                processTextMessageOnMain(
                    arrayListOf(bean), TextMessageType.parse(displayType)
                        ?: return
                )
            }
        }

    }

    /**
     * 处理事件消息
     */
    fun parseEventMessage(dataObj: JSONObject) {
        // 中奖动画、用户主播升级动画、开通守护动画 缓存到消息队列
        when (val eventCode = dataObj.getString(EVENT_CODE)) {
            //这几种动画排队执行
            EventMessageType.LuckGift.name,
            EventMessageType.UserUpLevel.name,
            EventMessageType.AnchorLevelChange.name,
            EventMessageType.SUPER_LUCK_GIFT.name,
            EventMessageType.OpenGuard.name ->/* MessageReceptor.putEventMessage(eventCode, (dataObj["context"] as JSONObject).toJSONString())*/
                MessageReceptor.putEventMessage(eventCode, dataObj.getJSONObject(EVENT_CONTEXT))
            else -> // 其他事件直接通知UI`
            {
                val data: Any?
                data = try {
                    val clazz = EventMessageType.valueOf(eventCode).klass
                    JSON.toJavaObject(dataObj.getJSONObject(EVENT_CONTEXT), clazz)
                } catch (e: IllegalArgumentException) {
                    logger.info("解析事件消息报错了类型：$eventCode")
                    e.printStackTrace()
                    //banner消息解析异常特殊处理 这里也就banner消息会出问题其打破了消息规范
                    dataObj.toJSONString()
                }
                processEventMessageOnMain(data, eventCode)
            }

        }
    }

    //执行文本消息类型的事件消息 比如跑道 入场特效 系统消息 在主线程
    fun processTextMessageOnMain(beanList: List<TplBean>, messageType: TextMessageType) {
        processOnMain {
            //            logger.info("processText 当前的切换线程:${Thread.currentThread().name}")
            processTextMessage(beanList, messageType)
        }
    }

    //执行私聊消息
    fun processPrivateTextMessageOnMain(messageList: Message) {
        processOnMain {
            //            logger.info("processText 当前的切换线程:${Thread.currentThread().name}")
            processPrivateMessage(messageList)
        }
    }

    //执行事件消息在主线程
    private fun processEventMessageOnMain(data: Any?, eventCode: String) {
        processOnMain {
            //            logger.info("processEvent 当前的切换线程:${Thread.currentThread().name}")
            processEventMessage(data, eventCode)
        }
    }

    private fun processOnMain(callback: () -> Unit) {
        Observable.empty<Any>().observeOn(AndroidSchedulers.mainThread()).doOnComplete {
            callback()
        }.subscribe()
    }

    fun registerEventProcessor(processor: EventMessageProcessor<*>): Unit {
        eventProcessors.add(processor)
    }


    /**
     * 移除单个EventProcessor
     */
    fun removeEventProcessor(processor: EventMessageProcessor<*>) {
        eventProcessors.remove(processor)
    }

    fun processEventMessage(data: Any?, eventCode: String) {
        try {
            if (data is String && !TextUtils.isEmpty(data)) {
                eventProcessors.forEach {
//                    if (it.getEventType().name == "BannerMessage") {
//                        //直播间banner消息，直接传递
//                        it.processBridge(data)
//                        return
//                    }
                    if (it.getEventType().name == eventCode) {
                        val raw: Any? = wrapEvent(data, EventMessageType.valueOf(eventCode))
                        if (raw != null) {
                            it.processBridge(raw)
                        } else {
                            it.processBridge()
                        }
                    }

                }

            } else {
                eventProcessors.filter { it.getEventType().name == eventCode }.forEach {
                    if (data != null) {
                        it.processBridge(data)
                    } else {
                        it.processBridge()
                    }
                }
            }
        } catch (e: Exception) {
            //因为切换线程了 会有潜在的崩溃
            reportCrash(e)
            e.printStackTrace()
        }
    }

    private fun <T> wrapEvent(data: String, eventCode: EventMessageType): T? {
        if (EventMessageType.ANIMATION.equals(eventCode)) {
            logger.info("wrapEvent ANIMATION执行了：${eventCode.name}")
            return JsonUtil.deserializeAsObject(data, eventCode.klass)
        }
        val deserializeAsObject: EventMessageContent<T> =
            JsonUtil.deserializeAsObject(
                data,
                ReflectUtil.type(EventMessageContent::class.java, eventCode.klass)
            )
        val context: T? = deserializeAsObject.context
        return context
    }


    enum class MessageType {
        Text, Event, Animation
    }

    //Room、Broadcast、User
    enum class TargetType {
        Room, Broadcast, User, ChatRoom
    }

    //消息的房间类型
    enum class RoomType {
        LiveRoom, ChatRoom
    }

    /**
     * 这里只在已有的老消息使用
     */
    enum class DisplayType {
        Runway, ROYAL, GUARD, Welcome, Public, SystemNotice/*, WeekStar, FactoryCar, NewRedPacket*/;
    }

    /**
     * 新的Text类型消息全部使用TextMessageType
     */
    enum class TextMessageType {
        PRIVATE_MESSAGE, PUBLIC_MESSAGE, RUNWAY_MESSAGE, SYSTEM_NOTICE_MESSAGE, /*ROYAL_MESSAGE, GUARD_MESSAGE,*/ WELCOME_MESSAGE,
        WeekStar, FactoryCar, NewRedPacket, GiftBoxRunway, Headline, DrawAct, Planet;

        companion object {
            fun parse(value: String): TextMessageType? {
                return values().firstOrNull { it.name == value }
            }
        }
    }


    //除了私聊消息之外的其它消息
    interface MessageReceiver {
        fun getMessageType(): TextMessageType
        fun processMessage(messageList: List<TplBean>)
        fun isGlobal(): Boolean = false
    }

    //私聊消息使用
    interface PrivateMessageReceiver {
        fun processMessage(msg: Message)
    }

    //上神聊天室消息使用
    interface PublicMessageReceiver {
        fun processMessage(msg: Message)
    }

    /**
     * 文本消息处理器
     */
    interface TextMessageReceiver : MessageReceiver {
        override fun getMessageType(): TextMessageType = TextMessageType.PUBLIC_MESSAGE
    }

//    interface PrivateTextMessageReceiver : MessageReceiver {
//        override fun getMessageType(): TextMessageType = TextMessageType.PRIVATE_MESSAGE
//    }

    interface RunwayMessageReceiver : MessageReceiver {
        override fun getMessageType(): TextMessageType = TextMessageType.RUNWAY_MESSAGE
    }

    interface SysNoticeMessageReceiver : MessageReceiver {
        override fun getMessageType(): TextMessageType = TextMessageType.SYSTEM_NOTICE_MESSAGE
    }

    interface WeekStarMessageReceiver : MessageReceiver {
        override fun getMessageType(): TextMessageType = TextMessageType.WeekStar
    }

    interface HeadlineMessageReceiver : MessageReceiver {
        override fun getMessageType(): TextMessageType = TextMessageType.Headline
    }

    interface DrawActMessageReceiver : MessageReceiver {
        override fun getMessageType(): TextMessageType = TextMessageType.DrawAct
    }

    interface PlanetMessageReceiver : MessageReceiver {
        override fun getMessageType(): TextMessageType = TextMessageType.Planet
    }


    interface RedPacketMessageReceiver : MessageReceiver {
        override fun getMessageType(): TextMessageType = TextMessageType.NewRedPacket
    }

    //    //贵族入场
//    interface RoyalMessageReceiver : MessageReceiver {
//        override fun getMessageType(): TextMessageType = TextMessageType.ROYAL_MESSAGE
//    }
//
//    //守护进场
//    interface GuardMessageReceiver : MessageReceiver {
//        override fun getMessageType(): TextMessageType = TextMessageType.GUARD_MESSAGE
//    }
    //入场消息
    interface WelcomeMessageReceiver : MessageReceiver {
        override fun getMessageType(): TextMessageType = TextMessageType.WELCOME_MESSAGE
    }

    //豪车大亨消息
    interface HuxuryCarMessageReceiver : MessageReceiver {
        override fun getMessageType(): TextMessageType = TextMessageType.FactoryCar
    }

    //礼盒跑道
    interface BoxRunwayMessageReceiver : MessageReceiver {
        override fun getMessageType(): TextMessageType = TextMessageType.GiftBoxRunway
    }

    /**
     * 事件处理器
     */
    interface EventMessageProcessor<in T> {
        fun getEventType(): EventMessageType
        fun process(data: T)

        // 停播，开播事件，"context"没有值
        fun processBridge(raw: Any? = VoidResult()) = process(raw as T)

        //标记是不是全局订阅
        fun isGlobal(): Boolean = false
    }

    /**
     * 送礼事件的处理
     */
    interface GiftMessageProcessor : EventMessageProcessor<SendGiftEvent> {
        override fun getEventType() = EventMessageType.SendGift
    }

    /**
     * PK创建信息处理
     */
    interface PKCreateMessageProcess : EventMessageProcessor<PKCreateEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.PkOperate
    }

    /**
     * PK开始的处理
     */
    interface PKStartMessageProcess : EventMessageProcessor<PKStartEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.PkStarting
    }

    /**
     * PK过程中CDN切换的消息
     */
    interface PkToggleProviderMessageProcess : EventMessageProcessor<PKStartEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.PkToggleProvider
    }

    /**
     * PK比分变化的处理
     */
    interface PKScoreChangeMessageProcess : EventMessageProcessor<PKScoreChangeEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.PkScoreChange
    }

    /**
     * PK结果
     */
    interface PKResultMessageProcess : EventMessageProcessor<PKResultEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.PkResult
    }

    /**
     * PK结束 真正的结束
     */
    interface PKFinishMessageProcess : EventMessageProcessor<PKFinishEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.PkFinish
    }

    /**
     * PK道具
     */
    interface PkPropMessageProcess : EventMessageProcessor<PkPropEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.PkProp
    }

    /**
     * PK道具使用提醒
     */
    interface PkPropUseWarnMessageProcess : EventMessageProcessor<PkPropUseWarnEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.PkPropUseWarn
    }

    /**
     * PK礼物任务
     */
    interface PkTaskMessageProcess : EventMessageProcessor<PkTaskEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.PkTask
    }

    /**
     * PK积分任务
     */
    interface PkScoreTaskMessageProcess : EventMessageProcessor<PkScoreEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.PkScoreTask
    }

    /**
     * 月赛结果变化
     */
    interface MonthPKResultMessageProcess : EventMessageProcessor<MonthPKResultEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.YUESAI_PRE_TOPCHANGE
    }


    /**
     * 愚人节福蛋消息
     */
    interface YuRenJieProcess : EventMessageProcessor<YuRenJieEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.YuRenJie2018TTL
    }

    /**
     * 本场贡献榜变化消息
     */
    interface ContributionProcess : EventMessageProcessor<ContributionEvent> {
        override fun getEventType(): EventMessageType = EventMessageType.RoomTotalScoreChange
    }

    /**
     * 开通守护的处理
     */
    interface OpenGuardProcessor : EventMessageProcessor<OpenGuardEvent> {
        override fun getEventType() = EventMessageType.OpenGuard

    }

    /**
     * 开通临时守护的处理
     */
    interface OpenExperienceGuardProcessor : EventMessageProcessor<OpenGuardEvent> {
        override fun getEventType() = EventMessageType.OpenExperienceGuard
    }

    /**
     * 排行榜事件的处理
     */
//    interface RankMessageProcessor : EventMessageProcessor<RankingEvent> {
//        override fun getEventType() = EventMessageType.RANKING_SNAPSHOT
//    }

    /**
     * 主播经验事件的处理
     */
    interface AnchorUpLevelProcessor : EventMessageProcessor<AnchorUpgradeEvent> {
        override fun getEventType() = EventMessageType.AnchorLevelChange
    }

    /**
     * 弹幕的处理
     */
    interface DanMuProcessor : EventMessageProcessor<BarrageEvent> {
        override fun getEventType() = EventMessageType.SendDanmu
    }

    /**
     * 广播弹幕的处理
     */
    interface BRDanMuProcessor : EventMessageProcessor<BarrageEvent> {
        override fun getEventType() = EventMessageType.BROADCAST
    }

    /**
     * 用户列表变化处理
     */
    interface RoomUserMessageProcessor : EventMessageProcessor<RoomUserChangeEvent> {
        override fun getEventType() = EventMessageType.RoomUsersChange
    }

    /**
     * 幸运礼物消息
     */
    interface LuckGiftMessageProcessor : EventMessageProcessor<LuckGiftEvent> {
        override fun getEventType() = EventMessageType.LuckGift
    }

    /**
     * 超级幸运礼物
     */
    interface SuperLuckGiftMessageProcessor : EventMessageProcessor<SuperLuckGiftEvent> {
        override fun getEventType() = EventMessageType.SUPER_LUCK_GIFT

    }

    /**
     * 动画消息
     */
    interface AnimationMessageProcessor : EventMessageProcessor<AnimEventBean> {
        override fun getEventType() = EventMessageType.ANIMATION
    }

    /**
     * 清除动画消息
     */
    interface ClearAnimationMessageProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.CLEAR_ANIMATION
    }

    /**
     * 主播、用户升级
     */
    interface UserLevelChangeMessageProcessor : EventMessageProcessor<UserUpgradeEvent> {
        override fun getEventType() = EventMessageType.UserUpLevel
    }

    /**
     * 主播、用户升级
     */
    interface RoyalUpLevelChangeMessageProcessor : EventMessageProcessor<RoyalUpLevelEvent> {
        override fun getEventType() = EventMessageType.RoyalUpLevel
    }

    /**
     * 用户经验条变化
     */
    interface UserExpChangeMessageProcessor : EventMessageProcessor<UserExpChangeEvent> {
        override fun getEventType() = EventMessageType.UserExpChange
    }

    /**
     * 飘心动画消息
     */
    interface AddHeartMessageProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.ADD_HEART
    }

    /**
     * 开播
     */
    interface StartLivingMessageProcessor : EventMessageProcessor<OpenShowEvent> {
        override fun getEventType() = EventMessageType.StartLiving
    }

    /**
     * 连麦操作事件
     */
    interface MicOperateProcessor : EventMessageProcessor<MicOperateBean> {
        override fun getEventType() = EventMessageType.MicOperate
    }

    /**
     * 连麦开始
     */
    interface MicStartingProcessor : EventMessageProcessor<MicActionBean> {
        override fun getEventType() = EventMessageType.MicStarting
    }

    /**
     * 连麦中切换CDN
     */
    interface MicToggleProviderProcessor : EventMessageProcessor<MicActionBean> {
        override fun getEventType() = EventMessageType.MicToggleProvider
    }

    /**
     * 连麦断掉
     */
    interface MicDisconnectProcessor : EventMessageProcessor<MicActionBean> {
        override fun getEventType() = EventMessageType.MicDisconnect
    }

    /**
     * 连麦结束
     */
    interface MicFinishProcessor : EventMessageProcessor<MicActionBean> {
        override fun getEventType() = EventMessageType.MicFinished
    }

    /**
     * 停播
     */
    interface StopLivingMessageProcessor : EventMessageProcessor<CloseShowEvent> {
        override fun getEventType() = EventMessageType.StopLiving
    }

    interface RefreshUserMessageProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.RefreshUser
    }

    /**
     * 主播升级进度消息的处理
     */
    interface AnchorLevelProgressProcessor : EventMessageProcessor<AnchorLevelProgressEvent> {
        override fun getEventType() = EventMessageType.AnchorExpChange
    }

    /**
     * 年度盛典秒杀消息
     */
    interface YearSeckillMessageProcessor : EventMessageProcessor<YearSeckillResult> {
        override fun getEventType() = EventMessageType.SecKillCountDown
    }

    /**
     * 年度盛典活动Banner 数据变化消息
     */
    interface YearBannerChangeMessageProcessor : EventMessageProcessor<YearBannerResult> {
        override fun getEventType() = EventMessageType.NdsdTopChange
    }

    /**
     * 年度盛典红包数据变化消息
     */
    interface YearRPChangeMessageProcessor : EventMessageProcessor<YearRedPackageResult> {
        override fun getEventType() = EventMessageType.RedPacket
    }

    /**
     * 双旦活动Banner 数据变化消息
     */
    interface NewYearRPChangeMessageProcessor : EventMessageProcessor<NewYearBannerResult> {
        override fun getEventType() = EventMessageType.ActTopChange
    }

    /**
     * 有任务完成消息 代表可领取
     */
    interface FinishMissionMessageProcessor : EventMessageProcessor<FinishMissionBean> {
        override fun getEventType() = EventMessageType.FinishMission
    }

    /**
     * 有成就完成消息
     */
    interface FinishAchievementMessageProcessor : EventMessageProcessor<FinishAchievementBean> {
        override fun getEventType() = EventMessageType.FinishAchievement
    }

    /**
     * 直播间banner消息
     */
    interface RoomBannerMessageProcessor : EventMessageProcessor<String> {
        override fun getEventType() = EventMessageType.BannerMessage
    }

    /**
     * 消息中心红点消息
     */
    interface MsgRedPointMessageProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.MsgCenterNewMsg
    }

    /**
     * 首页活动消息
     */
    interface OrdinaryActivitiesChangeProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.OrdinaryActivitiesChange
    }

    /**
     * 账号注销通过消息
     */
    interface DestroyAccountMessageProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.DestroyAccount
    }

    /**
     * PK竞猜结果消息
     */
    interface PkGuessEndMessageProcessor : EventMessageProcessor<PkGuessResult> {
        override fun getEventType() = EventMessageType.PkGuessEnd
    }

    /**
     * PK竞猜赔率变化消息
     */
    interface PkGuessOddsChangeMessageProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.PkGuessOddsChange
    }

    /**
     * 通用的额外样式直播间消息
     */
    interface PublicCustomMsgProcessor : EventMessageProcessor<PublicCustomMsgBean> {
        override fun getEventType() = EventMessageType.PublicCustomMsg
    }

    /**
     * 通用的额外样式直播间消息
     */
    interface UserFansLevelChangeProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.UserFansLevelChange
    }

    /**
     * 聊天室祝福卡数量变动
     */
    interface ChatRoomCardsChangeProcessor : EventMessageProcessor<ChatRoomCardsChangeBean> {
        override fun getEventType() = EventMessageType.ChatRoomCardsChange
    }

    /**
     * 聊天室人数变动
     */
    interface ChatRoomUsersChangeProcessor : EventMessageProcessor<ChatRoomUsersChangeBean> {
        override fun getEventType() = EventMessageType.ChatRoomUsersChange
    }

    /**
     * 异步红包数据变化消息
     */
    interface SyncRPChangeMessageProcessor : EventMessageProcessor<YearRedPackageResult> {
        override fun getEventType() = EventMessageType.RobRedPacketResult
    }

    /**
     * 上神跑道直播间消息
     */
    interface UpGodRunWayMessageProcessor : EventMessageProcessor<TplBean> {
        override fun getEventType() = EventMessageType.UpGodRunWay
    }

    /**
     * 聊天室关闭消息
     */
    interface CloseChatRoomProcessor : EventMessageProcessor<CloseChatRoomBean> {
        override fun getEventType() = EventMessageType.CloseChatRoom
    }

    /**
     * 聊天室关闭消息
     */
    interface ChatRoomKickUserProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.ChatRoomKickUser
    }

    /**
     * 聊天室封禁消息
     */
    interface BlockChatRoomProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.BlockChatRoom
    }


    /**
     * 猜字谜消息
     * @author WanZhiYuan
     * @version 4.15
     * @iterativeDate 2019/07/25
     */
    interface GuessWordsMessageProcessor : EventMessageProcessor<BubbleResult> {
        override fun getEventType() = EventMessageType.RiddleChange
    }

    /**
     * 翻牌游戏消息
     * @author WanZhiYuan
     * @version 4.16
     * @iterativeDate 2019/08/09
     */
    interface FlipCardMessageProcessor : EventMessageProcessor<FlipCardResult> {
        override fun getEventType() = EventMessageType.TurnCard
    }


    /*
     * 闯关刷新消息
     */
    interface PassLevelTopUserChangeProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.PassLevelTopUserChange
    }

    /*
     * 猜字谜第一名奖励消息
     */
    interface RiddleAwardProcessor : EventMessageProcessor<WordPuzzleAward> {
        override fun getEventType() = EventMessageType.RiddleAward
    }

    /**
     * 主播端PK开始消息
     */
    interface AnchorPkStartingProcessor : EventMessageProcessor<AnchorPKStartEvent> {
        override fun getEventType() = EventMessageType.AnchorPkStarting
    }

    /**
     * 主播端PK过程中切换CDN消息
     */
    interface AnchorPkToggleProviderProcessor : EventMessageProcessor<AnchorPKStartEvent> {
        override fun getEventType() = EventMessageType.AnchorPkToggleProvider
    }

    /**
     * 主播端PK结束消息
     */
    interface AnchorPkFinishProcessor : EventMessageProcessor<AnchorPKStartEvent> {
        override fun getEventType() = EventMessageType.AnchorPkFinish
    }

    /**
     * 主播端连麦开始消息
     */
    interface AnchorMicStartingProcessor : EventMessageProcessor<MicActionBean> {
        override fun getEventType() = EventMessageType.AnchorMicStarting
    }

    /**
     * 主播端连麦过程中切换CDN消息
     */
    interface AnchorMicToggleProviderProcessor : EventMessageProcessor<MicActionBean> {
        override fun getEventType() = EventMessageType.AnchorMicToggleProvider
    }

    /**
     * 主播端连麦结束消息
     */
    interface AnchorMicFinishedProcessor : EventMessageProcessor<MicActionBean> {
        override fun getEventType() = EventMessageType.AnchorMicFinished
    }

    /**
     * 主播端连麦断开消息
     */
    interface AnchorMicDisconnectProcessor : EventMessageProcessor<MicActionBean> {
        override fun getEventType() = EventMessageType.AnchorMicDisconnect
    }

    /*
     * 激活贵族体验卡
     */
    interface ActiveRoyalProcessor : EventMessageProcessor<RoyalCardBean> {
        override fun getEventType() = EventMessageType.RechargeActiveRoyalCard
    }

    /*
     * 运营弹窗发
     */
    interface OptPopupWindowProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.OptPopupWindow
    }

    /*
     * 横竖屏切换
     */
    interface RoomCutoverScreenTypeProcessor : EventMessageProcessor<SwitchScreen> {
        override fun getEventType() = EventMessageType.RoomCutoverScreenType
    }

    /*
     * pk拯救 周星动画消息
     */
    interface PkMicSalvationAlertProcessor : EventMessageProcessor<PkMicSalvationInfo> {
        override fun getEventType() = EventMessageType.StartPkMicSalvationFans
    }

    /*
     * pk拯救开始
     */
    interface StartPkMicSalvationProcessor : EventMessageProcessor<PkMicSalvationStartInfo> {
        override fun getEventType() = EventMessageType.StartPkMicSalvation
    }

    /**
     * PK拯救变化
     */
    interface PkMicSalvationChangeProcessor : EventMessageProcessor<PkMicSalvationChangeInfo> {
        override fun getEventType() = EventMessageType.PkMicSalvationChange
    }

    /*
     * 热度跑道
     */
    interface HotTrackProcessor : EventMessageProcessor<HotTrackInfo> {
        override fun getEventType() = EventMessageType.HotPositionChange
    }

    /*
     * 刷新段位赛消息
     */
    interface PkRanRefreshProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.StageChange
    }


    /*
     * CDN切换成功消息
     */
    interface ToggleCDNSuccProcessor : EventMessageProcessor<PlayInfoBean> {
        override fun getEventType() = EventMessageType.ToggleCDNSucc
    }

    /**
     * 开始切换CDN消息
     */
    interface ToggleCDNProcessor : EventMessageProcessor<SwitchPublishBaseData> {
        override fun getEventType() = EventMessageType.ToggleCDN
    }

    /**
     * 月卡购买消息
     */
    interface OpenMonthCardProcessor : EventMessageProcessor<OpenMonthCardMessage> {
        override fun getEventType() = EventMessageType.OpenMonthCard
    }

    /**
     * 主播发布动态（帖子/视频）审核通过消息
     */
    interface AnchorDynamicRedDotProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.AnchorDynamicRedDot
    }

    /**
     * 三重礼包可以领取消息
     */
    interface CallBackPackFinishProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.CallBackPackFinish
    }

//    /**
//     * 炮台变更事件
//     */
//    interface PlanetBatteryChangeProcessor : EventMessageProcessor<PlanetBatteryChangeBean> {
//        override fun getEventType() = EventMessageType.PlanetBatteryChange
//    }
//
//    /**
//     * 获得爆能枪卡事件
//     */
//    interface PlanetCasualCardGetProcessor : EventMessageProcessor<PlanetCasualCardGetBean> {
//        override fun getEventType() = EventMessageType.PlanetCasualCardGet
//    }
//
//    /**
//     * 休闲模式  最高分用户变更事件
//     */
//    interface PlanetCasualTopRefreshProcessor : EventMessageProcessor<TopUserInfo> {
//        override fun getEventType() = EventMessageType.PlanetCasualTopRefresh
//    }
//
//    /**
//     * 主播开启幸运星球模式
//     */
//    interface ReleaseLuckcyPlanetProcessor : EventMessageProcessor<LuckyStartBean> {
//        override fun getEventType() = EventMessageType.ReleaseLuckcyPlanet
//    }
//
//    /**
//     * 幸运星球消失
//     */
//    interface LuckyPlanetDisappearProcessor : EventMessageProcessor<LuckyPlanetDisappearBean> {
//        override fun getEventType() = EventMessageType.LuckyPlanetDisappear
//    }
//
//    /**
//     * 攻击模式 霸主信息变更
//     */
//    interface PlanetScoreChangeProcessor : EventMessageProcessor<PlanetScoreChangeBean> {
//        override fun getEventType() = EventMessageType.PlanetScoreChange
//    }

    /**
     * 直播间Banner变化消息
     */
    interface RoomBannerChangeProcessor : EventMessageProcessor<RoomBannerChangeBean> {
        override fun getEventType() = EventMessageType.RoomBannerChange
    }

    /**
     * 弹幕卡数量变化
     */
    interface GetDanmuCardProcessor : EventMessageProcessor<PlayerDataChanged> {
        override fun getEventType() = EventMessageType.GetDanmuCard
    }

    /**
     * 主播嘉宾名单变更
     * @author WanZhiYuan
     * @since 4.31
     * @date 2020/05/08
     */
    interface GuestStatusProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.GuestPrivileges
    }

    /**
     * 主题房节目开始
     */
    interface ThemeSessionStartProcessor : EventMessageProcessor<PlayInfoBean> {
        override fun getEventType() = EventMessageType.ThemeSessionStart
    }

    /**
     * 主题房关闭
     */
    interface ThemeSessionFinishProcessor : EventMessageProcessor<ThemeSessionFinishBean> {
        override fun getEventType() = EventMessageType.ThemeSessionFinish
    }

    /**
     * 主题房节目单变化
     */
    interface ThemeProgramShowChangeNoticeProcessor : EventMessageProcessor<ProgramRoomBean> {
        override fun getEventType() = EventMessageType.ThemeProgramShowChangeNotice
    }


    /**
     * 语音通话被叫消息
     */
    interface NetCallReceiveProcessor : EventMessageProcessor<NetCallReceiveBean> {
        override fun getEventType() = EventMessageType.NetCallReceive
        override fun isGlobal() = true
    }

    /**
     * 语音通话接通消息
     */
    interface NetCallAcceptProcessor : EventMessageProcessor<NetCallAcceptBean> {
        override fun getEventType() = EventMessageType.NetCallAccept
    }

    /**
     * 主叫取消会话消息
     */
    interface NetCallCancelProcessor : EventMessageProcessor<NetCallReceiveBean> {
        override fun getEventType() = EventMessageType.NetCallCancel
    }

    /**
     * 挂断消息
     */
    interface NetCallHangUpProcessor : EventMessageProcessor<NetCallHangUpBean> {
        override fun getEventType() = EventMessageType.NetCallHangUp
    }

    /**
     * 被叫拒绝通话消息
     */
    interface NetCallRefuseProcessor : EventMessageProcessor<NetCallReceiveBean> {
        override fun getEventType() = EventMessageType.NetCallRefuse
    }

    /**
     * 会话断开消息（服务端断开）
     */
    interface NetCallDisconnectProcessor : EventMessageProcessor<VoidResult> {
        override fun getEventType() = EventMessageType.NetCallDisconnect
    }

    /**
     * 对方忙消息
     */
    interface NetCallBusyProcessor : EventMessageProcessor<NetCallReceiveBean> {
        override fun getEventType() = EventMessageType.NetCallBusy
    }

    /**
     * 余额不足提醒消息
     */
    interface NetCallBalanceRemindProcessor : EventMessageProcessor<NetCallBalanceRemindBean> {
        override fun getEventType() = EventMessageType.NetCallBalanceRemind
    }

    interface IntimateChangeProcessor : EventMessageProcessor<IntimateBean> {
        override fun getEventType() = EventMessageType.IntimateChange
        override fun isGlobal() = true
    }

    /**
     * 踢人消息
     */
    interface KickUserProcessor : EventMessageProcessor<OperatorMessageBean> {
        override fun getEventType() = EventMessageType.KickUser
    }
//    /**
//     * 禁言消息
//     */
//    interface MuteUserProcessor : EventMessageProcessor<OperatorMessageBean> {
//        override fun getEventType() = EventMessageType.MuteUser
//    }
    /**
     * 封禁账户消息
     */
    interface BanUserProcessor : EventMessageProcessor<OperatorMessageBean> {
        override fun getEventType() = EventMessageType.BanUser
    }

    /**
     * 直播封禁（用户不允许进入任何直播间）
     */
    interface BanUserLivingProcessor : EventMessageProcessor<OperatorMessageBean> {
        override fun getEventType() = EventMessageType.BanUserLiving
    }

    //直播间热度变化
    interface RoomHeatChangeProcessor : EventMessageProcessor<RoomHeatChangeBean> {
        override fun getEventType() = EventMessageType.RoomHeatChange
    }

    /**
     * 匿名语音超时消息
     */
    interface AnonyVoiceTimeoutProcessor : EventMessageProcessor<UserIdListBean> {
        override fun getEventType() = EventMessageType.AnonyVoiceTimeout
    }

    /**
     * 匿名语音成功
     */
    interface AnonyVoiceConnectProcessor : EventMessageProcessor<AnonyVoiceSuccess> {
        override fun getEventType() = EventMessageType.AnonyVoiceConnect
    }

    /**
     * 匿名语音挂断消息
     */
    interface AnonyVoiceHangUpProcessor : EventMessageProcessor<AnonyVoiceHangUpBean> {
        override fun getEventType() = EventMessageType.AnonyVoiceHangUp
    }

    /**
     * 匿名语音 公开身份消息
     */
    interface AnonyVoiceOpenProcessor : EventMessageProcessor<UserInfoInRoom> {
        override fun getEventType() = EventMessageType.AnonyVoiceOpen
    }

    /**
     * 匿名语音  邀请消息
     */
    interface AnonyVoiceInviteProcessor : EventMessageProcessor<AnonyVoiceInviteBean> {
        override fun getEventType() = EventMessageType.AnonyVoiceInvite

        override fun isGlobal() = true
    }


}

enum class EventMessageType(val klass: Class<*>) {
    /** 礼物 **/
    SendGift(SendGiftEvent::class.java)
    /** 排行榜 **/
//        ,
//        RANKING_SNAPSHOT(RankingEvent::class.java)
    /** 主播等级变化 **/
    ,
    AnchorLevelChange(AnchorUpgradeEvent::class.java)
    /** 用户升级 **/
    ,
    UserUpLevel(UserUpgradeEvent::class.java),

    /** 贵族升级 **/
    RoyalUpLevel(RoyalUpLevelEvent::class.java),

    UserExpChange(UserExpChangeEvent::class.java)
    /** 用户列表 **/
    ,
    RoomUsersChange(RoomUserChangeEvent::class.java)
    /** 开通守护 **/
    ,
    OpenGuard(OpenGuardEvent::class.java)
    ,

    /** 开通体验守护 **/
    OpenExperienceGuard(OpenGuardEvent::class.java)
    /** 幸运礼物 **/
    ,
    LuckGift(LuckGiftEvent::class.java)
    ,
    SUPER_LUCK_GIFT(SuperLuckGiftEvent::class.java)
    /** 弹幕礼物 **/
    ,
    SendDanmu(BarrageEvent::class.java)
    /** 广播弹幕礼物 **/
    ,
    BROADCAST(BarrageEvent::class.java)
    /** 飘心动画消息 **/
    ,
    ADD_HEART(VoidResult::class.java)

    /** 动画消息 **/
    ,
    ANIMATION(AnimEventBean::class.java)
    ,
    CLEAR_ANIMATION(VoidResult::class.java),

    /** 开播事件 **/
    StartLiving(OpenShowEvent::class.java),

    /** 停播事件 **/
    StopLiving(CloseShowEvent::class.java),

    /**PK开始**/
    PkStarting(PKStartEvent::class.java),

    /**PK过程中切换CDN**/
    PkToggleProvider(PKStartEvent::class.java),

    /**PK分数变化**/
    PkScoreChange(PKScoreChangeEvent::class.java),

    /**PK结果**/
    PkResult(PKResultEvent::class.java),

    /**pk创建的消息**/
    PkOperate(PKCreateEvent::class.java),

    PkFinish(PKFinishEvent::class.java),

    /**PK道具消息**/
    PkProp(PkPropEvent::class.java),

    /**PK积分任务消息**/
    PkScoreTask(PkScoreEvent::class.java),

    /**PK礼物任务消息**/
    PkTask(PkTaskEvent::class.java),

    /**PK道具使用消息**/
    PkPropUseWarn(PkPropUseWarnEvent::class.java),

    /**月赛PK结果**/
    YUESAI_PRE_TOPCHANGE(MonthPKResultEvent::class.java),

    /** 主播升级进度条 **/
    AnchorExpChange(AnchorLevelProgressEvent::class.java),

    /** 端午节礼物 **/
    BOAT_PROGRESS(BoatProgressEventBean::class.java),

    //答题开始通知
    QuestionStart(VoidResult::class.java),

    YuRenJie2018TTL(YuRenJieEvent::class.java),
    RoomTotalScoreChange(ContributionEvent::class.java),

    RefreshUser(VoidResult::class.java),

    MicOperate(MicOperateBean::class.java),
    MicStarting(MicActionBean::class.java),

    //连麦过程当中切换CDN消息
    MicToggleProvider(MicActionBean::class.java),
    MicFinished(MicActionBean::class.java),
    MicDisconnect(MicActionBean::class.java),//主播心跳断开时发送

    //年度盛典
    SecKillCountDown(YearSeckillResult::class.java),
    NdsdTopChange(YearBannerResult::class.java),
    RedPacket(YearRedPackageResult::class.java),

    //异步红包通知
    RobRedPacketResult(YearRedPackageResult::class.java),

    //双旦活动
    ActTopChange(NewYearBannerResult::class.java),
    FinishMission(FinishMissionBean::class.java),
    FinishAchievement(FinishAchievementBean::class.java),

    /**
     * 直播间banner消息
     */
    BannerMessage(String::class.java),

    //消息中心红点消息
    MsgCenterNewMsg(VoidResult::class.java),

    /**
     *PK竞猜赔率变化
     */
    PkGuessOddsChange(VoidResult::class.java),

    /**
     * PK竞猜结束消息
     */
    PkGuessEnd(PkGuessResult::class.java),

    /**
     * 额外样式公聊消息
     */
    PublicCustomMsg(PublicCustomMsgBean::class.java),


    //特权变化消息
    UserFansLevelChange(VoidResult::class.java),

    /**
     * 祝福卡数量变动消息
     */
    ChatRoomCardsChange(ChatRoomCardsChangeBean::class.java),

    /**
     * 直播间人数变动消息
     */
    ChatRoomUsersChange(ChatRoomUsersChangeBean::class.java),


    //上神跑道消息
    UpGodRunWay(TplBean::class.java),


    //关闭直播间消息
    CloseChatRoom(CloseChatRoomBean::class.java),

    //被提出聊天消息
    ChatRoomKickUser(VoidResult::class.java),

    //聊天室封禁消息
    BlockChatRoom(VoidResult::class.java),


    //气泡消息
    RiddleChange(BubbleResult::class.java),

    //翻牌游戏消息
    TurnCard(FlipCardResult::class.java),


    //闯关最佳刷新标识
    PassLevelTopUserChange(VoidResult::class.java),

    //猜字谜中奖消息
    RiddleAward(WordPuzzleAward::class.java),

    //横竖屏切换消息
    RoomCutoverScreenType(SwitchScreen::class.java),

    //开启PK拯救动画（周星动画）
    StartPkMicSalvationFans(PkMicSalvationInfo::class.java),

    //开启PK拯救
    StartPkMicSalvation(PkMicSalvationStartInfo::class.java),

    //PK拯救消息变化
    PkMicSalvationChange(PkMicSalvationChangeInfo::class.java),

    //激活贵族体验卡消息
    RechargeActiveRoyalCard(RoyalCardBean::class.java),

    //运营弹窗消息
    OptPopupWindow(VoidResult::class.java),

    //热度体系消息
    HotPositionChange(HotTrackInfo::class.java),

    //PK开始消息，主播端
    AnchorPkStarting(AnchorPKStartEvent::class.java),

    //PK中SDK切换消息
    AnchorPkToggleProvider(AnchorPKStartEvent::class.java),

    //PK结束消息，主播端
    AnchorPkFinish(AnchorPKStartEvent::class.java),

    //连麦开始消息，主播端
    AnchorMicStarting(MicActionBean::class.java),

    //连麦中，SDK切换
    AnchorMicToggleProvider(MicActionBean::class.java),

    //连麦结束消息，主播端
    AnchorMicFinished(MicActionBean::class.java),

    //连麦断开消息，主播端
    AnchorMicDisconnect(MicActionBean::class.java),

    //刷新PK段位赛
    StageChange(VoidResult::class.java),

    //CDN切换消息
    //这个消息又代表主题房切换主播流
    ToggleCDNSucc(PlayInfoBean::class.java),

    //主播端开始切换CDN消息
    ToggleCDN(SwitchPublishBaseData::class.java),

    OrdinaryActivitiesChange(VoidResult::class.java),

    //账号注销成功的通知
    DestroyAccount(VoidResult::class.java),

    //月卡购买消息
    OpenMonthCard(OpenMonthCardMessage::class.java),

    //关注主播动态/视频变更
    AnchorDynamicRedDot(VoidResult::class.java),

    //三重礼包可以领取消息
    CallBackPackFinish(VoidResult::class.java),

//    //炮台变更事件
//    PlanetBatteryChange(PlanetBatteryChangeBean::class.java),
//
//    //休闲模式，获得爆能枪卡事件
//    PlanetCasualCardGet(PlanetCasualCardGetBean::class.java),
//
//    //休闲模式，最高分用户变更事件
//    PlanetCasualTopRefresh(TopUserInfo::class.java),
//
//    //主播开启幸运星球标识位
//    ReleaseLuckcyPlanet(LuckyStartBean::class.java),
//
//    //幸运星球消失消息
//    LuckyPlanetDisappear(LuckyPlanetDisappearBean::class.java),
//
//    //霸主信息变更消息
//    PlanetScoreChange(PlanetScoreChangeBean::class.java),

    //直播间banner变化消息(开启幸运星球)
    RoomBannerChange(RoomBannerChangeBean::class.java),

    /**
     * 嘉宾信息变更
     * @author WanZhiYuan
     * @since 4.31
     * @date 2020/05/08
     */
    GuestPrivileges(VoidResult::class.java),

    //弹幕卡变更消息
    GetDanmuCard(PlayerDataChanged::class.java),

    /**
     * 主题房间相关
     */
    //开场通知
    ThemeSessionStart(PlayInfoBean::class.java),

    // 主题房直播间场次结束通知
    ThemeSessionFinish(ThemeSessionFinishBean::class.java),

    //主题房节目单变化消息
    ThemeProgramShowChangeNotice(ProgramRoomBean::class.java),


    //接下来是欢鹊使用
    //别人向你发起语音通话
    NetCallReceive(NetCallReceiveBean::class.java),

    //语音成功消息
    NetCallAccept(NetCallAcceptBean::class.java),

    //主叫取消会话消息
    NetCallCancel(NetCallReceiveBean::class.java),

    //挂断消息
    NetCallHangUp(NetCallHangUpBean::class.java),

    //被叫拒绝通话
    NetCallRefuse(NetCallReceiveBean::class.java),

    //通话断开消息(服务端断开)
    NetCallDisconnect(VoidResult::class.java),

    //对方忙消息
    NetCallBusy(NetCallReceiveBean::class.java),

    //余额不足提醒消息
    NetCallBalanceRemind(NetCallBalanceRemindBean::class.java),

    //亲密度变化消息
    IntimateChange(IntimateBean::class.java),

    //踢人消息
    KickUser(OperatorMessageBean::class.java),

    //封禁账户消息
    BanUser(OperatorMessageBean::class.java),

    //直播封禁（用户不允许进入任何直播间）
    BanUserLiving(OperatorMessageBean::class.java),

    //直播间热度变动消息
    RoomHeatChange(RoomHeatChangeBean::class.java),

    //匹配超时消息
    AnonyVoiceTimeout(UserIdListBean::class.java),

    //匹配成功消息
    AnonyVoiceConnect(AnonyVoiceSuccess::class.java),

    //匿名语音挂断消息
    AnonyVoiceHangUp(AnonyVoiceHangUpBean::class.java),

    //公开信息消息
    AnonyVoiceOpen(UserInfoInRoom::class.java),

    //匿名语音 邀请消息
    AnonyVoiceInvite(AnonyVoiceInviteBean::class.java)

    //禁言消息
//    MuteUser(OperatorMessageBean::class.java),
//    //设备封禁消息
//    BanUserDevice(OperatorMessageBean::class.java),
//    //设为房管消息
//    UpToRoomManager(OperatorMessageBean::class.java),
//    //取消房管消息
//    CancelRoomManager(OperatorMessageBean::class.java),
}
