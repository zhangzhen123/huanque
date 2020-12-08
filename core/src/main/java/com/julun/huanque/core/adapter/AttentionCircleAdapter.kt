package com.julun.huanque.core.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.CircleGroup
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.CircleGroupTabType
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/11/24 10:37
 *@描述 圈子  关注模块 使用的Adapter
 */
class AttentionCircleAdapter : BaseDelegateMultiAdapter<Any, BaseViewHolder>(), LoadMoreModule {
    companion object {
        //圈子
        const val Circle = 0x001

        //我的圈子空状态
        const val Circle_Mine_Attention_Empty = 0x002

        //推荐圈子的标题
        const val Title_Recommend_Circle = 0x003

//        //推荐圈子
//        const val Circle_Recommend = 0x004
    }

    var hideAction = false

    init {

        setMultiTypeDelegate(object : BaseMultiTypeDelegate<Any>() {
            override fun getItemType(data: List<Any>, position: Int): Int {
                return when (data.getOrNull(position)) {
                    is CircleGroup -> {
                        Circle
                    }
                    is String -> {
                        Circle_Mine_Attention_Empty
                    }
                    else -> {
                        Title_Recommend_Circle
                    }
                }
            }
        })
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()?.addItemType(Circle, R.layout.recycler_item_circle)
            ?.addItemType(Circle_Mine_Attention_Empty, R.layout.empty_circle_mine_attention)
            ?.addItemType(Title_Recommend_Circle, R.layout.recycler_item_recommend_title)


        addChildClickViewIds(R.id.tv_action)
    }

    override fun convert(holder: BaseViewHolder, item: Any) {
        when (holder.itemViewType) {
            Circle -> {
                //我的圈子
                if (item is CircleGroup) {
                    val sdv_header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
                    sdv_header.loadImage("${item.groupPic}${BusiConstant.OSS_160}", 56f, 56f)
                    val tv_action = holder.getView<TextView>(R.id.tv_action)
                    if (item.type == CircleGroupTabType.Recom) {
                        //推荐数据
                        if (item.joined == BusiConstant.True) {
                            //已经加入，显示退出样式
                            tv_action.isSelected = true
                            tv_action.text = "退出"
                        } else {
                            //显示加入样式
                            tv_action.isSelected = false
                            tv_action.text = "加入"
                        }
                    }
                    holder.setText(R.id.tv_circle_name, item.groupName)
                        .setText(R.id.tv_circle_introduction, item.groupDesc)
                        .setText(R.id.tv_hot_value, StringHelper.formatNum(item.heatValue))
                        .setVisible(R.id.tv_action, !hideAction && item.type == CircleGroupTabType.Recom)
                }

            }
            Circle_Mine_Attention_Empty -> {
                //我的圈子为空
                holder.setText(R.id.tv_attetnion_content, "$item")
            }
            Title_Recommend_Circle -> {
                //推荐圈子的标题

            }
        }


    }
}