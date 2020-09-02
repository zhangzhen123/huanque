package com.julun.huanque.common.manager

import android.app.Activity
import com.julun.huanque.common.init.CommonInit
import java.util.*

/**
 * Created by nirack on 17-5-12.
 * 只管理activity的生命周期
 */
class ActivitiesManager private constructor() {
    companion object {
        //使用 by lazy 实现单例
        val INSTANCE: ActivitiesManager by lazy { ActivitiesManager() }
    }

    private val activities = Stack<Activity>()
    fun push(activity: Activity) {
        activities.push(activity)
    }

    val totalActivityCount: Int
        get() = activities.size

    val currentActivity: Activity?
        get() = if (activities.size > 0) {
            activities.peek()
        } else {
            null
        }

    /**
     * 关闭app
     */
    fun finishApp() {
        while (activities.size > 0) {
            val pop = activities.pop()
            pop?.finish()
        }
        CommonInit.getInstance().setCurrentActivity(null)
    }

    /**
     * finish掉栈顶的Activity
     */
    fun finishTopActivity() {
        if (activities.size > 0) {
            val pop = activities.pop()
            pop?.finish()
        }
    }

    fun removeActivities(activity: Activity) {
        activities.remove(activity)
    }

    /**
     * 移除特定的Activity
     */
    fun removeActivity(className: String) {
        var act: Activity? = null
        activities.forEach {
            if (it.localClassName == className) {
                act = it
                return@forEach
            }
        }
        act?.finish()
    }
}