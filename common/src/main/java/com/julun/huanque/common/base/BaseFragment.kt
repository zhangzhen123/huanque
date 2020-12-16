package com.julun.huanque.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.julun.huanque.common.utils.ULog
import com.trello.rxlifecycle4.components.support.RxFragment

/**
 *@Anchor zhangzhen
 *@Date 2020/6/27 12:31
 *@Description Fragment的基本封装
 *
 **/

abstract class BaseFragment : RxFragment(), BaseContainer {

    protected val logger = ULog.getLogger(this.javaClass.name)
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val layoutId = getLayoutId()
        if (layoutId <= 0) {
            throw NotImplementedError("设置的layoutId 必须是真实存在的")
        }
        logger.info("onCreateView: ")
        registerSelfAsEventHandler()
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view, savedInstanceState)
        initEvents(view)
    }

    /**
     * 供父类调用
     */
    open fun onParentHiddenChanged(hidden: Boolean) {
        logger.info("当前的父类界面开始显隐hide=$hidden")
    }
    override fun onDestroy() {
        unregisterSelfAsEventHandler()
        onViewDestroy()
        super.onDestroy()
    }

}
