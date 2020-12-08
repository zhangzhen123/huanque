package com.julun.huanque.core.adapter

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.DynamicComment
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.setImageSpan
import com.julun.huanque.common.widgets.draweetext.DraweeSpanTextView
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.core.R
import com.luck.picture.lib.tools.ScreenUtils
import java.lang.StringBuilder

/**
 *@创建者   dong
 *@创建时间 2020/11/26 17:29
 *@描述 动态详情 的二级评论
 */
class DynamicDetailCommentSecondAdapter : BaseQuickAdapter<DynamicComment, BaseViewHolder>(R.layout.recycler_item_dynamic_detail_comment_second) {

    init {
        addChildClickViewIds(R.id.tv_praise, R.id.sdv_header)
    }

    override fun convert(holder: BaseViewHolder, item: DynamicComment) {
        val sdv_header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
        sdv_header.loadImage(item.headPic, 30f, 30f)

        val tv_praise = holder.getView<TextView>(R.id.tv_praise)
        tv_praise.isActivated = item.hasPraise
        tv_praise.text = StringHelper.formatNumWithTwoDecimals(item.praiseNum.toLong())
        holder.setText(R.id.tv_content, EmojiSpanBuilder.buildEmotionSpannable(context, item.content))
            .setText(R.id.tv_time, item.createTime)

        //处理昵称
//        val replyNickname = item.replyNickname
//        //评论人是否是楼主
//        val originalPoster = item.originalPoster == BusiConstant.True
//        val nicknameContent = StringBuilder()
//        nicknameContent.append(item.nickname)
//        if (originalPoster) {
//            //是楼主
//            nicknameContent.append(" # ")
//        }
//        if (replyNickname.isNotEmpty()) {
//            //有回复的对象
//            nicknameContent.append(" # ").append(replyNickname)
//        }
//        //被回复人是否是楼主（楼主回复自己，被回复人不显示楼主标识）
//        //是否显示被回复人的楼主
//        val showOriginal = (item.originalReply == BusiConstant.True && !originalPoster)
//        if (showOriginal) {
//            nicknameContent.append(" #")
//        }
//
//        val builder = DraweeSpanStringBuilder(nicknameContent.toString())
//        if (originalPoster) {
//            //渲染评论人楼主
//            val start = item.replyNickname.length + 1
//            if (start < nicknameContent.length && start >= 0) {
//                builder.setImageSpan(
//                    context, R.mipmap.icon_original_poster_comment, start, start, dp2px(32), dp2px(16)
//                )
//            }
//        }
//        if (replyNickname.isNotEmpty()) {
//            //渲染箭头
//            val start = if (showOriginal) {
//                nicknameContent.length - replyNickname.length - 4
//            } else {
//                nicknameContent.length - replyNickname.length - 2
//            }
//            if (start < nicknameContent.length && start >= 0) {
//                builder.setImageSpan(
//                    context, R.mipmap.icon_arrow_reply, start, start, dp2px(7), dp2px(9)
//                )
//            }
//        }
//
//        if (showOriginal) {
//            //被回复人是否是楼主
//            val start = nicknameContent.length - 1
//            if (start < nicknameContent.length && start >= 0) {
//                builder.setImageSpan(
//                    context, R.mipmap.icon_original_poster_comment, start, start, dp2px(32), dp2px(16)
//                )
//            }
//        }
//        val tv_nickname = holder.getView<DraweeSpanTextView>(R.id.tv_nickname)
//        tv_nickname.setDraweeSpanStringBuilder(builder)
        val tv_owner_nickname = holder.getView<TextView>(R.id.tv_owner_nickname)
        val tv_reply_nickname = holder.getView<TextView>(R.id.tv_reply_nickname)

        //回复人昵称
        val nickname = item.nickname
        //被回复人昵称
        val replyNickname = item.replyNickname
        val original = item.originalPoster == BusiConstant.True
        val replyOriginal = item.originalReply == BusiConstant.True
        //可用的宽度
        //左间距：101 右间距：54 回复图标：7+5  楼主图标：32+5
        val enableWidth = if (replyOriginal || original) {
            //有楼主
            ScreenUtils.getScreenWidth(context) - dp2px(101 + 54 + 7 + 5 + 32 + 5)
        } else {
            ScreenUtils.getScreenWidth(context) - dp2px(101 + 54 + 7 + 5)
        }
        //控制楼主标识显示与隐藏
        holder.setGone(R.id.iv_owner_first, !original)
            .setGone(R.id.iv_owner_second, (original || !replyOriginal))

        val ownerNicknameParams = tv_owner_nickname.layoutParams as? LinearLayout.LayoutParams
        val replyNicknameParams = tv_reply_nickname.layoutParams as? LinearLayout.LayoutParams

        //需要显示的文本
        val paint = tv_owner_nickname.paint
        val firstNicknameWidth = paint.measureText(nickname)
        val secondNicknameWidth = paint.measureText(replyNickname)

        val needWidth = firstNicknameWidth + secondNicknameWidth

        if (needWidth <= enableWidth) {
            //可以正常显示,直接设置
            ownerNicknameParams?.weight = 0f
            replyNicknameParams?.weight = 0f
            tv_owner_nickname.text = nickname
            tv_reply_nickname.text = replyNickname
        } else {
            //不能正常显示
            if (firstNicknameWidth * 2 > enableWidth) {
                //第一个昵称过长，显示不下
                ownerNicknameParams?.weight = 1f
            } else {
                ownerNicknameParams?.weight = 0f
            }

            if (secondNicknameWidth * 2 > enableWidth) {
                //第二个昵称过长，显示不下
                replyNicknameParams?.weight = 1f
            } else {
                replyNicknameParams?.weight = 0f
            }
        }
        tv_owner_nickname.layoutParams = ownerNicknameParams
        tv_reply_nickname.layoutParams = replyNicknameParams
        //设置昵称
        tv_owner_nickname.text = nickname
        tv_reply_nickname.text = replyNickname

    }
}