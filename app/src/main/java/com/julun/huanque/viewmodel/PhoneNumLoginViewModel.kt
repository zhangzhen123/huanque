package com.julun.huanque.viewmodel

import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geetest.sdk.GT3ConfigBean
import com.geetest.sdk.GT3ErrorBean
import com.geetest.sdk.GT3GeetestUtils
import com.geetest.sdk.GT3Listener
import com.ishumei.smantifraud.SmAntiFraud
import com.julun.huanque.BuildConfig
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.Session
import com.julun.huanque.common.bean.forms.GetValidCode
import com.julun.huanque.common.bean.forms.MobileLoginForm
import com.julun.huanque.common.bean.forms.MobileQuickForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.MsgTag.TAG
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.svga.SVGAHelper.logger
import com.julun.huanque.net.service.UserService
import com.julun.huanque.utils.GeeTestHttpUtils
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

/**
 *@创建者   dong
 *@创建时间 2020/7/3 14:49
 *@描述 手机号登录使用的ViewModel
 */
class PhoneNumLoginViewModel : BaseViewModel() {

    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //倒计时标识位
    val tickState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //验证码按钮是否无响应标识
    val codeReponse: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //登录数据
    val loginData: MutableLiveData<Session> by lazy { MutableLiveData<Session>() }

    private val gt3Config: GT3ConfigBean by lazy {
        val gt3ConfigBean = GT3ConfigBean()
        // 设置验证模式，1：bind，2：unbind
        gt3ConfigBean.pattern = 1
        // 设置点击灰色区域是否消失，默认不消失
        gt3ConfigBean.isCanceledOnTouchOutside = true
        // 设置debug模式，开代理可调试
        gt3ConfigBean.isDebug = BuildConfig.DEBUG
        // 设置语言，如果为null则使用系统默认语言
        gt3ConfigBean.lang = null
        // 设置加载webview超时时间，单位毫秒，默认10000，仅且webview加载静态文件超时，不包括之前的http请求
        gt3ConfigBean.timeout = 10000
        // 设置webview请求超时(用户点选或滑动完成，前端请求后端接口)，单位毫秒，默认10000
        gt3ConfigBean.webviewTimeout = 10000
        gt3ConfigBean
    }
    var gt3GeetestUtils: GT3GeetestUtils? = null

    private val mListener = object : GT3Listener() {

        /**
         * api1结果回调
         * @param result
         */
        override fun onApi1Result(result: String?) {
//            Log.e(TAG, "GT3BaseListener-->onApi1Result-->" + result!!)
        }

        /**
         * 验证码加载完成
         * @param duration 加载时间和版本等信息，为json格式
         */
        override fun onDialogReady(duration: String?) {
//            Log.e(TAG, "GT3BaseListener-->onDialogReady-->" + duration!!)
        }

        /**
         * 验证结果
         * @param result
         */
        override fun onDialogResult(result: String?) {
//            Log.e(TAG, "GT3BaseListener-->onDialogResult-->" + result!!)
            // 开启api2逻辑
            RequestAPI2().execute(result)
        }

        /**
         * api2回调
         * @param result
         */
        override fun onApi2Result(result: String?) {
//            Log.e(TAG, "GT3BaseListener-->onApi2Result-->" + result!!)
        }

        /**
         * 统计信息，参考接入文档
         * @param result
         */
        override fun onStatistics(result: String) {
            Log.e(TAG, "GT3BaseListener-->onStatistics-->$result")
        }

        /**
         * 验证码被关闭
         * @param num 1 点击验证码的关闭按钮来关闭验证码, 2 点击屏幕关闭验证码, 3 点击返回键关闭验证码
         */
        override fun onClosed(num: Int) {
            Log.e(TAG, "GT3BaseListener-->onClosed-->$num")
        }

        /**
         * 验证成功回调
         * @param result
         */
        override fun onSuccess(result: String) {
            Log.e(TAG, "GT3BaseListener-->onSuccess-->$result")
        }

        /**
         * 验证失败回调
         * @param errorBean 版本号，错误码，错误描述等信息
         */
        override fun onFailed(errorBean: GT3ErrorBean) {
            Log.e(TAG, "GT3BaseListener-->onFailed-->$errorBean")
        }

        /**
         * api1回调
         */
        override fun onButtonClick() {
            RequestAPI1().execute()
        }
    }

    private var captchaURL: String = ""

    private var validateURL: String = ""

