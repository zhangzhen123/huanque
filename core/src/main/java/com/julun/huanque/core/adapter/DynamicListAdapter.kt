package com.julun.huanque.core.adapter


import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.DynamicItemBean
import com.julun.huanque.common.bean.beans.PhotoBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.interfaces.PhotoOnItemClick
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/23 10:04
 *
 *@Description: DynamicListAdapter 通用的动态列表 很多地方要复用
 *
 *
 */
class DynamicListAdapter : BaseQuickAdapter<DynamicItemBean, BaseViewHolder>(R.layout.item_dynamic_list),
    LoadMoreModule, PhotoOnItemClick {

    companion object {
        //dp
        val SINGLE_PHOTO_MAX_WIDTH = dp2px(300)
        val SINGLE_PHOTO_MAX_HEIGHT = dp2px(300)
        val SINGLE_PHOTO_DEFAULT = dp2px(200)
        val SINGLE_PHOTO_MINI_SIZE = dp2px(150)
        val space = dp2px(5)

        const val HOME_FOLLOW = 0
        const val HOME_RECOM = 1
        const val POST_LIST_OTHER = 2 //他人列表
        const val POST_LIST_ME = 3//我的动态列表

        /**
         * 4个方格模式的宽度
         */
        val Width_4: Int by lazy {
            ((ScreenUtils.screenWidthFloat - dp2px(20) - space * 2) / 3 * 2 + space).toInt()
        }
    }

    var mOnItemAdapterListener: OnItemAdapterListener? = null

    //对于图片子列表设置一个公共的缓存池 提高效率
    private val mPhotoViewPool: RecyclerView.RecycledViewPool by lazy {
        RecyclerView.RecycledViewPool()
    }

    var showType: Int = 0

    //记录当前的用户自己
    var curUserId: Long = SessionUtils.getUserId()

    init {
        mPhotoViewPool.setMaxRecycledViews(0, 20)
        addChildClickViewIds(
            R.id.user_info_holder,
            R.id.btn_action,
            R.id.sdv_photo,
            R.id.tv_follow_num,
            R.id.tv_comment_num,
            R.id.tv_share_num,
            R.id.iv_more_action,
            R.id.tv_circle_name
        )
    }

    override fun convert(holder: BaseViewHolder, item: DynamicItemBean) {

        val rl = item.pics.map { PhotoBean(url = it) }.toMutableList()
        val list = if (rl.size > 4) {
            rl.subList(0, 4)
        } else {
            rl
        }
        val headPic = holder.getView<SimpleDraweeView>(R.id.header_pic)

        val authTag = holder.getView<SimpleDraweeView>(R.id.sd_auth_tag)
        if (item.authMark.isNotEmpty()) {
            authTag.show()
            ImageUtils.loadImageWithHeight_2(authTag, item.authMark, dp2px(16))
        } else {
            authTag.hide()
        }
        ImageHelper.setDefaultHeaderPic(headPic, item.sex)

        headPic.loadImage(item.headPic + BusiConstant.OSS_160, 66f, 66f)
        val name = if (item.nickname.length > 10) {
            "${item.nickname.substring(0, 10)}…"
        } else {
            item.nickname
        }

        holder.setText(R.id.tv_mkf_name, name).setText(R.id.tv_dyc_content, item.content)
            .setText(R.id.tv_location, " · ${item.city}")
        val tvContent = holder.getView<TextView>(R.id.tv_dyc_content)
        if (item.city.isEmpty()) {
            holder.setGone(R.id.tv_location, true)
        } else {
            holder.setGone(R.id.tv_location, false)
        }
        val time = holder.getView<TextView>(R.id.tv_time)
        time.text = /*TimeUtils.formatLostTime1(*/item.postTime
        //获取文字是否显示完全
        if (item.hasEllipsis == null) {
            tvContent.post(Runnable {
                val ellipsisCount: Int = tvContent.getLayout().getEllipsisCount(tvContent.getLineCount() - 1)
                //是否超出范围:如果行数大于3或者而且ellipsisCount>0超出范围，会显示省略号。
                if (item.hasEllipsis == null) {
                    item.hasEllipsis = !(tvContent.getLineCount() <= 4 && ellipsisCount == 0)
                }
                //如果文字没有超出范围，则隐藏按钮。
                holder.setGone(R.id.tv_expansion, !item.hasEllipsis!!)
            })
        } else {
            holder.setGone(R.id.tv_expansion, !item.hasEllipsis!!)
        }
        holder.setText(R.id.tv_follow_num, StringHelper.formatNum(item.praiseNum))
        holder.setText(R.id.tv_comment_num, StringHelper.formatNum(item.commentNum))
        holder.setText(R.id.tv_share_num, StringHelper.formatNum(item.shareNum))
        //
        if ((showType == HOME_RECOM || showType == HOME_FOLLOW) && item.userId != curUserId) {
            holder.setGone(R.id.btn_action, item.follow)
        } else {
            holder.setGone(R.id.btn_action, true)
        }
        holder.getView<TextView>(R.id.tv_follow_num).isActivated = item.hasPraise

        if (item.group == null) {
            holder.setGone(R.id.tv_circle_name, true)
        } else {
            holder.setGone(R.id.tv_circle_name, false)
            holder.getView<TextView>(R.id.tv_circle_name).text = item.group!!.groupName
        }
        when {
            list.isNotEmpty() -> {
                if (list.size == 1) {
                    holder.setGone(R.id.rv_photos, true).setVisible(R.id.sdv_photo, true)
                    val sgSdv = holder.getView<SimpleDraweeView>(R.id.sdv_photo)
                    val rvLp = sgSdv.layoutParams as ConstraintLayout.LayoutParams
                    val pic = list[0].url
                    val map = StringHelper.parseUrlParams(pic)
                    var h = map["h"]?.toIntOrNull() ?: SINGLE_PHOTO_DEFAULT
                    var w = map["w"]?.toIntOrNull() ?: SINGLE_PHOTO_DEFAULT
                    if (h > w) {
                        if (h > SINGLE_PHOTO_MAX_HEIGHT) {
                            w = w * SINGLE_PHOTO_MAX_HEIGHT / h
                            h = SINGLE_PHOTO_MAX_HEIGHT
                        } else if (h < SINGLE_PHOTO_MINI_SIZE) {
                            //最小不能小于最小网格
//                            w = w * SINGLE_PHOTO_MINI_SIZE / h
//                            h = SINGLE_PHOTO_MINI_SIZE
                            w = SINGLE_PHOTO_MINI_SIZE
                            h = SINGLE_PHOTO_MINI_SIZE
                        }
                    } else {
                        if (w > SINGLE_PHOTO_MAX_WIDTH) {
                            h = SINGLE_PHOTO_MAX_WIDTH * h / w
                            w = SINGLE_PHOTO_MAX_WIDTH
                        } else if (w < SINGLE_PHOTO_MINI_SIZE) {
//                            h = SINGLE_PHOTO_MINI_SIZE * h / w
//                            w = SINGLE_PHOTO_MINI_SIZE
                            w = SINGLE_PHOTO_MINI_SIZE
                            h = SINGLE_PHOTO_MINI_SIZE
                        }
                    }
                    rvLp.height = h
                    rvLp.width = w
                    sgSdv.requestLayout()
                    sgSdv.loadImageInPx(pic, w, height = h)
                } else {
                    holder.setVisible(R.id.rv_photos, true).setGone(R.id.sdv_photo, true)
                    val rv = holder.getView<RecyclerView>(R.id.rv_photos)
                    rv.setRecycledViewPool(mPhotoViewPool)
                    rv.setHasFixedSize(true)
                    val rvLp = rv.layoutParams as ConstraintLayout.LayoutParams
                    if (list.size >= 4 || list.size == 2) {
                        rvLp.width = Width_4
                    } else {
                        rvLp.width = 0
                    }
                    val spanCount = when (list.size) {
//                        1 -> {
//                            1
//                        }
                        2 -> {
                            2
                        }
                        3 -> {
                            3
                        }
                        4 -> {

                            2
                        }
                        else -> 3
                    }
                    rv.layoutManager = GridLayoutManager(context, spanCount)
                    if (rv.itemDecorationCount <= 0) {
                        rv.addItemDecoration(
                            GridLayoutSpaceItemDecoration2(space)
                        )
                    }

                    val mPhotosAdapter: DynamicPhotosAdapter
                    if (rv.adapter != null) {
                        mPhotosAdapter = rv.adapter as DynamicPhotosAdapter
//                            mPhotosAdapter.testPosition(holder.adapterPosition)
                    } else {
                        mPhotosAdapter = DynamicPhotosAdapter()
                        rv.adapter = mPhotosAdapter
                    }
                    mPhotosAdapter.setList(list)
                    mPhotosAdapter.currentPosition = holder.layoutPosition
                    mPhotosAdapter.totalList = rl
                    mPhotosAdapter.setOnItemClickListener(this)
                }

            }
            else -> {
                holder.setGone(R.id.rv_photos, true).setGone(R.id.sdv_photo, true)
            }
        }


    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, position: Int) {
        logger("点击了子adp position=$position")
        val adp = adapter as DynamicPhotosAdapter
        mOnItemAdapterListener?.onPhotoClick(adp.currentPosition, position, adp.totalList)
    }

    interface OnItemAdapterListener {
        fun onPhotoClick(
            index: Int,
            position: Int,
            list: MutableList<PhotoBean>
        )
    }
}
