package com.julun.huanque.realname

import android.Manifest
import android.app.Activity
import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.security.realidentity.RPEventListener
import com.alibaba.security.realidentity.RPResult
import com.alibaba.security.realidentity.RPVerify
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.RealNameConstants
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.interfaces.routerservice.RealNameCallback
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.realname.net.bean.RealNameBean
import com.julun.huanque.realname.net.form.RealNameForm
import com.julun.huanque.realname.net.service.RealNameService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 实名认证服务
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/09
 */
@Route(path = ARouterConstant.REALNAME_SERVICE)
class RealNameProvider : IRealNameService {

    private var mCallback: RealNameCallback? = null
    private var mIsRequesting: Boolean = false
    private val logger = ULog.getLogger(RealNameProvider::class.java.simpleName)

    private var mTokenJob: Job? = null

    override fun init(context: Context?) {
        // 初始化实人认证SDK。
        RPVerify.init(context ?: return)
    }

    override fun startRealName(
        activity: Activity,
        realName: String,
        realIdCard: String,
        callback: RealNameCallback
    ) {
        if (mIsRequesting) {
            return
        }
        mIsRequesting = true
        setRealCallback(callback)
        checkAndGo(activity, RealNameConstants.TYPE_NAME, realName, realIdCard)
    }

    override fun startRealHead(activity: Activity, callback: RealNameCallback) {
        if (mIsRequesting) {
            return
        }
        mIsRequesting = true
        setRealCallback(callback)
        checkAndGo(activity, RealNameConstants.TYPE_HEAD)
    }

    private fun setRealCallback(callback: RealNameCallback?) {
        mCallback = callback
    }

    override fun release() {
        mCallback = null
        //取消协程
        mTokenJob?.cancel()
    }

    private fun checkAndGo(
        activity: Activity,
        type: String,
        realName: String? = null,
        realIdCard: String? = null
    ) {
        RxPermissions(activity).requestEachCombined(
            Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        execute(activity, type, realName, realIdCard)
                    }
                    permission.shouldShowRequestPermissionRationale -> {
                        ToastUtils.show("获取权限失败")
                        mIsRequesting = false
                    }
                    else -> {
                        mIsRequesting = false
                        logger.info("获取权限被永久拒绝")
                        ToastUtils.show(
                            "无法获取到相关权限，请手动到设置中开启\n" +
                                    "如果还是无法开启可能是手机自带管家或第三方管理软件禁止了权限！"
                        )
                    }
                }
            }
    }

    /**
     * 打开一个认证页（H5）
     */
    private fun start(
        activity: Activity, info: RealNameBean,
        type: String,
        realName: String? = null,
        realIdCard: String? = null
    ) {
        if (!info.need) {
            //不需要打开扫描页
            save(type, realName, realIdCard)
            return
        }
        logger.info("打开实名认证认证页")
        RPVerify.start(activity, info.token, object : RPEventListener() {
            override fun onFinish(auditResult: RPResult, code: String, msg: String) {
                when (auditResult) {
                    RPResult.AUDIT_PASS -> {
                        // 认证通过。建议接入方调用实人认证服务端接口DescribeVerifyResult来获取最终的认证状态，并以此为准进行业务上的判断和处理
                        save(type, realName, realIdCard)
                    }
                    RPResult.AUDIT_FAIL -> {
                        // 认证不通过。建议接入方调用实人认证服务端接口DescribeVerifyResult来获取最终的认证状态，并以此为准进行业务上的判断和处理
                        save(type, realName, realIdCard)
                    }
                    else -> {
                        // 未认证，RPResult.AUDIT_NOT具体原因可通过code来区分（code取值参见下方表格），通常是用户主动退出或者姓名身份证号实名校验不匹配等原因，导致未完成认证流程
                        if (code == "-1") {
                            callback(RealNameConstants.TYPE_CANCEL, "认证取消")
                        } else {
                            save(type, realName, realIdCard)
                        }
                    }
                }
            }
        })
    }

    private fun callback(type: String, message: String, percent: Int? = null) {
        this.mCallback?.let { callback ->
            callback.onCallback(type, message, percent)
        }
    }

    private fun execute(
        activity: Activity,
        type: String,
        realName: String? = null,
        realIdCard: String? = null
    ) {
        mTokenJob = GlobalScope.launch(Dispatchers.Main) {
            try {
                val info = Requests.create(RealNameService::class.java)
                    .getCertificationToken(
                        RealNameForm(
                            authType = type,
                            realName = realName,
                            certNum = realIdCard
                        )
                    ).dataConvert()
                //协程并未取消，那么就可以继续往下执行
                if (mTokenJob?.isActive == true) {
                    //获取token并打开人脸识别页面
                    start(activity, info, type, realName, realIdCard)
                } else {
                    callback(RealNameConstants.TYPE_CANCEL, "")
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                if (e is ResponseError) {
                    callback(RealNameConstants.TYPE_ERROR, "")
                } else {
                    callback(RealNameConstants.TYPE_ERROR, "网络异常，请稍后重试！")
                }
            } finally {
                mIsRequesting = false
            }
        }
    }

    private fun save(
        type: String,
        realName: String? = null,
        realIdCard: String? = null
    ) {
        //认证步骤要从save接口获取接口再返回出去，需要等待
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val result = Requests.create(RealNameService::class.java).saveCertificationRes(
                    RealNameForm(
                        authType = type,
                        realName = realName,
                        certNum = realIdCard
                    )
                ).dataConvert(intArrayOf(1301))
                callback(RealNameConstants.TYPE_SUCCESS, "认证成功~！", result.perfection)
            } catch (e: Throwable) {
                e.printStackTrace()
                if (e is ResponseError) {
                    callback(RealNameConstants.TYPE_FAIL, e.busiMessage)
                } else {
                    callback(RealNameConstants.TYPE_FAIL, "网络异常，请重试~！")
                }
            }
        }
    }
}