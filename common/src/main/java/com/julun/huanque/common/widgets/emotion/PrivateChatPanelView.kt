package com.julun.huanque.common.widgets.emotion

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.julun.huanque.common.interfaces.EventListener
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import kotlinx.android.synthetic.main.panel_view_private_chat.view.*

/**
 *@创建者   dong
 *@创建时间 2020/7/16 15:29
 *@描述 表情面板
 */
class PrivateChatPanelView(context: Context?, attrs: AttributeSet?) : IPanelView, ConstraintLayout(context, attrs) {

    private var triggerViewId = 0
    private var isToggle = true

    //普通表情的Adapter
    private var mNormalEmojiAdapter = NormalEmojiAdapter()

    //特权表情的Adapter
    private var mPrivilegeAdapter = PrerogativeEmojiAdapter()

    //动画表情的Adapter
    private var mAnimationAdapter = AnimationAdapter()

    private var mNeedLevel = 0

    private var mCurrentLevel = 0


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

    /**
     * 设置专属表情所需要等级
     * @param needLevel 解锁专属表情所需等级
     * @param currentLevel 当前两人的亲密度等级
     */
    fun setIntimate(needLevel: Int, currentLevel: Int) {
        mNeedLevel = needLevel
        mCurrentLevel = currentLevel

        val str = "亲密度等级$needLevel，立即解锁"
        val spannableString = SpannableString(str)
        val colorText = "立即解锁"
        val index = str.indexOf(colorText)
        spannableString.setSpan(
            ForegroundColorSpan(GlobalUtils.getColor(R.color.send_private_chat)),
            index,
            index + colorText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        tv_privilege_attetnion.text = spannableString

        showPrivilegeShade()
    }

    /**
     * 特权表情的遮罩是否显示
     */
    private fun showPrivilegeShade() {
        if (iv_prerogative.isSelected && mNeedLevel > mCurrentLevel) {
            //需要显示特权表情遮罩
            view_shade.show()
            tv_privilege_attetnion.show()
        } else {
            view_shade.hide()
            tv_privilege_attetnion.hide()
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
            if (mPrivilegeAdapter.itemCount == 0) {
                //未设置数据,设置数据
                val list = mutableListOf<Emotion>()
                list.addAll(Emotions.getEmotions(EmojiType.PREROGATIVE))
                list.addAll(Emotions.getEmotions(EmojiType.PREROGATIVE))
                list.addAll(Emotions.getEmotions(EmojiType.PREROGATIVE))
                list.addAll(Emotions.getEmotions(EmojiType.PREROGATIVE))
                list.addAll(Emotions.getEmotions(EmojiType.PREROGATIVE))
                list.addAll(Emotions.getEmotions(EmojiType.PREROGATIVE))
                list.addAll(Emotions.getEmotions(EmojiType.PREROGATIVE))
                list.addAll(Emotions.getEmotions(EmojiType.PREROGATIVE))
                mPrivilegeAdapter.setList(list)
            }
            showRecyclerView(iv_prerogative, recyclerView_prerogative)
        }

        iv_high.onClickNew {
            if (mAnimationAdapter.itemCount == 0) {
                //未设置数据,设置数据
                val list = mutableListOf<Emotion>()
                list.addAll(Emotions.getEmotions(EmojiType.ANIMATION))
                mAnimationAdapter.setList(list)
            }
            showRecyclerView(iv_high, recyclerView_animation)
        }
        recyclerView_emoji.mEventListener = object : EventListener {
            override fun onDispatch(ev: MotionEvent?) {
                if (ev?.action == MotionEvent.ACTION_UP) {
                    mListener?.onActionUp()
                }
            }
        }
        recyclerView_prerogative.mEventListener = object : EventListener {
            override fun onDispatch(ev: MotionEvent?) {
                if (ev?.action == MotionEvent.ACTION_UP) {
                    mListener?.onActionUp()
                }
            }
        }

        tv_privilege_attetnion.onClickNew {
            //弹窗说明弹窗
            mListener?.showPrivilegeFragment("ZSBQ")
        }

        iv_delete.onClickNew {
            mListener?.onClickDelete()
        }
    }

    private fun initRecyclerView() {
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
            if (position < mNormalEmojiAdapter.data.size) {
                val tempData = mNormalEmojiAdapter.getItem(position)
                mListener?.onLongClick(EmojiType.NORMAL, view, tempData)
                view.isSelected = true
            }
            return@setOnItemLongClickListener true
        }

        recyclerView_prerogative.layoutManager = GridLayoutManager(context, 5)
        recyclerView_prerogative.adapter = mPrivilegeAdapter

        mPrivilegeAdapter.setOnItemClickListener { adapter, view, position ->
            if (position < mPrivilegeAdapter.data.size) {
                val tempData = mPrivilegeAdapter.getItem(position)
                mListener?.onClick(EmojiType.PREROGATIVE, tempData)
            }
        }

        mPrivilegeAdapter.setOnItemLongClickListener { adapter, view, position ->
            if (position < mPrivilegeAdapter.data.size) {
                val tempData = mPrivilegeAdapter.getItem(position)
                mListener?.onLongClick(EmojiType.PREROGATIVE, view, tempData)
                view.isSelected = true
            }
            return@setOnItemLongClickListener true
        }

        recyclerView_animation.layoutManager = GridLayoutManager(context, 4)
        recyclerView_animation.adapter = mAnimationAdapter
        mAnimationAdapter.setOnItemClickListener { adapter, view, position ->
            if (position < mAnimationAdapter.data.size) {
                val tempData = mAnimationAdapter.getItem(position)
                mListener?.onClick(EmojiType.ANIMATION, tempData)
            }
        }
    }

    /**
     * 显示对应的选中视图和RecyclerView
     */
    private fun showRecyclerView(view: View, rv: RecyclerView) {
        val viewList = arrayListOf<View>(iv_emoji, iv_prerogative, iv_high)
        val rvList = arrayListOf<RecyclerView>(recyclerView_emoji, recyclerView_prerogative, recyclerView_animation)

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
        showPrivilegeShade()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        assertView()
    }
}

/**
 * 普通图片Adapter
 */
private class NormalEmojiAdapter() : BaseQuickAdapter<Emotion, BaseViewHolder>(R.layout.vh_emotion_item_layout) {
    override fun convert(holder: BaseViewHolder, item: Emotion) {
        holder.setImageResource(R.id.image, item.drawableRes)
    }
}

/**
 * 特权图片Adapter
 */
private class PrerogativeEmojiAdapter : BaseQuickAdapter<Emotion, BaseViewHolder>(R.layout.vh_privilege_emotion_item_layout) {
    override fun convert(holder: BaseViewHolder, item: Emotion) {
        holder.setImageResource(R.id.image, item.drawableRes)
    }
}

/**
 * 动画表情Adapter
 */
private class AnimationAdapter : BaseQuickAdapter<Emotion, BaseViewHolder>(R.layout.vh_animation_emotion_item_layout) {
    override fun convert(holder: BaseViewHolder, item: Emotion) {
        holder.setImageResource(R.id.image, item.drawableRes)

    }
}
