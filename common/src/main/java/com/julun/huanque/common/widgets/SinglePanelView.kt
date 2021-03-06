package com.julun.huanque.common.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.R
import com.julun.huanque.common.adapter.AnimationAdapter
import com.julun.huanque.common.adapter.NormalEmojiAdapter
import com.julun.huanque.common.adapter.PrerogativeEmojiAdapter
import com.julun.huanque.common.constant.EmojiType
import com.julun.huanque.common.interfaces.EmojiInputListener
import com.julun.huanque.common.widgets.layoutmanager.WrapContentGridLayoutManager
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.emotion.Emotion
import com.julun.huanque.common.widgets.emotion.Emotions
import kotlinx.android.synthetic.main.panel_single_emoji.view.*

/**
 *@创建者   dong
 *@创建时间 2020/8/14 11:52
 *@描述
 * @param type 表情类型
 */
class SinglePanelView(val type: String, context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    constructor(type: String, context: Context) : this(type, context, null)

    private var mNeedLevel = 0

    private var mCurrentLevel = 0

    private var hasManager : Boolean = false

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
                WrapContentGridLayoutManager(context, 4)
            }
            EmojiType.ANIMATION -> {
                //动画表情
                adapter = PrerogativeEmojiAdapter().apply { setList(Emotions.getEmotions(EmojiType.ANIMATION)) }
                WrapContentGridLayoutManager(context, 4)
            }
            else -> {
                //普通表情
                adapter = NormalEmojiAdapter().apply { setList(Emotions.getEmotions(EmojiType.NORMAL)) }
                WrapContentGridLayoutManager(context, 7)
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
            ToastUtils.show("亲密等级达到Lv3才能发送专属表情哦")
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
    fun setIntimate(needLevel: Int, currentLevel: Int,hasManager : Boolean = false) {
        mNeedLevel = needLevel
        mCurrentLevel = currentLevel
        this.hasManager = hasManager

        showPrivilegeShade()
    }

    /**
     * 特权表情的遮罩是否显示
     */
    private fun showPrivilegeShade() {
//        if (!hasManager &&  type == EmojiType.PREROGATIVE && (mNeedLevel > mCurrentLevel || mCurrentLevel == 0)) {
//            //需要显示特权表情遮罩
//            view_shade.show()
//            tv_privilege_attetnion.show()
//            tv_privilege_attetnion.text = "亲密等级Lv3可解锁"
//
//        } else {
            view_shade.hide()
            tv_privilege_attetnion.hide()
//        }

    }

}