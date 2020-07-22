package com.julun.huanque.message.adapter

import android.net.Uri
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
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
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.bean.message.VoiceConmmunicationSimulate
import com.julun.huanque.common.constant.MessageCustomBeanType
import com.julun.huanque.common.constant.VoiceResultType
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.message.R
import io.rong.imlib.model.Message
import io.rong.message.ImageMessage
import io.rong.message.TextMessage
import org.jetbrains.anko.bottomPadding
import java.io.File
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
        addChildClickViewIds(R.id.sdv_image)
        addChildClickViewIds(R.id.tv_content)
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

            ImageUtils.setDefaultHeaderPic(sdv_header, otherUserInfo?.headPic ?: "")

            ImageUtils.loadImage(sdv_header, otherUserInfo?.headPic ?: "", 40f, 40f)
        } else {
            if (helper.itemViewType == MINE) {

//                helper.addOnClickListener(R.id.sdv_header)
            }
            //todo 发送状态先关闭(发送中和重试状态)
//            val ivSendFail = helper.getView<ImageView>(R.id.iv_send_fail)
//            val sendProgress = helper.getView<ProgressBar>(R.id.send_progress)
//            when (item.sentStatus) {
//                Message.SentStatus.FAILED -> {
//                    //发送失败
//                    ivSendFail.show()
//                    sendProgress.hide()
//                }
//                Message.SentStatus.SENDING -> {
//                    //发送中
//                    ivSendFail.hide()
//                    sendProgress.show()
//                }
//                Message.SentStatus.SENT -> {
//                    //已发送
//                    ivSendFail.hide()
//                    sendProgress.hide()
//                }
//            }
            //显示本人头像
            val sdv_header = helper.getView<SimpleDraweeView>(R.id.sdv_header)
            ImageUtils.setDefaultHeaderPic(sdv_header, SessionUtils.getSex())
            ImageUtils.loadImage(sdv_header, SessionUtils.getHeaderPic(), 40f, 40f)
        }

        if (content is CustomMessage) {
            //自定义消息
            when (content.type) {
                MessageCustomBeanType.Gift -> {
                    showMessageView(helper, GIFT_VIEW, helper.itemViewType)
                    showGiftView(helper, content.context)
                }
                MessageCustomBeanType.Expression_Privilege -> {
                    //特权表情
                    showMessageView(helper, PIC_MESSAGE, helper.itemViewType)
//                    showGiftView(helper, content.context)
                    val imageResource = EmojiSpanBuilder.getPrivilegeResource(context, content.context)
                    ImageUtils.loadImageLocal(helper.getView(R.id.sdv_image), imageResource)
                    showTextImageQueBi(helper.getView<TextView>(R.id.tv_quebi), item, helper.adapterPosition)
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
                    showVoiceView(tvContent, helper.getView<TextView>(R.id.tv_quebi), content.context)
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

                tvContent.text = EmojiSpanBuilder.buildEmotionSpannable(context, content.content, true)
//                    EmojiUtil.message2emoji(content.content)
                //判断是否显示文本鹊币
                if (helper.itemViewType == OTHER) {
                    showTextImageQueBi(helper.getView<TextView>(R.id.tv_quebi), item, helper.adapterPosition)
                }
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
                    showTextImageQueBi(helper.getView<TextView>(R.id.tv_quebi), item, helper.adapterPosition)
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
            if (Math.abs(item.sentTime - previousData.sentTime) < 5 * 60 * 1000) {
                //两条消息间隔在5分钟以内
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

        when (messageType) {
            TEXT_MESSAGE -> {
                //显示普通文案视图
                sdv_header.show()
                rl_content?.show()
                tv_content.show()

                tv_pic_content.hide()
                sdv_image.hide()
            }
            PIC_MESSAGE_SYSTEM -> {
                //显示图片视图
                sdv_header.show()

                rl_content?.hide()
                tv_content.hide()

                sdv_image.hide()
                tv_pic_content.hide()
            }
            ACTION_VIEW -> {
                rl_content?.hide()
                tv_content.hide()
                sdv_header.hide()
//                fl_living?.hide()
            }
            PIC_MESSAGE -> {
                sdv_header.show()
                rl_content?.show()
                sdv_image.show()

                tv_pic_content.hide()
                tv_content.hide()
            }
            GIFT_VIEW -> {
                sdv_header.show()
                rl_content?.show()
                sdv_image.show()
                tv_pic_content.show()

                tv_content.hide()
            }
            else -> {

            }
        }
    }

    /**
     * 显示礼物视图的数据
     */
    private fun showGiftView(helper: BaseViewHolder, str: String) {
        try {
            val chatGift = JsonUtil.deserializeAsObject<ChatGift>(str, ChatGift::class.java)
            ImageUtils.loadImage(helper.getView(R.id.sdv_image), chatGift.selPic, 100f, 100f)
            val draweeView = helper.getView<SimpleDraweeSpanTextView>(R.id.tv_pic_content)
            val bi = chatGift.beans * max(chatGift.giftCount, 1)
            val str = "送你一${chatGift.giftName}+#$bi"

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
    private fun showVoiceView(tv: TextView, qbTv: TextView, str: String) {

        try {
            val voiceBean = JsonUtil.deserializeAsObject<VoiceConmmunicationSimulate>(str, VoiceConmmunicationSimulate::class.java)
            var showStr = ""
            when (voiceBean.type) {
                VoiceResultType.CONMMUNICATION_FINISH -> {
                    //已接通，正常结束
                    showStr = "聊天时长 ${TimeUtils.countDownTimeFormat1(voiceBean.duration)}"
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
                    showStr = "已取消"
                }
                VoiceResultType.MINE_REFUSE -> {
                    showStr = "已拒绝"
                }
                else -> {
                }
            }
            tv.text = showStr

            if (voiceBean.totalBeans > 0 && voiceBean.billUserId != SessionUtils.getUserId()) {
                //本人是收费方
                qbTv.text = "${voiceBean.totalBeans}"
                qbTv.show()
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
    private fun showTextImageQueBi(tv: TextView, item: Message, position: Int) {
        if (item.senderUserId == SessionUtils.getSessionId()) {
            tv.hide()
            return
        }
        val content = item.content
        if (content !is TextMessage && content !is ImageMessage) {
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

        } catch (e: Exception) {
            e.printStackTrace()
        }
        val fee = user?.targetUserObj?.fee ?: 0
        if (fee > 0 && item.senderUserId != SessionUtils.getSessionId()) {
            //对方发送消息
            //消息时间
            val sendTime = item.sentTime
            //离当前消息最近的一次本人发送消息
            var mineReplyTime = 0L
            //获取离当前消息最近的本人发送消息
            for (index in position until data.size) {
                val msg = data[index]
                val conent = msg.content
                if ((conent is TextMessage || conent is ImageMessage) && msg.senderUserId == "${SessionUtils.getUserId()}") {
                    //获取到需要的消息
                    mineReplyTime = msg.sentTime
                    break
                }
            }

            if (mineReplyTime > 0 && mineReplyTime - sendTime < DAY) {
                //24小时内回复
                tv.show()
                tv.text = "$fee"
            } else {
                //24小时外回复
                tv.hide()
            }
        } else {
            tv.hide()
        }
    }
}