    /**
     * 请求api1
     */
    internal inner class RequestAPI1 : AsyncTask<Void, Void, JSONObject>() {

        override fun doInBackground(vararg params: Void): JSONObject? {
            val string = GeeTestHttpUtils.requestGet(captchaURL)
            Log.e(TAG, "doInBackground: $string")
            var jsonObject: JSONObject? = null
            try {
                jsonObject = JSONObject(string)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return jsonObject
        }

        override fun onPostExecute(parmas: JSONObject?) {
            // 继续验证
            Log.i(TAG, "RequestAPI1-->onPostExecute: $parmas")
            // SDK可识别格式为
            // {"success":1,"challenge":"06fbb267def3c3c9530d62aa2d56d018","gt":"019924a82c70bb123aae90d483087f94"}
            //  设置返回api1数据，即使为null也要设置，SDK内部已处理
            gt3Config.setApi1Json(parmas)
            // 继续api验证
            gt3GeetestUtils?.getGeetest()
        }
    }

    /**
     * 请求api2
     */
    internal inner class RequestAPI2 : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String? {
            return if (!TextUtils.isEmpty(params[0])) {
                GeeTestHttpUtils.requestPost(validateURL, params[0])
            } else {
                null
            }
        }

        override fun onPostExecute(result: String?) {
            Log.i(TAG, "RequestAPI2-->onPostExecute: $result")
            if (!TextUtils.isEmpty(result)) {
                try {
                    val jobj = JSONObject(result)
                    val code = jobj.getString("code")
                    val msg = jobj.getString("message")
                    when (code) {

                        "200" -> {
                            val data = jobj.getJSONObject("data")
                            if (data != null) {
                                val flag = data.getBoolean("flag")
                                if (flag) {
                                    //极验  验证通过  开始倒计时
                                    gt3GeetestUtils?.dismissGeetestDialog()
                                    tickState.value = true
//                                    gt3GeetestUtils.dialog.setOnDismissListener {
//                                        tickState.value = true
//                                    }
                                    logger.info("验证成功完成-------")
                                } else {
                                    //校验失败
                                    tickState.value = false
                                    val message = data.getString("message")
                                    if (TextUtils.isEmpty(message)) {
                                        ToastUtils.show(data.getString("验证失败"))
                                    } else {
                                        ToastUtils.show(message)
                                    }
                                    gt3GeetestUtils?.dismissGeetestDialog()
                                }
                            } else {
                                //校验失败
                                tickState.value = false
                                ToastUtils.show("验证失败")
                                gt3GeetestUtils?.dismissGeetestDialog()
                            }
                        }
                        else -> {
                            //校验失败
                            tickState.value = false
                            ToastUtils.show(msg)
                            gt3GeetestUtils?.dismissGeetestDialog()
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    //校验失败
                    tickState.value = false
                    ToastUtils.show("解析异常，请重试")
                    gt3GeetestUtils?.dismissGeetestDialog()
                }
            } else {
                gt3GeetestUtils?.showFailedDialog()
            }
        }
    }


    /**
     * 登录
     */
    fun login(phoneNum: String, code: String) {

        viewModelScope.launch {
//            request({ val result = userService.mobileLogin(MobileLoginForm(phoneNum, code)).dataConvert() })
            request({
                val result = userService.mobileLogin(MobileLoginForm(phoneNum, code, shuMeiDeviceId = SmAntiFraud.getDeviceId() ?: "")).dataConvert()
                SessionUtils.setSession(result)
                loginData.postValue(result)
            }, {
                if (it is ResponseError) {
                    ToastUtils.show(it.busiMessage)
                }
            })
        }
    }

    /**
     * 请求手机验证码接口
     */
    fun startGetValidCode(phone: String) {
        codeReponse.value = false
        viewModelScope.launch {
            request({
                val result = userService.startGetValidCode(GetValidCode(phone)).dataConvert()
                var openGeetest = result?.get("flag")
                when (openGeetest) {//是否唤起极验
                    true -> startGeeTest(phone)
                    else -> tickState.value = true //请求验证码正常返回   开始倒计时
                }
            }, {
                if (it is ResponseError) {
                    when (it.busiCode) {
                        5015, 5011, 5012, 5010, 1106, 503 -> ToastUtils.show(it.busiMessage)
                    }
                }
            }, {
                codeReponse.value = true
            })
        }
    }

    private fun startGeeTest(phone: String) {
        gt3GeetestUtils = GT3GeetestUtils(CommonInit.getInstance().getCurrentActivity() ?: return)
        //首先请求后台是否需要极验
        //需要就调起极验
        //最后调起请求验证码
        captchaURL = BuildConfig.SERVICE_BASE_URL_DEV + "user/acct/login/initGeeTest?"
        // 设置二次验证的URL，需替换成自己的服务器URL
        validateURL = BuildConfig.SERVICE_BASE_URL_DEV + "user/acct/login/verifyGeeTest?"
        captchaURL += "mobile=$phone"
        validateURL += "mobile=$phone"
        logger.info("captchaURL:" + captchaURL + " validateURL:" + validateURL)
//        gt3GeetestUtils?.getGeetest(CommonInit.getInstance().getCurrentActivity(), captchaURL, validateURL, null, mListener)
//        //设置是否可以点击屏幕边缘关闭验证码
//        gt3GeetestUtils?.setDialogTouch(true)
        // 配置bean文件，也可在oncreate初始化
        gt3Config.listener = mListener
        gt3GeetestUtils?.init(gt3Config)
        // 开启验证
        gt3GeetestUtils?.startCustomFlow()

    }
}