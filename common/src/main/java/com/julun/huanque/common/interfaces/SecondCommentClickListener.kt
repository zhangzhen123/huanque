package com.julun.huanque.common.interfaces

import com.julun.huanque.common.bean.beans.DynamicComment

/**
 *@创建者   dong
 *@创建时间 2020/11/27 13:30
 *@描述 评论列表 点击
 */
interface SecondCommentClickListener {
    //二级评论列表 点击
    /**
     * @param secondComment 2级评论对象
     */
    fun secondCommentClick(secondComment: DynamicComment)

}