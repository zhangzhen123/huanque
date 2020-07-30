package com.julun.huanque.core.ui.live.adapter

import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.LiveFollowBean
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.setTFDinCdc2
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.core.R


/**
 *
 *@作者: zhangzhen
 *
 *@Date 2019/7/16 17:00
 *
 *@Description  直播间右侧的关注列表
 *
 */
class LiveFollowAdapter : BaseQuickAdapter<LiveFollowBean, BaseViewHolder>(R.layout.item_live_follow_list), LoadMoreModule {
    override fun convert(vh: BaseViewHolder, program: LiveFollowBean) {
        //普通直播Item
        ImageUtils.loadImageLocal(vh.getView(R.id.bg_shadow), R.mipmap.bg_shadow_home_item)
        //观看用户数量展示
        val userCount = vh.getView<TextView>(R.id.user_count)
        if (program.onlineUserNum != null) {
            userCount.show()
            userCount.text = "${program.onlineUserNum}"
            userCount.setTFDinCdc2()
        } else {
            userCount.hide()
        }

        //主播昵称
        vh.setText(R.id.anchor_nickname, program.programName)

        //item封面
        val strPic = program.coverPic
        ImageUtils.loadImage(vh.getView(R.id.anchorPicture), "$strPic", 130f, 130f)

        //直播状态 默认都是开播的
         vh.getView<View>(R.id.llLiving).show()


    }
}