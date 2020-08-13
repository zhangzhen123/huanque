package com.julun.huanque.core.widgets.live.message

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.interfaces.EventListener
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.textColor


/**
 * 聊天消息组件
 * Created by djp on 2016/11/29.
 */
class PublicChatMessageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val logger = ULog.getLogger("PublicChatMessageView")
    var mEventListener: EventListener? = null
    val messageRecyclerView: MessageRecyclerView by lazy {
        MessageRecyclerView(context, null)
    }
    private val newMessageNotice: TextView by lazy {
        TextView(context).apply {
            text = "有新消息"
            textColor = ContextCompat.getColor(context, R.color.color_red_two)
            backgroundResource = R.drawable.bg_shape_white2
            gravity = Gravity.CENTER
            setPadding(dip(14), 0,dip(14), 0)
            compoundDrawablePadding = 5
            setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.mipmap.icon_arrow_down), null)
        }
    }

    init {
        val params = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        params.leftMargin = dip(5)
        this.addView(messageRecyclerView, params)

        val tvp = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,dip(24))
        tvp.leftMargin = dip(10)
        tvp.bottomMargin=dip(2)
        tvp.gravity = Gravity.START or Gravity.BOTTOM
        newMessageNotice.hide()
        this.addView(newMessageNotice, tvp)
    }
    fun addMessages(messageList: List<TplBean>) {
//        messageList.forEach { it.useBold = true }
        //如果此时view隐藏 就不再处理消息
//        if(visibility== View.GONE){
//            return
//        }
        checkIsInBottom()
        messageRecyclerView.addItems(messageList)
    }

    fun clearMessages() {
        newMessageNotice.hide()
        messageRecyclerView.clearMessages()
    }


    override fun onFinishInflate() {
        super.onFinishInflate()
        messageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == SCROLL_STATE_DRAGGING) {
                    messageRecyclerView.isUserScroll = true
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!recyclerView.canScrollVertically(1)) {
//                        logger.info("说明划到底了")
                        messageRecyclerView.isUserScroll = false
                        newMessageNotice.hide()
                    }
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            }
        })

        newMessageNotice.onClickNew {
            messageRecyclerView.isUserScroll = false
            newMessageNotice.hide()
            messageRecyclerView.scrollToBottomQuickly()//此方法不会触发scroll监听
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        mEventListener?.onDispatch(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun checkIsInBottom() {
        val manager = messageRecyclerView.layoutManager as LinearLayoutManager
        val lastPosition = manager.findLastVisibleItemPosition()
        if (lastPosition < manager.itemCount - 1) {
//            logger.info("说明有未读消息")

            if (messageRecyclerView.isUserScroll) {
//                logger.info("显示未读标识")
                newMessageNotice.show()
            }
        }
    }
}