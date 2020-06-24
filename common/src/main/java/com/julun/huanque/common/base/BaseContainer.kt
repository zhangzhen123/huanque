package com.julun.huanque.common.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.julun.huanque.common.utils.ForceUtils
import org.greenrobot.eventbus.EventBus

/**
 * Created by nirack on 16-10-28.
 */
interface BaseContainer {

    fun isRegisterEventBus(): Boolean {
        return false
    }

    fun unregisterSelfAsEventHandler() {
//        EventDispatcherCenter.unRegisterEventHandler(this)
        if (isRegisterEventBus())
            EventBus.getDefault().unregister(this)
    }

    fun registerSelfAsEventHandler() {
//        EventDispatcherCenter.registerEventHandler(this)
        if (isRegisterEventBus())
            EventBus.getDefault().register(this)
    }

//    override fun getRequestCallerId(): String {
//        if (this is BaseActivity){
//            return this.UUID
//        }else if (this is BaseFragment) {
//            return this.UUID
//        }else{
//            throw NotImplementedError("BaseContainer 的实例,在既不是 BaseActivity 又不是 BaseFragment 的时候,需要自己实现 getRequestCallerId() 方法 ")
//        }
//    }

    /**
     * 返回容器需要的layout文件
     */
    fun getLayoutId(): Int

    /**
     * 初始化view相关的信息
     */
    fun initViews(rootView: View, savedInstanceState: Bundle?)

    /**
     * @param rootView activity/fragment 的rootView
     * 初始化事件相关的信息
     */
    fun initEvents(rootView: View) {
    }

    fun onViewDestroy() {
//        this.cancelAllRequest()
    }


    /**
     * 为 {@link Activity#startActivityForResult} 提供更便利的入口
     *
     * @see
     */
    fun startActivityForResult(target: Class<out BaseActivity>, requestCode: Int, vararg extra: Bundle): Unit {
        val bundle: Bundle = Bundle().apply { extra.forEach { putAll(it) } }
        if (this is BaseActivity) {
            this.startActivityForResult(Intent(this, target).apply { putExtras(bundle) }, requestCode)
        } else if (this is BaseFragment) {
            this.startActivityForResult(Intent(this.activity, target).apply { putExtras(bundle) }, requestCode)
        } else {
            throw UnsupportedOperationException("${this.javaClass.name} 对象不是视图容器...")
        }
    }

    /**
     * activity 跳转
     * @param next
     * *
     * @param extra
     */
    fun jump(next: Class<out Activity>, intentFlag: Int = 0, extra: Bundle? = null) {
//        val bundle: Bundle = Bundle().apply { extra.forEach { putAll(it) } }
        if (this is BaseActivity) {
            val intent = Intent(this, next).apply {
                this.flags = intentFlag
                extra?.let { putExtras(it) }
            }
            if (ForceUtils.activityMatch(intent)) {
                startActivity(intent)
            }
        } else if (this is BaseFragment) {
            val intent = Intent(activity, next).apply { this.flags = intentFlag }.apply {
                this.flags = intentFlag
                extra?.let { putExtras(it) }
            }
            if (ForceUtils.activityMatch(intent)) {
                activity?.startActivity(intent)
            }
        } else {
            throw UnsupportedOperationException("${this.javaClass.name} 对象不是视图容器...")
        }
    }

}