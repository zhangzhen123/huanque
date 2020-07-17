package com.julun.huanque.common.helper

import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils


/**
 * 普通存储信息管理 处理不重要或者不用启动就初始化的内容 一般使用[SPUtils]进行存储
 */
object StorageHelper {

    private const val AD_VERSION: String = "AdVersion"
    /**
     * 保存ad
     */
    fun setAdVersion(headerPic: String) {
        SPUtils.commitString(AD_VERSION, headerPic)
    }

    fun getAdVersion() = SPUtils.getString(AD_VERSION, "")


}