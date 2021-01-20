package com.julun.huanque.core.adapter

import android.animation.Animator
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.FavoriteUserBean
import com.julun.huanque.common.bean.beans.HomePagePicBean
import com.julun.huanque.common.constant.BooleanType
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import org.jetbrains.anko.imageResource
import java.math.RoundingMode
import java.text.DecimalFormat

class FavoriteAdapter : BaseQuickAdapter<FavoriteUserBean, BaseViewHolder>(R.layout.item_favorite_user_list) {
    //去掉底部栏49 顶部tab=53 中间tab=40 其他边距20
    private val itemHeight = (ScreenUtils.getRealScreenHeight() - ScreenUtils.statusHeight - dp2px(49 + 53 + 40 + 20)) / 2

    init {
        addChildClickViewIds(R.id.ll_top_tag_holder)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
//        val tempData = getItemOrNull(position)
//        if (tempData != null) {
//            val card_img = holder.getViewOrNull<SimpleDraweeView>(R.id.card_img)
//            if (card_img != null) {
//                ViewCompat.setTransitionName(card_img, "Image${tempData.userId}")
////                    val tv_user_name = holder.getView<TextView>(R.id.tv_user_name)
////                    ViewCompat.setTransitionName(tv_user_name, "TextView${tempData.userId}")
//            }
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val holder = super.onCreateViewHolder(parent, viewType)
        if (viewType == 0) {
            val cardView = holder.getView<CardView>(R.id.card_view)
            val sdvLp = cardView.layoutParams
            sdvLp.height = itemHeight
        }

        return holder
    }

    override fun convert(holder: BaseViewHolder, item: FavoriteUserBean) {
        val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
        sdv.loadImageNoResize(item.coverPic)

        val age = if (item.age != 0) {
            "${item.age}岁"
        } else {
            "未知"
        }
        val sdvTag = holder.getView<SimpleDraweeView>(R.id.sdv_tag)
        sdvTag.loadImage(item.tagIcon, 16f, 16f)
        holder.setText(R.id.tv_tag, "${item.tagName} ${item.picCnt}")

        holder.setText(R.id.tv_age, age)

        val tvDistance = holder.getView<TextView>(R.id.tv_distance)
        val ivDistance = holder.getView<ImageView>(R.id.iv_distance)
        if (item.distance != -1) {
            val area = if (item.area.length > 4) {
                item.area.substring(0, 4) + "..."
            } else {
                item.area
            }
//            tvDistance.show()
//            when {
//                item.distance < 1000 -> {
//                    holder.setText(R.id.tv_distance, "${item.distance}")
//                    holder.setText(R.id.tv_location, "m $area")
//                }
//                else -> {
//                    val format = DecimalFormat("#.0")
//                    format.roundingMode = RoundingMode.DOWN
//                    val dt = format.format((item.distance / 1000.0))
//                    holder.setText(R.id.tv_distance, dt)
//                    holder.setText(R.id.tv_location, "km $area")
//                }
//            }
            if (item.sameCity) {
                tvDistance.show()
                ivDistance.hide()
                when {
                    item.distance < 1000 -> {
                        holder.setText(R.id.tv_distance, "${item.distance}")
                        holder.setText(R.id.tv_location, "m $area")
                    }
                    else -> {
                        val format = DecimalFormat("#.0")
                        format.roundingMode = RoundingMode.DOWN
                        val dt = format.format((item.distance / 1000.0))
                        holder.setText(R.id.tv_distance, dt)
                        holder.setText(R.id.tv_location, "km $area")
                    }
                }
            } else {
                tvDistance.hide()
                ivDistance.show()
                holder.setText(R.id.tv_location, area)
                when {
                    item.distance <= 100000 -> {
                        ivDistance.imageResource = R.mipmap.icon_home_distance_car
                    }
                    item.distance <= 800000 -> {
                        //显示动车
                        ivDistance.imageResource = R.mipmap.icon_home_distance_rail_way
                    }
                    else -> {
                        ivDistance.show()
                        //显示飞机
                        ivDistance.imageResource = R.mipmap.icon_home_distance_air_plan
                    }
                }

            }

        } else {
            tvDistance.hide()
            ivDistance.show()
            val starList = mutableListOf<String>("金星", "木星", "水星", "火星", "土星")
            val currentStar = starList.random()
            ivDistance.imageResource = R.mipmap.icon_home_distance_rocket
            holder.setText(R.id.tv_location, currentStar)
        }
        if (item.interactTips.isNotEmpty()) {
            holder.setText(R.id.tv_top_right_tips, item.interactTips).setGone(R.id.tv_top_right_tips, false)
        } else {
            holder.setGone(R.id.tv_top_right_tips, true)
        }
        val rvPics = holder.getView<RecyclerView>(R.id.rv_pics)
        if (item.coverPicList.size <= 1) {
            rvPics.hide()
        } else {
            rvPics.show()
            rvPics.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            val mPicsAdapter: NearbyPicListAdapter
            if (rvPics.adapter != null) {
                mPicsAdapter = rvPics.adapter as NearbyPicListAdapter
            } else {
                mPicsAdapter = NearbyPicListAdapter()
                rvPics.adapter = mPicsAdapter
            }
            if (rvPics.itemDecorationCount <= 0) {
                rvPics.addItemDecoration(
                    HorizontalItemDecoration(dp2px(4))
                )
            }
            val list = mutableListOf<HomePagePicBean>()
            kotlin.run {
                item.coverPicList.forEachIndexed { index, pic ->
                    if (index > 2) {
                        return@run
                    }
                    if (index == 0) {
                        list.add(HomePagePicBean(pic, selected = BooleanType.FALSE))
                    } else {
                        list.add(HomePagePicBean(pic, selected = BooleanType.FALSE))
                    }

                }
            }
            mPicsAdapter.setList(list)
        }
    }

    override fun startAnim(anim: Animator, index: Int) {
        anim.duration = 250L
        anim.startDelay = index * 80L
        anim.start()
    }
}
