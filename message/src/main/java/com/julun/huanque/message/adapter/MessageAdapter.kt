package com.julun.huanque.message.adapter

import android.net.Uri
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.module.UpFetchModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.span.DraweeSpan
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.facebook.drawee.span.SimpleDraweeSpanTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.beans.ChatGift
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.SendRoomInfo
import com.julun.huanque.common.bean.message.ExpressionAnimationBean
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.bean.message.VoiceConmmunicationSimulate
import com.julun.huanque.common.constant.FingerGuessingResult
import com.julun.huanque.common.constant.MessageCustomBeanType
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.VoiceResultType
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.interfaces.WebpAnimatorListener
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.message.R
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.rong.imlib.model.Message
import io.rong.message.ImageMessage
import io.rong.message.TextMessage
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.textColor
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.max


/**
 *@创建者   dong
 *@创建时间 2019/5/21 9:25
 *@描述
 */
class MessageAdapter : BaseDelegateMultiAdapter<Message, BaseViewHolder>(), UpFetchModule {

    //私聊的情况下，保存对方的用户信息
    var otherUserInfo: ChatUser? = null

    init {
        addChildClickViewIds(R.id.sdv_image,R.id.tv_content,R.id.con_send_room)
        addChildLongClickViewIds(R.id.tv_content)
    }

    companion object {
        //其它人的消息
        const val OTHER = 0x001

        //本人消息
        const val MINE = 0x002

        //提示消息
        const val SYSTEM = 0x003

        //自定义消息
        const val CUSTOM = 0x004

        //显示文案消息
        const val TEXT_MESSAGE = "TEXT_MESSAGE"

        //显示图片消息(系统消息)
        const val PIC_MESSAGE_SYSTEM = "PIC_MESSAGE_SYSTEM"

        //图片消息
        const val PIC_MESSAGE = "PIC_MESSAGE"

        //单独显示底部操作视图
        const val ACTION_VIEW = "ACTION_VIEW"

        //礼物视图
        const val GIFT_VIEW = "GIFT_VIEW"

        //传送门视图
        const val SEND_ROOM_VIEW = "SEND_ROOM_VIEW"
    }

    init {
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<Message>() {

            override fun getItemType(data: List<Message>, position: Int): Int {
                val t = data.getOrNull(position)
                return if (t?.senderUserId == "${SessionUtils.getUserId()}") {
                    MINE
                } else {
                    OTHER
                }
            }
        })
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()?.addItemType(OTHER, R.layout.recycler_item_other)
            ?.addItemType(MINE, R.layout.recycler_item_mine)
            ?.addItemType(SYSTEM, R.layout.recycler_item_message_system)

