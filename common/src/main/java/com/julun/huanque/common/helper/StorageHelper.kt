package com.julun.huanque.common.helper

import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils


/**
 * 普通存储信息管理 处理不重要或者不用启动就初始化的内容 一般使用[SPUtils]进行存储
 */
object StorageHelper {

    private const val AD_VERSION: String = "AdVersion"

    private const val PHONE_NUM_CACHE: String = "phone_num_cache"

    //直播间第一次展示右侧关注
    private const val FIRST_LIVE_SHOW_FOLLOW = "FIRST_LIVE_SHOW_FOLLOW"

    //直播间第一次展示手势引导
    private const val FIRST_LIVE_GUIDE_GESTURE = "FIRST_LIVE_GUIDE_GESTURE"

    //记录通知提醒刷新的日期
    private const val NOTIFY_REFRESH_DATE = "notify_refresh_date"

    private const val NEED_GUIDE_TO_SPEAK = "NEED_GUIDE_TO_SPEAK"
    //提现方式
    private const val WITHDRAW_TYPE = "WITHDRAW_TYPE"
    /**
     * 保存ad
     */
    fun setAdVersion(headerPic: String) {
        SPUtils.commitString(AD_VERSION, headerPic)
    }

    fun getAdVersion() = SPUtils.getString(AD_VERSION, "")

    /**
     * 记录最近一次的登录的PhoneNum 方便登录
     */
    fun setPhoneNumCache(headerPic: String) {
        SPUtils.commitString(PHONE_NUM_CACHE, headerPic)
    }

    fun getPhoneNumCache() = SPUtils.getString(PHONE_NUM_CACHE, "")


    //直播间是否展示关注
    fun setLiveShowFollow(status: Boolean) {
        SharedPreferencesUtils.commitBoolean(FIRST_LIVE_SHOW_FOLLOW, status)
    }
    fun getLiveShowFollowStatus() = SharedPreferencesUtils.getBoolean(FIRST_LIVE_SHOW_FOLLOW, true)



    //直播间是否展示手势引导
    fun setLiveFirstGestureGuide(status: Boolean) {
        SharedPreferencesUtils.commitBoolean(FIRST_LIVE_GUIDE_GESTURE, status)
    }
    fun getLiveFirstGestureGuideStatus() = SharedPreferencesUtils.getBoolean(FIRST_LIVE_GUIDE_GESTURE, true)


    fun setNotifyRefreshDate(date: String) {
        SharedPreferencesUtils.commitString(NOTIFY_REFRESH_DATE, date)
    }
    fun getNotifyRefreshDate() = SharedPreferencesUtils.getString(NOTIFY_REFRESH_DATE, "")

    //是否需要引导发言
    fun setNeedGuideToSpeak(status: Boolean) {
        SharedPreferencesUtils.commitBoolean(NEED_GUIDE_TO_SPEAK, status)
    }

    fun getNeedGuideToSpeakStatus() = SharedPreferencesUtils.getBoolean(NEED_GUIDE_TO_SPEAK, true)


    fun setWithdrawType(date: String) {
        SharedPreferencesUtils.commitString(WITHDRAW_TYPE, date)
    }
    fun getWithdrawType() = SharedPreferencesUtils.getString(WITHDRAW_TYPE, "")
}