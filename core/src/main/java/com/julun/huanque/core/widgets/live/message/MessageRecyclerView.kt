package com.julun.huanque.core.widgets.live.message

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.bean.ChatMessageBean
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.widgets.draweetext.DraweeSpanTextView
import com.julun.huanque.core.R
import org.jetbrains.anko.matchParent
import java.lang.StringBuilder


/**
 *
 * Created by djp on 2017/1/9.
 */
class MessageRecyclerView(context: Context, attributeSet: AttributeSet?) : androidx.recyclerview.widget.RecyclerView(context, attributeSet) {
    private val logger = ULog.getLogger("MessageRecyclerView")
    //是否是主播身份
    var isAnchor = false

    var isUserScroll = false //用户正在滑动

    companion object {
        const val NORMAL = 1
        const val STYLE_FOLLOW = 2
        const val STYLE_TREASURE_BOX = 3 //宝箱功能
        const val STYLE_FANS_JOIN = 4//粉丝团引导
        const val STYLE_FANS_JOIN_OTHER = 5//粉丝团加入
        //星球霸主消息
        const val STYLE_PLANET = 6

        const val ACTION_FOLLOW = "follow"
        const val ACTION_GRAB_BOX = "grab_box"
        const val ACTION_JOIN_FANS = "join_fans"
        const val ACTION_PLANET = "planet"
        private const val MAX_LINES: Int = 100       //最大保留条目数
        private const val ITEMS_COUNT_TO_REMOVE = 40 //到达最大条目数的时候，删除的条目数
    }

    fun addOtherItem(bean: ChatMessageBean) {
        chatRecordAdapter.addData(bean)
        if (!isUserScroll)
            scrollToBottom()
    }

    fun changeItemLayout(bean: ChatMessageBean) {
        val updateContent = bean.content
        chatRecordAdapter.data.forEach {
            val content = it.content
            if (it.itemType == bean.itemType) {
                if (content is ChatFollow && updateContent is ChatFollow) {
                    content.follow = updateContent.follow
                } else if (content is FansBean && updateContent is FansBean) {
                    content.isJoin = updateContent.isJoin
                }
                //其他。。。
            }
        }
        chatRecordAdapter.notifyDataSetChanged()
    }

    fun addItems(messageList: List<TplBean>) {
        //统一到主线程 不再来回切换线程  增与删同步执行
//        Single.just(messageList).observeOn(AndroidSchedulers.mainThread()).subscribe { items ->
        messageList.forEach {
            it.useBg = true
            chatRecordAdapter.addData(ChatMessageBean(it, NORMAL))
        }

        val totalCount = messageList.size + chatRecordAdapter.itemCount
        val diff = totalCount - MAX_LINES
        if (diff > 0) {//如果大于了,删掉前面的 ,此处要保证,删掉的条目数  MAX_LINES 一定要小于一共的条目数
            if (diff + ITEMS_COUNT_TO_REMOVE < messageList.size) {
                (0..diff + ITEMS_COUNT_TO_REMOVE).forEach { _ ->
                    chatRecordAdapter.remove(0)
                }
            }
        }
//
        if (!isUserScroll)
            scrollToBottom()

    }

    fun scrollToBottom() {
        if (chatRecordAdapter.itemCount > 0) {
            smoothScrollToPosition(chatRecordAdapter.itemCount - 1)
        }
    }

    fun scrollToBottomQuickly() {
        if (chatRecordAdapter.itemCount > 0) {
            scrollToPosition(chatRecordAdapter.itemCount - 1)
        }
    }

    @Synchronized
    fun clearMessages() {
        chatRecordAdapter.setNewData(null)
    }

