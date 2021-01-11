package com.julun.huanque.core.utils

import android.app.Activity
import android.content.Intent
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.core.ui.homepage.*

/**
 *@创建者   dong
 *@创建时间 2021/1/11 9:12
 *@描述 编辑页面使用的工具类
 */
object EditUtils {
    //需要跳转的class列表
    val jumpList = mutableListOf<Class<out BaseActivity>>()


    /**
     * 跳转下一次activity
     * @param act 当前的activity
     * @param index 当前的index
     */
    fun jumpActivity(act: Activity, nextIndex: Int) {
        val jumpClass = jumpList.getOrNull(nextIndex) ?: return
        val intent = Intent(act, jumpClass)
        if (ForceUtils.activityMatch(intent)) {
            intent.putExtra(ParamConstant.Index, nextIndex)
            act.startActivity(intent)
        }
    }

    /**
     *往下一级跳转
     */
    fun goToNext(act: Activity, currentIndex: Int) {
        if (currentIndex < 0 || jumpList.getOrNull(currentIndex + 1) == null) {
            //没有跳转逻辑或者已经是最后一个
            act.finish()
        } else {
            //需要继续跳转
            jumpActivity(act, currentIndex + 1)
            act.finish()
        }
    }


}