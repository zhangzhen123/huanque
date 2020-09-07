package com.julun.huanque.core.ui.main.makefriend

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.flexbox.FlexboxLayoutManager
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor
import kotlin.math.ceil

class MakeFriendsAdapter : BaseMultiItemQuickAdapter<HomeItemBean, BaseViewHolder>(null), LoadMoreModule,
    PhotosAdapter.OnItemClick {

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
        mPhotoViewPool.setMaxRecycledViews(0, 20)

        addChildClickViewIds(
            R.id.iv_audio_play, R.id.btn_action,
            R.id.ll_task, R.id.ll_balance,
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
            ImageUtils.loadGifImageLocal(holder.getView<SimpleDraweeView>(R.id.living_tag), R.mipmap.anim_living)
        }
        return holder
    }

    override fun convert(holder: BaseViewHolder, item: HomeItemBean) {

//        logger("itemViewType:" + holder.itemViewType)
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
                ImageHelper.setDefaultHeaderPic(headPic, bean.sex)

                headPic.loadImage(bean.headPic + BusiConstant.OSS_160, 66f, 66f)
                val name = if (bean.nickname.length > 5) {
                    "${bean.nickname.substring(0, 5)}…"
                } else {
                    bean.nickname
                }
                val sign = if (bean.mySign.length > 15) {
                    "${bean.mySign.substring(0, 15)}…"
                } else {
                    bean.mySign
                }
                holder.setText(R.id.tv_mkf_name, name).setText(R.id.tv_mkf_sign, sign)
                    .setText(R.id.tv_location, bean.city)
                if (bean.city.isEmpty()) {
                    holder.setGone(R.id.tv_location, true)
                } else {
                    holder.setGone(R.id.tv_location, false)
                }
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
                        sex.backgroundResource = R.drawable.bg_shape_mkf_sex_female
                    }
                    else -> {
                        val drawable = ContextCompat.getDrawable(context, R.mipmap.icon_sex_male)
                        if (drawable != null) {
                            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                            sex.setCompoundDrawables(drawable, null, null, null)
                        }
                        sex.textColor = Color.parseColor("#58CEFF")
                        sex.backgroundResource = R.drawable.bg_shape_mkf_sex_male
                    }
                }
                val btn_action = holder.getView<TextView>(R.id.btn_action)
                if (bean.anchor && bean.living) {
                    btn_action.text = "围观"
                    btn_action.textColor = GlobalUtils.formatColor("#FF8E8E")
                    btn_action.backgroundResource = R.drawable.bg_home_onlooker
                } else {
                    btn_action.text = "私信"
                    btn_action.textColor = GlobalUtils.formatColor("#FFCC00")
                    btn_action.backgroundResource = R.drawable.bg_stroke_btn1
                }
                when {
                    list.isNotEmpty() -> {
                        val rv = holder.getView<RecyclerView>(R.id.rv_photos)
                        rv.setRecycledViewPool(mPhotoViewPool)
                        rv.setHasFixedSize(true)
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
                        mPhotosAdapter.currentPosition = holder.layoutPosition
                        mPhotosAdapter.totalList = rl
                        mPhotosAdapter.setOnItemClickListener(this)
//                        mPhotosAdapter.setOnItemClickListener { adapter, _, position ->
//                            val adp=adapter as PhotosAdapter
//                            mOnItemAdapterListener?.onPhotoClick(adp.currentPosition, position, adp.totalList)
//                        }


                    }
                    bean.introduceVoice.isNotEmpty() -> {

                        holder.setGone(R.id.ll_audio, false).setGone(R.id.rv_photos, true).setGone(R.id.rv_tags, true)
                        holder.setText(R.id.tv_audio_time, "${bean.currentPlayProcess}”")
//                        holder.addOnClickListener(R.id.iv_audio_play)
                        val play = holder.getView<ImageView>(R.id.iv_audio_play)
                        play.isActivated = bean.isPlay
                        if (bean.isPlay) {
                            if (holder.itemView.getTag(R.id.play_tag_key) != ParamConstant.IS_AUDIO_PLAY) {
                                ImageUtils.loadGifImageLocal(holder.getView(R.id.sdv_audio_state), R.mipmap.anim_audio_playing)
                            }
                            holder.itemView.setTag(R.id.play_tag_key, ParamConstant.IS_AUDIO_PLAY)
                        } else {
                            holder.itemView.setTag(R.id.play_tag_key, null)
                            ImageUtils.loadImageLocal(holder.getView(R.id.sdv_audio_state), R.mipmap.anim_audio_playing)
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
                val tvTask = holder.getView<TextView>(R.id.tv_task)

                tvBalance.text = headerInfo.taskBar.myCash
                val content = StringBuilder()
                content.append("${headerInfo.taskBar.label}：")
                val start = content.length//记录开始位置
                content.append(headerInfo.taskBar.desc)
                val styleSpan1A = StyleSpan(Typeface.BOLD)
//                val end = content.length
                val sp = SpannableString(content)
                sp.setSpan(styleSpan1A, 0, start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                tvTask.text = sp


                rv.layoutManager = GridLayoutManager(context, 3)
//                if (rv.itemDecorationCount <= 0) {
//                    rv.addItemDecoration(HorizontalItemDecoration(dp2px(10)))
//                }
                val mHeaderNavAdapter: HeaderNavAdapter
                if (rv.adapter != null) {
                    mHeaderNavAdapter = rv.adapter as HeaderNavAdapter
                } else {
                    //动态计算的宽高
                    //图片宽度
                    val singlePicWidth = (ScreenUtils.getScreenWidth() - dp2px(48)) / 3
                    val tempHeight = ceil(singlePicWidth * 210 / 327.0 + dp2px(8)).toInt()

                    val rvParams = rv.layoutParams
                    rvParams.height = tempHeight
                    rv.layoutParams = rvParams

                    mHeaderNavAdapter = HeaderNavAdapter(singlePicWidth)
                    rv.adapter = mHeaderNavAdapter
                }

                mHeaderNavAdapter.setList(headerInfo.moduleList)
                mHeaderNavAdapter.onAdapterClickNew { _, _, position ->
                    mOnItemAdapterListener?.onHeadClick(mHeaderNavAdapter.getItem(position))
                }
//                holder.addOnClickListener(R.id.tv_go_make_money)
            }
            HomeItemBean.GUIDE_TO_ADD_TAG -> {
//                holder.addOnClickListener(R.id.tv_btn_lift)
//                holder.addOnClickListener(R.id.iv_guide_tag_close)
            }
            HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION -> {
                val bean = item.content as? HomeRemind ?: return
                val rv = holder.getView<RecyclerView>(R.id.rv_add_photos)
                val logo = holder.getView<SimpleDraweeView>(R.id.sdv_logo)
                val tvName = holder.getView<TextView>(R.id.tv_name)
                logo.loadImage(SessionUtils.getHeaderPic(), 30f, 30f)

                val name = if (SessionUtils.getNickName().length > 5) {
                    "${SessionUtils.getNickName().substring(0, 5)}…"
                } else {
                    SessionUtils.getNickName()
                }
                tvName.text = name
//                rv.setRecycledViewPool(mPhotoViewPool)
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
                    rv.addItemDecoration(HorizontalItemDecoration(dp2px(15)))
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

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, position: Int) {
        logger("点击了子adp position=$position")
        val adp = adapter as PhotosAdapter
        mOnItemAdapterListener?.onPhotoClick(adp.currentPosition, position, adp.totalList)
    }
}
