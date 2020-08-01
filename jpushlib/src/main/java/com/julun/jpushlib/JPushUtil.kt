package com.julun.jpushlib


import android.app.Activity
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import cn.jpush.android.api.BasicPushNotificationBuilder
import cn.jpush.android.api.CustomPushNotificationBuilder
import cn.jpush.android.api.JPushInterface
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.ULog
import org.greenrobot.eventbus.util.ErrorDialogManager
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern


object JPushUtil {
    const val TAG: String = "JPushUtil"


//    private val appService: AppService by lazy { Requests.create(AppService::class.java) }

    const val ACTION = "action"//该动作的类型
    const val LIVE = "live"//房间

    //    const val PROGRAMID = "programId"//房间号
    //新增随机跳转房间
    const val PROGRAMIDS = "programIds"//房间号数组
    const val STORE = "store"//商城
    const val MESSAGE = "msgCenter"//消息中心
    const val RECHARGE = "recharge" //我的界面
    const val VIDEO = "video"//视频详情
    const val NOTICEID = "noticeId"

    //    val ISINAPP = "isInApp"
    const val MONTHCARD = "monthCard"//跳转月卡详情页
    const val ISNEEDLOG = "isNeedLog"
    const val VIDEOID = "videoId"
    private const val USERCENTER = "userCenter"//跳转到个人中心
    private const val WEEK_STAR = "weekStar"//跳转到周星
    private const val COMMENT = "comment"//跳转到评论
    private const val PRAISE = "praise"//跳转到点赞
    private const val ACTIVITY = "activity"//跳转到活动
    private const val COUPON = "coupon"//跳转到优惠券
    private const val DISCOUNT = "discount"//跳转到折扣券
    const val URL = "url"//网址标志
    const val LINK = "link"//网址

    /**消息Id */
    const val KEY_MSGID = "msg_id"

    /**该通知的下发通道 */
    const val KEY_WHICH_PUSH_SDK = "rom_type"

    /**通知标题 */
    const val KEY_TITLE = "n_title"

    /**通知内容 */
    const val KEY_CONTENT = "n_content"

    /**通知附加字段 */
    const val KEY_EXTRAS = "n_extras"


    // 校验Tag Alias 只能是数字,英文字母和中文
    fun isValidTagAndAlias(s: String): Boolean {
        val p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_@!#$&*+=.|￥¥]+$")
        val m = p.matcher(s)
        return m.matches()
    }

    fun getDeviceId(context: Context): String {
        val deviceId = JPushInterface.getUdid(context)
        return deviceId
    }

    fun isConnected(context: Context): Boolean {
        val conn = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = conn.activeNetworkInfo
        return info != null && info.isConnected
    }


    fun setNotificationStyle(context: Context) {
        val builder = BasicPushNotificationBuilder(context)
        builder.statusBarDrawable = R.mipmap.ic_launcher
        builder.notificationFlags = Notification.FLAG_AUTO_CANCEL or Notification.FLAG_SHOW_LIGHTS  //设置为自动消失和呼吸灯闪烁
        builder.notificationDefaults = (Notification.DEFAULT_SOUND
                or Notification.DEFAULT_VIBRATE
                or Notification.DEFAULT_LIGHTS)  // 设置为铃声、震动、呼吸灯闪烁都要
        JPushInterface.setDefaultPushNotificationBuilder(builder)
    }

    /**
     * 设置通知栏样式 - 定义通知栏Layout //有些手机会报错
     */
    fun setNotificationStyleCustom(activity: Activity) {
        val builder =
            CustomPushNotificationBuilder(activity, R.layout.view_customer_notitfication_layout, R.id.icon, R.id.title, R.id.text)

        builder.statusBarDrawable = R.mipmap.ic_launcher
        builder.layoutIconDrawable = R.mipmap.ic_launcher
        builder.developerArg0 = "developerArg2"
        //第一个参数是通知栏样式编号 后台推送时可选样式用到
        JPushInterface.setPushNotificationBuilder(2, builder)
    }