    private val chatRecordAdapter = object : BaseMultiItemQuickAdapter<ChatMessageBean, BaseViewHolder>(ArrayList()) {
        init {
            addItemType(NORMAL, R.layout.item_cmp_chat_record)
//            addItemType(STYLE_FOLLOW, R.layout.item_cmp_chat_guide_follow)
//            addItemType(STYLE_FANS_JOIN, R.layout.item_cmp_guide_join_fans)
//            addItemType(STYLE_FANS_JOIN_OTHER, R.layout.item_cmp_chat_record)
//            addItemType(STYLE2,R.layout.item_cmp_chat_record)
        }

        override fun convert(holder: BaseViewHolder, item: ChatMessageBean) {
//            logger.info("itemViewType:" + holder.itemViewType)
            when (holder.itemViewType) {
                NORMAL -> {
                    val tpl = item.content as TplBean
                    val txtInfo = holder.getView<DraweeSpanTextView>(R.id.chatContent)
                    try {
                        if (tpl.privateMessage && tpl.userInfo?.msgType == 1) {
                            //私聊消息,并且userinfo内部的nickname为空，表示需要特殊显示
                            txtInfo.render(tpl.specialExtra(), tpl.userInfo?.textColor ?: "#FFFFFF")
                        } else {
                            txtInfo.render(tpl.preProcess())
                        }
                        //针对上神左边界的处理
//                        val tpllp = txtInfo.layoutParams as RecyclerView.LayoutParams
//                        if (tpl.userInfo?.displayType?.contains(BusiConstant.DisplayType.SSCOLORFUL) == true) {
//                            tpllp.leftMargin = DensityHelper.dp2px(0f)
//                        } else {
//                            tpllp.leftMargin = DensityHelper.dp2px(3f)
//                        }
//                logger.info("新的下标 -->>> $position 解析后的文本：${item.realTxt} 原始文本：${item.textTpl}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        logger.info("发生错误了,, $e ${JsonUtil.seriazileAsString(item)}")
                    }
                }
//                STYLE_FOLLOW -> {
//                    val follow = item.content as ChatFollow
//                    holder.setGone(R.id.follow, !follow.follow)
//                    ImageUtils.loadImage(holder.getView(R.id.anchor_header_image), follow.anchorUrl, 34f, 34f)
//                    holder.addOnClickListener(R.id.follow)
//                }
//                STYLE_TREASURE_BOX -> {
//                    val box = item.content as TreasureBoxBean
//                    holder.setText(R.id.program_name, box.programName)
//                    ImageUtils.loadImageLocal(holder.getView(R.id.treasure_box_image), R.mipmap.message_treasure_box)
//                    holder.addOnClickListener(R.id.grab_box)
//                }
//                STYLE_FANS_JOIN -> {
//                    val fans = item.content as FansBean
//                    holder.setGone(R.id.join, !fans.isJoin)
//                    ImageUtils.loadImage(holder.getView(R.id.anchor_header_image), fans.anchorUrl, 34f, 34f)
//                    holder.addOnClickListener(R.id.join)
//                }
//                STYLE_FANS_JOIN_OTHER -> {
//                    val fans = item.content as FansBean
//                    val list = arrayListOf<TIBean>()
//                    val text = TIBean()
//                    text.type = 0
//                    text.textColor = "#55E6FF"
//                    text.textSize = DensityHelper.dp2px(14f)
//                    text.text = "${fans.nickname} 加入了 ${fans.programName}的粉丝团"
//                    list.add(text)
//                    if (!fans.isJoin) {
//                        val image = TIBean()
//                        image.type = 1
//                        image.imgRes = R.mipmap.lm_icon_fans_join_too
//                        image.height = DensityHelper.dp2px(16f)
//                        image.width = DensityHelper.dp2px(52f)
//                        list.add(image)
//                    }
//                    val txtInfo = holder.getView<DraweeSpanTextView>(R.id.chatContent)
//                    ImageUtils.renderTextAndImage(list)?.let {
//                        txtInfo.renderBaseText(it)
//                    }
//                    holder.addOnClickListener(R.id.chatContent)
//                }
                else -> {
                }
            }
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                if (onChatMessageItemClickListener != null) {
                    onChatMessageItemClickListener!!.onChatMessageItemClick(null, null)
                }
            }
        }
        return super.onTouchEvent(e)
    }

    init {
        layoutManager = object : androidx.recyclerview.widget.LinearLayoutManager(context, RecyclerView.VERTICAL, false) {
            private var MILLISECONDS_PER_INCH = 0.15f

            override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {
                val linearSmoothScroller = object : LinearSmoothScroller(recyclerView.context) {
                    //This returns the milliseconds it takes to
                    //scroll one pixel.
                    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                        return MILLISECONDS_PER_INCH
                        //返回滑动一个pixel需要多少毫秒
                    }

                }
                linearSmoothScroller.targetPosition = position
                startSmoothScroll(linearSmoothScroller)
            }
            //通过外部设置速度
/*
            fun setSpeedSlow() {
                //自己在这里用density去乘，希望不同分辨率设备上滑动速度相同
                //0.3f是自己估摸的一个值，可以根据不同需求自己修改
                MILLISECONDS_PER_INCH = resources.displayMetrics.density * 0.3f
            }

            fun setSpeedFast() {
                MILLISECONDS_PER_INCH = resources.displayMetrics.density * 0.03f
            }
            */
        }
        overScrollMode = View.OVER_SCROLL_NEVER
        layoutParams = FrameLayout.LayoutParams(matchParent, matchParent)
        adapter = chatRecordAdapter
