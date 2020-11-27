package com.julun.huanque.common.interfaces

import android.view.MotionEvent

/**
 *@创建者   dong
 *@创建时间 2019/8/2 14:43
 *@描述 事件分发的监听 暂时只有DiapatchTouchEvent执行回调
 */
interface EventDispatchListener {

    /**
     * @return 当此点击是否生效 true 生效  false 不生效
     */
    fun onDispatch(ev: MotionEvent?): Boolean

}