package com.julun.huanque.common.basic

import java.io.Serializable


/**
 * 用于tab+fragment中数据的封装传递
 */
data class TabBean(var type:String,var title:String="",var select:Boolean=false,var num:Int=0):Serializable