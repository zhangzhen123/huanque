package com.julun.huanque.message.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.LocalConversation
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.message.R
import io.rong.message.TextMessage

/**
 *@创建者   dong
 *@创建时间 2019/5/17 14:22
 *@描述 会话使用
 */
class ConversationListAdapter : BaseQuickAdapter<LocalConversation, BaseViewHolder>(R.layout.recycler_item_conversion) {

    override fun convert(helper: BaseViewHolder, item: LocalConversation) {
        val msg = item.conversation.latestMessage
        if (item.showUserInfo != null) {
            //存在用户信息
            ImageUtils.loadImage(helper.getView<SimpleDraweeView>(R.id.sdv_header), item.showUserInfo?.headPic ?: "", 50f, 50f)
            helper.setText(R.id.tv_nickname, item.showUserInfo?.nickname ?: "")

        }
        //不存在用户信息
//            if (msg is TextMessage) {
//                //文本消息
//                msg.extra?.let {
//                    val user: RoomUserChatExtra? = JsonUtil.deserializeAsObject(it, RoomUserChatExtra::class.java)
//                    if (user?.userId != SessionUtils.getUserId()) {
//                        helper.setText(R.id.tv_nickname, user?.nickname ?: "")
//                    } else {
//                        helper.setText(R.id.tv_nickname, item.conversation.targetId)
//                    }
//                }
//            }
        //        }


        when (msg) {
            is TextMessage -> {
                helper.setText(R.id.tv_content, msg.content)
            }
            //            is CustomMessage -> {
            //                try {
            //                    val customBean = JsonUtil.deserializeAsObject<MessageActionBean>(msg.context, MessageActionBean::class.java)
            //                    helper.setText(R.id.tv_content, customBean.content)
            //                } catch (e: Exception) {
            //                    e.printStackTrace()
            //                }
            //            }
            else -> {
                helper.setText(R.id.tv_content, "")
            }
        }
        val tvUnread = helper.getView<TextView>(R.id.tv_unread_count)
        val unreadCount = item.conversation.unreadMessageCount
        tvUnread.text = "${StringHelper.formatMessageCount(unreadCount)}"
        if (unreadCount > 0) {
            tvUnread.show()
        } else {
            tvUnread.hide()
        }

        helper.setText(R.id.tv_time, TimeUtils.formatDetailTime(item.conversation.sentTime))

        //存在会话消息，显示常规的布局
        helper.setVisible(R.id.tv_time, true)
    }
}