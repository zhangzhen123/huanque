package com.julun.huanque.message.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.BaseAdapter
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.SingleHeartBean
import com.julun.huanque.common.bean.beans.WatchHistoryBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.message.R
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor

/**
 *@创建者   dong
 *@创建时间 2021/1/18 20:28
 *@描述 访问历史Adapter
 */
class HeartBeatAdapter : BaseQuickAdapter<SingleHeartBean, BaseViewHolder>(R.layout.recycler_item_header_beat), LoadMoreModule {

    override fun convert(holder: BaseViewHolder, item: SingleHeartBean) {
        val sdv = holder.getView<SimpleDraweeView>(R.id.sdv)
        if (item.unLock == BusiConstant.True) {
            //解锁
//            sdv.loadImage()
        } else {
            //未解锁

        }


    }
}