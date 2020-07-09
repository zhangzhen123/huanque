package com.julun.jpushlib

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import cn.jpush.android.api.JPushInterface
import com.julun.huanque.common.utils.ULog
import org.json.JSONException
import org.json.JSONObject

/**
 * 自定义接收器
 *
 *
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
class JPushCustomReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras?:return
//        ULog.d(TAG, "[JPushCustomReceiver] onReceive - " + intent.action + ", extras: " + printBundle(bundle))

        when (intent.action) {
            JPushInterface.ACTION_REGISTRATION_ID -> {
                val regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID)
                ULog.d(TAG, "[JPushCustomReceiver] 接收Registration Id : " + regId!!)
            }
            JPushInterface.ACTION_MESSAGE_RECEIVED -> {
                ULog.d(TAG, "[JPushCustomReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE)!!)
                processCustomMessage(context, bundle)
            }
            JPushInterface.ACTION_NOTIFICATION_RECEIVED -> {
                ULog.d(TAG, "[JPushCustomReceiver] 接收到推送下来的通知")
                val notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID)
                ULog.d(TAG, "[JPushCustomReceiver] 接收到推送下来的通知的ID: " + notifactionId)
            }
            JPushInterface.ACTION_RICHPUSH_CALLBACK -> {
                ULog.d(TAG, "[JPushCustomReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA)!!)
                //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
            }
            JPushInterface.ACTION_CONNECTION_CHANGE -> {
                val connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false)
                ULog.d(TAG, "[JPushCustomReceiver]" + intent.action + " connected state change to " + connected)
            }
            JPushInterface.ACTION_NOTIFICATION_OPENED -> {
                ULog.d(TAG, "[JPushCustomReceiver] 用户点击打开了通知")
                JPushUtil.pushStartGoTo(bundle, context)
            }
            else -> {
                ULog.d(TAG, "[JPushCustomReceiver] Unhandled intent - " + intent.action)
            }

        }

    }

    //send msg to MainActivity
    private fun processCustomMessage(context: Context, bundle: Bundle) {
        //		new ToastUtils(LingMengApp.Companion.getNewActivity ())
        val message = bundle.getString(JPushInterface.EXTRA_MESSAGE)
        val extras = bundle.getString(JPushInterface.EXTRA_EXTRA)

//        ToastUtils.show("[JPushCustomReceiver] 收到自定义消息  message is $message , \n extras = $extras")
        /*
            val msgIntent = Intent(MainActivity.MESSAGE_RECEIVED_ACTION)
            msgIntent.putExtra(MainActivity.KEY_MESSAGE, message)
            if (!ExampleUtil.isEmpty(extras)) {
                try {
                    val extraJson = JSONObject(extras)
                    if (extraJson.length() > 0) {
                        msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras)
                    }
                } catch (e: JSONException) {

                }

            }
            context.sendBroadcast(msgIntent)
        */
    }

    val TAG: String? = "Jpush"

    class KeyValue {
        var key: String? = null
        var value: String? = null
        var noticeId: String? = null
        var isNeedULog: String? = null
        var videoId: String? = null
    }

    /**
     * 根据推送的 extras值 跳转到指定页面
     */
//    fun strtGoTo(bundle: Bundle, context: Context) {
//        val value = bundle.getString(JPushInterface.EXTRA_ALERT)
//        var keyValue = KeyValue()
//        for (key in bundle.keySet()) {
//            if (key == JPushInterface.EXTRA_EXTRA) {
//                if (bundle.getString(JPushInterface.EXTRA_EXTRA)!!.isEmpty()) {
//                    ULog.i(TAG, "This message has no Extra data开始跳转到主页")
//                    //
//                    continue
//                }
//                try {
//                    val json = JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA))
//                    keyValue.noticeId = json.optString(NOTICEID)
//                    keyValue.isNeedULog = json.optString(ISNEEDULog)
////                    keyValue.isInApp = json.optString(ISINAPP)
//                    var myValue2: String//这个是关键值
//                    val it = json.keys()
//                    while (it.hasNext()) {
//                        val myKey = it.next().toString()
//                        if (ACTION == myKey) {
//                            val myValue = json.optString(myKey)
//                            if (LIVE == myValue) {
//                                //获取房间号进行跳转
//                                myValue2 = json.optString(PROGRAMID)
//                                keyValue.key = myValue
//                                keyValue.value = myValue2
////                                startOpenLive(context,myValue2.toInt())
//                                //
//                            } else if (URL == myValue) {
//                                //获取网址进行跳转
//                                myValue2 = json.optString(LINK)
//                                keyValue.key = myValue
//                                keyValue.value = myValue2
////                                startOpenLink(context,myValue2)
//                            } else if (STORE == myValue) {
//                                keyValue.key = myValue
//                            } else if (MESSAGE == myValue) {
//                                keyValue.key = myValue
//                            } else if (RECHARGE == myValue) {
//                                keyValue.key = myValue
//                            } else if (VIDEO == myValue) {
//                                keyValue.key = myValue
//                                keyValue.videoId = json.optString(VIDEOID)
//                            }
//                        }
//                    }
//                } catch (e: Exception) {
//                    ULog.e(TAG, "Get message extra JSON error!")
//                }
//            }
//        }
//
//        //启动统计日志
//        when (keyValue.isNeedULog) {
//            "T" -> appService.countULog(CountULogForm(typeValue = keyValue.noticeId)).whatEver()
//        }
//
//        when (keyValue.key) {
//            URL -> {
//                ULog.i(TAG, "开始跳转到网页:" + keyValue.value)
//                startOpenLink(context, keyValue.value)
//            }
//            LIVE -> {
//                ULog.i(TAG, "开始跳转到房间")
//                val programId: Int? = keyValue.value?.toInt()
//                if (programId != null) {
//                    startOpenLive(context, programId)
//                } else {
//                    reportCrash("推送进入直播间的时候未能获取房间号 获取的数据 =>: $value  ,抽取数据 ${JsonUtil.seriazileAsString(keyValue)} ")
//                }
//            }
//            STORE -> {
//                ULog.i(TAG, "开始跳转到商城")
//                startOpenStore(context)
//            }
//            MESSAGE -> {
//                ULog.i(TAG, "开始跳转到消息中心")
//                startOpenMsgCenter(context)
//            }
//            RECHARGE -> {
//                ULog.i(TAG, "开始跳转到充值中心")
//                startOpenRecharge(context)
//            }
//            VIDEO -> {
//                ULog.i(TAG, "开始跳转到视频详情")
//                val videoId: Long? = keyValue.videoId?.toLong()
//                startOpenVideoDetail(context, videoId)
//            }
//            else -> {
//                if (CommonInit.getInstance().isAppOnForeground) {
//                    ULog.i(TAG, "不处理")
//                } else {
//                    ULog.i(TAG, "开始跳转到主页")
//                    val intent = Intent(context, MainActivity::class.java)
//                    startActivity(context, intent)
//                }
//            }
//        }
//    }

