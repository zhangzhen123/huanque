package com.julun.huanque.common.base

import android.annotation.TargetApi
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.julun.huanque.common.utils.ULog
import com.trello.rxlifecycle4.components.support.RxFragment

/**
 * Created by nirack on 16-10-27.
 */
abstract class BaseFragment : RxFragment(), BaseContainer {

    protected val TAG: String by lazy { this.javaClass.name }
    protected val logger = ULog.getLogger(this.javaClass.name)!!

    //    var supportedChildFragmentManager: FragmentManager? = null
//        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            childFragmentManager
//        } else {
//            contextActivity?.fragmentManager
//        }
    var supportedChildFragmentManager: FragmentManager? = null
        get() = childFragmentManager

    override fun onDetach() {
        super.onDetach()
/*        try {
            val childFragmentManager = Fragment::class.java.getDeclaredField("mChildFragmentManager")
            if (childFragmentManager == null) {
//                supportedChildFragmentManager = contextActivity?.fragmentManager
                supportedChildFragmentManager = null
            } else {
                childFragmentManager.isAccessible = true
                childFragmentManager.set(this, null)
            }

            supportedChildFragmentManager = null

        } catch (e: NoSuchFieldException) {
//            reportCrash(e)    //这个问题不需要再上报了,属于正常情况
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }*/

    }


    init {
        ULog.d(TAG, "BaseFragment() called")
        val arguments = Bundle()
        setArguments(arguments)//保证在后续 #getArguments 调用的时候不返回 null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layoutId = getLayoutId()
        if (layoutId <= 0) {
            throw NotImplementedError("设置的layoutId 必须是真实存在的")
        }
        ULog.i(TAG, "onCreateView: ")
        registerSelfAsEventHandler()
        return inflater.inflate(layoutId, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val start: Long = System.currentTimeMillis()
        if (isAdded) {
            initViews(requireView(), savedInstanceState)
        }
        initEvents(requireView())
        val end: Long = System.currentTimeMillis()
        ULog.i(TAG, " onActivityCreated 初始化 fragment <${this.javaClass.name}> 耗时: ${end - start}")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        val arguments = arguments ?: //到这里的时候，如果不手动调用  setArguments(null) 这里是不会抛错的.
//                throw RuntimeException("即使 无具体的参数需要设置也不要手动设置一个null ，也不要设置一个 null ,不要设置一个 null ")
        if (savedInstanceState != null) {
            arguments?.putAll(savedInstanceState)
        }
        super.onCreate(savedInstanceState)
    }

    @TargetApi(23)
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDestroy() {
        unregisterSelfAsEventHandler()
        onViewDestroy()
        super.onDestroy()
    }

    //为了收集当前的生命周期状态

    override fun onResume() {
        super.onResume()
    }


    override fun onStop() {
        super.onStop()
    }


}
