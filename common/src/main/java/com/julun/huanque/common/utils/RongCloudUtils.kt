package com.julun.huanque.common.utils

import com.julun.huanque.common.manager.SessionManager

import io.rong.imlib.RongIMClient

/**
 * Created by dong on 2017/11/21.
 */
object RongCloudUtils {
    /**
     * 融云是否需要重连
     * 根据融云技术指导   只有在 NETWORK_UNAVAILABLE 状态下才需要手动重连
     */
    fun RongCloudNeedConnectedManually() = RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.NETWORK_UNAVAILABLE
            || RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.DISCONNECTED

    /**
     * 融云连接是否可用
     * 只有在  CONNECTED  状态下才是可用
     */
    fun RongCloudIsConnected() = RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED

    /**
     * 是否是游客身份
     * 有sessionid同时isRegUser= false为游客
     */
    fun isGuest() = SessionUtils.getSessionId().isNotEmpty() && !SessionUtils.getIsRegUser()

    /**
     * 是否有身份
     * sessionId不为空  表示有身份
     */
    fun isIdentify() = SessionUtils.getSessionId().isNotEmpty()

    /**
     * 是否需要验证session
     */
    fun isNeedCheckSession() = SessionUtils.getSessionId().isNotEmpty() && !SessionManager.isCheckSession
}