package com.julun.huanque.common.utils

object SessionUtils {

    private val SESSION_ID: String = "SESSION_ID"
    //直播权限
    private val PUSH_PERMIS: String = "push_permis"
    //登录用户id
    private val USER_ID: String = "USER_ID"
    private val NICK_NAME: String = "NICK_NAME"
    //融云聊天token
    private var RONG_IM_TOKEN: String = "RONG_IM_TOKEN"
    //是否注册用户
    private var IS_REG_USER: String = "IS_REG_USER"
//todo
//    var newSession: NewSession? = null
//
//    private fun setSession(session: NewSession) {
//        if (session == null) {
//            return
//        }
//        SessionManager.isCheckSession = true
//        ULog.i("Planet 设置sessionId 1")
//        setSessionId(session.sessionId)
//        setUserId(session.userId)
//        setRongImToken(session.imToken)
//        if (session.userType != null) {
//            setIsRegUser(!session.userType.equals(BusiConstant.UserType.Visitor))
//        } else {
//            setIsRegUser(false)
//        }
//        val agree = if (session.agreeUp) 1 else 0
//        setAgreeUp(agree)
//        setNickName(session.nickname)
//        setUserType(session.userType)
//        setPicId(session.headPic)
//        setPushPermis(session.hasPushPermis)
//        newSession = session
//    }

//    //与deleteSession合并 不再单独调用
//    private fun clearSession() {
//        ULog.i("Planet 设置sessionId 2")
//        setSessionId("")
//        setUserId(0)
//        setRongImToken("")
//        setIsRegUser(false)
//        setNickName("")
//        setUserType("")
//        setPushPermis("")
//        newSession = null
//    }

    fun getPushPermis(): String {
        return SharedPreferencesUtils.getString(PUSH_PERMIS, "")
    }

    fun setPushPermis(pushPermis: String) {
        SharedPreferencesUtils.commitString(PUSH_PERMIS, pushPermis)
    }


    fun getSessionId(): String {
        return SharedPreferencesUtils.getString(SESSION_ID, "")
    }

    fun setSessionId(sessionId: String) {
        ULog.i("Planet 设置sessionId = $sessionId")
        SharedPreferencesUtils.commitString(SESSION_ID, sessionId)
    }

    fun getUserId(): Int {
        return SharedPreferencesUtils.getInt(USER_ID, 0)
    }

    fun setUserId(userId: Int) {
        SharedPreferencesUtils.commitInt(USER_ID, userId)
    }

    fun getNickName(): String {
        return SharedPreferencesUtils.getString(NICK_NAME, "")
    }

    fun setNickName(nickName: String) {
        SharedPreferencesUtils.commitString(NICK_NAME, nickName)
    }

    fun getRongImToken(): String {
        return SharedPreferencesUtils.getString(RONG_IM_TOKEN, "")
    }

    fun setRongImToken(rongImToken: String) {
        SharedPreferencesUtils.commitString(RONG_IM_TOKEN, rongImToken)
    }

    fun getIsRegUser(): Boolean {
        //注册用户  同时  同意了隐私协议   才算可以正常使用用户
        return SharedPreferencesUtils.getBoolean(IS_REG_USER, false) /*&& getAgreeUp() == 1*/
    }

    fun setIsRegUser(isRegUser: Boolean) {
        SharedPreferencesUtils.commitBoolean(IS_REG_USER, isRegUser)
    }

    fun getIsRegUserNotCheckAgreeUp(): Boolean {
        return SharedPreferencesUtils.getBoolean(IS_REG_USER, false)
    }
//
//
//    //保存Session
//    fun saveSession(session: NewSession) {
//        //加入数据库有延迟，先保存在SP。保存数据库出错，直接删除
//        setSession(session)
//        Completable.fromAction {
//            val database = huanqueDatabase.getInstance()
//            try {
//                database.beginTransaction()
//                database.sessionDao().deleteSession()
//                database.sessionDao().insert(session)
//                database.setTransactionSuccessful()
//            } catch (e: Exception) {
//                e.printStackTrace()
//                ULog.i("Planet 设置sessionId 3")
//                clearSession()
//            } finally {
//                database.endTransaction()
//            }
//        }.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    //                    setSession(session)
//                }, { e ->
//                    e.printStackTrace()
//                    ULog.i("Planet 设置sessionId 4")
//                    clearSession()
//                })
//
//    }
//
//    //session过期 /  退出登录   删除  session
//    fun deleteSession(callback: () -> Unit = {}) {
//        val database = huanqueDatabase.getInstance()
//        Observable.create<NewSession> {
//            val session = database.sessionDao().getSingleSession()
//            if (session != null) {
//                it.onNext(session)
//                it.onComplete()
//            } else {
//                it.onComplete()
//            }
//        }.map {
//            try {
//                ULog.i("Planet 设置sessionId 5")
//                clearSession()//合并处理 不再单独调用
//                database.beginTransaction()
//                it.isLogin = false
//                database.sessionDao().insert(it)
////                database.messageDao().clearMessageBean()//清空系统消息
//                database.setTransactionSuccessful()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                database.endTransaction()
//            }
//        }
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    callback()
//                    CommonInit.getInstance().getCommonListener()?.logout()
////                    ZegoApiManager.getInstance().refreshUserInfo()
//                }, {})
//
//
//    }

    /**
     * 退出成功后操作以及回调
     */
    fun loginOutSuccess(callback: () -> Unit) {
//        deleteSession {
//            if (RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED)
//                RongCloudManager.logout(callback)
//            else {
//                callback()
//            }
//        }
    }
}