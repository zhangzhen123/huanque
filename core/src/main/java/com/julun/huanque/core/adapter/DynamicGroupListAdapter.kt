package com.julun.huanque.core.adapter


import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.DynamicGroup
import com.julun.huanque.common.bean.beans.DynamicItemBean
import com.julun.huanque.common.bean.beans.PhotoBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.interfaces.PhotoOnItemClick
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/23 10:04
 *
 *@Description: DynamicListAdapter 通用的动态列表 很多地方要复用
 *
 *
 */
class DynamicGroupListAdapter : BaseQuickAdapter<DynamicGroup, BaseViewHolder>(R.layout.item_dynamic_group) {

    var showType: Int = -1
    override fun convert(holder: BaseViewHolder, item: DynamicGroup) {

        val pic = holder.getView<SimpleDraweeView>(R.id.sdv_pic)
        pic.loadImage(item.groupPic, 80f, 60f)
        val name = if (item.groupName.length > 6) {
            item.groupName.substring(0, 6)
        } else {
            item.groupName
        }
        val heat = holder.getView<TextView>(R.id.tv_heat)
        if (showType == DynamicListAdapter.HOME_RECOM) {
            heat.show()
            val drawable = ContextCompat.getDrawable(context, R.mipmap.icon_circle_hot_value)
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                heat.setCompoundDrawables(drawable, null, null, null)
            }
            heat.text = "${StringHelper.formatNum(item.heatValue)}"
        } else {
            heat.setCompoundDrawables(null, null, null, null)
            if(item.hasNewPost){
                heat.show()
                heat.text = "有新动态"
            }else{
                heat.inVisible()
            }

        }
        holder.setText(R.id.tv_title, name)


    }
}
