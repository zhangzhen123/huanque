package com.julun.huanque.core.ui.share

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.UserCardShareInfo
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.widgets.HomeCardTagView

class UserCardShareAdapter(var info: UserCardShareInfo? = null) : BaseDelegateMultiAdapter<Any, BaseViewHolder>(null) {


    companion object {
        const val FIRST_TYPE = 1
        const val SECOND_TYPE = 2
//        const val THIRD_TYPE = 3
    }

//    private val with = ScreenUtils.getScreenWidth() - dp2px(55) * 2

    init {
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<Any>() {
            override fun getItemType(data: List<Any>, position: Int): Int {
                return when (position) {
                    0 -> {
                        SECOND_TYPE
                    }
                    else -> {
                        FIRST_TYPE
                    }
//                    else -> {
//                        THIRD_TYPE
//                    }
                }
            }
        })
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()?.addItemType(FIRST_TYPE, R.layout.item_user_share_card_01)
            ?.addItemType(SECOND_TYPE, R.layout.item_user_share_card_02)
//            ?.addItemType(THIRD_TYPE, R.layout.item_user_share_card_03)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        logger("onCreateViewHolder itemViewType:$viewType")
        val holder = super.onCreateViewHolder(parent, viewType)
//        val container = holder.getViewOrNull<CardView>(R.id.card_view)
//        container?.let {
//            val cl = container.layoutParams
//            cl.width = with
//        }
        if (viewType == FIRST_TYPE) {
            holder.getViewOrNull<HomeCardTagView>(R.id.ani_tag_01)?.setLeftDot(false)
            holder.getViewOrNull<HomeCardTagView>(R.id.ani_tag_02)?.setLeftDot(true)
            holder.getViewOrNull<HomeCardTagView>(R.id.ani_tag_03)?.setLeftDot(true)
        }
        return holder
    }

    override fun convert(holder: BaseViewHolder, item: Any) {
        val userInfo = info ?: return
//        logger("itemViewType:" + holder.itemViewType)
        val ivCode = holder.getView<AppCompatImageView>(R.id.iv_q_code)
        ivCode.setImageBitmap(item as Bitmap)
        when (holder.itemViewType) {
            FIRST_TYPE -> {
                val img = holder.getView<SimpleDraweeView>(R.id.card_img)
                img.loadImageNoResize(userInfo.headPic)
                when {
                    userInfo.realName -> {
                        holder.setGone(R.id.iv_auth, false)
                        holder.setImageResource(R.id.iv_auth, R.mipmap.icon_auth_success_share)
                    }
                    userInfo.headRealPeople -> {
                        holder.setGone(R.id.iv_auth, false)
                        holder.setImageResource(R.id.iv_auth, R.mipmap.icon_real_head_share)
                    }
                    else -> {
                        holder.setGone(R.id.iv_auth, true)
                    }
                }
                var tags = ""
                kotlin.run {
                    userInfo.likeTagList.forEachIndexed { index, userTagBean ->
                        if (index == 2 || index == userInfo.likeTagList.size - 1) {
                            tags += userTagBean.tagName
                            return@run
                        }
                        tags += userTagBean.tagName + "、"
                    }
                }
                if (tags.isNotEmpty()) {
                    holder.setText(R.id.tv_like_tags, tags)
                } else {
                    holder.setText(R.id.tv_like_tags, "你就是我的理想型")
                }
                holder.setText(R.id.tv_user_name, userInfo.nickname).setText(R.id.tv_sign, userInfo.mySign)
                    .setText(R.id.tv_social_wish, userInfo.wish)

                if (userInfo.area.isNotEmpty()) {
                    holder.setText(R.id.tv_area, userInfo.area)
                } else {
                    val starList = mutableListOf<String>("金星", "木星", "水星", "火星", "土星")
                    val currentStar = starList.random()
                    holder.setText(R.id.tv_area, currentStar)
                }

                val strAgeSex = if (userInfo.sexType == Sex.MALE) {
                    " / ${userInfo.age}岁 男"
                } else {
                    " / ${userInfo.age}岁 女"
                }
                holder.setText(R.id.tv_age, strAgeSex)

                val tagView01 = holder.getView<HomeCardTagView>(R.id.ani_tag_01)
                val tag01 = userInfo.authTagList.getOrNull(0)
                if (tag01 != null) {
                    tagView01.show()
                    tagView01.startSetData(tag01)
                } else {
                    tagView01.hide()
                }
                val tagView02 = holder.getView<HomeCardTagView>(R.id.ani_tag_02)
                val tag02 = userInfo.authTagList.getOrNull(1)
                if (tag02 != null) {
                    tagView02.show()
                    tagView02.startSetData(tag02)
                } else {
                    tagView02.hide()
                }
                val tagView03 = holder.getView<HomeCardTagView>(R.id.ani_tag_03)
                val tag03 = userInfo.authTagList.getOrNull(2)
                if (tag03 != null) {
                    tagView03.show()
                    tagView03.startSetData(tag03)
                } else {
                    tagView03.hide()
                }

            }

            SECOND_TYPE -> {
                val img = holder.getView<SimpleDraweeView>(R.id.card_img)
                val imgBg = holder.getView<SimpleDraweeView>(R.id.card_img_bg)
                img.loadImage(userInfo.headPic, 50f, 50f)
                ImageUtils.loadImageWithBlur(imgBg, userInfo.headPic, 2, 150)
                holder.setText(R.id.tv_social_wish, userInfo.wish).setText(R.id.tv_user_name, userInfo.nickname)
                    .setText(R.id.tv_sign, userInfo.mySign)

                if (userInfo.area.isNotEmpty()) {
                    holder.setText(R.id.tv_area, userInfo.area)
                } else {
                    val starList = mutableListOf<String>("金星", "木星", "水星", "火星", "土星")
                    val currentStar = starList.random()
                    holder.setText(R.id.tv_area, currentStar)
                }
                when {
                    userInfo.realName -> {
                        holder.setGone(R.id.iv_auth, false)
                        holder.setImageResource(R.id.iv_auth, R.mipmap.icon_auth_success_share)
                    }
                    userInfo.headRealPeople -> {
                        holder.setGone(R.id.iv_auth, false)
                        holder.setImageResource(R.id.iv_auth, R.mipmap.icon_real_head_share)
                    }
                    else -> {
                        holder.setGone(R.id.iv_auth, true)
                    }
                }

                val strAgeSex = if (userInfo.sexType == Sex.MALE) {
                    "${userInfo.age}岁 男"
                } else {
                    "${userInfo.age}岁 女"
                }
                holder.setText(R.id.tv_age, strAgeSex)
                var myTags = ""
                kotlin.run {
                    userInfo.authTagList.forEachIndexed { index, userTagBean ->
                        if (index == 2 || index == userInfo.likeTagList.size - 1) {
                            myTags += userTagBean.tagName
                            return@run
                        }
                        myTags += userTagBean.tagName + "、"
                    }
                }
                var myLikeTags = ""
                kotlin.run {
                    userInfo.likeTagList.forEachIndexed { index, userTagBean ->
                        if (index == 2 || index == userInfo.likeTagList.size - 1) {
                            myLikeTags += userTagBean.tagName
                            return@run
                        }
                        myLikeTags += userTagBean.tagName + "、"
                    }
                }
                if (myTags.isNotEmpty()) {
                    holder.setText(R.id.tv_my_tags, myTags)
                } else {
                    holder.setText(R.id.tv_my_tags, "等待发掘")
                }
                if (myLikeTags.isNotEmpty()) {
                    holder.setText(R.id.tv_like_tags, myLikeTags)
                } else {
                    holder.setText(R.id.tv_like_tags, "你就是我的理想型")
                }

                if (userInfo.constellation.isNotEmpty()) {
                    holder.setText(R.id.tv_constellation, userInfo.constellation).setGone(R.id.fl_constellation, false)
                } else {
                    holder.setGone(R.id.fl_constellation, true)
                }
            }


        }
    }

}
