package com.julun.huanque.core.ui.main.makefriend

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.HeaderNavigateBean
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.inVisiable
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R
import org.jetbrains.anko.backgroundResource

class HeaderNavAdapter : BaseQuickAdapter<HeaderNavigateBean, BaseViewHolder>(R.layout.item_item_mkf_header) {

    override fun convert(holder: BaseViewHolder?, item: HeaderNavigateBean?) {
        if (holder == null || item == null) {
            return
        }
        val container = holder.getView<View>(R.id.header_container)
        when (item.type) {
            HeaderNavigateBean.MASK_QUEEN -> {
                container.backgroundResource = R.mipmap.bg_header_mask
            }
            HeaderNavigateBean.ANONYMOUS_VOICE -> {
                container.backgroundResource = R.mipmap.bg_header_audio
            }
            HeaderNavigateBean.LIVING -> {
                container.backgroundResource = R.mipmap.bg_header_living
            }
            HeaderNavigateBean.DAY_TOP -> {
                container.backgroundResource = R.mipmap.bg_header_day_top
            }
        }
        val imgView = holder.getView<SimpleDraweeView>(R.id.sdv_user_head)
        if (item.url.isNotEmpty()) {
            imgView.show()
            imgView.loadImage(item.url, 60f, 60f)
        } else {
            imgView.inVisiable()
        }

        holder.setText(R.id.tv_title1, item.title1)
        holder.setText(R.id.tv_title2, item.title2)

    }
}
