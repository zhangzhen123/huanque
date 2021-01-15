package com.julun.huanque.core.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.loadImageNoResize
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/12/28 11:21
 *@描述 标签图片列表Adapter
 */
class TagListAdapter : BaseQuickAdapter<UserTagBean, BaseViewHolder>(R.layout.recycler_item_tag_list) {
    private val likeDrawable = GlobalUtils.getDrawable(R.mipmap.icon_tag_like_heart)
    private val gettedDrawable = GlobalUtils.getDrawable(R.mipmap.icon_tag_getted)

    //是否是喜欢的样式
    var mLikeStyle: Boolean = false
    override fun convert(holder: BaseViewHolder, item: UserTagBean) {
        val adapterPosition = holder.adapterPosition
        if (adapterPosition % 4 == 0) {
            holder.itemView.setPadding(0, 0, 1, 0)
        } else if (adapterPosition % 4 == 3) {
            holder.itemView.setPadding(1, 0, 0, 0)
        } else {
            holder.itemView.setPadding(1, 0, 1, 0)
        }
        val sdv_tag = holder.getView<SimpleDraweeView>(R.id.sdv_tag)
        sdv_tag.loadImageNoResize(item.tagPic)
        val sdv_icon = holder.getView<SimpleDraweeView>(R.id.sdv_icon)
        sdv_icon.loadImageNoResize(item.tagIcon)

        holder.setText(R.id.tv_tag_name, item.tagName)
        val tv_like_statue = holder.getView<TextView>(R.id.tv_like_statue)
        tv_like_statue.isSelected = item.mark == BusiConstant.True
        if (item.mark == BusiConstant.True) {
            //显示like图标
            val drawable = if (!mLikeStyle) {
                gettedDrawable
            } else {
                likeDrawable
            }
            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            tv_like_statue.setCompoundDrawables(drawable, null, null, null)
            if (!mLikeStyle) {
                tv_like_statue.text = "已认证"
            } else {
                tv_like_statue.text = "已喜欢"
            }
        } else {
            tv_like_statue.setCompoundDrawables(null, null, null, null)
            if (!mLikeStyle) {
                tv_like_statue.text = "去认证"
            } else {
                tv_like_statue.text = "去喜欢"
            }
        }
    }
}