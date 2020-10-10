package com.julun.huanque.common.suger

import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.BuildConfig
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.helper.DefaultRxTransformer
import com.julun.huanque.common.helper.RunOnMainSchedulerTransformer
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.UserHeartManager
import com.julun.huanque.common.net.CancelableObservableSubscriber
import com.julun.huanque.common.utils.LoginStatusUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.reflect.ClassInfo
import com.julun.huanque.common.utils.reflect.ReflectUtil
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.rong.imlib.RongIMClient
import java.lang.reflect.Array
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*


private const val MSG_DATA_FAILURE = "未能正确的获取数据"
private const val REQUEST_RETURN_TYPE_DEF_ERROR = -200
const val OTHER_DEF_ERROR = -100//其他错误各种报错异常

//给各个 接口添加扩展方法
private fun <T> Observable<Root<T>>.afterRequest(intArray: IntArray? = null): Observable<T> = this.map {
    return@map mapper(it, intArray)
}

private fun <T> Flowable<Root<T>>.afterRequest(intArray: IntArray? = null): Flowable<T> = this.map {
    return@map mapper(it, intArray)
}

private fun <T> Single<Root<T>>.afterRequest(intArray: IntArray? = null): Single<T> = this.map {
    return@map mapper(it, intArray)
}

private fun <T> Maybe<Root<T>>.afterRequest(intArray: IntArray? = null): Maybe<T> = this.map {
    return@map mapper(it, intArray)
}

// 500 , 1000
fun <T> mapper(it: Root<T>, intArray: IntArray? = null): T {
    val code = it.code
    //很多错误码会统一在此进行全局统一处理 如果需要单独处理 特殊声明后这里会过滤
    if (intArray != null && code in intArray && code != 200) {
        throw throw ResponseError(it.code, it.message.toString())
    }

    when (code) {
        // 成功
        ErrorCodes.SUCCESS -> {
            val data: T? = it.data
            if (data == null) {
                val typeParameter: Type? = it.typeParameter
                val classInfo: ClassInfo = if (typeParameter is Class<*>) {
                    ReflectUtil.getClassInfo(typeParameter)
                } else if (typeParameter is ParameterizedType) {
                    val rawType = typeParameter.rawType
                    if (rawType is Class<*>) {
                        ReflectUtil.getClassInfo(rawType)
                    } else {
                        throw ResponseError(REQUEST_RETURN_TYPE_DEF_ERROR, MSG_DATA_FAILURE)
                    }
                } else if (typeParameter is GenericArrayType) {//数组,提前处理了
                    val rawClazz: Class<*> = typeParameter.genericComponentType as Class<*>
                    return Array.newInstance(rawClazz, 0) as T
                } else {//其他情况不处理
                    throw ResponseError(REQUEST_RETURN_TYPE_DEF_ERROR, MSG_DATA_FAILURE)
                }

                val voidClass: Class<VoidResult> = VoidResult::class.java
                //如果是 VoidResult ,直接返回
                if (classInfo.isOf(voidClass)) {
                    return VoidResult() as T
                }

                //依次判断是不是集合或者Map
                return when {
                    classInfo.isOf(List::class.java) -> {
                        ArrayList<Any>() as T
                    }
                    classInfo.isOf(Map::class.java) -> {
                        HashMap<Any, Any>() as T
                    }
                    classInfo.isOf(Set::class.java) -> {
                        HashSet<Any>() as T
                    }
                    else -> { //抛出业务异常, -200 ,消息为未能正确获取数据
                        throw ResponseError(REQUEST_RETURN_TYPE_DEF_ERROR, MSG_DATA_FAILURE)
                    }
                }
            }
            return data
        }

        // 除了200成功返回it外，其他都抛出异常返回false，到errorHandle中去处理


        // session失效
        ErrorCodes.SESSION_PAST -> {
            //清空session
            SessionUtils.clearSession()
            //登出融云
            if (RongIMClient.getInstance().currentConnectionStatus == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
                RongCloudManager.logout()
            }
            UserHeartManager.stopBeat()
            LoginStatusUtils.logout()
            //跳转登录页面
            ARouter.getInstance().build(ARouterConstant.LOGIN_ACTIVITY).navigation()
            ActivitiesManager.INSTANCE.finishActivityExcept("com.julun.huanque.activity.LoginActivity")
        }

        // 系统异常
        ErrorCodes.SYSTEM_ERROR -> {
            val message: String = if (BuildConfig.DEBUG) {
                it.message.toString()
            } else {
                "系统异常"
            }
            ToastUtils.show2(message)
//            Observable.just(message).compose(DefaultRxTransformer<String>()).subscribe {
//                try {
//                    ToastUtils.show(it)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
        }

//        // 余额不足
//        ErrorCodes.BALANCE_NOT_ENOUGH -> {
//
//        }
        //账号已经被封禁
        ErrorCodes.ACCOUNT_HAS_BLOCK -> {
            val activity = CommonInit.getInstance().getCurrentActivity()
            if (activity != null)
                MyAlertDialog(activity, false).showAlertWithOK(
                    it.message.toString(), okText = "知道了"
                )
        }
        // 其他系统定义错误代码
        else -> {
            ToastUtils.show2(it.message.toString())
//            Observable.just(it.message.toString()).compose(DefaultRxTransformer<String>()).subscribe {
//                try {
//
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    reportCrash("弹出窗口出错", e)
//                }
//            }

        }
    }

    throw ResponseError(it.code, it.message.toString())
}

