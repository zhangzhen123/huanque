package com.julun.huanque.message.adapter

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.FriendBean
import com.julun.huanque.common.bean.beans.FriendContent
import com.julun.huanque.common.bean.beans.TIBean
import com.julun.huanque.common.constant.MessageConstants
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.MessageFormatUtils
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.common.widgets.draweetext.DraweeSpanTextView
import com.julun.huanque.message.R
import io.rong.imlib.model.Message
import io.rong.message.TextMessage

/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/14
 */
class FriendsAdapter : BaseQuickAdapter<Message, BaseViewHolder>(R.layout.item_friends_msg_list),
    LoadMoreModule {
    init {
        addChildClickViewIds(R.id.clFriendsRootView)
        addChildClickViewIds(R.id.tvMessage)
    }

    override fun convert(holder: BaseViewHolder, info: Message) {
        val time = info.sentTime

        val rootView = holder.getView<View>(R.id.clFriendsRootView)
        val messageView = holder.getView<View>(R.id.tvMessage)

        val item = info.content as? TextMessage
        item?.let {
            val customBean: FriendContent? =
                MessageFormatUtils.parseJsonFromTextMessage(FriendContent::class.java, item.content)

            val itemInfo = customBean?.context

            rootView.setTag(R.id.msg_bean_id, itemInfo)
            messageView.setTag(R.id.msg_bean_id, itemInfo)

            ImageUtils.loadImage(holder.getView(R.id.ivHead),itemInfo?.friendHeadPic?:"",56f,56f)

            holder.setText(R.id.tvNickname,itemInfo?.friendNickname?:"")
                .setText(R.id.tvDate,TimeUtils.formatMessageTime(time))


            MessageFormatUtils.renderImage(holder.getView(R.id.tvDesc),itemInfo?:return)
        }
    }

}