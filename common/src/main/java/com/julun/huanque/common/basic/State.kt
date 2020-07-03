package com.julun.huanque.common.basic

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

/**
 * 这个标识作为此次查询的类型
 * [INIT]代表初始化加载 一般表示第一次
 * [REFRESH]代表下拉刷新
 * [LOAD_MORE]代表加载更多
 */
enum class QueryType {
    INIT, REFRESH, LOAD_MORE
}