package com.julun.huanque.message.adapter

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.module.UpFetchModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.ChatUserBean
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.widgets.live.chatInput.EmojiUtil
import com.julun.huanque.message.R
import io.rong.imlib.model.Message
import io.rong.message.TextMessage
import org.jetbrains.anko.bottomPadding

/**
 *@创建者   dong
 *@创建时间 2019/5/21 9:25
 *@描述
 */
class MessageAdapter : BaseDelegateMultiAdapter<Message, BaseViewHolder>(), UpFetchModule {

    //私聊的情况下，保存对方的用户信息
    var otherUserInfo: ChatUserBean? = null

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

        //显示图片消息
        const val PIC_MESSAGE = "PIC_MESSAGE"

        //单独显示底部操作视图
        const val ACTION_VIEW = "ACTION_VIEW"
    }

    init {
//        multiTypeDelegate = object : MultiTypeDelegate<Message>() {
//            override fun getItemType(t: Message?): Int {
//                return if (t?.senderUserId == "${SessionUtils.getUserId()}") {
//                    MINE
//                } else {
//                    OTHER
//                }
//            }
//        }
//        multiTypeDelegate.registerItemType(OTHER, R.layout.recycler_item_other)
//            .registerItemType(MINE, R.layout.recycler_item_mine)
//            .registerItemType(SYSTEM, R.layout.recycler_item_message_system)
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

        addChildClickViewIds(R.id.sdv_header,R.id.iv_send_fail)
    }

    override fun convert(helper: BaseViewHolder, item: Message) {
        if (helper == null || item == null) {
            return
        }

        val content = item.content
        //其它的普通聊天消息
        if (helper.itemViewType == OTHER) {
            //头像和直播状态
            ImageUtils.loadImage(helper.getView(R.id.sdv_header), otherUserInfo?.headPic ?: "", 40f, 40f)
            //点击事件
//            helper.addOnClickListener(R.id.sdv_header)
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
            ImageUtils.loadImage(helper.getView(R.id.sdv_header), SessionUtils.getHeaderPic(), 40f, 40f)
        }

        //通用内容处理
        val tvContent = helper.getView<TextView>(R.id.tv_content)
        tvContent.maxWidth = ScreenUtils.getScreenWidth() - DensityHelper.dp2px(65f) * 2
        if (content is TextMessage) {
            showMessageView(helper, TEXT_MESSAGE)
            tvContent.text = EmojiUtil.message2emoji(content.content)
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

        val conAction = helper.getView<View>(R.id.con_action)
        //自定义消息处理
//        val draweeSpanTextView = helper.getView<DraweeSpanTextView>(R.id.draweeSpanTextView)
//        if (content is CustomMessage) {
//            helper.addOnClickListener(R.id.draweeSpanTextView).addOnClickListener(R.id.view_pic)
//            try {
//                //自定义消息
//                val customBean = JsonUtil.deserializeAsObject<MessageActionBean>(content.context,
//                                                                                 MessageActionBean::class.java)
//
//                if (customBean.msyType == "2") {
//                    //营销消息
//                    if (customBean.pic.isNotEmpty()) {
//                        //有图片样式
//                        showMessageView(helper, PIC_MESSAGE)
//                        conAction.hide()
//
//                        val sdv_pic = helper.getView<SimpleDraweeView>(R.id.sdv_pic)
//                        val tv_pic = helper.getView<TextView>(R.id.tv_pic)
//
//                        ImageUtils.loadImage(sdv_pic, customBean.pic, 160f, 63f)
//                        tv_pic.text = customBean.content
//                    } else {
//                        //无图片样式
//                        showMessageView(helper, TEXT_MESSAGE)
//                        if (helper.itemViewType == MINE) {
//                            conAction.hide()
//                        } else {
//                            conAction.show()
//                        }
//                        tvContent.text = EmojiUtil.message2emoji(customBean.content)
//                        val tplBean = customBean.textVO.preProcess()
//                        draweeSpanTextView.render(tplBean)
//                    }
//                } else {
//                    //系统消息  不支持的消息类型
//                    showMessageView(helper, ACTION_VIEW)
//                    draweeSpanTextView.text = "无法识别的消息，请升级到最新版本"
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        } else {
//            conAction.hide()
//        }
        conAction.hide()
//        helper.addOnClickListener(R.id.iv_send_fail)
    }

    /**
     * 显示消息视图
     * @param picMessage 是否是图片消息
     */
    private fun showMessageView(holder: BaseViewHolder, messageType: String) {
        //普通内容区域
        val rl_content = holder.getView<View>(R.id.rl_content)
        val tv_content = holder.getView<TextView>(R.id.tv_content)
        //图片消息 相关视图
        val view_pic = holder.getView<View>(R.id.view_pic)
        val sdv_pic = holder.getView<View>(R.id.sdv_pic)
        val view_divider_pic = holder.getView<View>(R.id.view_divider_pic)
        val tv_pic = holder.getView<View>(R.id.tv_pic)
        //操作视图
        val con_action = holder.getView<View>(R.id.con_action)
        //头像视图
        val sdv_header = holder.getView<View>(R.id.sdv_header)
//        val fl_living = holder.getView<View>(R.id.fl_living)

        when (messageType) {
            TEXT_MESSAGE -> {
                //显示普通文案视图
                sdv_header.show()
                rl_content?.show()
                tv_content.show()

                view_pic.hide()
                sdv_pic.hide()
                view_divider_pic.hide()
                tv_pic.hide()
            }
            PIC_MESSAGE -> {
                //显示图片视图
                sdv_header.show()

                rl_content?.hide()
                tv_content.hide()

                view_pic.show()
                sdv_pic.show()
                view_divider_pic.show()
                tv_pic.show()
            }
            ACTION_VIEW -> {
                //只显示底部操作视图,隐藏其他视图
                con_action.show()

                rl_content?.hide()
                tv_content.hide()
                view_pic.hide()
                sdv_pic.hide()
                view_divider_pic.hide()
                tv_pic.hide()
                sdv_header.hide()
//                fl_living?.hide()
            }
            else -> {

            }
        }


    }


}