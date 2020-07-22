package com.julun.huanque.common.interfaces

import android.view.View
import com.julun.huanque.common.bean.beans.IntimatePrivilege
import com.julun.huanque.common.widgets.emotion.Emotion

/**
 *@创建者   dong
 *@创建时间 2020/7/16 17:27
 *@描述 表情输入监听
 */
interface EmojiInputListener {
    //点击事件
    fun onClick(type: String, emotion: Emotion)

    //长按事件
    fun onLongClick(type: String, view: View, emotion: Emotion)

    //抬起事件
    fun onActionUp()

    //删除事件
    fun onClickDelete()

    //显示特权弹窗
    fun showPrivilegeFragment(code: String)
}