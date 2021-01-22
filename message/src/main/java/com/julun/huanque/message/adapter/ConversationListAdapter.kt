package com.julun.huanque.message.adapter

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.LocalConversation
import com.julun.huanque.common.bean.beans.ActionMessageContent
import com.julun.huanque.common.bean.beans.FriendContent
import com.julun.huanque.common.bean.beans.IntimateTouchBean
import com.julun.huanque.common.bean.message.CustomMessage
import com.julun.huanque.common.bean.message.CustomSimulateMessage
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.message.R
import io.rong.imlib.model.MessageContent
import io.rong.message.ImageMessage
import io.rong.message.TextMessage
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.imageResource

/**
 *@创建者   dong
 *@创建时间 2019/5/17 14:22
 *@描述 会话使用
 */
class ConversationListAdapter : BaseQuickAdapter<LocalConversation, BaseViewHolder>(R.layout.recycler_item_conversion) {

    //免打扰列表
    var blockList = mutableListOf<String>()

    //主播Id
    var curAnchorId = ""

    init {
        addChildClickViewIds(R.id.sdv_header)
    }

    override fun convert(helper: BaseViewHolder, item: LocalConversation) {

        val targetId = item.conversation.targetId
        val con = helper.getView<View>(R.id.con)
        try {
            con.backgroundColor = if (curAnchorId == targetId) {
                Color.parseColor("#fcfbf9")
            } else {
                Color.WHITE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val msg: MessageContent? = item.conversation.latestMessage
        //头像
        val sdvHeader = helper.getView<SimpleDraweeView>(R.id.sdv_header)
        //欢遇状态
        val ivHuanyu = helper.getView<ImageView>(R.id.iv_huanyu)
        //官方图标
        val ivPic = helper.getView<ImageView>(R.id.iv_pic)
        val tv_draft = helper.getView<View>(R.id.tv_draft)

        if (item.showUserInfo != null) {
            //存在用户信息
            ImageHelper.setDefaultHeaderPic(sdvHeader, item.showUserInfo?.sex ?: "")
            //设置默认头像
            ImageUtils.loadImage(sdvHeader, "${item.showUserInfo?.headPic ?: ""}${BusiConstant.OSS_160}", 56f, 56f)
            helper.setText(R.id.tv_nickname, item.showUserInfo?.nickname ?: "")
            if (item.showUserInfo?.userType == UserType.Manager) {
                //官方
                ivPic.show()
                ivPic.imageResource = R.mipmap.icon_guan_home_page
                ivHuanyu.hide()
            } else {
                //欢遇状态
                val meetResource = ImageHelper.getMeetStatusResource(item.showUserInfo?.meetStatus ?: "")
                if (SessionUtils.getSex() == Sex.FEMALE && meetResource > 0) {
                    //当前用户为女性，并且和对方存在欢遇标识 显示图标
                    ivHuanyu.show()
                    ivHuanyu.setImageResource(meetResource)
                } else {
                    //隐藏图标
                    ivHuanyu.hide()
                }
                //亲密度等级
                val level = item.showUserInfo?.intimateLevel ?: 0
                val levelPic = ImageHelper.getIntimateLevelPic(level)

                if (levelPic > 0) {
                    ivPic.show()
                    ivPic.imageResource = levelPic
                } else {
                    ivPic.hide()
                }
            }
        } else {
            //用户数据为空，判断是否是系统和鹊友会话
            ivHuanyu.hide()

            if (targetId == SystemTargetId.systemNoticeSender) {
                //系统通知
                ImageUtils.loadImageLocal(sdvHeader, R.mipmap.icon_message_system)
                helper.setText(R.id.tv_nickname, "系统通知")
                ivPic.show()
                ivPic.imageResource = R.mipmap.icon_guan_home_page
            } else {
                ivPic.hide()
            }
            if (targetId == SystemTargetId.friendNoticeSender) {
                //鹊友通知
                ImageUtils.loadImageLocal(sdvHeader, R.mipmap.icon_message_friend)
                helper.setText(R.id.tv_nickname, "鹊友通知")
            }

            if (targetId == SystemTargetId.commentNoticeSender) {
                //评论消息
                ImageUtils.loadImageLocal(sdvHeader, R.mipmap.icon_message_comment)
                helper.setText(R.id.tv_nickname, "评论")
            }

            if (targetId == SystemTargetId.praiseNoticeSender) {
                //点赞消息
                ImageUtils.loadImageLocal(sdvHeader, R.mipmap.icon_message_praise)
                helper.setText(R.id.tv_nickname, "点赞")
            }

            val info = item.strangerInfo
            if (targetId?.isNotEmpty() != true) {
                //陌生人消息
                var time = 0L
                var unreadCount = 0
                tv_draft.hide()
                try {
                    time = info[LocalConversation.TIME]?.toLong() ?: 0
                    unreadCount = info[LocalConversation.UNREADCOUNT]?.toInt() ?: 0
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                ImageUtils.loadImageLocal(sdvHeader, R.mipmap.icon_message_stranger)
                helper.setText(R.id.tv_nickname, "陌生人消息")
                    .setText(R.id.tv_time, TimeUtils.formatMessageListTime(time))

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
                        tv_draft.hide()
                        val msgConent = MessageFormatUtils.formatSysMsgContent(msg.content ?: "")
                        helper.setText(R.id.tv_content, msgConent?.context?.body ?: "")
                    }
                    SystemTargetId.friendNoticeSender -> {
                        tv_draft.hide()
                        val msgConent: FriendContent? = MessageFormatUtils.parseJsonFromTextMessage(FriendContent::class.java, msg.content ?: "")
                        MessageFormatUtils.renderImage(helper.getView(R.id.tv_content), msgConent?.context ?: return, true)
                    }
                    SystemTargetId.praiseNoticeSender -> {
                        //点赞
                        tv_draft.hide()
                        if (item.conversation.unreadMessageCount > 0) {
                            val msgConent: ActionMessageContent? =
                                MessageFormatUtils.parseJsonFromTextMessage(ActionMessageContent::class.java, msg.content ?: "")
                            helper.setText(R.id.tv_content, "${msgConent?.context?.nickname} 给你点赞")
                        } else {
                            helper.setText(R.id.tv_content, "赞你的都在这")
                        }

                    }
                    SystemTargetId.commentNoticeSender -> {
                        //评论
                        tv_draft.hide()
                        if (item.conversation.unreadMessageCount > 0) {
                            //有未读消息
                            val msgConent: ActionMessageContent? =
                                MessageFormatUtils.parseJsonFromTextMessage(ActionMessageContent::class.java, msg.content ?: "")
                            helper.setText(R.id.tv_content, "${msgConent?.context?.nickname}： ${msgConent?.context?.comment}")
                        } else {
                            helper.setText(R.id.tv_content, "评论你的都在这")
                        }

                    }
                    else -> {
                        if (!showDraftView(helper, targetId)) {
                            helper.setText(R.id.tv_content, EmojiSpanBuilder.buildEmotionSpannable(context, msg.content ?: ""))
                        }

                    }
                }
            }
            is ImageMessage -> {
                //图片消息
                if (!showDraftView(helper, targetId)) {
                    helper.setText(R.id.tv_content, "[图片]")
                }
            }
            is CustomMessage -> {
                if (!showDraftView(helper, targetId)) {
                    when (msg.type) {
                        MessageCustomBeanType.Gift -> {
                            helper.setText(R.id.tv_content, "[私信礼物]")
                        }
                        MessageCustomBeanType.Expression_Privilege -> {
                            helper.setText(R.id.tv_content, "[表情]")
                        }
                        MessageCustomBeanType.Expression_Animation -> {
                            helper.setText(R.id.tv_content, "[表情]")
                        }
                        MessageCustomBeanType.SendRoom -> {
                            helper.setText(R.id.tv_content, "[传送门]")
                        }
                        MessageCustomBeanType.PostShare -> {
                            //动态分享
                            helper.setText(R.id.tv_content, "分享给你一条信息")
                        }
                        else -> {
                            helper.setText(R.id.tv_content, "")
                        }
                    }
                }
            }
            is CustomSimulateMessage -> {
                if (!showDraftView(helper, targetId)) {
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
                        MessageCustomBeanType.MessageFee -> {
                            //付费数据
                            val msgContext = msg.context
                            try {
                                val showContent = JsonUtil.deserializeAsObject<String>(msgContext, String::class.java)
                                helper.setText(R.id.tv_content, showContent)
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }

                        }
                        MessageCustomBeanType.Initim_Attention -> {
                            //亲密度变动数据
                            val msgContext = msg.context
                            try {
                                val showContent = JsonUtil.deserializeAsObject<IntimateTouchBean>(msgContext, IntimateTouchBean::class.java)
                                helper.setText(R.id.tv_content, showContent.content)
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }
                        else -> {
                            helper.setText(R.id.tv_content, "")
                        }
                    }
                }
            }
            else -> {
                if (targetId != null && !showDraftView(helper, targetId)) {
                    if (targetId == SystemTargetId.praiseNoticeSender) {
                        helper.setText(R.id.tv_content, "赞你的都在这")
                    } else if (targetId == SystemTargetId.commentNoticeSender) {
                        helper.setText(R.id.tv_content, "评论你的都在这")
                    } else if (targetId?.isNotEmpty() == true) {
                        helper.setText(R.id.tv_content, "")
                    }
                    if (targetId == curAnchorId) {
                        helper.setText(R.id.tv_content, "hi，我是${item.showUserInfo?.nickname}，快来和我聊天吧！")
                    }
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

            helper.setText(R.id.tv_time, TimeUtils.formatMessageListTime(item.conversation.sentTime))
        }

        if (curAnchorId == targetId && msg == null) {
            //当前主播会话
            //该会话当中没有消息,显示引导文案和私聊文本
            helper.setGone(R.id.tv_time, true)
                .setVisible(R.id.tv_chat, true)

        } else {
            //存在会话消息，显示常规的布局
            helper.setVisible(R.id.tv_time, true)
                .setGone(R.id.tv_chat, true)
        }
    }

    /**
     * 显示草稿数据
     * @return 是否显示了草稿
     */
    private fun showDraftView(helper: BaseViewHolder, targetId: String): Boolean {
        val key = GlobalUtils.getDraftKey(targetId, true)
        val draft = SPUtils.getString(key, "")
        val tv_draft = helper.getView<View>(R.id.tv_draft)
        if (draft.isEmpty()) {
            tv_draft.hide()
            return false
        } else {
            //显示草稿数据
            tv_draft.show()
            helper.setText(R.id.tv_content, EmojiSpanBuilder.buildEmotionSpannable(context, draft))
            return true
        }
    }
}