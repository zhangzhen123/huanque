package com.julun.huanque.common.basic

import java.lang.reflect.Type

class Root<T> {

    var code: Int = -9999
    var message: Any? = null
    var data: T? = null

    /**
     * 不是"正常的"属性,只是为了方便在请求完成之后,如果出现了空值,根据这个类型参数来判断如何给 data属性赋值
     */
    var typeParameter: Type? = null

}