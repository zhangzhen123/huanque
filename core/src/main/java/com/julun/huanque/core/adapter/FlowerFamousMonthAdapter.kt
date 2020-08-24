package com.julun.huanque.core.adapter

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.FamousUser
import com.julun.huanque.common.bean.beans.SingleFamousMonth
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/8/24 16:58
 *@描述 名人榜  每月的Adapter
 */
class FlowerFamousMonthAdapter : BaseQuickAdapter<SingleFamousMonth, BaseViewHolder>(R.layout.recycler_item_famous_month) {
    override fun convert(holder: BaseViewHolder, item: SingleFamousMonth) {
        val tvMonth = holder.getView<TextView>(R.id.tv_month)
        val month = item.month
        if (month >= 10) {
            tvMonth.text = "$month"
        } else {
            tvMonth.text = "0$month"
        }
        tvMonth.setTFDINCondensedBold()
        val recyclerViewInner = holder.getView<RecyclerView>(R.id.recyclerView_inner)
        if (recyclerViewInner.adapter == null) {
            //未设置过Adapter
            recyclerViewInner.layoutManager = GridLayoutManager(context, 3)
            val headeView = LayoutInflater.from(context).inflate(R.layout.view_famous_header, null)

            recyclerViewInner.adapter = FlowerFamousUserAdapter().apply {
                if (headeView != null) {
                    addHeaderView(headeView)
                }
            }
        }

        val adapter = recyclerViewInner.adapter as? FlowerFamousUserAdapter ?: return
        val headerLayout = adapter.headerLayout ?: return
        val dataList = item.userList

        if (headerLayout.childCount > 0) {
            val headerView = headerLayout.getChildAt(0)
            //第一个视图
            val sdv_first = headerView.findViewById<SimpleDraweeView>(R.id.sdv_first)
            val view_shader_first = headerView.findViewById<View>(R.id.view_shader_first)
            val tv_nickname_first = headerView.findViewById<TextView>(R.id.tv_nickname_first)
            val tv_day_first = headerView.findViewById<TextView>(R.id.tv_day_first)

            if (ForceUtils.isIndexNotOutOfBounds(0, dataList)) {
                val firstData = dataList[0]
                sdv_first.show()
                view_shader_first.show()
                tv_nickname_first.show()
                tv_day_first.show()
                sdv_first.loadImage(firstData.headPic, 200f, 200f)
                tv_nickname_first.text = firstData.nickname
                tv_day_first.text = getDayContent(firstData.day)
            } else {
                sdv_first.hide()
                view_shader_first.hide()
                tv_nickname_first.hide()
                tv_day_first.hide()
            }

            //第二个视图
            val sdv_second = headerView.findViewById<SimpleDraweeView>(R.id.sdv_second)
            val view_shader_second = headerView.findViewById<View>(R.id.view_shader_second)
            val tv_nickname_second = headerView.findViewById<TextView>(R.id.tv_nickname_second)
            val tv_day_second = headerView.findViewById<TextView>(R.id.tv_day_second)
            if (ForceUtils.isIndexNotOutOfBounds(1, dataList)) {
                val secondData = dataList[1]
                sdv_second.show()
                view_shader_second.show()
                tv_nickname_second.show()
                tv_day_second.show()
                sdv_second.loadImage(secondData.headPic, 95f, 95f)
                tv_nickname_second.text = secondData.nickname
                tv_day_second.text = getDayContent(secondData.day)
            } else {
                sdv_second.hide()
                view_shader_second.hide()
                tv_nickname_second.hide()
                tv_day_second.hide()
            }

            //第三个视图
            val sdv_third = headerView.findViewById<SimpleDraweeView>(R.id.sdv_third)
            val view_shader_third = headerView.findViewById<View>(R.id.view_shader_third)
            val tv_nickname_third = headerView.findViewById<TextView>(R.id.tv_nickname_third)
            val tv_day_third = headerView.findViewById<TextView>(R.id.tv_day_third)
            if (ForceUtils.isIndexNotOutOfBounds(2, dataList)) {
                val thirdData = dataList[2]
                sdv_third.show()
                view_shader_third.show()
                tv_nickname_third.show()
                tv_day_third.show()
                sdv_third.loadImage(thirdData.headPic, 95f, 95f)
                tv_nickname_third.text = thirdData.nickname
                tv_day_third.text = getDayContent(thirdData.day)
            } else {
                sdv_third.hide()
                view_shader_third.hide()
                tv_nickname_third.hide()
                tv_day_third.hide()
            }
        }

        val adapterData = mutableListOf<FamousUser>()
        dataList.forEachIndexed { index, data ->
            if (index >= 3) {
                adapterData.add(data)
            }
        }
        adapter.setList(adapterData)

        //指示线的适配
        val position = holder.adapterPosition
        val view_line = holder.getView<View>(R.id.view_line)
        val lineParams = view_line.layoutParams as? ConstraintLayout.LayoutParams
        if (position == 0) {
            //第一个视图
            lineParams?.topMargin = dp2px(10)
        } else {
            //不是第一个视图
            lineParams?.topMargin = 0
        }

        val iv_circle_bottom = holder.getView<View>(R.id.iv_circle_bottom)
        if (itemCount == position + 1) {
            //最后一个视图
            lineParams?.bottomMargin = dp2px(10)
            lineParams?.bottomToBottom = R.id.iv_circle_bottom
            iv_circle_bottom.show()
        } else {
            //不是最后一个视图
            lineParams?.bottomMargin = 0
            lineParams?.bottomToBottom = R.id.recyclerView_inner
            iv_circle_bottom.hide()
        }
        view_line.layoutParams = lineParams
    }

    private fun getDayContent(day: Int): String {
        return if (day >= 10) {
            "$day"
        } else {
            "0${day}"
        }
    }


}