package com.julun.huanque.common.bean


open class BaseData {
    var roomIds: ArrayList<String>? = null
    var msgId: String = ""
    var targetType: String? = null// Room、Broadcast、User
    var msgType: String = ""
    var userIds: ArrayList<Int>? = null
    //房间类型
    var roomTypes: MutableList<String> = mutableListOf()
    var data: Any? = null
}