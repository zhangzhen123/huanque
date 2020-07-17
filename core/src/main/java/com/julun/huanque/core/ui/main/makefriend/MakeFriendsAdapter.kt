package com.julun.huanque.core.ui.main.makefriend

import android.graphics.Color
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.flexbox.FlexboxLayoutManager
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import org.jetbrains.anko.textColor

class MakeFriendsAdapter : BaseMultiItemQuickAdapter<HomeItemBean, BaseViewHolder>(null), LoadMoreModule {

    var mOnItemAdapterListener: OnItemAdapterListener? = null

    //对于图片子列表设置一个公共的缓存池 提高效率
    private val mPhotoViewPool: RecyclerView.RecycledViewPool by lazy {
        RecyclerView.RecycledViewPool()
    }

    init {
        addItemType(HomeItemBean.HEADER, R.layout.item_mkf_header)
        addItemType(HomeItemBean.NORMAL, R.layout.item_mkf_normal)
        addItemType(HomeItemBean.GUIDE_TO_ADD_TAG, R.layout.item_mkf_guide_to_add_tag)
        addItemType(HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION, R.layout.item_mkf_guide_to_complete_info)
        mPhotoViewPool.setMaxRecycledViews(0, 10)

        addChildClickViewIds(
            R.id.iv_audio_play, R.id.btn_action,
            R.id.tv_go_make_money, R.id.tv_btn_lift,
            R.id.iv_guide_tag_close, R.id.iv_guide_info_close
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        logger("onCreateViewHolder itemViewType:$viewType")
        val holder = super.onCreateViewHolder(parent, viewType)
        if (viewType == HomeItemBean.HEADER) {
            holder.getView<TextView>(R.id.tv_balance).setTFDinCdc2()
        } else if (viewType == HomeItemBean.NORMAL) {
            holder.getView<TextView>(R.id.tv_audio_time).setTFDinAltB()
        }
        return holder
    }

    override fun convert(holder: BaseViewHolder, item: HomeItemBean) {

        logger("itemViewType:" + holder.itemViewType)
        when (holder.itemViewType) {
            HomeItemBean.NORMAL -> {
                val bean = item.content as HomeRecomItem

                val rl = bean.coverPicList.map { PhotoBean(url = it) }.toMutableList()
                val list = if (rl.size > 4) {
                    rl.subList(0, 4)
                } else {
                    rl
                }
                val headPic = holder.getView<SimpleDraweeView>(R.id.header_pic)

                holder.setGone(R.id.living_fg, !bean.living)

                val livingTag = holder.getView<SimpleDraweeView>(R.id.living_tag)
                val authTag = holder.getView<SimpleDraweeView>(R.id.sd_auth_tag)

                if (bean.living) {
                    livingTag.show()
                    authTag.hide()

                } else {
                    livingTag.hide()
                    if (bean.authMark.isNotEmpty()) {
                        authTag.show()
                        ImageUtils.loadImageWithHeight_2(authTag, bean.authMark, dp2px(13))
                    } else {
                        authTag.hide()
                    }
                }


                headPic.loadImage(bean.headPic, 46f, 46f)
                val name = if (bean.nickname.length > 5) {
                    "${bean.nickname.substring(0, 5)}…"
                } else {
                    bean.nickname
                }

                holder.setText(R.id.tv_mkf_name, name).setText(R.id.tv_mkf_sign, bean.mySign)
                    .setText(R.id.tv_location, bean.city)

                val sex = holder.getView<TextView>(R.id.tv_sex)
                sex.text = "${bean.age}"
                when (bean.sex) {//Male、Female、Unknow

                    Sex.FEMALE -> {
                        val drawable = ContextCompat.getDrawable(context, R.mipmap.icon_sex_female)
                        if (drawable != null) {
                            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                            sex.setCompoundDrawables(drawable, null, null, null)
                        }
                        sex.textColor = Color.parseColor("#FF9BC5")
                    }
                    else -> {
                        val drawable = ContextCompat.getDrawable(context, R.mipmap.icon_sex_male)
                        if (drawable != null) {
                            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                            sex.setCompoundDrawables(drawable, null, null, null)
                        }
                        sex.textColor = Color.parseColor("#9BE2FF")
                    }
                }
                if (bean.anchor && bean.living) {
                    holder.setText(R.id.btn_action, "围观")
                } else {
                    holder.setText(R.id.btn_action, "私信")
                }
                when {
                    list.isNotEmpty() -> {
                        val rv = holder.getView<RecyclerView>(R.id.rv_photos)
                        rv.setRecycledViewPool(mPhotoViewPool)
                        rv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                        holder.setGone(R.id.ll_audio, true).setGone(R.id.rv_tags, true)
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
                        mPhotosAdapter.setList(list)
                        mPhotosAdapter.setOnItemClickListener { _, _, position ->
                            mOnItemAdapterListener?.onPhotoClick(holder.layoutPosition, position, rl)
                        }


                    }
                    bean.introduceVoice.isNotEmpty() -> {
                        holder.setGone(R.id.ll_audio, false).setGone(R.id.rv_photos, true).setGone(R.id.rv_tags, true)
                        holder.setText(R.id.tv_audio_time, "${bean.currentPlayProcess}”")
//                        holder.addOnClickListener(R.id.iv_audio_play)
                        val play = holder.getView<ImageView>(R.id.iv_audio_play)
                        play.isActivated = bean.isPlay
                        if (bean.isPlay) {
                            holder.itemView.setTag(R.id.play_tag_key, ParamConstant.IS_AUDIO_PLAY)
                        } else {
                            holder.itemView.setTag(R.id.play_tag_key, null)
                        }
                    }
                    bean.tagList.isNotEmpty() -> {
                        holder.setGone(R.id.ll_audio, true).setGone(R.id.rv_photos, true).setGone(R.id.rv_tags, false)
                        val tagRv = holder.getView<RecyclerView>(R.id.rv_tags)
                        tagRv.layoutManager = FlexboxLayoutManager(context)
                        val mTagAdapter: TagAdapter
                        if (tagRv.adapter != null) {
                            mTagAdapter = tagRv.adapter as TagAdapter
                        } else {
                            mTagAdapter = TagAdapter()
                            tagRv.adapter = mTagAdapter
                        }
                        mTagAdapter.setList(bean.tagList)

                    }
                    else -> {
                        holder.setGone(R.id.ll_audio, true).setGone(R.id.rv_photos, true).setGone(R.id.rv_tags, true)
                    }
                }
//                holder.addOnClickListener(R.id.btn_action)

            }
            HomeItemBean.HEADER -> {
                val headerInfo = item.content as? HeadNavigateInfo ?: return
                val rv = holder.getView<RecyclerView>(R.id.header_recyclerView)
                val tvBalance = holder.getView<TextView>(R.id.tv_balance)
                tvBalance.text = "${headerInfo.myCash}"

                rv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                if (rv.itemDecorationCount <= 0) {
                    rv.addItemDecoration(HorizontalItemDecoration(dp2px(10)))
                }
                val mHeaderNavAdapter: HeaderNavAdapter
                if (rv.adapter != null) {
                    mHeaderNavAdapter = rv.adapter as HeaderNavAdapter
                } else {
                    mHeaderNavAdapter = HeaderNavAdapter()
                    rv.adapter = mHeaderNavAdapter
                }

                mHeaderNavAdapter.setList(headerInfo.moduleList)
                mHeaderNavAdapter.setOnItemClickListener { _, _, position ->
                    mOnItemAdapterListener?.onHeadClick(mHeaderNavAdapter.getItem(position))
                }
//                holder.addOnClickListener(R.id.tv_go_make_money)
            }
            HomeItemBean.GUIDE_TO_ADD_TAG -> {
//                holder.addOnClickListener(R.id.tv_btn_lift)
//                holder.addOnClickListener(R.id.iv_guide_tag_close)
            }
            HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION -> {
                val bean = item.content as? CoverRemind ?: return
                val rv = holder.getView<RecyclerView>(R.id.rv_add_photos)
                val logo = holder.getView<SimpleDraweeView>(R.id.sdv_logo)
                val name = holder.getView<TextView>(R.id.tv_name)
                logo.loadImage(bean.headPic, 30f, 30f)
                name.text = bean.nickname
                rv.setRecycledViewPool(mPhotoViewPool)
                rv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

                val list = arrayListOf<PhotoBean>()
                repeat(4) {
                    val url = bean.picList.getOrNull(it)
                    if (url != null) {
                        list.add(PhotoBean(url = url))
                    } else {
                        list.add(PhotoBean(res = R.mipmap.icon_upload_image))
                    }

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
                mPhotosAdapter.setList(list)
//                holder.addOnClickListener(R.id.iv_guide_info_close)
            }
        }
    }

    interface OnItemAdapterListener {
        fun onPhotoClick(
            index: Int,
            position: Int,
            list: MutableList<PhotoBean>
        )

        fun onHeadClick(item: HeadModule?)
    }
}
