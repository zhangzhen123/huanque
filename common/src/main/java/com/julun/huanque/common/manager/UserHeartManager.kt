package com.julun.huanque.common.manager

import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.forms.UserOnlineHeartForm
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.AppService
import com.julun.huanque.common.suger.handleWithResponse
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.nothing
import com.julun.huanque.common.utils.SessionUtils
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
    private const val HEART_DURATION = 60L

    //最大重试次数
    private const val MAX_RQ = 3
    private val service: AppService by lazy { Requests.create(AppService::class.java) }
    private var currentProgramId: Long? = null
    private var onlineId: String? = null
    var disposable: Disposable? = null
    private fun startHeartbeat() {
        disposable?.dispose()
        disposable = Observable.interval(HEART_DURATION, HEART_DURATION, TimeUnit.SECONDS).subscribe {
            val form = UserOnlineHeartForm(currentProgramId, onlineId)
            service.alive(form).nothing()

        }
    }

    private var rqCount: Int = 0

    /**
     * 总的启动入口 控制心跳逻辑
     * [refresh]对于重新登录和刚注册的用户 必需重新获取onlineId
     */
    fun startCheckOnline(refresh: Boolean = false) {
        if (!SessionUtils.getIsRegUser() || !SessionUtils.getRegComplete()) {
            logger("只有注册用户才会启动心跳")
            return
        }
        if (refresh) {
            logger("需要刷新onlineId 重新获取online")
            startOnline()
        } else {
            if (onlineId == null) {
                logger("当前没有onlineId 开始获取online")
                startOnline()
            } else {
                if (disposable==null || disposable!!.isDisposed) {
                    logger("onlineId还在 重新启动心跳")
                    startHeartbeat()
                } else {
                    logger("onlineId还在 心跳还在")
                }
            }

        }
    }

    /**
     * 开始启动在线功能
     */
    private fun startOnline() {
        service.online().handleWithResponse({
            logger("online成功")
            onlineId = it.onlineId
            startHeartbeat()
        }, { e ->
            //刷新用户 不是500的错误以及其他非业务错误 这里进行重试
            if (e is ResponseError && (e.busiCode != 500) || (e !is ResponseError)) {
                logger("startOnline失败了 5秒后重试")
                Observable.timer(5, TimeUnit.SECONDS).subscribe {
                    if (rqCount < MAX_RQ) {
                        startOnline()
                        rqCount++
                    }

                }
            }


        }, specifiedCodes = intArrayOf(401, -1)
        )
    }

    fun stopBeat() {
        onlineId = null
        disposable?.dispose()
    }

    /**
     * 设置当前直播间id
     */
    fun setProgramId(programId: Long?) {
        currentProgramId = programId
    }

    fun getProgramId() = currentProgramId
}