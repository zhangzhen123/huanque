package com.julun.huanque.common.bean

/**
 *
 *@author zhangzhen
 *@data 2018/8/9
 * 封装一个容器基类 统一传入数据 isPull是否下拉  noMore是不是最后一页
 * 专门处理列表
 *
 * @alter WanZhiYuan
 * @since 4.28
 * @date 2020/03/17
 * @detail 新增参数 type 数据类型
 **/
open class RootListLiveData<T>(
    var isPull: Boolean = false,
    var list: List<T> = arrayListOf(),
    var hasMore: Boolean = false,
    var extDataJson: String? = null,
    var extData: Data? = null,
    var type: Any? = null
)

/**
 * 接口统一扩展参数
 */
class Data {
    var totalCount: Int = 0

    //4.17 粉丝团小主回馈扩展参数
    /** 粉丝等级 **/
    var fansLevel: Int? = null
    /** 已收到回馈次数 **/
    var gotTimes: Int? = null
    /** 剩余回馈次数 **/
    var leftTimes: String? = null
}
