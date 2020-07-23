package com.julun.huanque.message.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.LocalConversation
import com.julun.huanque.common.bean.beans.FriendContent
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.constant.MessageCustomBeanType
import com.julun.huanque.common.constant.SystemTargetId
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.message.R
import io.rong.message.ImageMessage
import io.rong.message.TextMessage

/**
 *@创建者   dong
 *@创建时间 2019/5/17 14:22
 *@描述 会话使用
 */
class ConversationListAdapter : BaseQuickAdapter<LocalConversation, BaseViewHolder>(R.layout.recycler_item_conversion) {

    //免打扰列表
    var blockList = mutableListOf<String>()

    init {
        addChildClickViewIds(R.id.sdv_header)
    }

    override fun convert(helper: BaseViewHolder, item: LocalConversation) {
        val msg = item.conversation.latestMessage
        //头像
        val sdvHeader = helper.getView<SimpleDraweeView>(R.id.sdv_header)
        //欢遇状态
        val ivHuanyu = helper.getView<ImageView>(R.id.iv_huanyu)
        //亲密度等级
        val tvRoyalLevel = helper.getView<TextView>(R.id.tv_royal_level)
        //官方图标
        val ivGuan = helper.getView<View>(R.id.iv_guan)
        val targetId = item.conversation.targetId

        if (item.showUserInfo != null) {
            ivGuan.hide()
            //存在用户信息
            ImageUtils.setDefaultHeaderPic(sdvHeader, item.showUserInfo?.sex ?: "")
            //设置默认头像
            ImageUtils.loadImage(sdvHeader, item.showUserInfo?.headPic ?: "", 50f, 50f)
            helper.setText(R.id.tv_nickname, item.showUserInfo?.nickname ?: "")
            //欢遇状态
            val meetResource = ImageUtils.getMeetStatusResource(item.showUserInfo?.meetStatus ?: "")
            if (meetResource > 0) {
                //显示图标
                ivHuanyu.show()
                ivHuanyu.setImageResource(meetResource)
            } else {
                //隐藏图标
                ivHuanyu.hide()
            }
            //亲密度等级
            val level = item.showUserInfo?.intimateLevel ?: 0
            if (level > 0) {
                tvRoyalLevel.text = "Lv.$level"
                tvRoyalLevel.show()
            } else {
                tvRoyalLevel.hide()
            }
        } else {
            //用户数据为空，判断是否是系统和鹊友会话
            ivHuanyu.hide()
            tvRoyalLevel.hide()


            if (targetId == SystemTargetId.systemNoticeSender) {
                //系统通知
                ImageUtils.loadImageLocal(sdvHeader, R.mipmap.icon_message_system)
                helper.setText(R.id.tv_nickname, "系统消息")
                ivGuan.show()
            } else {
                ivGuan.hide()
            }
            if (targetId == SystemTargetId.friendNoticeSender) {
                //鹊友通知
                ImageUtils.loadImageLocal(sdvHeader, R.mipmap.icon_message_friend)
                helper.setText(R.id.tv_nickname, "鹊友通知")
            }

            val info = item.strangerInfo
            if (targetId?.isNotEmpty() != true) {
                //陌生人消息
                var time = 0L
                var unreadCount = 0
                try {
                    time = info[LocalConversation.TIME]?.toLong() ?: 0
                    unreadCount = info[LocalConversation.UNREADCOUNT]?.toInt() ?: 0
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                ImageUtils.loadImageLocal(sdvHeader, R.mipmap.icon_message_stranger)
                helper.setText(R.id.tv_nickname, "陌生人消息")
                    .setText(R.id.tv_time, TimeUtils.formatDetailTime(time))

                val nickname = info[LocalConversation.NICKNAME]
                val tvContent = helper.getView<TextView>(R.id.tv_content)
                if (nickname?.isNotEmpty() == true) {
                    tvContent.text = "${info[LocalConversation.NICKNAME]}发来一条消息"
                } else {
                    tvContent.text = ""
                }


                val tvUnread = helper.getView<TextView>(R.id.tv_unread_count)
                tvUnread.text = StringHelper.formatMessageCount(unreadCount)
                if (unreadCount > 0) {
                    tvUnread.show()
                    tvUnread.isEnabled = !blockList.contains(targetId)
                } else {
                    tvUnread.hide()
                }
            }
        }


        when (msg) {
            is TextMessage -> {
                //文本消息
                when (targetId) {
                    SystemTargetId.systemNoticeSender -> {
                        val msgConent = MessageFormatUtils.formatSysMsgContent(msg.content)
                        helper.setText(R.id.tv_content, msgConent?.context?.body ?: "")
                    }
                    SystemTargetId.friendNoticeSender -> {
                        val msgConent: FriendContent? = MessageFormatUtils.parseJsonFromTextMessage(FriendContent::class.java, msg.content)
                        MessageFormatUtils.renderImage(helper.getView(R.id.tv_content), msgConent?.context ?: return)
                    }
                    else -> {
                        helper.setText(R.id.tv_content, EmojiSpanBuilder.buildEmotionSpannable(context, msg.content))
                    }
                }
            }
            is ImageMessage -> {
                //图片消息
                helper.setText(R.id.tv_content, "[图片]")
            }
            is CustomMessage -> {
                if (msg.type == MessageCustomBeanType.Gift) {
                    helper.setText(R.id.tv_content, "[私信礼物]")
                } else {
                    helper.setText(R.id.tv_content, "")
                }
            }
            is CustomSimulateMessage -> {
                when (msg.type) {
                    MessageCustomBeanType.Voice_Conmmunication_Simulate -> {
                        //语音通话消息
                        helper.setText(R.id.tv_content, "[语音通话]")
                    }
                    MessageCustomBeanType.SYSTEM_MESSAGE -> {
                        //模拟系统消息
                        helper.setText(R.id.tv_content, "暂无新消息")
                    }
                    MessageCustomBeanType.FRIEND_MESSAGE -> {
                        //模拟好友消息
                        helper.setText(R.id.tv_content, "暂无新消息")
                    }
                    else -> {
                        helper.setText(R.id.tv_content, "")
                    }
                }
            }
            else -> {
                if (targetId?.isNotEmpty() == true) {
                    helper.setText(R.id.tv_content, "")
                }
            }
        }

        if (targetId?.isNotEmpty() == true) {
            //非陌生人消息
            val tvUnread = helper.getView<TextView>(R.id.tv_unread_count)
            val unreadCount = item.conversation.unreadMessageCount
            tvUnread.text = "${StringHelper.formatMessageCount(unreadCount)}"
            if (unreadCount > 0) {
                tvUnread.show()
                tvUnread.isEnabled = !blockList.contains(targetId)
            } else {
                tvUnread.hide()
            }

            helper.setText(R.id.tv_time, TimeUtils.formatDetailTime(item.conversation.sentTime))
        }

        //存在会话消息，显示常规的布局
        helper.setVisible(R.id.tv_time, true)
    }
}