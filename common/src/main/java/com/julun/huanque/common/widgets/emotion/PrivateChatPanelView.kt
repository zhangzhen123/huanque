package com.julun.huanque.common.widgets.emotion

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.effective.android.panel.view.panel.IPanelView
import com.julun.huanque.common.R
import com.julun.huanque.common.constant.EmojiType
import com.julun.huanque.common.interfaces.EmojiInputListener
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import kotlinx.android.synthetic.main.panel_view_private_chat.view.*

/**
 *@创建者   dong
 *@创建时间 2020/7/16 15:29
 *@描述
 */
class PrivateChatPanelView(context: Context?, attrs: AttributeSet?) : IPanelView, ConstraintLayout(context, attrs) {

    private var triggerViewId = 0
    private var isToggle = true

    //普通表情的Adapter
    private var mNormalEmojiAdapter = EmojiAdapter()

    //点击表情的回调
    var mListener: EmojiInputListener? = null

    init {
        if (context != null && attrs != null) {
            LayoutInflater.from(context).inflate(R.layout.panel_view_private_chat, this, true)

            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CusPanelView, 0, 0)
            triggerViewId = typedArray.getResourceId(R.styleable.CusPanelView_cus_panel_trigger, -1)
            isToggle = typedArray.getBoolean(R.styleable.CusPanelView_cus_panel_toggle, isToggle)
            typedArray.recycle()

            initListener()
            initRecyclerView()
        }

    }

    override fun getBindingTriggerViewId(): Int = triggerViewId

    override fun isTriggerViewCanToggle(): Boolean = isToggle

    override fun isShowing(): Boolean = isShown

    override fun assertView() {
        if (triggerViewId == -1) {
            throw RuntimeException("PanelView -- you must set 'panel_layout' and panel_trigger by Integer id")
        }
        iv_emoji.performClick()
    }

    /**
     * 设置监听
     */
    private fun initListener() {
        iv_emoji.onClickNew {
            showRecyclerView(iv_emoji, recyclerView_emoji)
        }
        iv_prerogative.onClickNew {
            showRecyclerView(iv_prerogative, recyclerView_prerogative)
        }
        iv_high.onClickNew {
            showRecyclerView(iv_high, recyclerView_high)
        }
    }

    private fun initRecyclerView() {
        recyclerView_emoji.layoutManager = GridLayoutManager(context, 7)
        recyclerView_emoji.adapter = mNormalEmojiAdapter
        mNormalEmojiAdapter.setList(Emotions.getEmotions())
        mNormalEmojiAdapter.setOnItemClickListener { adapter, view, position ->
            if (position < mNormalEmojiAdapter.data.size) {
                val tempData = mNormalEmojiAdapter.getItem(position)
                mListener?.onClick(EmojiType.NORMAL, tempData)
            }
        }

        mNormalEmojiAdapter.setOnItemLongClickListener { adapter, view, position ->
            if (position < mNormalEmojiAdapter.data.size) {
                val tempData = mNormalEmojiAdapter.getItem(position)
                mListener?.onLongClick(view, tempData)
            }
            return@setOnItemLongClickListener false
        }

        recyclerView_prerogative.layoutManager = GridLayoutManager(context, 7)
//        recyclerView_prerogative.adapter = mNormalEmojiAdapter

        recyclerView_high.layoutManager = GridLayoutManager(context, 7)
//        recyclerView_high.adapter = mNormalEmojiAdapter
    }

    /**
     * 显示对应的选中视图和RecyclerView
     */
    private fun showRecyclerView(view: View, rv: RecyclerView) {
        val viewList = arrayListOf<View>(iv_emoji, iv_prerogative, iv_high)
        val rvList = arrayListOf<RecyclerView>(recyclerView_emoji, recyclerView_prerogative, recyclerView_high)

        viewList.forEach {
            it.isSelected = it == view
        }

        rvList.forEach {
            if (it == rv) {
                it.show()
            } else {
                it.hide()
            }
        }

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        assertView()
    }


}

/**
 * 图片Adapter
 */
private class EmojiAdapter() : BaseQuickAdapter<Emotion, BaseViewHolder>(R.layout.vh_emotion_item_layout) {
    override fun convert(holder: BaseViewHolder, item: Emotion) {
        holder.setImageResource(R.id.image, item.drawableRes)
    }
}
