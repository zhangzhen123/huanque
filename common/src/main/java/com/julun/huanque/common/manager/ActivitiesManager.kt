package com.julun.huanque.common.manager

import android.app.Activity
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.logger
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
    fun finishActivity(className: String) {
        var act: Activity? = null
        run {
            activities.forEach {
                if (className.contains(it.localClassName)) {
                    act = it
                    return@run
                }
            }
        }
        act?.finish()
    }

    /**
     * 关闭特定页面之外的其他页面
     */
    fun finishActivityExcept(className: String) {
        activities.forEach {
            if (!className.contains(it.localClassName)) {
                it.finish()
            } else {
                logger("Except localClassName = ${it.localClassName}")
            }
        }
    }

    /**
     * 是否包含某个Activity
     * @param activityName activity完整类名
     */
    fun hasActivity(activityName: String): Boolean {
        activities.forEach {
            if (activityName.contains(it.localClassName)) {
                return true
            }
        }
        return false
    }
}