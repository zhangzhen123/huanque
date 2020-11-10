package com.julun.huanque.core.ui.main.makefriend

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.PhotoBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.loadImageLocal
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.core.R

class PhotosAdapter : BaseQuickAdapter<PhotoBean, BaseViewHolder>(R.layout.item_photo) {
    private var mOnItemClick: OnItemClick? = null
    fun setOnItemClickListener(listener: OnItemClick) {
        mOnItemClick = listener
    }

    override fun convert(holder: BaseViewHolder, item: PhotoBean) {
        val imgView = holder.getView<SimpleDraweeView>(R.id.sdv_photo)
        if (item.url.isNotEmpty()) {
            imgView.loadImage(item.url + BusiConstant.OSS_120, 60f, 60f)
            imgView.hierarchy.actualImageScaleType = ScalingUtils.ScaleType.CENTER_CROP
        } else {
            imgView.hierarchy.actualImageScaleType = ScalingUtils.ScaleType.FIT_XY
            imgView.loadImageLocal(item.res)
        }
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
        if (living && holder.adapterPosition == 0) {
            holder.setVisible(R.id.sdv_right_bottom_tag, true)
            ImageUtils.loadGifImageLocal(holder.getView<SimpleDraweeView>(R.id.sdv_right_bottom_tag), R.mipmap.anim_living)
        } else {
            holder.setGone(R.id.sdv_right_bottom_tag, true)
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

    //是否在直播中的标识
    var living: Boolean = false

    //由于BaseQuickAdapter的点击事件只会在onCreateViewHolder绑定一次 对于复用池的viewHolder这里就会出问题 所以自定义一个item点击事件每次cover重新绑定
    interface OnItemClick {
        fun onItemClick(adapter: BaseQuickAdapter<*, *>, position: Int)
    }


}
