package com.julun.huanque.core.adapter

import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.AwardGood
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.core.R

/**
 * Created by dong on 2018/7/5.
 */
class EggResultItemAdapter : BaseQuickAdapter<AwardGood, BaseViewHolder>(R.layout.item_egg_result) {

    private var scaleRate = 0f
    override fun convert(helper: BaseViewHolder, item: AwardGood) {
        val tv_name = helper.getView<TextView>(R.id.tv_gift_name)
        val tv_num = helper.getView<TextView>(R.id.tv_gift_num)
        val constraintLayout = helper.getView<View>(R.id.constraintLayout)
        val sdv_gift = helper.getView<SimpleDraweeView>(R.id.sdv_gift)

        tv_name.show()
        tv_num.show()
        ImageUtils.loadImage(sdv_gift, item.pic, 46f, 46f)
        tv_name.text = item.prizeName
        tv_num.text = "x${item.count}"


    }
}