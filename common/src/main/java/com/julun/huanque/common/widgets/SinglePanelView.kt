package com.julun.huanque.common.widgets

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.R
import com.julun.huanque.common.adapter.AnimationAdapter
import com.julun.huanque.common.adapter.NormalEmojiAdapter
import com.julun.huanque.common.adapter.PrerogativeEmojiAdapter
import com.julun.huanque.common.constant.EmojiType
import com.julun.huanque.common.interfaces.EmojiInputListener
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.widgets.emotion.Emotion
import com.julun.huanque.common.widgets.emotion.Emotions
import kotlinx.android.synthetic.main.panel_single_emoji.view.*
import kotlinx.android.synthetic.main.panel_view_private_chat.view.*

/**
 *@创建者   dong
 *@创建时间 2020/8/14 11:52
 *@描述
 * @param type 表情类型
 */
class SinglePanelView(val type: String, context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    constructor(type: String, context: Context?) : this(type, context, null)

    private var mNeedLevel = 0

    private var mCurrentLevel = 0

    init {
        context?.let { con ->
            LayoutInflater.from(con).inflate(R.layout.panel_single_emoji, this)
            if (type == EmojiType.PREROGATIVE) {
                view_shade.show()
                tv_privilege_attetnion.show()
            }
            initRecyclerView()
            initListener()
        }
    }

    //点击表情的回调
    var mListener: EmojiInputListener? = null

    private fun initRecyclerView() {
        val adapter: BaseQuickAdapter<Emotion, BaseViewHolder>
        val gridLayoutManager = when (type) {
            EmojiType.PREROGATIVE -> {
                //特权表情
                adapter = AnimationAdapter().apply { setList(Emotions.getEmotions(EmojiType.PREROGATIVE)) }
                GridLayoutManager(context, 4)
            }
            EmojiType.ANIMATION -> {
                //动画表情
                adapter = PrerogativeEmojiAdapter().apply { setList(Emotions.getEmotions(EmojiType.ANIMATION)) }
                GridLayoutManager(context, 4)
            }
            else -> {
                //普通表情
                adapter = NormalEmojiAdapter().apply { setList(Emotions.getEmotions(EmojiType.NORMAL)) }
                GridLayoutManager(context, 7)
            }
        }
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener { ad, view, position ->
            if (position < ad.data.size) {
                val tempData = ad.getItem(position) as? Emotion
                mListener?.onClick(type, tempData ?: return@setOnItemClickListener)
            }
        }
        adapter.setOnItemLongClickListener { ad, view, position ->
            if (position < ad.data.size) {
                val tempData = ad.getItem(position) as? Emotion
                mListener?.onLongClick(type, view, tempData ?: return@setOnItemLongClickListener true)
                view.isSelected = true
            }
            return@setOnItemLongClickListener true
        }
    }

    /**
     * 设置监听
     */
    private fun initListener() {
        tv_privilege_attetnion.onClickNew {
            //弹窗说明弹窗
            mListener?.showPrivilegeFragment("ZSBQ")
        }

        view_shade.onClickNew {
            //屏蔽事件
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

        showPrivilegeShade()
    }

    /**
     * 特权表情的遮罩是否显示
     */
    private fun showPrivilegeShade() {
        if (type == EmojiType.PREROGATIVE && mNeedLevel > mCurrentLevel) {
            //需要显示特权表情遮罩
            view_shade.show()
            tv_privilege_attetnion.show()

            val str = "亲密度等级$mNeedLevel，立即解锁"
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

        } else {
            view_shade.hide()
            tv_privilege_attetnion.hide()
        }

    }

}