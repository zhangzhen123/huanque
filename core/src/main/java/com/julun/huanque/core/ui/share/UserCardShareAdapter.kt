package com.julun.huanque.core.ui.share

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.UserCardShareInfo
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.loadImageNoResize
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.core.R

class UserCardShareAdapter(var info: UserCardShareInfo? = null) : BaseDelegateMultiAdapter<Any, BaseViewHolder>(null) {


    companion object {
        const val FIRST_TYPE = 1
        const val SECOND_TYPE = 2
        const val THIRD_TYPE = 3
    }

    private val with = ScreenUtils.getScreenWidth() - dp2px(20) * 2

    init {
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<Any>() {
            override fun getItemType(data: List<Any>, position: Int): Int {
                return when (position) {
                    0 -> {
                        FIRST_TYPE
                    }
                    1 -> {
                        SECOND_TYPE
                    }
                    else -> {
                        THIRD_TYPE
                    }
                }
            }
        })
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()?.addItemType(FIRST_TYPE, R.layout.item_user_share_card_01)
            ?.addItemType(SECOND_TYPE, R.layout.item_user_share_card_02)
            ?.addItemType(THIRD_TYPE, R.layout.item_user_share_card_03)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        logger("onCreateViewHolder itemViewType:$viewType")
        val holder = super.onCreateViewHolder(parent, viewType)
        val container = holder.getViewOrNull<CardView>(R.id.card_view)
        container?.let {
            val cl = container.layoutParams
            cl.width = with
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

                holder.setText(R.id.tv_user_name, userInfo.nickname).setText(R.id.tv_sign, userInfo.mySign)
                    .setText(R.id.tv_area, userInfo.area).setText(R.id.tv_age, "${userInfo.age}")
                    .setText(R.id.tv_like_tags, tags).setText(R.id.tv_social_wish, userInfo.wish)
                if (userInfo.sexType == Sex.MALE) {
                    holder.setImageResource(R.id.iv_sex, R.mipmap.icon_sex_male)
                } else {
                    holder.setImageResource(R.id.iv_sex, R.mipmap.icon_sex_female)
                }


            }

            SECOND_TYPE -> {
                val img = holder.getView<SimpleDraweeView>(R.id.card_img)
                img.loadImage(userInfo.headPic, 50f, 50f)
                holder.setText(R.id.tv_wish, userInfo.wish).setText(R.id.tv_user_name, userInfo.nickname)
                    .setText(R.id.tv_sign, userInfo.mySign).setText(R.id.tv_area, userInfo.area)
                    .setText(R.id.tv_age, "${userInfo.age}")

                if (userInfo.sexType == Sex.MALE) {
                    holder.setImageResource(R.id.iv_sex, R.mipmap.icon_sex_male)
                } else {
                    holder.setImageResource(R.id.iv_sex, R.mipmap.icon_sex_female)
                }
                var myTags = ""
                kotlin.run {
                    userInfo.authTagList.forEachIndexed { index, userTagBean ->
                        if (index == 2 || index == userInfo.likeTagList.size - 1) {
                            myTags += userTagBean.tagName
                            return@run
                        }
                        myTags += userTagBean.tagName + "\n"
                    }
                }
                var myLikeTags = ""
                kotlin.run {
                    userInfo.likeTagList.forEachIndexed { index, userTagBean ->
                        if (index == 2 || index == userInfo.likeTagList.size - 1) {
                            myLikeTags += userTagBean.tagName
                            return@run
                        }
                        myLikeTags += userTagBean.tagName + "\n"
                    }
                }
                holder.setText(R.id.tv_my_tags, myTags).setText(R.id.tv_like_tags, myLikeTags)
            }
            THIRD_TYPE -> {

            }

        }
    }

}
