package com.julun.huanque.common.widgets.emotion

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.effective.android.panel.view.panel.IPanelView
import com.julun.huanque.common.R
import com.julun.huanque.common.constant.EmojiType
import com.julun.huanque.common.interfaces.EmojiInputListener
import com.julun.huanque.common.interfaces.EventListener
import com.julun.huanque.common.suger.onClickNew
import kotlinx.android.synthetic.main.panel_view_public_chat.view.*
import kotlinx.android.synthetic.main.panel_view_public_chat.view.iv_delete

/**
 *@创建者   dong
 *@创建时间 2020/7/31 18:00
 *@描述 直播间使用的表情输入框
 */
class PuclicChatPanelView(context: Context, attrs: AttributeSet?) : IPanelView, ConstraintLayout(context, attrs) {
    private var triggerViewId = 0
    private var isToggle = true
    //普通表情的Adapter
    private var mNormalEmojiAdapter = EmojiAdapter()
    //点击表情的回调
    var mListener: EmojiInputListener? = null

    init {
        if (context != null && attrs != null) {
            LayoutInflater.from(context).inflate(R.layout.panel_view_public_chat, this, true)

            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CusPanelView, 0, 0)
            triggerViewId = typedArray.getResourceId(R.styleable.CusPanelView_cus_panel_trigger, -1)
            isToggle = typedArray.getBoolean(R.styleable.CusPanelView_cus_panel_toggle, isToggle)
            typedArray.recycle()

            initListener()
            initRecyclerView()
        }
    }

    private fun initRecyclerView(){
        recyclerView_emoji.layoutManager = GridLayoutManager(context, 7)
        recyclerView_emoji.adapter = mNormalEmojiAdapter
        mNormalEmojiAdapter.setList(Emotions.getEmotions(EmojiType.NORMAL))
        mNormalEmojiAdapter.setOnItemClickListener { adapter, view, position ->
            if (position < mNormalEmojiAdapter.data.size) {
                val tempData = mNormalEmojiAdapter.getItem(position)
                mListener?.onClick(EmojiType.NORMAL, tempData)
            }
        }
        mNormalEmojiAdapter.setOnItemLongClickListener { adapter, view, position ->
            if (position < adapter.data.size) {
                val tempData = adapter.getItem(position) as? Emotion
                mListener?.onLongClick(EmojiType.NORMAL, view, tempData ?: return@setOnItemLongClickListener true)
                view.isSelected = true
            }
            return@setOnItemLongClickListener true
        }
    }

    private fun initListener(){
        iv_delete.onClickNew {
            mListener?.onClickDelete()
        }

        recyclerView_emoji.mEventListener = object : EventListener {
            override fun onDispatch(ev: MotionEvent?) {
                if (ev?.action == MotionEvent.ACTION_UP) {
                    mListener?.onActionUp()
                }
            }

        }
    }

    override fun assertView() {
    }

    override fun getBindingTriggerViewId(): Int = triggerViewId

    override fun isTriggerViewCanToggle(): Boolean = isToggle

    override fun isShowing(): Boolean = isShown

    /**
     * 普通图片Adapter
     */
    private class EmojiAdapter() : BaseQuickAdapter<Emotion, BaseViewHolder>(R.layout.vh_emotion_item_layout) {
        override fun convert(holder: BaseViewHolder, item: Emotion) {
            holder.setImageResource(R.id.image, item.drawableRes)
        }
    }
}