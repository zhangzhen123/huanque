package com.julun.huanque.core.ui.main.makefriend

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.PhotoBean
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.loadImageLocal
import com.julun.huanque.common.suger.logger
import com.julun.huanque.core.R

class PhotosAdapter : BaseQuickAdapter<PhotoBean, BaseViewHolder>(R.layout.item_photo) {

    override fun convert(holder: BaseViewHolder?, item: PhotoBean?) {
        if (holder == null || item == null) {
            return
        }
        val imgView=holder.getView<SimpleDraweeView>(R.id.sdv_photo)
        if(item.url.isNotEmpty()){
            imgView.loadImage(item.url,60f,60f)
        }else{
            imgView.loadImageLocal(item.res)
        }


    }
    var last=0
    fun testPosition(position:Int){
        logger("当前的使用位置=$position 上一次：$last" )
        last=position
    }
}
