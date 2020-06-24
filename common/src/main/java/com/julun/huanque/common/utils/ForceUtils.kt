package com.julun.huanque.common.utils

import android.content.Intent
import android.content.pm.PackageManager
import com.julun.huanque.common.init.CommonInit

/**
 * 判断数组下标是否越界的工具类
 */
object ForceUtils {

    /**
     * 检测跳转的activity是否存在
     * @return true 表示activity存在，可以跳转     false表示activity不符合跳转条件
     */
    fun activityMatch(intent: Intent): Boolean {
        return CommonInit.getInstance().getApp().packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
    }


    fun isIndexNotOutOfBounds(position: Int, collection: Collection<*>): Boolean {
        return isCollectionNotEmpty(collection) && position < collection.size && position >= 0
    }

    private fun isCollectionEmpty(collection: Collection<*>?, vararg args: Collection<*>): Boolean {
        if (collection == null || collection.isEmpty()) {
            return true
        }
        for (arg in args) {
            if (arg == null || arg.isEmpty()) {
                return true
            }
        }
        return false
    }

    private fun isCollectionNotEmpty(collection: Collection<*>, vararg args: Collection<*>): Boolean {
        return !isCollectionEmpty(collection, *args)
    }
}