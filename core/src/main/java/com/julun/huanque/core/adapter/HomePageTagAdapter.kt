package com.julun.huanque.core.adapter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R
import org.jetbrains.anko.imageResource

/**
 *@创建者   dong
 *@创建时间 2020/12/22 20:33
 *@描述 主页tag Adapter
 */
class HomePageTagAdapter : BaseQuickAdapter<UserTagBean, BaseViewHolder>(R.layout.recycler_item_home_tag) {
    //是否是喜欢的标签
    var like = false

    //是否是我的标签
    var mine = false

    //缺省页面的数据
    var emptyContent = ""
    override fun convert(holder: BaseViewHolder, item: UserTagBean) {
        holder.setText(R.id.tv_empty, emptyContent)
        //认证标识
        val iv_tag_auth = holder.getView<ImageView>(R.id.iv_tag_auth)
        //遮罩
        val view_shader = holder.getView<View>(R.id.view_shader)
        if (like) {
            iv_tag_auth.imageResource = R.drawable.sel_mine_tag_like
        } else {
            iv_tag_auth.imageResource = R.drawable.sel_mine_tag_auth
        }
        if (!mine) {
            iv_tag_auth.hide()
            view_shader.hide()
        } else {
            iv_tag_auth.show()
            if (item.mark == BusiConstant.True) {
                iv_tag_auth.isSelected = true
                view_shader.hide()
            } else {
                iv_tag_auth.isSelected = false
                view_shader.show()
            }


        }
        val con_empty = holder.getView<View>(R.id.con_empty)
        if (item.tagId == 0) {
            //空布局
            con_empty.show()
        } else {
            con_empty.hide()
        }

        val sdv_pic = holder.getView<SimpleDraweeView>(R.id.sdv_pic)
        sdv_pic.loadImage(item.tagPic)
        holder.setText(R.id.tv_tag_name, item.tagName)
        val sdv_icon = holder.getView<SimpleDraweeView>(R.id.sdv_icon)
        sdv_icon.loadImage(item.tagIcon)

    }
}