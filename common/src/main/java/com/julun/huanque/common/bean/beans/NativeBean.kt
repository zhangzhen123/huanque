package com.julun.huanque.common.bean.beans

enum class CollapsingToolbarLayoutState {
    EXPANDED,
    COLLAPSED,
    INTERNEDIATE
}

class TagManagerBean(var tagName: String = "", var num: Int = 0,var type:String="") {

    override fun toString(): String {
        return "TagManagerBean(tagName='$tagName', num=$num, type='$type')"
    }
}