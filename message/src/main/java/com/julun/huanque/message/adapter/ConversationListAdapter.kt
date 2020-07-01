package com.julun.huanque.message.adapter

import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.julun.huanque.R
import com.julun.huanque.common.bean.LocalConversation
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.TimeUtils
import io.rong.imlib.model.Conversation
import io.rong.message.TextMessage
import org.jetbrains.anko.backgroundColor

/**
 *@创建者   dong
 *@创建时间 2019/5/17 14:22
 *@描述
 */
class ConversationListAdapter :
    BaseQuickAdapter<LocalConversation, BaseViewHolder>(R.layout.recycler_item_conversion) {
    var curAnchorId = ""

    //直播间以外的消息列表不显示勋章
    var isLive: Boolean = false

    override fun convert(helper: BaseViewHolder?, item: LocalConversation?) {
        if (helper == null || item == null) {
            return
        }
        val targetId = item.conversation.targetId
        val con = helper.getView<View>(R.id.con)
        try {
            con.backgroundColor = if (curAnchorId == targetId) {
                //con.back
                Color.parseColor("#fcfbf9")
            } else {
                Color.WHITE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val msg = item.conversation.latestMessage
        //        if (item.showUserInfo != null) {
        //            //存在用户信息
        //            ImageUtils.loadImage(helper.getView<SimpleDraweeView>(R.id.sdv_header), item.showUserInfo?.headPic
        //                    ?: "", 50f, 50f)
        //            helper.setText(R.id.tv_nickname, item.showUserInfo?.nickname ?: "")
        //            val ivRoyalLevel = helper.getView<ImageView>(R.id.iv_royal_level)
        //            val ivUserLevel = helper.getView<ImageView>(R.id.iv_user_level)
        //            if (item.showUserInfo?.userType == BusiConstant.ChatUserType.Anchor) {
        //                //主播身份
        //                ivRoyalLevel.hide()
        //                if (!isLive) {
        //                    ivUserLevel.hide()
        //                } else {
        //                    val anchorLevel = item.showUserInfo?.anchorLevel ?: 0
        //                    if (anchorLevel > 0) {
        //                        ivUserLevel.show()
        //                        ivUserLevel.setImageResource(ImageUtils.getAnchorLevelResId(anchorLevel))
        //                    } else {
        //                        ivUserLevel.hide()
        //                    }
        //                }
        //            } else {
        //                //用户身份
        //                if (!isLive) {
        //                    ivRoyalLevel.hide()
        //                    ivUserLevel.hide()
        //                } else {
        //                    ivRoyalLevel.show()
        //                    val userLevel = item.showUserInfo?.userLevel ?: 0
        //                    if (userLevel > 0) {
        //                        ivUserLevel.show()
        //                        ivUserLevel.setImageResource(ImageUtils.getUserLevelImg(userLevel))
        //                    } else {
        //                        ivUserLevel.hide()
        //                    }
        //                    val royalLevel = item.showUserInfo?.royalLevel ?: 0
        //                    if (royalLevel > 0) {
        //                        ivRoyalLevel.setImageResource(ImageUtils.getRoyalLevelImgRound(royalLevel))
        //                    }
        //                }
        //            }
        //        } else {
        //不存在用户信息
        if (msg is TextMessage) {
            //文本消息
            //                msg.extra?.let {
            //                    val user: RoomUserChatExtra? = JsonUtil.deserializeAsObject(it, RoomUserChatExtra::class.java)
            //                    if (user?.userId != SessionUtils.getUserId()) {
            //                        helper.setText(R.id.tv_nickname, user?.nickname ?: "")
            //                    } else {
            helper.setText(R.id.tv_nickname, item.conversation.targetId)
            //                    }
            //                }
        }
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