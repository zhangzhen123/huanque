package com.julun.huanque.common.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.julun.huanque.common.interfaces.EmojiInputListener
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.widgets.SinglePanelView
import com.julun.huanque.common.widgets.emotion.Emotion

/**
 *@创建者   dong
 *@创建时间 2020/8/14 11:42
 *@描述 私聊表情使用的adapter
 */
class PanelAdapter(val typeList: List<String>, val context: Context) : PagerAdapter() {
    var outEmojiInputListener: EmojiInputListener? = null
    private val innerEmojiInputListener = object : EmojiInputListener {
        override fun onClick(type: String, emotion: Emotion) {
            outEmojiInputListener?.onClick(type, emotion)
        }

        override fun onLongClick(type: String, view: View, emotion: Emotion) {
            outEmojiInputListener?.onLongClick(type, view, emotion)
        }

        override fun onActionUp() {

        }

        override fun onClickDelete() {
        }

        override fun showPrivilegeFragment(code: String) {
            outEmojiInputListener?.showPrivilegeFragment(code)
        }

    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount() = typeList.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (ForceUtils.isIndexNotOutOfBounds(position, typeList)) {
            val singleView = SinglePanelView(typeList[position], context = context)
            singleView.mListener = innerEmojiInputListener
            container.addView(singleView)
            return singleView
        }
        return 1
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        if (obj is View) {
            container.removeView(obj)
        }
    }
}