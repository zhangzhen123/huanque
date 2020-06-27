package com.julun.huanque.common.basic

/**
 *
 *@author zhangzhen
 *@data 2018/8/9
 * 封装一个容器基类 统一传入数据 isPull是否下拉  noMore是不是最后一页
 * 专门处理列表
 **/
open class RootListLiveData<T>(
    var isPull: Boolean = false,
    var list: List<T> = arrayListOf(),
    var hasMore: Boolean = false,
    var extDataJson: String? = null
)