/**
 * 只针对数据正确返回的情况处理
 */
fun <T> Observable<Root<T>>.success(successHandler: Function1<T, Unit>) {
    this.afterRequest().compose(RunOnMainSchedulerTransformer()).subscribe(successHandler, {})
}

/**
 * 啥都不做,只要请求就可以
 */
fun <T> Observable<Root<T>>.whatEver() {
    this.afterRequest().compose(RunOnMainSchedulerTransformer()).subscribe({}, {})
}

/**
 * 不做任何处理
 * 但是错误还是要抓取 不然会抛出异常
 */
fun <T> Observable<Root<T>>.nothing() {
    this.subscribe({}, { it.printStackTrace() })
}

//添加处理函数
fun <T> Observable<Root<T>>.handleResponse(observableSubscriber: CancelableObservableSubscriber<T>) {
    this.afterRequest(observableSubscriber.specifiedCodes).compose(RunOnMainSchedulerTransformer<T>())
        .subscribe(observableSubscriber)
}

fun <T> Maybe<Root<T>>.handleResponse(observableSubscriber: CancelableObservableSubscriber<T>) {
    this.afterRequest(observableSubscriber.specifiedCodes).compose(RunOnMainSchedulerTransformer<T>())
        .subscribe(observableSubscriber)
}

fun <T> Single<Root<T>>.handleResponse(observableSubscriber: CancelableObservableSubscriber<T>) {
    this.afterRequest(observableSubscriber.specifiedCodes).compose(RunOnMainSchedulerTransformer<T>())
        .subscribe(observableSubscriber)
}

fun <T> Flowable<Root<T>>.handleResponse(observableSubscriber: CancelableObservableSubscriber<T>) {
    this.afterRequest(observableSubscriber.specifiedCodes).compose(RunOnMainSchedulerTransformer<T>())
        .subscribe(observableSubscriber)
}

//添加处理函数
/*以下函数不做线程切换*/
fun <T> Observable<Root<T>>.handleResponseByDefault(observableSubscriber: CancelableObservableSubscriber<T>) {
    this.afterRequest(observableSubscriber.specifiedCodes).subscribe(observableSubscriber)
}

fun <T> Maybe<Root<T>>.handleResponseByDefault(observableSubscriber: CancelableObservableSubscriber<T>) {
    this.afterRequest(observableSubscriber.specifiedCodes).subscribe(observableSubscriber)
}

fun <T> Single<Root<T>>.handleResponseByDefault(observableSubscriber: CancelableObservableSubscriber<T>) {
    this.afterRequest(observableSubscriber.specifiedCodes).subscribe(observableSubscriber)
}

fun <T> Flowable<Root<T>>.handleResponseByDefault(observableSubscriber: CancelableObservableSubscriber<T>) {
    this.afterRequest(observableSubscriber.specifiedCodes).subscribe(observableSubscriber)
}
/*以上函数不做线程切换*/

// 必须自己定义错误处理函数
fun <T> Observable<Root<T>>.handleWithRawResponse(
    successHandler: Function1<Root<T>, Unit>,
    errorHandler: Function1<Throwable, Unit> = {}
) {

//    val afterRequest: Observable<T> = this.afterRequest().compose(RunOnMainSchedulerTransformer<T>())

    this.subscribe(successHandler, errorHandler)

}

// 自定义的网络返回并可以过滤不要的错误码 这种请求与界面没有关联
fun <T> Observable<Root<T>>.handleWithResponse(
    successHandler: Function1<T, Unit>,
    errorHandler: Function1<Throwable, Unit> = {}, specifiedCodes: IntArray = intArrayOf()
) {

    val afterRequest: Observable<T> = this.afterRequest(specifiedCodes).compose(RunOnMainSchedulerTransformer<T>())

    afterRequest.subscribe(successHandler, errorHandler)

}