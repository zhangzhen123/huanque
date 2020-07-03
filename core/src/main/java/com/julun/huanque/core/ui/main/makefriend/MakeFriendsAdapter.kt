package com.julun.huanque.core.ui.main.makefriend

import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.julun.huanque.common.bean.beans.HeadNavigateInfo
import com.julun.huanque.common.bean.beans.HeaderNavigateBean
import com.julun.huanque.common.bean.beans.HomeItemBean
import com.julun.huanque.common.bean.beans.PhotoBean
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R

class MakeFriendsAdapter : BaseMultiItemQuickAdapter<HomeItemBean, BaseViewHolder>(null) {

    var mOnItemAdapterListener: OnItemAdapterListener? = null

    init {
        addItemType(HomeItemBean.HEADER, R.layout.item_mkf_header)
        addItemType(HomeItemBean.NORMAL, R.layout.item_mkf_normal)
        addItemType(HomeItemBean.GUIDE_TO_ADD_TAG, R.layout.item_mkf_guide_to_add_tag)
        addItemType(HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION, R.layout.item_mkf_guide_to_complete_info)
    }

    override fun convert(holder: BaseViewHolder?, item: HomeItemBean?) {
        if (holder == null || item == null) {
            return
        }
        logger("itemViewType:" + holder.itemViewType)
        when (holder.itemViewType) {
            HomeItemBean.NORMAL -> {

                //todo
                val list = arrayListOf<PhotoBean>()
                repeat(holder.layoutPosition % 4) {
                    list.add(PhotoBean("http://cdn.51lm.tv/lm/program/cover/053039203c24442ea99a852f7f60f69c.jpg"))
                }
                if (list == null || list.isEmpty()) {
                    holder.setGone(R.id.ll_audio, true).setGone(R.id.rv_photos, false)
                } else {
                    val rv = holder.getView<RecyclerView>(R.id.rv_photos)
                    rv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
                    holder.setGone(R.id.ll_audio, false)
                    rv.show()
                    if (rv.itemDecorationCount <= 0) {
                        rv.addItemDecoration(HorizontalItemDecoration(dp2px(5)))
                    }
                    val mPhotosAdapter: PhotosAdapter
                    if (rv.adapter != null) {
                        mPhotosAdapter = rv.adapter as PhotosAdapter
                        mPhotosAdapter.testPosition(holder.adapterPosition)
                    } else {
                        mPhotosAdapter = PhotosAdapter()
                        rv.adapter = mPhotosAdapter
                    }
                    mPhotosAdapter.replaceData(list)
                    mPhotosAdapter.setOnItemClickListener { _, _, position ->
                        val childItem = mPhotosAdapter.getItem(position) ?: return@setOnItemClickListener
                        mOnItemAdapterListener?.onListClick(holder.layoutPosition, position, childItem)
                    }

                }

            }
            HomeItemBean.HEADER -> {
                val headerInfo = item.content as? HeadNavigateInfo ?: return
                val rv = holder.getView<RecyclerView>(R.id.header_recyclerView)
                val tvBalance = holder.getView<TextView>(R.id.tv_balance)
                tvBalance.text = "${headerInfo.myCash}"

                rv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
                if (rv.itemDecorationCount <= 0) {
                    rv.addItemDecoration(HorizontalItemDecoration(dp2px(10)))
                }
//                val list = arrayListOf<HeaderNavigateBean>()
//
//                list.add(HeaderNavigateBean().apply {
//                    this.title1 = "蒙面女王"
//                    this.title2 = "今日剩余${3}次"
//                    this.type = HeaderNavigateBean.MASK_QUEEN
//                })
//                list.add(HeaderNavigateBean().apply {
//                    this.title1 = "匿名语音"
//                    this.title2 = "${11112}人参与"
//                    this.type = HeaderNavigateBean.ANONYMOUS_VOICE
//                })
//                list.add(HeaderNavigateBean().apply {
//                    this.title1 = "热门直播"
//                    this.title2 = "&#128293"
//                    this.type = HeaderNavigateBean.LIVING
//                })
//                list.add(HeaderNavigateBean().apply {
//                    this.title1 = "今日花魁"
//                    this.title2 = "昵称"
//                    this.type = HeaderNavigateBean.DAY_TOP
//                    this.url = "http://cdn.51lm.tv/lm/program/cover/053039203c24442ea99a852f7f60f69c.jpg"
//                })
                val mHeaderNavAdapter: HeaderNavAdapter
                if (rv.adapter != null) {
                    mHeaderNavAdapter = rv.adapter as HeaderNavAdapter
                } else {
                    mHeaderNavAdapter = HeaderNavAdapter()
                    rv.adapter = mHeaderNavAdapter
                }

                mHeaderNavAdapter.replaceData(headerInfo.moduleList)
            }
            HomeItemBean.GUIDE_TO_ADD_TAG -> {
            }
            HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION -> {

                val rv = holder.getView<RecyclerView>(R.id.rv_add_photos)
                rv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
                //todo
                val list = arrayListOf<PhotoBean>()
                repeat(4) {
                    list.add(PhotoBean(res = R.mipmap.icon_upload_photo))
                }
                if (rv.itemDecorationCount <= 0) {
                    rv.addItemDecoration(HorizontalItemDecoration(dp2px(10)))
                }
                val mPhotosAdapter: PhotosAdapter
                if (rv.adapter != null) {
                    mPhotosAdapter = rv.adapter as PhotosAdapter
                    mPhotosAdapter.testPosition(holder.adapterPosition+1000)
                } else {
                    mPhotosAdapter = PhotosAdapter()
                    rv.adapter = mPhotosAdapter
                }
                mPhotosAdapter.replaceData(list)
            }
        }
    }

    interface OnItemAdapterListener {
        fun onListClick(index: Int, position: Int, item: PhotoBean)
    }
}