//    fun startOpenLive(context: Context, programId: Int) {
//        appService.accessPushULog(ProgramIdForm(programId)).success {}
//        //无论请求成功与否,都打开直播间
//        var intent = Intent(context, PlayerActivity::class.java)
//        intent.putExtra(IntentParamKey.PROGRAM_ID.name, programId)
//        intent.putExtra(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, false)
//        startActivity(context, intent)
//    }

//    fun startOpenLink(context: Context, link: String?) {
//        var intent = Intent(context, PushWebActivity::class.java)
//        intent.putExtra(BusiConstant.PUSH_URL, link)
//        startActivity(context, intent)
//    }
//
//    fun startOpenMsgCenter(context: Context) {
//        if (!SessionUtils.getIsRegUser()) {
//            //未登录这个通知就忽略
//            return
//        }
//        val intent = Intent(context, MessageActivity::class.java)
//        val currentActivity = CommonInit.getInstance().getCurrentActivity()
//        if (currentActivity == null) {
//            intent.putExtra(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, false)
//        }
//        startActivity(context, intent)
//    }
//
//    fun startOpenRecharge(context: Context) {
//        if (!SessionUtils.getIsRegUser()) {
//            //未登录这个通知就忽略
//            return
//        }
//        val intent = Intent(context, RechargeCenterActivityNew::class.java)
//        val currentActivity = CommonInit.getInstance().getCurrentActivity()
//        if (currentActivity == null) {
//            intent.putExtra(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, false)
//        }
//        startActivity(context, intent)
//    }
//
//    fun startOpenVideoDetail(context: Context, videoId: Long?) {
//        val intent = Intent(context, DynamicVideoActivity::class.java)
//        intent.putExtra("VID", 0L)
//        intent.putExtra("FROM", "LIST")
//        intent.putExtra("VIDEOID", videoId ?: 0L)
//        BusiConstant.video_datas = null
//        val currentActivity = CommonInit.getInstance().getCurrentActivity()
//        if (currentActivity == null) {
//            intent.putExtra(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, false)
//        }
//        startActivity(context, intent)
//    }
//
//    private fun startActivity(context: Context, intent: Intent) {
//        if (ForceUtils.activityMatch(intent)) {
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            context.startActivity(intent)
//        }
//    }

    companion object {
        private val TAG = "JPushCustomReceiver"

        // 打印所有的 intent extra 数据
        private fun printBundle(bundle: Bundle?): String {
            if (null == bundle) return ""
            val sb = StringBuilder()
            for (key in bundle.keySet()) {
                if (key == JPushInterface.EXTRA_NOTIFICATION_ID) {
                    sb.append("\nkey:" + key + ", value:" + bundle.getInt(key))
                } else if (key == JPushInterface.EXTRA_CONNECTION_CHANGE) {
                    sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key))
                } else if (key == JPushInterface.EXTRA_EXTRA) {
                    if (TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_EXTRA))) {
                        ULog.i(TAG, "This message has no Extra data")
                        continue
                    }

                    try {
                        val json = JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA))
                        val it = json.keys()

                        while (it.hasNext()) {
                            val myKey = it.next().toString()
                            sb.append("\nkey:" + key + ", value: [" +
                                    myKey + " - " + json.optString(myKey) + "]")
                        }
                    } catch (e: JSONException) {
                        ULog.e(TAG, "Get message extra JSON error!")
                    }

                } else {
                    sb.append("\nkey:" + key + ", value:" + bundle.getString(key))
                }
            }
            return sb.toString()
        }
    }
}