package com.julun.huanque.common.manager

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.utils.ULog


/**
 * 对于有些弹窗需要在app内部每个页面都存在 相当于悬浮模式的弹窗管理显示
 *
 * 一次只能支持一个弹窗
 */
object GlobalDialogManager {
    val logger = ULog.getLogger("GlobalDialogManager")

    //有些弹窗不能在一些指定Activity页面弹出 这里记录下来每次弹出时过滤处理
    private val mGlobalDialogNoActivityListMap = hashMapOf<BaseDialogFragment, MutableList<String>>()


    private var currentAct: Activity? = null
    private val mLifecycleCallbacks: Application.ActivityLifecycleCallbacks =
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                logger.info("onActivityCreated=$activity")
            }

            override fun onActivityStarted(activity: Activity) {
                logger.info("onActivityStarted=$activity")
                currentAct = activity
            }

            override fun onActivityResumed(activity: Activity) {
                logger.info("onActivityResumed=$activity")
                currentAct = activity
                showCurrentDialog()
            }

            override fun onActivityPaused(activity: Activity) {
                logger.info("onActivityPaused=$activity")
                currentDialog?.dismiss()
            }

            override fun onActivityStopped(activity: Activity) {
                logger.info("onActivityStopped=$activity")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                logger.info("onActivityDestroyed=$activity")
            }
        }

    /**
     * 使用前必须先初始化
     */
    fun init(app: Application) {
        app.registerActivityLifecycleCallbacks(mLifecycleCallbacks)
    }


    /**
     * 同一时间只能处理一个弹窗
     */
    private var currentDialog: BaseDialogFragment? = null

    fun showDialog(dialog: BaseDialogFragment, noActivityList: MutableList<String>? = null) {
        currentDialog?.dismiss()
        currentDialog = dialog
        if (noActivityList != null) {
            mGlobalDialogNoActivityListMap[dialog] = noActivityList
        }
        showCurrentDialog()
    }

    private fun showCurrentDialog() {
        val act = currentAct ?: return
        val dialog = currentDialog ?: return
        var canShow = true
        val mNoFateActivityList = mGlobalDialogNoActivityListMap[dialog]
        run {
            mNoFateActivityList?.forEach {
                if (it.contains(act.localClassName)) {
                    canShow = false
                    return@run
                }
            }
        }
        if (!canShow) {
            return
        }
        dialog.dismiss()
        if (act is AppCompatActivity) {
            dialog.show(act.supportFragmentManager, "${dialog.javaClass.name}")
        }
    }

    fun closeDialog() {
        currentDialog?.dismiss()
        mGlobalDialogNoActivityListMap.clear()
        currentDialog = null
    }
}