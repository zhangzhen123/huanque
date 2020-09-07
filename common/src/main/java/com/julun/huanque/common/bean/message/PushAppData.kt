package com.julun.huanque.common.bean.message

// touchType, touchValue,  ， title, 是标题， pic
data class PushAppData(var touchType: String = "", var touchValue: String = "", var title: String = "", var pic: String = "") {
    override fun toString(): String {
        return "PushAppData(touchType='$touchType', touchValue='$touchValue', title='$title', pic='$pic')"
    }
}