package com.julun.platform_push.receiver


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.bean.message.PushAppData
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.UserHeartManager
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ULog
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import io.rong.push.RongPushClient
import org.json.JSONException
import org.json.JSONObject


object RPushUtil {
    const val TAG: String = "RPushUtil"


    /**
     * 处理点击事件，当前启动配置的Activity都是使用
     * Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
     * 方式启动，只需要在onCreate中调用此方法进行处理
     */
    fun handleOpenClick(context: Activity, intent: Intent?): Boolean {
        if (intent == null) return false
        var result: Boolean = true
        if (intent?.data?.scheme == "rong" && intent.data!!.getQueryParameter("isFromPush") != null) {
            if (intent.data!!.getQueryParameter("isFromPush") == "true") {
                val options = intent.getStringExtra("options") // 获取 intent 里携带的附加数据
                Log.i(TAG, "options:$options")
                try {
                    val jsonObject = JSONObject(options)
                    val appData = if (jsonObject.has("appData")) {
                        jsonObject.getString("appData")
                    } else {
                        ""
                    }
                    parseJson(appData, context)
                    if (jsonObject.has("rc")) {
                        val rcs = jsonObject.getString("rc")
                        val rc = JSONObject(rcs)
                        val targetId = rc.getString("tId") // 该推送通知对应的目标 id.
                        val pushId = rc.getString("id");  // 开发者后台使用广播推送功能发出的推送通知，会有该字段，代表该推送的唯一 id， 需要调用下面接口，上传用户打开事件。
                        if (!TextUtils.isEmpty(pushId)) {
                            RongPushClient.recordNotificationEvent(pushId) // 上传用户打开事件，以便进行推送打开率的统计。
                        }
                        if (rc.has("ext") && rc.getJSONObject("ext") != null) {
                            val ext =
                                rc.getJSONObject("ext").toString() // 使用开发者后台的广播推送功能时，填充的自定义键值对。
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    result = false
                }
            }
        }

        return result
    }

    private fun checkNeedConnectRongCloudAndHeartBeat() {
        RongCloudManager.connectRongCloudServerWithComplete(isFirstConnect = true)
        //每次从推送启动后 都手动调用一次心跳 防止心跳没开启
        UserHeartManager.startCheckOnline()
    }

    //这里统一处理所有的推送消息
    fun parseJson(jsonString: String, context: Context) {
        try {
            checkNeedConnectRongCloudAndHeartBeat()
            val bean = JsonUtil.deserializeAsObject<PushAppData>(jsonString, PushAppData::class.java)

            when (bean.touchType) {
                PushDataActionType.EditMineHomePage -> {
//                    startOpenRnPage(context, RnConstant.EDIT_MINE_HOMEPAGE)
                    ARouter.getInstance().build(ARouterConstant.EDIT_INFO_ACTIVITY).navigation()
                }
                PushDataActionType.Url -> {
                    ULog.i(TAG, "开始跳转到网页:" + bean.touchValue)
                    startOpenLink(context, bean.touchValue)
                }
                PushDataActionType.LiveRoom -> {
                    val programId: Long? = bean.touchValue.toLongOrNull()
                    ULog.i(TAG, "开始跳转到房间:$programId")
                    if (programId != null) {
                        startOpenLive(context, programId)
                    } else {
                        shouldOpenMain(context)
                    }
                }
                PushDataActionType.MineHomePage -> {
                    val bundle = Bundle().apply {
                        putLong(ParamConstant.UserId, SessionUtils.getUserId())
                    }
                    ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
                }
                PushDataActionType.AnchorCertPage -> {
                    startOpenRnPage(context, RnConstant.ANCHOR_CERT_PAGE)
                }
                PushDataActionType.FriendNotice -> {
                    startActivityByARouter(context, ARouterConstant.SysMsgActivity, bundle = Bundle().apply {
                        this.putString(ParamConstant.TYPE, bean.touchValue)
                    }, goHome = true)

                }
                PushDataActionType.SystemNotice -> {
                    startActivityByARouter(context, ARouterConstant.SysMsgActivity, bundle = Bundle().apply {
                        this.putString(ParamConstant.TYPE, bean.touchValue)
                    }, goHome = true)

                }
                PushDataActionType.OfficialCertPage -> {
                    startOpenRnPage(context, RnConstant.OFFICIAL_CERT_PAGE)
                }
                PushDataActionType.PlumFlower -> {
                    startActivityByARouter(context, ARouterConstant.PLUM_FLOWER_ACTIVITY, bundle = Bundle().apply {
                        this.putString(ParamConstant.TYPE, bean.touchValue)
                    }, goHome = true)
                }
                PushDataActionType.PrivateChat -> {
                    val targetId = bean.touchValue.toLongOrNull() ?: return
                    startActivityByARouter(context, ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY, bundle = Bundle().apply {
                        this.putLong(ParamConstant.TARGET_USER_ID, targetId)
                    }, goHome = true)
                }
                PushDataActionType.AccostWords -> {
                    startActivityByARouter(context, ARouterConstant.USE_FUL_WORD_ACTIVITY, goHome = true)

                }
                PushDataActionType.FateCome -> {
                    //缘分页面
                    startActivityByARouter(context, ARouterConstant.YUAN_FEN_ACTIVITY, goHome = true)
                }
                PushDataActionType.Message -> {
                    //消息列表
                    val bundle = Bundle()
                    bundle.putInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MESSAGE_FRAGMENT_INDEX)
                    startActivityByARouter(context, ARouterConstant.MAIN_ACTIVITY, bundle = bundle, goHome = false)
                }
                PushDataActionType.FriendHome -> {
                    //交友页面
                    val bundle = Bundle()
                    bundle.putInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX)
                    startActivityByARouter(context, ARouterConstant.MAIN_ACTIVITY, bundle = bundle, goHome = false)
                }
                else -> {
                    shouldOpenMain(context = context)
                }
            }

        } catch (e: Exception) {
            shouldOpenMain(context = context)
            e.printStackTrace()
            Log.e(TAG, "Get message extra JSON error!")
        }
    }

