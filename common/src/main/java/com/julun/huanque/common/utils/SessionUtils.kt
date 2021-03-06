package com.julun.huanque.common.utils

import com.julun.huanque.common.database.table.Session
import com.julun.huanque.common.helper.StorageHelper

/**
 * 重要的信息和启动配参放在这里 不重要的参数放在[com.julun.huanque.common.helper.StorageHelper]中 以提高初始化效率
 */
object SessionUtils {
    private const val SESSION_ID: String = "SESSION_ID"

    //登录用户id
    private const val USER_ID: String = "USER_ID"

    //用户昵称
    private const val NICK_NAME: String = "NICK_NAME"

    //头像
    private const val HEAD_PIC: String = "HEAD_PIC"

    //用户类型
    private const val USER_TYPE = "USER_TYPE"

    //声网token
    private const val VOICE_TOKEN = "VOICE_TOKEN"

    //是否是新用户
    private const val NEW_USER = "NEW_USER"

    //数据是否完整
    private const val REG_COMPLETE = "REG_COMPLETE"

    //融云聊天token
    private const val RONG_IM_TOKEN: String = "RONG_IM_TOKEN"

    //是否注册用户
    private const val REG_USER = "REG_USER"

    //用户性别
    private const val SEX = "SEX"

    //邀请码
    private const val INVITE_CODE = "INVITE_CODE"
//
//    //生日
//    private const val BIRTHDAY = "BIRTHDAY"

    //社交意愿
    private const val WISH_COMPLETE = "WISH_COMPLETE"


    //是否验证过session
    var isCheckSession = false

    var currentSession: Session? = null
    fun setSession(session: Session) {
        currentSession = session
        isCheckSession = true
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
        setWishComplete(session.wishComplete)
    }

    fun getSession(): Session {
        return currentSession ?: Session().apply {
            this.sessionId = getSessionId()
            this.nickname = getNickName()
            this.userId = getUserId()
            this.headPic = getHeaderPic()
            this.userType = getUserType()
            this.voiceToken = getAgoraToken()
            this.newUser = getNewUser()
            this.regComplete = getRegComplete()
            this.imToken = getRongImToken()
            this.regUser = getIsRegUser()
            this.sex = getSex()
            this.wishComplete = getWishComplete()
        }
    }

    //与deleteSession合并 不再单独调用
    fun clearSession() {
        currentSession = null
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
        //去除提现方式保存
        StorageHelper.setWithdrawType("")
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

    //设置邀请码
    fun setInviteCode(code: String) {
        SharedPreferencesUtils.commitString(INVITE_CODE, code)
    }

    fun getInviteCode() = SharedPreferencesUtils.getString(INVITE_CODE, "")

//    //生日
//    fun setBirthday(birthday: String) {
//        SharedPreferencesUtils.commitString(BIRTHDAY, birthday)
//    }
//
//
//    fun getBirthday() = SharedPreferencesUtils.getString(BIRTHDAY, "")

    //社交意愿是否完成
    fun setWishComplete(wishComplete: String) {
        SharedPreferencesUtils.commitString(WISH_COMPLETE, wishComplete)
    }

    fun getWishComplete() = SharedPreferencesUtils.getString(WISH_COMPLETE, "")


}