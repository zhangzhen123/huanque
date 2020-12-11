package com.julun.huanque.message.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.ActionMessageContent
import com.julun.huanque.common.bean.beans.SingleActionInMessage
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.MessageFormatUtils
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.message.R
import io.rong.imlib.model.Message
import io.rong.message.TextMessage
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor

/**
 *@创建者   dong
 *@创建时间 2020/11/27 16:56
 *@描述 消息评论列表使用的Adapter
 */
class PraiseListAdapter : BaseQuickAdapter<Message, BaseViewHolder>(R.layout.recycler_item_praise_list), LoadMoreModule {

    init {
        addChildClickViewIds(R.id.view_header_info, R.id.con_post_action)
    }

    override fun convert(holder: BaseViewHolder, info: Message) {

        val textMessage = info.content as? TextMessage
        textMessage?.let {
            val customBean: ActionMessageContent? =
                MessageFormatUtils.parseJsonFromTextMessage(ActionMessageContent::class.java, textMessage.content ?: "")
            val item = customBean?.context ?: return
            val sdv_header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
            val sdv_real_people = holder.getView<SimpleDraweeView>(R.id.sdv_real_people)
            val sdv_post = holder.getView<SimpleDraweeView>(R.id.sdv_post)

            sdv_header.loadImage(item.headPic, 46f, 46f)
            if (item.authMark.isNotEmpty()) {
                sdv_real_people.show()
                sdv_real_people.loadImage(item.authMark, 14f, 14f)
            } else {
                sdv_real_people.hide()
            }
            if (item.pic.isEmpty()) {
                sdv_post.hide()
            } else {
                sdv_post.show()
                sdv_post.loadImage(item.pic, 50f, 50f)
            }
            val postContent = if (item.comment.isNotEmpty()) {
                //点赞的是评论
                EmojiSpanBuilder.buildEmotionSpannable(context, item.comment)
            } else {
                //点赞的是动态
                if (item.content.isEmpty()) {
                    "分享图片"
                } else {
                    EmojiSpanBuilder.buildEmotionSpannable(context, item.content)
                }
            }
            val praiseConent = if (item.comment.isNotEmpty()) {
                "点赞你的评论"
            } else {
                "点赞你的动态"
            }


            holder.setText(R.id.tv_nickname, item.nickname)
                .setText(R.id.tv_post_content, postContent)
                .setText(R.id.tv_praise, praiseConent)
                .setText(R.id.tv_time, TimeUtils.formatMessageListTime(item.createTime))
                .setVisible(R.id.iv_host, item.originalPoster == BusiConstant.True)

            val tv_sex = holder.getView<TextView>(R.id.tv_sex)

            var sexDrawable: Drawable? = null
            //性别
            when (item.sex) {
                Sex.MALE -> {
                    tv_sex.backgroundResource = R.drawable.bg_shape_mkf_sex_male
                    sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_male)
                    tv_sex.textColor = Color.parseColor("#58CEFF")
                }
                Sex.FEMALE -> {
                    tv_sex.backgroundResource = R.drawable.bg_shape_mkf_sex_female
                    sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_female)
                    tv_sex.textColor = Color.parseColor("#FF9BC5")
                }
                else -> sexDrawable = null
            }
            if (sexDrawable != null) {
                sexDrawable.setBounds(0, 0, sexDrawable.minimumWidth, sexDrawable.minimumHeight)
                tv_sex.setCompoundDrawables(sexDrawable, null, null, null)
            } else {
                tv_sex.setCompoundDrawables(null, null, null, null)
            }
            tv_sex.text = "${item.age}"

            if (item.sex.isEmpty() || item.age == 0) {
                tv_sex.hide()
            } else {
                tv_sex.show()
            }
        }


    }
}