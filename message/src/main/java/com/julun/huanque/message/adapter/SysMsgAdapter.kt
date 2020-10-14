package com.julun.huanque.message.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.beans.SysMsgContent
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.MessageFormatUtils
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.message.R
import io.rong.imlib.model.Message
import io.rong.message.TextMessage

/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/14
 */
class SysMsgAdapter : BaseQuickAdapter<Message, BaseViewHolder>(R.layout.item_sys_msg_list),
    LoadMoreModule {
    init {
        addChildClickViewIds(R.id.llSysRootView)
    }

    override fun convert(holder: BaseViewHolder, info: Message) {
        val time = info.sentTime

        val rootView = holder.getView<View>(R.id.llSysRootView)

        val item = info.content as? TextMessage
        item?.let {
            val customBean: SysMsgContent? = MessageFormatUtils.parseJsonFromTextMessage(SysMsgContent::class.java, item.content ?: "")

            val itemInfo = customBean?.context

            rootView.setTag(R.id.msg_bean_id, itemInfo)


            //title head
            ImageUtils.loadImage(
                holder.getView(R.id.ivTitle),
                itemInfo?.icon ?: "",
                24f,
                24f
            )
            //title text
            holder.setText(R.id.tvTitle, itemInfo?.title ?: "")
            //title date
            holder.setText(R.id.tvDate, TimeUtils.formatMessageTime(time))

            //subTitle
            holder.setText(R.id.tvTitle, itemInfo?.subTitle ?: "")
            //desc
            holder.setText(R.id.tvBody, itemInfo?.body ?: "")

            if (itemInfo?.touchType?.isNotEmpty() == true) {
                holder.setVisible(R.id.tvCheckOut, true)
                    .setVisible(R.id.vLine, true)
            } else {
                holder.setGone(R.id.tvCheckOut, true)
                    .setGone(R.id.vLine, true)
            }
        }
    }

}