        addChildClickViewIds(R.id.sdv_header, R.id.iv_send_fail)
    }

    override fun convert(helper: BaseViewHolder, item: Message) {
        if (helper == null || item == null) {
            return
        }
        //EmojiSpanBuilder.buildEmotionSpannable(binding.getRoot().getContext(), chatInfo.message)
        val content = item.content
        //其它的普通聊天消息
        if (helper.itemViewType == OTHER) {
            //头像和直播状态
            val sdv_header = helper.getView<SimpleDraweeView>(R.id.sdv_header)

            ImageHelper.setDefaultHeaderPic(sdv_header, otherUserInfo?.headPic ?: "")

            ImageUtils.loadImage(sdv_header, otherUserInfo?.headPic ?: "", 40f, 40f)
        } else {
            if (helper.itemViewType == MINE) {

//                helper.addOnClickListener(R.id.sdv_header)
            }
            val ivSendFail = helper.getView<ImageView>(R.id.iv_send_fail)
            val sendProgress = helper.getView<ProgressBar>(R.id.send_progress)
            when (item.sentStatus) {
                Message.SentStatus.FAILED -> {
                    //发送失败
                    ivSendFail.show()
                    sendProgress.hide()
                }
                Message.SentStatus.SENDING -> {
                    //发送中
                    ivSendFail.hide()
                    sendProgress.show()
                }
                Message.SentStatus.SENT -> {
                    //已发送
                    ivSendFail.hide()
                    sendProgress.hide()
                }
            }
            //显示本人头像
            val sdv_header = helper.getView<SimpleDraweeView>(R.id.sdv_header)
            ImageHelper.setDefaultHeaderPic(sdv_header, SessionUtils.getSex())
            ImageUtils.loadImage(sdv_header, SessionUtils.getHeaderPic(), 40f, 40f)
        }

        if (content is CustomMessage) {
            //自定义消息
            when (content.type) {
                MessageCustomBeanType.Gift -> {
                    showMessageView(helper, GIFT_VIEW, helper.itemViewType)
                    showGiftView(helper, content.context, helper.itemViewType == MINE)
                }
                MessageCustomBeanType.Expression_Privilege -> {
                    //特权表情
                    showMessageView(helper, PIC_MESSAGE, helper.itemViewType)
//                    showGiftView(helper, content.context)
                    val imageResource = EmojiSpanBuilder.getPrivilegeResource(context, content.context)
                    ImageUtils.loadImageLocal(helper.getView(R.id.sdv_image), imageResource)
                    showTextImageQueBi(helper.getView<SimpleDraweeSpanTextView>(R.id.tv_quebi), item, helper.adapterPosition)
                }
                MessageCustomBeanType.Expression_Animation -> {
                    //动画表情
                    var map: Map<String, Any>? = null
                    var expressionAnimationBean: ExpressionAnimationBean? = null
                    try {
                        if (item.extra?.isNotEmpty() == true) {
                            map = JsonUtil.toJsonMap(item.extra)
                        }
                        expressionAnimationBean =
                            JsonUtil.deserializeAsObject<ExpressionAnimationBean>(content.context, ExpressionAnimationBean::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    showMessageView(helper, PIC_MESSAGE, helper.itemViewType)
                    if (expressionAnimationBean != null) {
                        val started = map?.get(ParamConstant.MSG_ANIMATION_STARTED) as? Boolean
                        val position = helper.layoutPosition
                        when (EmojiSpanBuilder.getPrivilegeResource(context, expressionAnimationBean.name)) {
                            R.drawable.icon_shaizi -> {
                                //骰子动效
                                showShaiziAnimation(item, helper.getView(R.id.sdv_image), expressionAnimationBean.result, started ?: false, position)

                            }
                            R.drawable.icon_caiquan -> {
                                //猜拳动效
                                showGuessAnimation(item, helper.getView(R.id.sdv_image), expressionAnimationBean.result, started ?: false, position)
                            }
                            else -> {
                            }
                        }
                    }
                    showTextImageQueBi(helper.getView<SimpleDraweeSpanTextView>(R.id.tv_quebi), item, helper.adapterPosition)
                }
                MessageCustomBeanType.SendRoom -> {
                    //传送门消息
                    showMessageView(helper, SEND_ROOM_VIEW, helper.itemViewType)
                    showSendRoomView(helper, content.context)
                }
                else -> {
                }
            }

        } else if (content is CustomSimulateMessage) {
            when (content.type) {
                MessageCustomBeanType.Voice_Conmmunication_Simulate -> {
                    //语音消息
                    showMessageView(helper, TEXT_MESSAGE, helper.itemViewType)
                    //
                    val tvContent = helper.getView<TextView>(R.id.tv_content)
                    if (helper.itemViewType == OTHER) {
                        tvContent.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_phone_voice, 0, 0, 0)
                    } else if (helper.itemViewType == MINE) {
                        tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icon_phone_voice, 0)
                    }
                    tvContent.compoundDrawablePadding = 15
                    showVoiceView(tvContent, helper.getView<SimpleDraweeSpanTextView>(R.id.tv_quebi), content.context)
                }
                else -> {
                }
            }
        } else {
            //通用内容处理
            val tvContent = helper.getView<TextView>(R.id.tv_content)

            tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            tvContent.compoundDrawablePadding = 0

            tvContent.maxWidth = ScreenUtils.getScreenWidth() - DensityHelper.dp2px(65f) * 2
            if (content is TextMessage) {
                showMessageView(helper, TEXT_MESSAGE, helper.itemViewType)
                //是否都是表情
                val allEmoji = EmojiSpanBuilder.allEmoji(context, content.content)

                if (allEmoji) {
                    tvContent.background = null
                }
                tvContent.text = EmojiSpanBuilder.buildEmotionSpannable(context, content.content, true)
//                    EmojiUtil.message2emoji(content.content)
                //判断是否显示文本鹊币
//                if (helper.itemViewType == OTHER) {
                showTextImageQueBi(helper.getView<SimpleDraweeSpanTextView>(R.id.tv_quebi), item, helper.adapterPosition)
//                }
            } else if (content is ImageMessage) {
                //图片信息
                showMessageView(helper, PIC_MESSAGE, helper.itemViewType)
                if (helper.itemViewType == OTHER) {
                    //接收方查看图片
//                content.localUri
                    val file = File("${content.thumUri}")
                    if (file.exists()) {
                        //显示缩略图
                        ImageUtils.loadNativeFilePath(helper.getView(R.id.sdv_image), "${content.thumUri}", 100f, 100f)
                    } else {
                        //显示远程图片
                        ImageUtils.loadImage(helper.getView(R.id.sdv_image), "${content.remoteUri}", 100f, 100f)
                    }
                    //
                    showTextImageQueBi(helper.getView<SimpleDraweeSpanTextView>(R.id.tv_quebi), item, helper.adapterPosition)
                } else {
                    //发送方查看图片
                    val file = File("${content.thumUri?.path}")
                    if (file.exists()) {
                        //图片存在，显示本地图片
                        ImageUtils.loadNativeFilePath(helper.getView(R.id.sdv_image), "${content.thumUri?.path}", 100f, 100f)
                    } else {
                        //图片不存在，显示远程图片
                        ImageUtils.loadImage(helper.getView(R.id.sdv_image), "${content.remoteUri}", 100f, 100f)
                    }
//                logger("DXC  exists = ${file.exists()} , remoteUri = ${content.remoteUri},content.thumUri.path = ${content.thumUri?.path},thumUri = ${content.thumUri}")
                }

            }
        }
        //时间相关处理
        val currentPosition = helper.adapterPosition
        val previousPosition = currentPosition - 1
        if (ForceUtils.isIndexNotOutOfBounds(previousPosition, data)) {
            val previousData = data[previousPosition]
            if (Math.abs(item.sentTime - previousData.sentTime) < 2 * 60 * 1000) {
                //两条消息间隔在2分钟以内
                helper.setGone(R.id.view_top, false)
                helper.setGone(R.id.group, true)
            } else {
                helper.setGone(R.id.view_top, true)
                helper.setGone(R.id.group, false)
                helper.setText(R.id.tv_time, TimeUtils.formatMessageTime(item.sentTime))
            }
        } else {
            helper.setGone(R.id.view_top, true)
            helper.setGone(R.id.group, false)
            helper.setText(R.id.tv_time, TimeUtils.formatMessageTime(item.sentTime))
        }
        val container = helper.getView<ConstraintLayout>(R.id.view_container)
        //如果是最后一条
        if (currentPosition == data.size - 1) {
            container.bottomPadding = DensityHelper.dp2px(10f)
        } else {
            container.bottomPadding = 0
        }

    }

    /**
     * 显示消息视图
     * @param picMessage 是否是图片消息
     */
    private fun showMessageView(holder: BaseViewHolder, messageType: String, itemType: Int) {
        //普通内容区域
        var rl_content: View? = null
        if (itemType == OTHER) {
            rl_content = holder.getView<View>(R.id.rl_content)
        }
        val tv_content = holder.getView<TextView>(R.id.tv_content)

        //头像视图
        val sdv_header = holder.getView<View>(R.id.sdv_header)
        val sdv_image = holder.getView<View>(R.id.sdv_image)
        val tv_pic_content = holder.getView<View>(R.id.tv_pic_content)
        //隐藏收益视图
        holder.getView<View>(R.id.tv_quebi).hide()

        val con_send_room = holder.getView<View>(R.id.con_send_room)


        when (messageType) {
            TEXT_MESSAGE -> {
                //显示普通文案视图
                sdv_header.show()
                rl_content?.show()
                tv_content.show()
                if (itemType == OTHER) {
                    tv_content.backgroundResource = R.drawable.bg_chat_other
                } else {
                    tv_content.backgroundResource = R.drawable.bg_chat_mine
                }

                tv_pic_content.hide()
                sdv_image.hide()
                con_send_room.hide()
            }
            PIC_MESSAGE_SYSTEM -> {
                //显示图片视图
                sdv_header.show()

                rl_content?.hide()
                tv_content.hide()

                sdv_image.hide()
                tv_pic_content.hide()
                con_send_room.hide()
            }
            ACTION_VIEW -> {
                rl_content?.hide()
                tv_content.hide()
                sdv_header.hide()
                con_send_room.hide()
//                fl_living?.hide()
            }
            PIC_MESSAGE -> {
                sdv_header.show()
                rl_content?.show()
                sdv_image.show()

                tv_pic_content.hide()
                tv_content.hide()
                con_send_room.hide()
            }
            GIFT_VIEW -> {
                sdv_header.show()
                rl_content?.show()
                sdv_image.show()
                tv_pic_content.show()

                tv_content.hide()
                con_send_room.hide()
            }

            SEND_ROOM_VIEW -> {
                //传送门视图
                sdv_header.show()
                con_send_room.show()

                rl_content?.hide()
                tv_content.hide()
                tv_pic_content.hide()
                sdv_image.hide()
            }
            else -> {

            }
        }
    }

    /**
     * 显示礼物视图的数据
     * @param mine 是否是送礼本人
     */
    private fun showGiftView(helper: BaseViewHolder, str: String, mine: Boolean) {
        try {
            val chatGift = JsonUtil.deserializeAsObject<ChatGift>(str, ChatGift::class.java)
            ImageUtils.loadImage(helper.getView(R.id.sdv_image), chatGift.selPic, 100f, 100f)
            val draweeView = helper.getView<SimpleDraweeSpanTextView>(R.id.tv_pic_content)
            draweeView.textSize = 14f
            draweeView.textColor = GlobalUtils.getColor(R.color.black_666)

            val bi = chatGift.beans * max(chatGift.giftCount, 1)
            val str = if (mine) {
                "送TA一${chatGift.giftName}#$bi"
            } else {
                "送你一${chatGift.giftName}#$bi"
            }


            val draweeSpanStringBuilder = DraweeSpanStringBuilder(str)
            val draweeHierarchy = GenericDraweeHierarchyBuilder.newInstance(context.resources)
                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                .build()
            val uri = Uri.parse("res://${context!!.packageName}/" + R.mipmap.icon_quebi_message)

            val controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build()

            val index = str.indexOf("#")

            draweeSpanStringBuilder.setImageSpan(
                context, /* Context */
                draweeHierarchy, /* hierarchy to be used */
                controller, /* controller to be used to update the hierarchy */
                index, /* image index within the text */
                30, /* image width */
                30, /* image height */
                false, /* auto resize */
                DraweeSpan.ALIGN_CENTER
            ) /* alignment */

            draweeSpanStringBuilder.setSpan(
                ForegroundColorSpan(GlobalUtils.getColor(R.color.color_quebi)), index, str.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            draweeView.setDraweeSpanStringBuilder(draweeSpanStringBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 显示模拟的语音消息
     */
    private fun showVoiceView(tv: TextView, qbTv: SimpleDraweeSpanTextView, str: String) {

        try {
            val voiceBean = JsonUtil.deserializeAsObject<VoiceConmmunicationSimulate>(str, VoiceConmmunicationSimulate::class.java)
            var showStr = ""
            when (voiceBean.type) {
                VoiceResultType.CONMMUNICATION_FINISH -> {
                    //已接通，正常结束
                    showStr = "通话时长 ${TimeUtils.countDownTimeFormat1(voiceBean.duration)}"
                }
                VoiceResultType.RECEIVE_REFUSE -> {
                    //对方已拒绝
                    showStr = "对方已拒绝"
                }
                VoiceResultType.RECEIVE_NOT_ACCEPT -> {
                    //对方未应答
                    showStr = "对方未应答"
                }
                VoiceResultType.RECEIVE_BUSY -> {
                    //对方忙
                    showStr = "对方忙"
                }
                VoiceResultType.CANCEL -> {
                    //主叫取消通话
                    if (voiceBean.createUserID == SessionUtils.getUserId()) {
                        //本人挂断
                        showStr = "已取消"
                    } else {
                        showStr = "对方已取消"
                    }
                }
                VoiceResultType.MINE_REFUSE -> {
                    showStr = "已拒绝"
                }
                else -> {
                }
            }
            tv.text = showStr

            if (voiceBean.totalBeans > 0) {
                //本人是收费方
                qbTv.text = "${voiceBean.totalBeans}"
                qbTv.show()
                showFee(qbTv, voiceBean.totalBeans, false)
            } else {
                qbTv.hide()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    /**
     * 显示文本/图片消息 鹊币
     */
    private fun showTextImageQueBi(tv: SimpleDraweeSpanTextView, item: Message, position: Int) {
//        if (item.senderUserId == SessionUtils.getSessionId()) {
//            tv.hide()
//            return
//        }
        val content = item.content
        if (content !is TextMessage && content !is ImageMessage && content !is CustomMessage) {
            tv.hide()
            return
        }
        var user: RoomUserChatExtra? = null
        try {
            if (content is TextMessage) {
                user = JsonUtil.deserializeAsObject(content.extra, RoomUserChatExtra::class.java)
            }

            if (content is ImageMessage) {
                user = JsonUtil.deserializeAsObject(content.extra, RoomUserChatExtra::class.java)
            }

            if (content is CustomMessage) {
                user = JsonUtil.deserializeAsObject(content.extra, RoomUserChatExtra::class.java)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        val fee = user?.targetUserObj?.fee ?: 0

        if (fee <= 0) {
            //免费消息，不显示
            tv.hide()
            return
        }
        tv.textSize = 10f
        tv.textColor = GlobalUtils.getColor(R.color.black_999)

        //消息回复间隔
        val replayTime = msgReplayTime(item, position)

        if (item.senderUserId == "${SessionUtils.getUserId()}") {
            //本人消息，费用退回，显示
            if (replayTime > DAY || (replayTime == 0L && (System.currentTimeMillis() - item.sentTime) > DAY)) {
                //回复时间超过一天,或者超过24小时内没有回复(消息费用退回)
                showFee(tv, fee, true)
                tv.show()
            } else {
                tv.hide()
            }
        } else {
            //他人消息,费用未退回，显示付费样式,费用退回，显示退费样式
            tv.show()
            if (replayTime > DAY || (replayTime == 0L && (System.currentTimeMillis() - item.sentTime) > DAY)) {
                //超时未回复(退回)
                showFee(tv, fee, true)
            } else {
                showFee(tv, fee, false)
            }

        }
    }

    /**
     *
     * 显示传送门样式
     */
    private fun showSendRoomView(helper: BaseViewHolder, str: String) {

        try {
            if (str.isEmpty()) {
                return
            }
            val sendRoomBean = JsonUtil.deserializeAsObject<SendRoomInfo>(str, SendRoomInfo::class.java)
            helper.setText(R.id.tv_title, sendRoomBean.title)
                .setText(R.id.tv_program_name, sendRoomBean.programName)
                .setText(R.id.tv_hot, "${sendRoomBean.heatValue}")
            ImageUtils.loadImage(helper.getView(R.id.sdc_cover), sendRoomBean.coverPic, 150f, 116f)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 显示收费样式
     * @param fee 费用
     * @param giveback 退回状态
     */
    private fun showFee(draweeView: SimpleDraweeSpanTextView, fee: Long, giveback: Boolean) {
        val str = if (giveback) {
            //费用退回
            "超过24小时未回复，# ${fee}已退回"
        } else {
            //费用未退回
            "# $fee"
        }

        val draweeSpanStringBuilder = DraweeSpanStringBuilder(str)
        val draweeHierarchy = GenericDraweeHierarchyBuilder.newInstance(context.resources)
            .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
            .build()
        val uri = Uri.parse("res://${context!!.packageName}/" + R.mipmap.icon_quebi_message)

        val controller = Fresco.newDraweeControllerBuilder()
            .setUri(uri)
            .setAutoPlayAnimations(true)
            .build()

        val index = str.indexOf("#")

        draweeSpanStringBuilder.setImageSpan(
            context, /* Context */
            draweeHierarchy, /* hierarchy to be used */
            controller, /* controller to be used to update the hierarchy */
            index, /* image index within the text */
            30, /* image width */
            30, /* image height */
            false, /* auto resize */
            DraweeSpan.ALIGN_CENTER
        ) /* alignment */

        draweeSpanStringBuilder.setSpan(
            ForegroundColorSpan(GlobalUtils.getColor(R.color.color_quebi)), index + 1, index + 2 + "$fee".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        draweeView.setDraweeSpanStringBuilder(draweeSpanStringBuilder)
    }

    /**
     * 消息回复间隔时间
     * @param targetMsg 目标消息
     * @param position 目标消息的position
     */
    private fun msgReplayTime(targetMsg: Message, position: Int): Long {
        //消息时间
        val sendTime = targetMsg.sentTime
        //离当前消息最近的一次对方发送消息时间
        var otherReplyTime = 0L
        //获取离当前消息最近的对方发送消息
        for (index in position until data.size) {
            val msg = data[index]
            val conent = msg.content
            if ((conent is TextMessage || conent is ImageMessage) && targetMsg.senderUserId != msg.senderUserId) {
                //获取到需要的消息
                otherReplyTime = msg.sentTime
                break
            }
        }
        return if (otherReplyTime == 0L) {
            0
        } else {
            otherReplyTime - sendTime
        }
    }

    /**
     * 显示骰子动画
     * @param msg 自定义消息
     * @param result 动画结果
     * @param started 是否播放过动画
     */
    private fun showShaiziAnimation(msg: Message, view: SimpleDraweeView, result: String, started: Boolean, position: Int) {
        val resultPic = when (result) {
            "2" -> {
                R.drawable.shaizi_2
            }
            "3" -> {
                R.drawable.shaizi_3
            }
            "4" -> {
                R.drawable.shaizi_4
            }
            "5" -> {
                R.drawable.shaizi_5
            }
            "6" -> {
                R.drawable.shaizi_6
            }
            else -> {
                //默认使用 1点
                R.drawable.shaizi_1
            }
        }
        if (!started) {
            //动画没有播放过，播放动画
            ImageUtils.showAnimator(
                view,
                "asset://${CommonInit.getInstance().getApp().assets}/webp/shaizi.webp",
                16, object : WebpAnimatorListener {
                    override fun onStart() {
                    }

                    override fun onError() {
                        setAnimationStarted(msg)
                        ImageUtils.loadImageLocal(view, resultPic)
//                        updateSingleMessage(msg, position)
                    }

                    override fun onEnd() {
                        setAnimationStarted(msg)
                        ImageUtils.loadImageLocal(view, resultPic)
//                        updateSingleMessage(msg, position)
                    }
                }
            )
        } else {
            //已经播放过
            ImageUtils.loadImageLocal(view, resultPic)
        }
    }

    /**
     * 数据发生变化
     */
    private fun updateSingleMessage(msg: Message, position: Int) {
        logger("Message 数据发生变化 position = $position")
//        notifyItemChanged(position)
        data.forEachIndexed { index, message ->
            if (message === msg) {
                //找到对应的Message
                notifyItemChanged(index)
                return
            }
        }
    }

    /**
     * 显示猜拳动画
     */
    private fun showGuessAnimation(msg: Message, view: SimpleDraweeView, result: String, started: Boolean, position: Int) {
        val resultList = arrayListOf<Int>(R.drawable.pic_guessing_scissors, R.drawable.pic_guessing_rock, R.drawable.pic_guessing_paper)
        val resultPic = when (result) {
            FingerGuessingResult.ROCK -> {
                //石头
                R.drawable.pic_guessing_rock
            }
            FingerGuessingResult.PAPER -> {
                //布
                R.drawable.pic_guessing_paper
            }
            else -> {
                //剪刀
                R.drawable.pic_guessing_scissors
            }
        }

        if (!started) {
            //动画没有播放过，播放动画
            Observable.interval(0, 200L, TimeUnit.MILLISECONDS)
                .take(10)
                .bindToLifecycle(view)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val pic = resultList[it.toInt() % resultList.size]
                    ImageUtils.loadImageLocal(view, pic)

                }, {
                    ImageUtils.loadImageLocal(view, resultPic)
                    setAnimationStarted(msg)
                    logger("Message 猜拳 结果1")
//                    updateSingleMessage(msg, position)
                }, {
                    ImageUtils.loadImageLocal(view, resultPic)
                    setAnimationStarted(msg)
                    logger("Message 猜拳 结果2")
//                    updateSingleMessage(msg, position)
                })
        } else {
            //直接显示结果
            ImageUtils.loadImageLocal(view, resultPic)
            logger("Message 猜拳 结果3")
        }
    }


    /**
     * 设置动画已经播放过
     */
    private fun setAnimationStarted(msg: Message) {
        try {
            val extra = JsonUtil.serializeAsString(GlobalUtils.addExtra(msg.extra ?: "", ParamConstant.MSG_ANIMATION_STARTED, true))
            msg.extra = extra

            if (msg.messageId > 0) {
                //数据库修改
                RongCloudManager.updateMessageExtra(msg.messageId, extra)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 更新发送状态
     */
    fun updateSentStatus(msg: Message) {
        var position = -1
        data.forEachIndexed { index, message ->
            if (msg === message) {
                position = index
                return@forEachIndexed
            }
        }
        getViewByPosition(position, R.id.iv_send_fail)
        val ivSendFail = getViewByPosition(position, R.id.iv_send_fail) ?: return
        val sendProgress = getViewByPosition(position, R.id.send_progress) ?: return
        when (msg.sentStatus) {
            Message.SentStatus.FAILED -> {
                //发送失败
                ivSendFail.show()
                sendProgress.hide()
            }
            Message.SentStatus.SENDING -> {
                //发送中
                ivSendFail.hide()
                sendProgress.show()
            }
            Message.SentStatus.SENT -> {
                //已发送
                ivSendFail.hide()
                sendProgress.hide()
            }
        }
    }

}