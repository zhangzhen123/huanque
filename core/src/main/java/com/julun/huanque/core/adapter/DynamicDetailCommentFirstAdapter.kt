package com.julun.huanque.core.adapter

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.DynamicComment
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.StatisticCode
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.interfaces.SecondCommentClickListener
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.reportClick
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.homepage.HomePageActivity

/**
 *@创建者   dong
 *@创建时间 2020/11/26 16:44
 *@描述 动态详情评论的一级评论
 */
class DynamicDetailCommentFirstAdapter : BaseQuickAdapter<DynamicComment, BaseViewHolder>(R.layout.recycler_item_dynamic_detail_comment_first),
    LoadMoreModule {
    init {
        addChildClickViewIds(R.id.ll_comment_more, R.id.tv_share_num, R.id.iv_praise, R.id.tv_praise,R.id.sdv_header)
    }

    //点击二级评论的监听
    var mSecondCommentClickListener: SecondCommentClickListener? = null

    override fun convert(holder: BaseViewHolder, item: DynamicComment) {
        //头像和真人标识
        val sdv_header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
        val sdv_real = holder.getView<SimpleDraweeView>(R.id.iv_mark)
        val sdv_riv_ownereal = holder.getView<View>(R.id.iv_owner)
        sdv_header.loadImage(item.headPic, 36f, 36f)
        if (item.headRealPeople) {
            sdv_real.show()
            sdv_real.loadImage(item.authMark, 14f, 14f)
        } else {
            sdv_real.hide()
        }

        if (item.originalPoster == BusiConstant.True) {
            sdv_riv_ownereal.show()
        } else {
            sdv_riv_ownereal.hide()
        }

        //点赞标识
        val iv_praise = holder.getView<View>(R.id.iv_praise)
        iv_praise.isActivated = item.hasPraise

        val commentContent = if (item.commentNum == 0) {
            "评论"
        } else {
            "${item.commentNum}"
        }
        val shareContent = if (item.shareNum == 0) {
            "分享"
        } else {
            "${item.shareNum}"
        }

        holder.setText(R.id.tv_nickname, item.nickname)
            .setText(R.id.tv_time, item.createTime)
            .setText(R.id.tv_content, EmojiSpanBuilder.buildEmotionSpannable(context, item.content))
            .setText(R.id.tv_praise, StringHelper.formatNumWithTwoDecimals(item.praiseNum.toLong()))
            .setText(R.id.tv_comment_num, commentContent)
            .setText(R.id.tv_share_num, shareContent)
            .setGone(R.id.ll_comment_more, !item.hasMore)

        //设置Adapter数据
        val mSecondAdapter = DynamicDetailCommentSecondAdapter()
        val recycler_second = holder.getView<RecyclerView>(R.id.recycler_second)
        val secondComments = item.secondComments
        if (secondComments.isEmpty()) {
            recycler_second.hide()
        } else {
            recycler_second.show()

            recycler_second.layoutManager = LinearLayoutManager(context)
            recycler_second.adapter = mSecondAdapter
            mSecondAdapter.setList(item.secondComments)
            mSecondAdapter.setOnItemClickListener { adapter, view, position ->
                val commentData = adapter.getItemOrNull(position) as? DynamicComment ?: return@setOnItemClickListener
                commentData.firstCommentId = item.commentId
                mSecondCommentClickListener?.secondCommentClick(commentData)
            }

            mSecondAdapter.setOnItemLongClickListener { adapter, view, position ->
                val commentData = adapter.getItemOrNull(position) as? DynamicComment
                commentData?.firstCommentId = item.commentId
                mSecondCommentClickListener?.secondLongCommentClick(view, commentData ?: return@setOnItemLongClickListener true)
                true
            }

            mSecondAdapter.setOnItemChildClickListener { adapter, view, position ->
                val commentData = adapter.getItemOrNull(position) as? DynamicComment ?: return@setOnItemChildClickListener
                commentData.firstCommentId = item.commentId
                if (view.id == R.id.tv_praise) {
                    //点赞
                    mSecondCommentClickListener?.praise(commentData)
                } else if (view.id == R.id.sdv_header) {
                    //跳转主页
                    CommonInit.getInstance().getCurrentActivity()?.let { act ->
                        HomePageActivity.newInstance(act,commentData.userId)
                        reportClick(StatisticCode.EnterUserPage+ StatisticCode.Post)
                    }


                }

            }
        }
    }
}