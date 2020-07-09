package com.julun.rnlib

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.devsupport.DoubleTapReloadRecognizer
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler
import com.julun.huanque.common.utils.ToastUtils

/**
 * 通过fragment去加载一个rn页面
 */
class RNPageFragment : Fragment(), DefaultHardwareBackBtnHandler {
    companion object {
        /**
         * 通过fragment去加载一个rn页面 入口
         * [moduleName]需要打开的rn模块
         */
        fun start(moduleName: String, initialProperties: Bundle? = null): RNPageFragment {
            return RNPageFragment().apply {
                arguments = Bundle().apply {
                    putString(RnConstant.MODULE_NAME, moduleName)
                    putBundle(RnConstant.INITIAL_PROPERTIES, initialProperties)
                }
            }
        }
    }

    private lateinit var mReactRootView: ReactRootView
    private var mReactInstanceManager: ReactInstanceManager? = null
    private val mDeveloperSupport = true
    private var mDoubleTapReloadRecognizer: DoubleTapReloadRecognizer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {

            mReactRootView = ReactRootView(requireActivity())
            mReactInstanceManager = RnManager.createReactInstanceManager(requireActivity().application)
            val moduleName = arguments?.getString(RnConstant.MODULE_NAME)
            val initialProperties = arguments?.getBundle(RnConstant.INITIAL_PROPERTIES)
            // 这个"App1"名字一定要和我们在index.js中注册的名字保持一致AppRegistry.registerComponent()
            mReactRootView.startReactApplication(mReactInstanceManager, moduleName, initialProperties)
            mReactRootView.setEventListener {
                Log.d("RNPageFragment", "我已加载完成")
            }
            mDoubleTapReloadRecognizer = DoubleTapReloadRecognizer()

        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.show("加载rn模块出错了")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return mReactRootView
    }

    override fun invokeDefaultOnBackPressed() {
        //todo
    }
//    fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
//        if (getUseDeveloperSupport()) {
//            if (keyCode == KeyEvent.KEYCODE_MENU) { //Ctrl + M 打开RN开发者菜单
//                mReactInstanceManager!!.showDevOptionsDialog()
//                return true
//            }
//            val didDoubleTapR = Assertions.assertNotNull(mDoubleTapReloadRecognizer)
//                .didDoubleTapR(keyCode, getCurrentFocus())
//            if (didDoubleTapR) { //双击R 重新加载JS
//                mReactInstanceManager!!.devSupportManager.handleReloadJS()
//                return true
//            }
//        }
//        return super.onKeyUp(keyCode, event)
//    }

//    private fun getUseDeveloperSupport(): Boolean {
//        return mReactInstanceManager != null && mDeveloperSupport
//    }

    override fun onPause() {
        super.onPause()
        mReactInstanceManager?.onHostPause(requireActivity())

    }

    override fun onResume() {
        super.onResume()
        mReactInstanceManager?.onHostResume(requireActivity(), this)

    }

    override fun onDestroy() {
        super.onDestroy()
        mReactInstanceManager?.onHostDestroy(requireActivity())
        mReactRootView.unmountReactApplication()
    }
}