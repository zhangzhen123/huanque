package com.julun.huanque.core.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.PhotoBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.interfaces.PhotoOnItemClick
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R

class DynamicPhotosAdapter : BaseQuickAdapter<PhotoBean, BaseViewHolder>(R.layout.item_dynamic_photo) {
    private var mOnItemClick: PhotoOnItemClick? = null
    fun setOnItemClickListener(listener: PhotoOnItemClick) {
        mOnItemClick = listener
    }

    override fun convert(holder: BaseViewHolder, item: PhotoBean) {
        val imgView = holder.getView<SimpleDraweeView>(R.id.sdv_photo)
        imgView.loadImage(item.url + BusiConstant.OSS_350, 85f, 85f)
//        imgView.hierarchy.actualImageScaleType = ScalingUtils.ScaleType.CENTER_CROP

        if (mOnItemClick != null) {
            holder.itemView.onClickNew {
                var position = holder.adapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return@onClickNew
                }
                position -= headerLayoutCount
                mOnItemClick?.onItemClick(this, position)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
//        logger("PhotosAdapter onCreateViewHolder viewType=$viewType")
        return super.onCreateViewHolder(parent, viewType)

    }

    var currentPosition = 0
    fun testPosition(position: Int) {
        logger("当前的使用位置=$position 上一次：$currentPosition")
        currentPosition = position
    }

    var totalList: MutableList<PhotoBean> = mutableListOf()


}
