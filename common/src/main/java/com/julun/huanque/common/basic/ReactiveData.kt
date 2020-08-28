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
    private var queryType:QueryType=QueryType.INIT,//记录该次请求的类型 默认也就三种 初始化 刷新 加载更多
    var error: ResponseError? = null
) {

    fun getT(): T? {
        return data
    }

    /**
     * 如果知道自己请求在合适的时机 可以使用这个调用
     */
    fun requireT(): T {
        return data ?: throw Exception("只有在成功状态才有数据 请检测调用时机")
    }

    fun isSuccess(): Boolean {
        return state == NetStateType.SUCCESS
    }

    /**
     * 该次请求的类型是不是刷新型的 主要用于列表请求
     */
    fun isRefresh():Boolean{
        return queryType!=QueryType.LOAD_MORE
    }
}

