package com.julun.huanque.common.basic

/**
 *
 *@author zhangzhen
 *@data 2018/8/9
 * 封装一个容器基类 统一传入数据 [isPull]是否下拉刷新 [hasMore]还有没有更多数据
 * 专门处理列表
 **/
open class RootListData<T>(
    var isPull: Boolean = false,
    var list: MutableList<T> = mutableListOf(),
    var hasMore: Boolean = false,
    var extDataJson: String? = null
)

