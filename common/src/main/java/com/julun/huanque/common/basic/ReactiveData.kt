package com.julun.huanque.common.basic

import java.lang.Exception

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/3 14:34
 *
 *@Description: ReactiveData 封装一个请求响应的完整反馈包装类 反馈每一个请求的所有状态信息
 *
 */
open class ReactiveData<T>(
    var state: NetStateType = NetStateType.IDLE,
    private var data: T? = null,
    var error: ResponseError?=null
) {
    fun getT(): T {
        return data ?: throw Exception("只有在成功状态才有数据 请检测调用时机")
    }
}

