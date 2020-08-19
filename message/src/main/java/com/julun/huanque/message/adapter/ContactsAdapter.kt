package com.julun.huanque.message.adapter

import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.SocialUserInfo
import com.julun.huanque.common.constant.ContactsTabType
import com.julun.huanque.common.constant.FollowStatus
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.message.R
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor

/**
 *@创建者   dong
 *@创建时间 2020/7/7 11:28
 *@描述 联系人使用的Adapter
 */
class ContactsAdapter : BaseQuickAdapter<SocialUserInfo, BaseViewHolder>(R.layout.recycler_item_contacts), LoadMoreModule {
    init {
        addChildClickViewIds(R.id.tv_action)
    }

    //当前的联系人类型
    var type = ""
    override fun convert(helper: BaseViewHolder, item: SocialUserInfo) {
        if (helper == null || item == null) {
            return
        }
        val sdv_header = helper.getView<SimpleDraweeView>(R.id.sdv_header)
        ImageHelper.setDefaultHeaderPic(sdv_header, item.sex)
        ImageUtils.loadImage(sdv_header, item.headPic, 56f, 56f)
        val oriNicknamge = item.nickname
        val showNickname = if (oriNicknamge.length > 5) {
            "${oriNicknamge.substring(0, 5)}..."
        } else {
            oriNicknamge
        }

        val ivMeet = helper.getView<ImageView>(R.id.iv_meet)
        val meetStatus = ImageHelper.getMeetStatusResource(item.meetStatus)
        if (SessionUtils.getSex() == Sex.FEMALE && meetStatus > 0) {
            ivMeet.imageResource = meetStatus
            ivMeet.show()
        } else {
            ivMeet.hide()
        }

        //亲密度等级
        val ivIntimate = helper.getView<ImageView>(R.id.iv_intimate)
        val intimateResource = ImageHelper.getIntimateLevelPic(item.intimateLevel)
        if (intimateResource > 0) {
            ivIntimate.show()
            ivIntimate.imageResource = intimateResource
        } else {
            ivIntimate.hide()
        }

        helper.setText(R.id.tv_nickname, showNickname)
        //亲密度和昵称使用
        val tvIntimateNumber = helper.getView<TextView>(R.id.tv_intimate_number)
        //亲密度变化值
        val tvChangeNumber = helper.getView<TextView>(R.id.tv_change_number)

        if (type == ContactsTabType.Intimate) {
            //显示亲密值
            tvIntimateNumber.text = "亲密度${item.num}"
            tvChangeNumber.show()

            val changeNumber = item.changeNum
            if (changeNumber > 0) {
                //上升效果
                tvChangeNumber.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_intimate_up, 0, 0, 0)
                tvChangeNumber.textColor = GlobalUtils.getColor(R.color.send_private_chat)
            } else if (changeNumber < 0) {
                //下降效果
                tvChangeNumber.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_intimate_down, 0, 0, 0)
                tvChangeNumber.textColor = GlobalUtils.formatColor("#04C5B9")
            } else {
                //持平效果
                tvChangeNumber.hide()
            }
            tvChangeNumber.text = "${Math.abs(changeNumber)}"

        } else {
            //显示个性签名
            tvChangeNumber.hide()
            val content = if (item.mySign.length < 13) {
                item.mySign
            } else {
                "${item.mySign.substring(0, 13)}..."
            }
            tvIntimateNumber.text = content
        }
        //
        val tvAction = helper.getView<TextView>(R.id.tv_action)

        //按钮颜色
        when (type) {
            ContactsTabType.Intimate, ContactsTabType.Friend -> {
                //密友，好友  显示私信样式
                tvAction.text = "私信"
                tvAction.backgroundResource = R.drawable.bg_icon_private_chat
                tvAction.textColor = GlobalUtils.getColor(R.color.send_private_chat)
                tvAction.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                tvAction.compoundDrawablePadding = 0
            }
            ContactsTabType.Follow, ContactsTabType.Fan -> {
                //关注，粉丝  显示关注样式
                tvAction.text = "私信"
                tvAction.backgroundResource = R.drawable.bg_icon_attention
                tvAction.textColor = GlobalUtils.getColor(R.color.black_999)
                var actionResource = 0
                var actionText = ""
                when (item.follow) {
                    FollowStatus.True -> {
                        //已关注
                        actionText = "已关注"
                        actionResource = R.mipmap.icon_attentioned
                    }
                    FollowStatus.Mutual -> {
                        //相互关注
                        actionText = "相互关注"
                        actionResource = R.mipmap.icon_attention_each_other
                    }
                    FollowStatus.False -> {
                        //未关注
                        actionText = "关注"
                        actionResource = R.mipmap.icon_add_attention
                    }
                    else -> {
                        actionResource = 0
                    }
                }
                tvAction.setCompoundDrawablesWithIntrinsicBounds(actionResource, 0, 0, 0)
                tvAction.compoundDrawablePadding = 5
                tvAction.text = actionText
            }
        }

//        helper.addOnClickListener(R.id.tv_action)

    }
}