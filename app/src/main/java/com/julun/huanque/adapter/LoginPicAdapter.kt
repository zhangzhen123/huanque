package com.julun.huanque.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.R
import com.julun.huanque.common.bean.beans.ChooseTagBean
import com.julun.huanque.common.bean.beans.LoginPicBean
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils

/**
 *@创建者   dong
 *@创建时间 2020/12/17 14:48
 *@描述
 */
class LoginPicAdapter() : BaseQuickAdapter<LoginPicBean, BaseViewHolder>(R.layout.recycler_item_login_pic) {
    val viewWidth = ScreenUtils.getScreenWidth() / 2
    val normalHeight = viewWidth * 500 /376
    val smallHeight = normalHeight / 3
    override fun convert(holder: BaseViewHolder, item: LoginPicBean) {
        val params = holder.itemView.layoutParams
        val position = holder.layoutPosition
        if (position == 0) {
            params.height = smallHeight
        } else {
            params.height = normalHeight
        }
        holder.itemView.layoutParams = params
        val headImage = holder.getView<SimpleDraweeView>(R.id.headImage)
        ImageUtils.loadImageLocal(headImage, item.resourceId)
    }


    override fun getItemCount(): Int {
        if (data.size == 0) {
            return 0
        } else {
            return Int.MAX_VALUE
        }
    }

    override fun getItem(position: Int): LoginPicBean {
        val count = data.size
        return data[position % count]
    }

    override fun getItemViewType(position: Int): Int {
        var count = headerLayoutCount + data.size
        if (data.size == 0) {
            //没有设置数据，就使用默认的值
            return super.getItemViewType(position)
        }
        //刚开始进入包含该类的activity时,count为0。就会出现0%0的情况，这会抛出异常，所以我们要在下面做一下判断
        if (count <= 0) {
            count = 1
        }
        val newPosition = position % count
        return super.getItemViewType(newPosition)
    }
}