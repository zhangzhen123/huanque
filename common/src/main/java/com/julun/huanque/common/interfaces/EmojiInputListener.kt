package com.julun.huanque.common.interfaces

import android.view.View
import com.julun.huanque.common.widgets.emotion.Emotion

/**
 *@创建者   dong
 *@创建时间 2020/7/16 17:27
 *@描述 表情输入监听
 */
interface EmojiInputListener {
    //
    fun onClick(type : String, emotion : Emotion)

    fun onLongClick(view : View,emotion : Emotion)
}