    /**
     * 根据推送的 extras值 跳转到指定页面
     */
    fun pushStartGoTo(bundle: Bundle, context: Context) {
//        val value = bundle.getString(JPushInterface.EXTRA_ALERT)
        for (key in bundle.keySet()) {
            if (key == JPushInterface.EXTRA_EXTRA) {
                if (bundle.getString(JPushInterface.EXTRA_EXTRA)!!.isEmpty()) {
                    ULog.i(TAG, "This message has no Extra data开始跳转到主页")
                    //
                    continue
                }
                Log.i(TAG, "pushStartGoTo:" + bundle.getString(JPushInterface.EXTRA_EXTRA))
                parseJson(bundle.getString(JPushInterface.EXTRA_EXTRA) ?: return, context)
            }
        }

    }

    /**
     * 处理点击事件，当前启动配置的Activity都是使用
     * Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
     * 方式启动，只需要在onCreate中调用此方法进行处理
     */
    fun handleOpenClick(context: Activity, intent: Intent?): Boolean {
        if (intent == null) return false
        var result: Boolean = true
        var data: String? = null
        //获取华为平台附带的jpush信息
        if (intent.data != null) {
            data = intent.data.toString()
        }
        //获取fcm或oppo平台附带的jpush信息
        if (TextUtils.isEmpty(data) && intent.extras != null) {
            data = intent.extras!!.getString("JMessageExtra")
        }

        Log.i(TAG, "msg content is " + data.toString())

        if (TextUtils.isEmpty(data)) return false

        try {
            val jsonObject = JSONObject(data)
            val msgId = jsonObject.optString(KEY_MSGID)
            val whichPushSDK = jsonObject.optInt(KEY_WHICH_PUSH_SDK).toByte()
            val title = jsonObject.optString(ErrorDialogManager.KEY_TITLE)
            val content = jsonObject.optString(KEY_CONTENT)
            val extras = jsonObject.optString(KEY_EXTRAS)
            val sb = StringBuilder()
            sb.append("msgId:")
            sb.append(msgId.toString())
            sb.append("\n")
            sb.append("title:")
            sb.append(title.toString())
            sb.append("\n")
            sb.append("content:")
            sb.append(content.toString())
            sb.append("\n")
            sb.append("extras:")
            sb.append(extras.toString())
            sb.append("\n")
            sb.append("platform:")
            sb.append(getPushSDKName(whichPushSDK))

            //上报点击事件
            JPushInterface.reportNotificationOpened(context, msgId, whichPushSDK)
            //处理华为和oppo只支持自家跳转的处理
            Log.i(TAG, "handleOpenClickToGo:" + extras)
            parseJson(extras, context)

        } catch (e: Exception) {
            result = false
            Log.i(TAG, "parse notification error")
        }

        return result
    }

    private fun getPushSDKName(whichPushSDK: Byte): String {
        val which = whichPushSDK.toInt()
        val name: String
        when (which) {
            0 -> name = "jpush"
            1 -> name = "xiaomi"
            2 -> name = "huawei"
            3 -> name = "meizu"
            4 -> name = "oppo"
            8 -> name = "fcm"
            else -> name = "jpush"
        }
        return name
    }


