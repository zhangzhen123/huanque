package com.julun.lingmeng.msaservice

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.bun.miitmdid.core.ErrorCode
import com.bun.miitmdid.core.MdidSdkHelper
import com.bun.miitmdid.interfaces.IIdentifierListener
import com.bun.miitmdid.interfaces.IdSupplier
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.interfaces.routerservice.IMSAService
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.utils.BalanceUtils
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 通过移动联盟SDK获取oaid
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/09/16
 * @detail 接入移动安全联盟的SDK用于获取多个机型的OAID，如果要获取 VAID，要去对应厂商的应用商店里注册自己的 appid
 * 并在supplierconfig.json中配置
 */
@Route(path = ARouterConstant.MSA_SERVICE)
class MSAService : IMSAService, IIdentifierListener {

    private var mOaid: String = ""

    override fun init(context: Context?) {
        //        val timeb = System.currentTimeMillis()
        // 通过反射调用，解决android 9以后的类加载升级，导至找不到so中的方法
        val nres = MdidSdkHelper.InitSdk(context, true, this)
        if (nres == ErrorCode.INIT_ERROR_RESULT_DELAY) {
            //正在异步获取结果
            SharedPreferencesUtils.commitBoolean(SPParamKey.Support_Oaid, true)
        } else {
            SharedPreferencesUtils.commitBoolean(SPParamKey.Support_Oaid, false)
        }
//        val timee = System.currentTimeMillis()
//        val offset = timee - timeb
//        logger.info("MSA SDK初始化并获取OAID时长 $offset -> nres = $nres")
//        if (nres == ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT) { //1008612 不支持的设备
//        } else if (nres == ErrorCode.INIT_ERROR_LOAD_CONFIGFILE) { //1008613 加载配置文件出错
//        } else if (nres == ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT) { //1008611 不支持的设备厂商
//        } else if (nres == ErrorCode.INIT_ERROR_RESULT_DELAY) { //1008614 获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
//        } else if (nres == ErrorCode.INIT_HELPER_CALL_ERROR) { //1008615 反射调用出错
//        }
    }

    /**
     * 获取初始化应用时读取到的oaid
     */
    override fun getOaid(): String {
        return mOaid
    }

    override fun OnSupport(isSupport: Boolean, _supplier: IdSupplier?) {
        _supplier ?: return
        mOaid = _supplier.oaid
        if (!SPUtils.getBoolean(SPParamKey.APP_START, false)) {
            //调用激活接口  oaid不支持无需等待接口返回
            GlobalScope.launch {
                kotlin.runCatching {
                    withContext(Dispatchers.IO) {
                        Requests.create(UserService::class.java).appStart().dataConvert()
                        SPUtils.commitBoolean(SPParamKey.APP_START, true)
                    }
                }
            }
        }
//        val vaid = _supplier.vaid
//        val aaid = _supplier.aaid
    }
}