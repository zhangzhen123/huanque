package com.julun.huanque.common.utils

import com.julun.huanque.common.database.table.Session
import com.julun.huanque.common.manager.SessionManager

/**
 * 重要的信息和启动配参放在这里 不重要的参数放在[com.julun.huanque.common.helper.StorageHelper]中 以提高初始化效率
 */
object SessionUtils {
    private val SESSION_ID: String = "SESSION_ID"

    //登录用户id
    private val USER_ID: String = "USER_ID"

    //用户昵称
    private val NICK_NAME: String = "NICK_NAME"

    //头像
    private val HEAD_PIC: String = ""

    //用户类型
    private val USER_TYPE = "USER_TYPE"

    //声网token
    private val VOICE_TOKEN = "VOICE_TOKEN"

    //是否是新用户
    private val NEW_USER = "NEW_USER"

    //数据是否完整
    private val REG_COMPLETE = "REG_COMPLETE"

    //融云聊天token
    private var RONG_IM_TOKEN: String = "RONG_IM_TOKEN"

    //是否注册用户
    private var REG_USER = "REG_USER"

    //用户性别
    private var SEX = "SEX"

    fun setSession(session: Session) {
        SessionManager.isCheckSession = true
        setSessionId(session.sessionId)
        setUserId(session.userId)
        setNickName(session.nickname)
        setHeaderPic(session.headPic)
        setUserType(session.userType)
        setAgoraToken(session.voiceToken)
        setNewUser(session.newUser)
        setRegComplete(session.regComplete)
        setRongImToken(session.imToken)
        setIsRegUser(session.regUser)
        setSex(session.sex)
    }

    //与deleteSession合并 不再单独调用
    fun clearSession() {
        setSessionId("")
        setUserId(0)
        setNickName("")
        setHeaderPic("")
        setUserType("")
        setAgoraToken("")
        setNewUser(false)
        setRegComplete(false)
        setRongImToken("")
        setIsRegUser(false)
    }


    /**
     * 保存头像
     */
    fun setHeaderPic(headerPic: String) {
        SharedPreferencesUtils.commitString(HEAD_PIC, headerPic)
    }

    fun getHeaderPic() = SharedPreferencesUtils.getString(HEAD_PIC, "")

    /**
     * 保存用户类型
     */
    fun setUserType(userType: String) {
        SharedPreferencesUtils.commitString(USER_TYPE, userType)
    }

    fun getUserType() = SharedPreferencesUtils.getString(USER_TYPE, "")

    /**
     * 设置声网token
     */
    fun setAgoraToken(agoraToken: String) {
        SharedPreferencesUtils.commitString(VOICE_TOKEN, agoraToken)
    }

    fun getAgoraToken() = SharedPreferencesUtils.getString(VOICE_TOKEN, "")

    /**
     * 设置是否是新用户
     */
    fun setNewUser(newUser: Boolean) {
        SharedPreferencesUtils.commitBoolean(NEW_USER, newUser)
    }

    fun getNewUser() = SharedPreferencesUtils.getBoolean(NEW_USER, false)

    /**
     * 设置数据是否完整
     */
    fun setRegComplete(complete: Boolean) {
        SharedPreferencesUtils.commitBoolean(REG_COMPLETE, complete)
    }

    fun getRegComplete() = SharedPreferencesUtils.getBoolean(REG_COMPLETE, false)


    fun getSessionId(): String {
        return SharedPreferencesUtils.getString(SESSION_ID, "")
    }

    fun setSessionId(sessionId: String) {
        ULog.i("Planet 设置sessionId = $sessionId")
        SharedPreferencesUtils.commitString(SESSION_ID, sessionId)
    }

    fun getUserId(): Long {
        return SharedPreferencesUtils.getLong(USER_ID, 0)
    }

    fun setUserId(userId: Long) {
        SharedPreferencesUtils.commitLong(USER_ID, userId)
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
        return SharedPreferencesUtils.getBoolean(REG_USER, false) /*&& getAgreeUp() == 1*/
    }

    fun setIsRegUser(isRegUser: Boolean) {
        SharedPreferencesUtils.commitBoolean(REG_USER, isRegUser)
    }

    //设置性别
    fun setSex(sex: String) {
        SharedPreferencesUtils.commitString(SEX, sex)
    }

    fun getSex() = SharedPreferencesUtils.getString(SEX, "")

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