    private fun parseJson(jsonString: String, context: Context) {
        val keyValue = KeyValue()
        try {
            val json = JSONObject(jsonString)
            keyValue.noticeId = json.optString(NOTICEID)
            keyValue.isNeedLog = json.optString(ISNEEDLOG)
//                    keyValue.isInApp = json.optString(ISINAPP)
            var myValue2: String//这个是关键值
            val it = json.keys()
            while (it.hasNext()) {
                val myKey = it.next().toString()
                if (ACTION == myKey) {
                    val myValue = json.optString(myKey)
                    keyValue.key = myValue
                    when {
                        LIVE == myValue -> {
                            //获取房间号进行跳转
                            val myValueString = json.optString(PROGRAMIDS)
                            val myValueArray = myValueString.split(",")
                            ULog.i(TAG, "随机跳转的链表$myValueArray")
                            val random = Random().nextInt(myValueArray.size)
                            keyValue.value = myValueArray[random]
                            //                                startOpenLive(context,myValue2.toInt())
                            //
                        }
                        URL == myValue -> {
                            //获取网址进行跳转
                            myValue2 = json.optString(LINK)
                            keyValue.value = myValue2
                            //                                startOpenLink(context,myValue2)
                        }
//                        STORE == myValue -> keyValue.key = myValue
//                        MESSAGE == myValue -> keyValue.key = myValue
//                        RECHARGE == myValue -> keyValue.key = myValue
                        VIDEO == myValue -> {
                            keyValue.videoId = json.optString(VIDEOID)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Get message extra JSON error!")
        }
        //启动统计日志
        when (keyValue.isNeedLog) {
//            "T" -> appService.countLog(CountLogForm(typeValue = keyValue.noticeId)).whatEver()
        }
        when (keyValue.key) {
            URL -> {
                ULog.i(TAG, "开始跳转到网页:" + keyValue.value)
                startOpenLink(context, keyValue.value)
            }
            LIVE -> {
                val programId: Long? = keyValue.value?.toLongOrNull()
                ULog.i(TAG, "开始跳转到房间:$programId")
                if (programId != null) {
                    startOpenLive(context, programId)
                } else {
                    shouldOpenMain(context)
                    reportCrash("推送进入直播间的时候未能获取房间号 获取的数据 =>: $jsonString  ,抽取数据 ${JsonUtil.seriazileAsString(keyValue)} ")
                }
            }
            else -> {
                shouldOpenMain(context = context)
            }
        }

    }


    //根据当前的情况是否跳转到主页
    fun shouldOpenMain(context: Context) {
        if (CommonInit.getInstance().isAppOnForeground) {
            if (CommonInit.getInstance().getCurrentActivity() is PushSpringboardActivity) {
                if (ActivitiesManager.totalActivityCount <= 1) {
                    ULog.i(TAG, "只有一个空跳板界面")
                    startActivityByARouter(context = context, pager = ARouterConstant.MAIN_ACTIVITY,goHome = false)

                } else {
                    ULog.i(TAG, "还有其他界面存在不处理")
                }
            } else {
                ULog.i(TAG, "顶层有其他界面不处理")
            }

        } else {
            ULog.i(TAG, "开始跳转到主页")
            startActivityByARouter(context = context, pager = ARouterConstant.MAIN_ACTIVITY,goHome = false)
        }
    }

    /**
     * 检查当前的情形是否需要返回首页 如果是系统唤醒的 只有一个空跳板界面就必须返回首页
     * 如果系统默认使用启动页唤起直接关闭启动页再跳转 防止跟启动页自己的逻辑冲突
     */
    private fun checkShouldGoHome(): Boolean {
        var result: Boolean = false
        val activity = CommonInit.getInstance().getCurrentActivity()
        if (activity is PushSpringboardActivity) {
            if (ActivitiesManager.totalActivityCount <= 1) {
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
            ULog.i(TAG, "顶层有其他界面不处理")
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
        startActivityByARouter(context = context, pager = ARouterConstant.MAIN_ACTIVITY, bundle = bundle,goHome = false)
    }


    fun startOpenLive(context: Context, programId: Long) {
//        appService.accessPushLog(ProgramIdForm(programId)).success {}
        //无论请求成功与否,都打开直播间
//        val intent = Intent(context, PlayerActivity::class.java)
//        intent.putExtra(IntentParamKey.PROGRAM_ID.name, programId)
////        checkShouldGoHome()
////        intent.putExtra(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, false)
//        startActivity(context, intent, true)
    }

    fun startOpenLink(context: Context, link: String?) {
//        val intent = Intent(context, PushWebActivity::class.java)
//        intent.putExtra(BusiConstant.WEB_URL, link)
//        startActivity(context, intent, true)
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

class KeyValue {
    var key: String? = null
    var value: String? = null
    var noticeId: String? = null
    var isNeedLog: String? = null
    var videoId: String? = null
}
