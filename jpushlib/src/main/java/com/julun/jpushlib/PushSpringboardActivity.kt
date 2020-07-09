package com.julun.jpushlib

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.julun.huanque.common.utils.ULog
import com.julun.jpushlib.JPushUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 *
 *@author zhangzhen
 *@data 2019/5/30
 *一个透明没有界面的activity作为推送跳板
 *
 **/


class PushSpringboardActivity : AppCompatActivity() {
    val logger = ULog.getLogger("PushSpringboardActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        logger.info("onCreate")
        super.onCreate(savedInstanceState)
        val window = window
        // 设置窗口位置在左上角
        window.setGravity(Gravity.START or Gravity.TOP)
        val params = window.attributes
        params.x = 0
        params.y = 0
        params.width = 1
        params.height = 1
        window.attributes = params
    }

    override fun onNewIntent(intent: Intent?) {
        logger.info("onNewIntent")
        super.onNewIntent(intent)
        if (!JPushUtil.handleOpenClick(this, intent)) {
            JPushUtil.shouldOpenMain(this)
        }
        delayToFinish()
    }

    //判断app在前台的状态在这里比较准确
    override fun onResume() {
        logger.info("onResume")
        super.onResume()
        if (!JPushUtil.handleOpenClick(this, intent)) {
            JPushUtil.shouldOpenMain(this)
        }
        delayToFinish()
    }

    private var dispose: Disposable? = null

    private fun delayToFinish() {
        dispose = Observable.timer(50, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            finish()
        }
    }

    override fun onDestroy() {
        dispose?.dispose()
        logger.info("onDestroy")
        super.onDestroy()
    }

}