package com.julun.huanque.common.bean.beans

open class RootListLiveData<T>(
        var isPull: Boolean = false,
        var list: List<T> = arrayListOf(),
        var hasMore: Boolean = false
)