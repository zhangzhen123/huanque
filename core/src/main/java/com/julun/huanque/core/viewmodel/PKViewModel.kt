package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.annotation.NonNull
import android.widget.TextView
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.PKInfoBean
import com.julun.huanque.common.bean.forms.PKInfoForm
import com.julun.huanque.common.bean.forms.ProgramIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.PKType
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.PkMicService
import com.julun.huanque.common.suger.handleResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 *
 *@author zhangzhen
 *@data 2018/11/16
 * pk相关
 *
 **/

class PKViewModel : BaseViewModel() {
    //Pk相关数据
//    val pkData: MutableLiveData<PKInfoBean> by lazy { MutableLiveData<PKInfoBean>() }
    //网络请求的pkStarting状态或者im消息的
    val pkStarting: MutableLiveData<PKInfoBean> by lazy { MutableLiveData<PKInfoBean>() }

    //pk状态  1 进行中  2 惩罚中   3 结束
    val pkState: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //    val pkClose: MutableLiveData<List<Int>> by lazy { MutableLiveData<List<Int>>() }
    var disposable: Disposable? = null
    val pkNum: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //调用接口获取的PK数据
    val remotoPkData: MutableLiveData<PKInfoBean> by lazy { MutableLiveData<PKInfoBean>() }

    //显示PK道具入口的标识位
    val openPropWindowData: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

//    private var closeDispose: Disposable? = null

    //获取PK信息
    fun getPkInfo(info: PKInfoForm) {
        Requests.create(PkMicService::class.java)
            .roomPkInfo(info)
            .handleResponse(makeSubscriber<PKInfoBean> {
                setPkStart(it)
                remotoPkData.value = it
            })
    }

    fun setPkStart(start: PKInfoBean) {
        val state = if ((start.seconds ?: 0) > 0) 1 else 2
        pkState.postValue(state)
        pkStarting.value = start
        pkNum.value = start.detailList?.size

        //统一后台处理关闭 本地不要手动倒计时关闭了
        //需要手动结束
//        if (start.seconds == -2 && start.closeSeconds != null && start.closeSeconds!! > 0) {
//            //
//            //需要手动结束
//            //
//            closeDispose = Observable.timer(start.closeSeconds!!.toLong(), TimeUnit.SECONDS)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe({
//                        logger.info("开始手动关闭pk以及流")
//                        val list = arrayListOf<Int>()
//                        start.detailList?.forEach {
//                            if (it.programId != null)
//                                list.add(it.programId!!)
//                        }
//                        pkClose.value = list
//                    })
//        }
    }

    //    fun cancelShutdownPk(){
//        closeDispose?.dispose()
//    }
    fun closeCountDown() {
        if (disposable != null) {
            disposable!!.dispose()
        }
    }

    @NonNull
    fun getShowTime(totalCount: Int?): String {
        return if (totalCount == null || totalCount <= 0) "00:00" else {
            "剩余:${showMin(totalCount)}:${showSecond(totalCount)}"
        }

    }

    fun showMin(totalCount: Int): String {
        return if (totalCount / 60 > 9) "${totalCount / 60}" else {
            "0${totalCount / 60}"
        }
    }

    fun showSecond(totalCount: Int): String {
        return if (totalCount % 60 > 9) "${totalCount % 60}" else {
            "0${totalCount % 60}"
        }
    }

    @NonNull
    private fun getShowTimeNew(totalCount: Int?): String {
        return if (totalCount == null || totalCount <= 0) "00:00" else {
            "${showMin(totalCount)}:${showSecond(totalCount)}"
        }

    }

    //新版本分钟
    private fun showMinNew(totalCount: Int) = "${totalCount / 60}:"

    //新版本秒
    private fun showSecondNew(totalCount: Int): String {
        return if (totalCount % 60 > 9) "${totalCount % 60}" else {
            "0${totalCount % 60}"
        }
    }


    fun countDown(@NonNull textView: TextView, totalCount: Int?, pkType: String? = PKType.PK) {
        textView.text = getShowTimeNew(totalCount)
        if (totalCount == null || totalCount <= 0) {
            return
        }
        var count: Int = totalCount

        Observable.interval(0, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Long> {
                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onNext(t: Long) {


                    if (count == 0) {
                        disposable!!.dispose()
                    }
                    textView.text = getShowTimeNew(count)

                    count--
                }

                override fun onComplete() {
                    disposable!!.dispose()
                }

                override fun onSubscribe(d: Disposable) {
                    disposable = d
                }

            })
    }

    fun userProp(programId: Int) {
        Requests.create(PkMicService::class.java)
            .useProp(ProgramIdForm(programId))
            .handleResponse(makeSubscriber<VoidResult> {

            })
    }

}