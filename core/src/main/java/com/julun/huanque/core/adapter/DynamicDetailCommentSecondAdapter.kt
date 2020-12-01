package com.julun.huanque.core.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.DynamicComment
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.setImageSpan
import com.julun.huanque.common.widgets.draweetext.DraweeSpanTextView
import com.julun.huanque.core.R
import java.lang.StringBuilder

/**
 *@创建者   dong
 *@创建时间 2020/11/26 17:29
 *@描述 动态详情 的二级评论
 */
class DynamicDetailCommentSecondAdapter : BaseQuickAdapter<DynamicComment, BaseViewHolder>(R.layout.recycler_item_dynamic_detail_comment_second) {

    init {
        addChildClickViewIds(R.id.tv_praise)
    }

    override fun convert(holder: BaseViewHolder, item: DynamicComment) {
        val sdv_header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
        sdv_header.loadImage(item.headPic, 30f, 30f)

        val tv_praise = holder.getView<TextView>(R.id.tv_praise)
        tv_praise.isActivated = item.hasPraise
        tv_praise.text = "${item.praiseNum}"
        holder.setText(R.id.tv_content, item.content)
            .setText(R.id.tv_time, item.createTime)

        //处理昵称
        val replyNickname = item.replyNickname
        //评论人是否是楼主
        val originalPoster = item.originalPoster == BusiConstant.True
        val nicknameContent = StringBuilder()
        nicknameContent.append(item.nickname)
        if (originalPoster) {
            //是楼主
            nicknameContent.append(" # ")
        }
        if (replyNickname.isNotEmpty()) {
            //有回复的对象
            nicknameContent.append(" # ").append(replyNickname)
        }
        //被回复人是否是楼主（楼主回复自己，被回复人不显示楼主标识）
        //是否显示被回复人的楼主
        val showOriginal = (item.originalReply == BusiConstant.True && !originalPoster)
        if (showOriginal) {
            nicknameContent.append(" #")
        }

        val builder = DraweeSpanStringBuilder(nicknameContent.toString())
        if (originalPoster) {
            //渲染评论人楼主
            val start = item.replyNickname.length + 1
            if (start < nicknameContent.length && start >= 0) {
                builder.setImageSpan(
                    context, R.mipmap.icon_original_poster_comment, start, start, dp2px(32), dp2px(16)
                )
            }
        }
        if (replyNickname.isNotEmpty()) {
            //渲染箭头
            val start = if (showOriginal) {
                nicknameContent.length - replyNickname.length - 4
            } else {
                nicknameContent.length - replyNickname.length - 2
            }
            if (start < nicknameContent.length && start >= 0) {
                builder.setImageSpan(
                    context, R.mipmap.icon_arrow_reply, start, start, dp2px(7), dp2px(9)
                )
            }
        }

        if (showOriginal) {
            //被回复人是否是楼主
            val start = nicknameContent.length - 1
            if (start < nicknameContent.length && start >= 0) {
                builder.setImageSpan(
                    context, R.mipmap.icon_original_poster_comment, start, start, dp2px(32), dp2px(16)
                )
            }
        }
        val tv_nickname = holder.getView<DraweeSpanTextView>(R.id.tv_nickname)
        tv_nickname.setDraweeSpanStringBuilder(builder)
    }
}