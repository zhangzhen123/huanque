package com.julun.huanque.core.adapter

import android.view.ViewGroup
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.SingleFlowerDayListBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.setTFDINCondensedBold
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.core.R

/**
 *@创建者   dong
 *@创建时间 2020/8/22 20:15
 *@描述 昨日榜，今日榜使用的Adapter
 */
class PlumFlowerListAdapter : BaseQuickAdapter<SingleFlowerDayListBean, BaseViewHolder>(R.layout.recycler_item_plum_flower_list) {
    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        val holder = super.onCreateDefViewHolder(parent, viewType)
        val img = holder.getView<SimpleDraweeView>(R.id.sdv_living)
        ImageUtils.loadGifImageLocal(img, R.mipmap.gif_is_living01)
        return holder
    }

    override fun convert(holder: BaseViewHolder, item: SingleFlowerDayListBean) {
        val tvNum = holder.getView<TextView>(R.id.tv_num)
        tvNum.text = item.ranking
        tvNum.setTFDINCondensedBold()

        holder.setText(R.id.tv_nickname, item.nickname)
            .setText(R.id.tv_score, StringHelper.formatNumber(item.score))

        val header = holder.getView<SimpleDraweeView>(R.id.sdv_header)
        ImageHelper.setDefaultHeaderPic(header, Sex.FEMALE)
        header.loadImage("${item.headPic}${BusiConstant.OSS_160}", 46f, 46f)

        if (item.living == BusiConstant.True) {
            holder.setVisible(R.id.fl_living, true)
        } else {
            holder.setGone(R.id.fl_living, true)
        }
    }
}