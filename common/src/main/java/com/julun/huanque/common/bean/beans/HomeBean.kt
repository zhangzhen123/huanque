package com.julun.huanque.common.bean.beans

import com.chad.library.adapter.base.entity.MultiItemEntity

class HomeItemBean(var showType: Int, var content: Any) : MultiItemEntity {
    companion object {
        const val HEADER = 1//头部
        const val NORMAL = 2//普通样式
        const val GUIDE_TO_ADD_TAG = 3//引导添加标签
        const val GUIDE_TO_COMPLETE_INFORMATION = 4//引导完善资料
    }

    override fun getItemType(): Int {
        return showType
    }
}

/**
 *
 * [url]图片地址 为空代表需要添加
 * [res]加载本地工程资源
 *
 */
class PhotoBean(
    var url: String = "",
    var res: Int = 0

)

class HeaderNavigateBean(
    var type: Int = -1,
    var url: String = "",
    var title1: String = "",
    var title2: String = ""

) {
    companion object {
        const val MASK_QUEEN = 1
        const val ANONYMOUS_VOICE = 2
        const val LIVING = 3
        const val DAY_TOP = 4
    }
}