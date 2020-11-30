package com.julun.huanque.core.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.DynamicComment
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
    override fun convert(holder: BaseViewHolder, item: DynamicComment) {
        val sdv_header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
        sdv_header.loadImage(item.headPic, 30f, 30f)

        val tv_praise = holder.getView<TextView>(R.id.tv_praise)
        tv_praise.isSelected = item.hasPraise
        tv_praise.text = "${item.praiseNum}"
        holder.setText(R.id.tv_content, item.content)
            .setText(R.id.tv_time, item.createTime)

        //处理昵称
        val replyNickname = item.replyNickname
        //是否是楼主
        val originalPoster = item.originalPoster
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

        val builder = DraweeSpanStringBuilder(nicknameContent.toString())
        if (originalPoster) {
            //渲染楼主
            val start = item.replyNickname.length + 1
            if (start < nicknameContent.length && start >= 0) {
                builder.setImageSpan(
                    context, R.mipmap.icon_original_poster_comment, start, start, dp2px(32), dp2px(16)
                )
            }
        }
        if (replyNickname.isNotEmpty()) {
            //渲染箭头
            val start = nicknameContent.length - replyNickname.length - 2
            if (start < nicknameContent.length && start >= 0) {
                builder.setImageSpan(
                    context, R.mipmap.icon_arrow_reply, start, start, dp2px(7), dp2px(9)
                )
            }

        }
        val tv_nickname = holder.getView<DraweeSpanTextView>(R.id.tv_nickname)
        tv_nickname.setDraweeSpanStringBuilder(builder)
    }
}