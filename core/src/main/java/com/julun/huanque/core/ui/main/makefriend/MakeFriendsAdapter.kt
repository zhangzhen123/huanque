package com.julun.huanque.core.ui.main.makefriend

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.flexbox.FlexboxLayoutManager
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import org.jetbrains.anko.textColor

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
//                val list = arrayListOf<PhotoBean>()
//                repeat(holder.layoutPosition % 4) {
//                    list.add(PhotoBean("http://cdn.51lm.tv/lm/program/cover/053039203c24442ea99a852f7f60f69c.jpg"))
//                }
                val bean = item.content as HomeRecomItem
                val list = bean.coverPicList.map { PhotoBean(url = it) }
                val headPic = holder.getView<SimpleDraweeView>(R.id.header_pic)
                holder.setGone(R.id.living_fg,bean.living).setGone(R.id.living_tag,bean.living)

                headPic.loadImage(bean.headPic, 46f, 46f)
                val name=if(bean.nickname.length>5){
                    "${bean.nickname.substring(0,5)}…"
                }else{
                    bean.nickname
                }
                val authTag = holder.getView<SimpleDraweeView>(R.id.sd_auth_tag)
                if(bean.authMark.isNotEmpty()){
                    authTag.show()
                    ImageUtils.loadImageWithHeight_2(authTag,bean.authMark,dp2px(13))
                }else{
                    authTag.hide()
                }
                holder.setText(R.id.tv_mkf_name, name).setText(R.id.tv_mkf_sign, bean.mySign)
                    .setText(R.id.tv_location, bean.city)

                val sex = holder.getView<TextView>(R.id.tv_sex)
                sex.text = "${bean.age}"
                when (bean.sex) {//Male、Female、Unknow

                    Sex.FEMALE -> {
                        val drawable = ContextCompat.getDrawable(mContext, R.mipmap.icon_mkf_female)
                        if (drawable != null) {
                            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                            sex.setCompoundDrawables(drawable, null, null, null)
                        }
                        sex.textColor = Color.parseColor("#FF9BC5")
                    }
                    else -> {
                        val drawable = ContextCompat.getDrawable(mContext, R.mipmap.icon_mkf_male)
                        if (drawable != null) {
                            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                            sex.setCompoundDrawables(drawable, null, null, null)
                        }
                        sex.textColor = Color.parseColor("#9BE2FF")
                    }
                }
                when {
                    list.isNotEmpty() -> {
                        val rv = holder.getView<RecyclerView>(R.id.rv_photos)
                        rv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
                        holder.setGone(R.id.ll_audio, false).setGone(R.id.rv_tags, false)
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
                            mOnItemAdapterListener?.onPhotoClick(holder.layoutPosition, position, childItem)
                        }


                    }
                    bean.introduceVoice.isNotEmpty() -> {
                        holder.setGone(R.id.ll_audio, true).setGone(R.id.rv_photos, false).setGone(R.id.rv_tags, false)
                        holder.setText(R.id.tv_audio_time, "${bean.currentPlayProcess}s")
                        holder.addOnClickListener(R.id.iv_audio_play)
                        val play=holder.getView<ImageView>(R.id.iv_audio_play)
                        play.isActivated = bean.isPlay
                    }
                    bean.tagList.isNotEmpty() -> {
                        holder.setGone(R.id.ll_audio, false).setGone(R.id.rv_photos, false).setGone(R.id.rv_tags, true)
                        val tagRv = holder.getView<RecyclerView>(R.id.rv_tags)
                        tagRv.layoutManager = FlexboxLayoutManager(mContext)
                        val mTagAdapter: TagAdapter
                        if (tagRv.adapter != null) {
                            mTagAdapter = tagRv.adapter as TagAdapter
                        } else {
                            mTagAdapter = TagAdapter()
                            tagRv.adapter = mTagAdapter
                        }
                        mTagAdapter.replaceData(bean.tagList)

                    }
                }
                holder.addOnClickListener(R.id.btn_action)

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
                mHeaderNavAdapter.setOnItemClickListener { _, _, position ->
                    mOnItemAdapterListener?.onHeadClick(mHeaderNavAdapter.getItem(position))
                }
                holder.addOnClickListener(R.id.tv_go_make_money)
            }
            HomeItemBean.GUIDE_TO_ADD_TAG -> {
                holder.addOnClickListener(R.id.tv_btn_lift)
                holder.addOnClickListener(R.id.iv_guide_tag_close)
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
                    mPhotosAdapter.testPosition(holder.adapterPosition + 1000)
                } else {
                    mPhotosAdapter = PhotosAdapter()
                    rv.adapter = mPhotosAdapter
                }
                mPhotosAdapter.replaceData(list)
                holder.addOnClickListener(R.id.iv_guide_info_close)
            }
        }
    }

    interface OnItemAdapterListener {
        fun onPhotoClick(index: Int, position: Int, item: PhotoBean)
        fun onHeadClick(item: HeadModule?)
    }
}
