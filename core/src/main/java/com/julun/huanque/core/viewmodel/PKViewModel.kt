package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.PKInfoBean
import com.julun.huanque.common.bean.forms.PKInfoForm
import com.julun.huanque.common.bean.forms.ProgramIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
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

    //pk状态  1 进行中  2 惩罚中   3 结束中 4 结束
    val pkState: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //    val pkClose: MutableLiveData<List<Int>> by lazy { MutableLiveData<List<Int>>() }
    var disposable: Disposable? = null
    val pkNum: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    //调用接口获取的PK数据
    val remotePkData: MutableLiveData<PKInfoBean> by lazy { MutableLiveData<PKInfoBean>() }

    //显示PK道具入口的标识位
    val openPropWindowData: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //    private var closeDispose: Disposable? = null
    val pkTime: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //获取PK信息
    fun getPkInfo(info: PKInfoForm) {
        Requests.create(PkMicService::class.java)
            .roomPkInfo(info)
            .handleResponse(makeSubscriber<PKInfoBean> {
                it.needAddMic = true
                it.needJustPlayStartAni = true
                setPkStart(it)
                remotePkData.value = it
            })
    }

    fun setPkStart(start: PKInfoBean) {
        //pk状态  1 进行中  2 惩罚中   3 结束中 4 结束
        val state = when {
            !start.roundFinish -> {
                1
            }
            start.punishRound -> {
                2
            }
            start.endRound -> {
                3
            }
            else -> {
                0
            }
        }
        pkState.postValue(state)
        pkStarting.value = start
        pkNum.value = start.detailList?.size
    }

    fun closeCountDown() {
        if (disposable != null) {
            disposable!!.dispose()
        }
    }

    fun getShowTime(totalCount: Int): String {
        return if (totalCount <= 0) "$currentTimeTitle  00:00" else {
            "$currentTimeTitle  ${showMin(totalCount)}:${showSecond(totalCount)}"
        }

    }

    private fun showMin(totalCount: Int): String {
        return if (totalCount / 60 > 9) "${totalCount / 60}" else {
            "0${totalCount / 60}"
        }
    }

    private fun showSecond(totalCount: Int): String {
        return if (totalCount % 60 > 9) "${totalCount % 60}" else {
            "0${totalCount % 60}"
        }
    }

    //当前时间标题
    private var currentTimeTitle: String = ""
    fun countDown(title: String, totalCount: Int?) {
        currentTimeTitle = title
//        textView.text = getShowTimeNew(totalCount)
        pkTime.postValue(getShowTime(totalCount ?: 0))
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
                    pkTime.postValue(getShowTime(count))
//                    textView.text = getShowTimeNew(count)

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

    fun userProp(programId: Long) {
        Requests.create(PkMicService::class.java)
            .useProp(ProgramIdForm(programId))
            .handleResponse(makeSubscriber<VoidResult> {

            })
    }

}