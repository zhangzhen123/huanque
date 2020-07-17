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