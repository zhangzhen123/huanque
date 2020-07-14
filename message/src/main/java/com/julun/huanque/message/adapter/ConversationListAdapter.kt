package com.julun.huanque.message.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.LocalConversation
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.constant.MeetStatus
import com.julun.huanque.common.constant.MessageCustomBeanType
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.message.R
import io.rong.message.ImageMessage
import io.rong.message.TextMessage

/**
 *@创建者   dong
 *@创建时间 2019/5/17 14:22
 *@描述 会话使用
 */
class ConversationListAdapter : BaseQuickAdapter<LocalConversation, BaseViewHolder>(R.layout.recycler_item_conversion) {

    init {
        addChildClickViewIds(R.id.sdv_header)
    }

    override fun convert(helper: BaseViewHolder, item: LocalConversation) {
        val msg = item.conversation.latestMessage
        if (item.showUserInfo != null) {
            //存在用户信息
            val sdvHeader = helper.getView<SimpleDraweeView>(R.id.sdv_header)
            ImageUtils.setDefaultHeaderPic(sdvHeader, item.showUserInfo?.sex ?: "")
            //设置默认头像
            ImageUtils.loadImage(sdvHeader, item.showUserInfo?.headPic ?: "", 50f, 50f)
            helper.setText(R.id.tv_nickname, item.showUserInfo?.nickname ?: "")
            //欢遇状态
            val ivHuanyu = helper.getView<ImageView>(R.id.iv_huanyu)
            val meetResource = GlobalUtils.getMeetStatusResource(item.showUserInfo?.meetStatus ?: "")
            if (meetResource > 0) {
                //显示图标
                ivHuanyu.show()
                ivHuanyu.setImageResource(meetResource)
            } else {
                //隐藏图标
                ivHuanyu.hide()
            }
        }


        when (msg) {
            is TextMessage -> {
                //文本消息
                helper.setText(R.id.tv_content, msg.content)
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
                if (msg.type == MessageCustomBeanType.Voice_Conmmunication_Simulate) {
                    //语音通话消息
                    helper.setText(R.id.tv_content, "[语音通话]")
                } else {
                    helper.setText(R.id.tv_content, "")
                }
            }
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