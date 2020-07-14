package com.julun.huanque.common.bean.beans

import java.io.Serializable

/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/14
 */
data class SysMsgContent(
    var context: SysMsgBean? = null,
    var eventCode: String = ""
) : Serializable

data class SysMsgBean(
    var subTitle: String = "",
    var touchValue: String = "",
    var touchType: String = "",
    var icon: String = "",
    var title: String = "",
    var body: String = ""
) : Serializable