    //根据当前的情况是否跳转到主页
    fun shouldOpenMain(context: Context) {
        if (CommonInit.getInstance().isAppOnForeground) {
            if (CommonInit.getInstance().getCurrentActivity() is RongPushSpringboardActivity) {
                if (ActivitiesManager.INSTANCE.totalActivityCount <= 1) {
                    ULog.i(TAG, "只有一个空跳板界面")
                    startActivityByARouter(context = context, pager = ARouterConstant.MAIN_ACTIVITY, goHome = false)

                } else {
                    ULog.i(TAG, "还有其他界面存在不处理")
                }
            } else {
                ULog.i(TAG, "顶层有其他界面不处理")
            }

        } else {
            ULog.i(TAG, "开始跳转到主页")
            startActivityByARouter(context = context, pager = ARouterConstant.MAIN_ACTIVITY, goHome = false)
        }
    }

    /**
     * 检查当前的情形是否需要返回首页 如果是系统唤醒的 只有一个空跳板界面就必须返回首页
     * 如果系统默认使用启动页唤起直接关闭启动页再跳转 防止跟启动页自己的逻辑冲突
     */
    private fun checkShouldGoHome(): Boolean {
        var result: Boolean = false
        val activity = CommonInit.getInstance().getCurrentActivity()
        if (activity is RongPushSpringboardActivity) {
            if (ActivitiesManager.INSTANCE.totalActivityCount <= 1) {
                ULog.i(TAG, "只有一个空跳板界面")
                result = true
            } else {
                ULog.i(TAG, "还有其他界面存在不处理")
            }
        } else if (activity?.javaClass?.simpleName == "WelcomeActivity") {
            ULog.i(TAG, "顶层是启动页")
            activity.finish()
            result = true
        } else {
            if (ActivitiesManager.INSTANCE.totalActivityCount <= 0) {
                ULog.i(TAG, "没有任何页面 要返回首页")
                result = true
            } else {
                ULog.i(TAG, "顶层有其他界面不处理")
            }

        }
        return result
    }

    //必须跳转到主页
    private fun shouldGotoMain(context: Context, index: Int? = 0) {
        ULog.i(TAG, "确定跳转到主页")
        val bundle: Bundle = Bundle()
        if (index != null) {
            bundle.putInt(IntentParamKey.TARGET_INDEX.name, index)
        }
        startActivityByARouter(context = context, pager = ARouterConstant.MAIN_ACTIVITY, bundle = bundle, goHome = false)
    }


    private fun startOpenLive(context: Context, programId: Long) {
        //无论请求成功与否,都打开直播间
        startActivityByARouter(context = context, pager = ARouterConstant.PLAYER_ACTIVITY, bundle = Bundle().apply {
            putLong(IntentParamKey.PROGRAM_ID.name, programId)
            putString(ParamConstant.FROM, PlayerFrom.Push)
        })
    }

    private fun startOpenLink(context: Context, link: String?) {
        val intent = Intent(context, WebActivity::class.java)
        intent.putExtra(BusiConstant.WEB_URL, link)
        startActivity(context, intent)
    }

    private fun startOpenRnPage(context: Context, page: String?) {
        val intent = Intent(context, RNPageActivity::class.java)
        intent.putExtra(RnConstant.MODULE_NAME, page)
        startActivity(context, intent)
    }

    /**
     * 统一执行的跳转  统一判断场景  尽量不要用路由 如果用路由 自己手动调用checkShouldGoHome检查
     * [context]起跳上下文
     * [intent]组装好的意图
     * [goHome]是否回到首页
     *
     */
    private fun startActivity(context: Context, intent: Intent, goHome: Boolean = true) {
        if (ForceUtils.activityMatch(intent)) {
            //统一执行checkShouldGoHome 该方法一定要执行
            if (checkShouldGoHome()) {
                if (goHome) {
                    intent.putExtra(IntentParamKey.EXTRA_FLAG_GO_HOME.name, true)
                }
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }

    /**
     * 统一执行的跳转  统一判断场景  用路由的方式
     */
    private fun startActivityByARouter(context: Context? = null, pager: String, bundle: Bundle? = null, goHome: Boolean = true) {
        val bd = bundle ?: Bundle()
        //统一执行checkShouldGoHome 该方法一定要执行
        if (checkShouldGoHome()) {
            if (goHome) {
                bd.putBoolean(IntentParamKey.EXTRA_FLAG_GO_HOME.name, true)
            }
        }
        ARouter.getInstance().build(pager).with(bd).navigation(context)

    }
}

