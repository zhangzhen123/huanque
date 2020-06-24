package com.julun.huanque.common.bean

import androidx.annotation.StringRes

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/24 11:21
 *
 *@Description: 请求状态
 *
 */
data class NetState(var state: NetStateType, var message: String = "")

/**
 * 应用状态
 */
enum class NetStateType {
    LOADING,
    SUCCESS,
    ERROR,
    //    EMPTY,
    NETWORK_ERROR,

//    TIP,
}