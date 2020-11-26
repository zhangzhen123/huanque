package com.julun.huanque.common.helper

import com.julun.huanque.common.bean.beans.HomePageTab
import com.julun.huanque.common.bean.beans.PublishDynamicCache
import com.julun.huanque.common.bean.forms.PublishStateForm
import com.julun.huanque.common.utils.DateHelper
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

    private const val NEED_GUIDE_TO__PLAY_BIRD = "need_guide_to_play_bird"

    //上一次的今日缘分弹出时间
    private const val LAST_TODAY_FATE_TIME = "last_today_fate_time"

    private const val LATEST_HOME_CATEGORY_VERSION = "latestHomeCategoryVersion"

    //首页的直播节目单tab分类
    private const val HOME_CATEGORY = "HOME_CATEGORY"

    //默认的首页定位tab
    private const val DEFAULT_HOME_TAB = "DefaultHomeTab"

    //是否展示首页的
    private const val SHOW_SOCIAL_TAB = "showSocialTab"

    //保存发布草稿
    private const val PUB_STATE_CACHE = "PUB_STATE_CACHE"

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
        SPUtils.commitBoolean(FIRST_LIVE_SHOW_FOLLOW, status)
    }

    fun getLiveShowFollowStatus() = SPUtils.getBoolean(FIRST_LIVE_SHOW_FOLLOW, true)


    //直播间是否展示手势引导
    fun setLiveFirstGestureGuide(status: Boolean) {
        SPUtils.commitBoolean(FIRST_LIVE_GUIDE_GESTURE, status)
    }

    fun getLiveFirstGestureGuideStatus() = SPUtils.getBoolean(FIRST_LIVE_GUIDE_GESTURE, true)


    fun setNotifyRefreshDate(date: String) {
        SPUtils.commitString(NOTIFY_REFRESH_DATE, date)
    }

    fun getNotifyRefreshDate() = SPUtils.getString(NOTIFY_REFRESH_DATE, "")

    //是否需要引导发言
    fun setNeedGuideToSpeak(status: Boolean) {
        SPUtils.commitBoolean(NEED_GUIDE_TO_SPEAK, status)
    }

    fun getNeedGuideToSpeakStatus() = SPUtils.getBoolean(NEED_GUIDE_TO_SPEAK, true)


    fun setWithdrawType(date: String) {
        SPUtils.commitString(WITHDRAW_TYPE, date)
    }

    fun getWithdrawType() = SPUtils.getString(WITHDRAW_TYPE, "")


    fun setNeedBirdGuide(need: Boolean) {
        val userId: Long = SessionUtils.getUserId()
        if (userId == 0L) {
            return
        }
        SPUtils.commitBoolean("${NEED_GUIDE_TO__PLAY_BIRD}-${userId}", need)
    }

    //带上用户Id
    fun getNeedBirdGuide(): Boolean {
        val userId: Long = SessionUtils.getUserId()
        return SPUtils.getBoolean("${NEED_GUIDE_TO__PLAY_BIRD}-${userId}", true)
    }

    //只代表今日缘分完成逻辑
    fun setLastTodayFateTime() {
        val today = DateHelper.formatNow()
        val userId: Long = SessionUtils.getUserId()
        SPUtils.commitString("$LAST_TODAY_FATE_TIME-${userId}", today)
    }

    fun getLastTodayFateTime(): String {
        val userId: Long = SessionUtils.getUserId()
        return SPUtils.getString("$LAST_TODAY_FATE_TIME-${userId}", "")
    }

    //只记录今日缘分弹框逻辑 一天只弹一次
    fun setLastTodayFateDialogTime() {
        val today = DateHelper.formatNow()
        val userId: Long = SessionUtils.getUserId()
        SPUtils.commitString("$LAST_TODAY_FATE_TIME-dialog-${userId}", today)
    }

    fun getLastTodayFateDialogTime(): String {
        val userId: Long = SessionUtils.getUserId()
        return SPUtils.getString("$LAST_TODAY_FATE_TIME-dialog-${userId}", "")
    }

    //记录当前最新的首页tab版本号
    fun setLatestHomeCategoryVersion(version: String) {
        SPUtils.commitString(LATEST_HOME_CATEGORY_VERSION, version)
    }

    fun getLatestHomeCategoryVersion(): String {
        return SPUtils.getString(LATEST_HOME_CATEGORY_VERSION, "")
    }

    //记录当前最新的首页tab列表对象
    fun setProgramTabObj(tab: HomePageTab) {
        SPUtils.commitObject(HOME_CATEGORY, tab)
    }

    fun getProgramTabObj(): HomePageTab? {
        return SPUtils.getObject<HomePageTab>(HOME_CATEGORY, HomePageTab::class.java)
    }


    //设置默认首页定位tab
    fun setDefaultHomeTab(tab: String) {
        if (tab.isNotEmpty()) {
            SharedPreferencesUtils.commitString(DEFAULT_HOME_TAB, tab)
        }
    }

    fun getDefaultHomeTab() = SharedPreferencesUtils.getString(DEFAULT_HOME_TAB, "")

    //设置默认首页定位tab
    fun setHideSocialTab(show: Boolean) {
        SharedPreferencesUtils.commitBoolean(SHOW_SOCIAL_TAB, show)
    }

    fun getHideSocialTab() = SharedPreferencesUtils.getBoolean(SHOW_SOCIAL_TAB, false)


    //保存发布草稿
    fun setPubStateCache(tab: PublishDynamicCache) {
        SPUtils.commitObject(PUB_STATE_CACHE, tab)
    }

    //删除
    fun removePubStateCache() {
        SPUtils.remove(PUB_STATE_CACHE)
    }

    fun getPubStateCache(): PublishDynamicCache? {
        return SPUtils.getObject<PublishDynamicCache>(PUB_STATE_CACHE, PublishDynamicCache::class.java)
    }

}