//        itemAnimator = DefaultItemAnimator()
        /*
        itemAnimator = object :DefaultItemAnimator(){

            override fun animateAdd(holder: ViewHolder): Boolean {
                val view = holder.itemView
                ObjectAnimator.ofFloat(view,View.ALPHA,0f,1f).setDuration(2000L)
                        .start()
                return true
            }
        }
        */
/*
    //去除于2019/4/30
        //用户滑动时不让自动滑到底部 有个200毫秒的缓冲时间
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_DRAGGING) {
                    isUserScroll = true
                    val message = Message()
                    message.what = 111
                    mHandler.removeMessages(111)
                    mHandler.sendMessageDelayed(message, 3000L)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })
*/

        chatRecordAdapter.setOnItemClickListener { a, v, position ->
            //            logger.info("点击了item：$position")
            val item = chatRecordAdapter.getItem(position) ?: return@setOnItemClickListener
            when (item.itemType) {

                NORMAL -> {
                    val tpl = item.content as? TplBean ?: return@setOnItemClickListener

                    if (isAnchor) {
                        //主播身份
                        onPrivateItemClick?.let {
                            it.onClick(tpl.userInfo)
                        }
                    } else {
                        if (onChatMessageItemClickListener != null) {
                            val info = tpl.userInfo
                            val userInfo = UserInfoBean(info?.userId
                                    ?: 0, (info?.anchorLevel ?: 0) > 0, info?.royalLevel
                                    ?: 0, "", info?.displayType)
                            onChatMessageItemClickListener?.onChatMessageItemClick(userInfo, tpl)
                        }
                    }
                }
                STYLE_FOLLOW -> {

                }
            }

        }
        chatRecordAdapter.setOnItemChildClickListener { adapter, view, position ->
            val item = chatRecordAdapter.getItem(position) ?: return@setOnItemChildClickListener
//            when (view.id) {
//                R.id.follow -> {
//                    onChatMessageItemClickListener?.onOtherAction(ACTION_FOLLOW, null)
//                }
//                R.id.grab_box -> {
//                    val box = item.content as TreasureBoxBean
//                    onChatMessageItemClickListener?.onOtherAction(ACTION_GRAB_BOX, box.programId)
//                }
//                R.id.join,R.id.chatContent -> {
//                    val fans = item.content as? FansBean ?: return@setOnItemChildClickListener
//                    onChatMessageItemClickListener?.onOtherAction(ACTION_JOIN_FANS, fans)
//                }
//                R.id.tv_planet_message -> {
//                    val planetBean = item.content as? PlanetMessageBean ?: return@setOnItemChildClickListener
//                    onChatMessageItemClickListener?.onOtherAction(ACTION_PLANET, planetBean)
//                }
//                else -> {
//                    logger.info("点击了其他的 $position")
//                }
//            }
        }
    }


/*
     //去除于2019/4/30
    internal var mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                111 -> {
                    logger.info("终于收到了-------")
                    isUserScroll = false
                }
            }
            super.handleMessage(msg)
        }
    }

    override fun onDetachedFromWindow() {
        mHandler.removeMessages(111)
        super.onDetachedFromWindow()
    }
*/

    private var onChatMessageItemClickListener: OnChatMessageItemClickListener? = null
    fun setOnChatMessageItemClickListener(onChatMessageItemClickListener: OnChatMessageItemClickListener) {
        this.onChatMessageItemClickListener = onChatMessageItemClickListener
    }

    private var onPrivateItemClick: OnPrivateItemClick? = null

    fun setOnPrivateItemClickListener(listener: OnPrivateItemClick) {
        onPrivateItemClick = listener
    }

    // 暂时不需要参数，后面有需求自己添加吧
    interface OnChatMessageItemClickListener {
        fun onChatMessageItemClick(user: UserInfoBean?, bean: TplBean?)
        fun onOtherAction(action: String, extra: Any?)
    }

    interface OnPrivateItemClick {
        fun onClick(userEtra: RoomUserChatExtra?)
    }
}
