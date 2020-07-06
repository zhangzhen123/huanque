package com.julun.rnlib

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.facebook.infer.annotation.Assertions
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.devsupport.DoubleTapReloadRecognizer
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler

/**
 * Android 通过Activity打开RN页面
 */

class RNPageActivity : AppCompatActivity(), DefaultHardwareBackBtnHandler {

    companion object {
        /**
         * 通过fragment去加载一个rn页面 入口
         * [moduleName]需要打开的rn模块
         */
        fun start(activity: ComponentActivity, moduleName: String, initialProperties: Bundle? = null) {
            val intent = Intent(activity, RNPageActivity::class.java)
            intent.putExtra(RnConstant.MODULE_NAME, moduleName)
            intent.putExtra(RnConstant.INITIAL_PROPERTIES, initialProperties)
            activity.startActivity(intent)
        }
    }

    private lateinit var mReactRootView: ReactRootView
    private var mReactInstanceManager: ReactInstanceManager? = null
    private val mDeveloperSupport = true
    private var mDoubleTapReloadRecognizer: DoubleTapReloadRecognizer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mReactRootView = ReactRootView(this)
        mReactInstanceManager = RnManager.createReactInstanceManager(application)
        RnManager.curActivity=this
        val intent = intent
        val moduleName = intent.getStringExtra(RnConstant.MODULE_NAME)
        val initialProperties = intent.getBundleExtra(RnConstant.INITIAL_PROPERTIES)
        // 这个"App1"名字一定要和我们在index.js中注册的名字保持一致AppRegistry.registerComponent()
        mReactRootView.startReactApplication(mReactInstanceManager, moduleName, initialProperties)
        mDoubleTapReloadRecognizer = DoubleTapReloadRecognizer()
        mReactRootView.setEventListener {
            Log.d("RNPageFragment", "我已加载完成")
        }
        setContentView(mReactRootView)
    }

    override fun invokeDefaultOnBackPressed() {
        super.onBackPressed()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (useDeveloperSupport) {
            if (keyCode == KeyEvent.KEYCODE_MENU) { //Ctrl + M 打开RN开发者菜单
                mReactInstanceManager!!.showDevOptionsDialog()
                return true
            }
            val didDoubleTapR = Assertions.assertNotNull(mDoubleTapReloadRecognizer)
                .didDoubleTapR(keyCode, currentFocus)
            if (didDoubleTapR) { //双击R 重新加载JS
                mReactInstanceManager!!.devSupportManager.handleReloadJS()
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private val useDeveloperSupport: Boolean
        private get() = mReactInstanceManager != null && mDeveloperSupport

    override fun onPause() {
        super.onPause()
        mReactInstanceManager?.onHostPause(this)

    }

    override fun onResume() {
        super.onResume()
        mReactInstanceManager?.onHostResume(this, this)

    }

    override fun onBackPressed() {
        if (mReactInstanceManager != null) {
            mReactInstanceManager!!.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mReactInstanceManager?.onHostDestroy(this)
        mReactRootView.unmountReactApplication()
        RnManager.curActivity=null
    }


}