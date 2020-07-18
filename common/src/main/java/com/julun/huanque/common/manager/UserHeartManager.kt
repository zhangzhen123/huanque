package com.julun.huanque.common.manager

import com.julun.huanque.common.bean.forms.UserOnlineHeartForm
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.AppService
import com.julun.huanque.common.suger.handleWithResponse
import com.julun.huanque.common.suger.logger
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/18 16:18
 *
 *@Description: 用户心跳管理器
 *
 */
object UserHeartManager {
    private const val HEART_DURATION=60L
    private val service: AppService by lazy { Requests.create(AppService::class.java) }
    private var currentProgramId: Int? = null
    var disposable: Disposable? = null
    fun startHeartbeat() {
        disposable?.dispose()
        disposable = Observable.interval(HEART_DURATION, HEART_DURATION, TimeUnit.SECONDS).subscribe {
            val form = UserOnlineHeartForm(currentProgramId)
            service.alive(form).handleWithResponse({
                logger("心跳成功")
            }
            )

        }
    }

    fun stopBeat() {
        disposable?.dispose()
    }
}