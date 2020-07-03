package com.julun.huanque.common.utils

import java.lang.reflect.ParameterizedType

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/1 10:03
 *
 *@Description: 通用的工具类 跟业务无关联
 *
 */
object CommonUtil {
    /**
     * 通过泛型去反射出Class
     */
    fun <T> getClass(t: Any): Class<T> {
        // 通过反射 获取父类泛型 (T) 对应 Class类
        return (t.javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[0]
                as Class<T>
    }
}