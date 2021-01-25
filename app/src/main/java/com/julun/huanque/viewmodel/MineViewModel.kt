package com.julun.huanque.viewmodel

import androidx.lifecycle.*
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.UserDetailInfo
import com.julun.huanque.common.bean.forms.UpdateInformationForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.BalanceUtils
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.utils.SPUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/11 14:05
 *
 *@Description: MineViewModel
 *
 */
class MineViewModel : BaseViewModel() {

    private val userService: UserService by lazy {
        Requests.create(UserService::class.java)
    }
    val checkAuthorResult: MutableLiveData<ReactiveData<Boolean>> by lazy { MutableLiveData<ReactiveData<Boolean>>() }

    //余额数据
    val balance: LiveData<Long> by lazy { BalanceUtils.getBalance() }

    //当前邀请码时间
    val codeShowStatus: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //协程请求示例
    val userInfo: LiveData<ReactiveData<UserDetailInfo>> = queryState.switchMap {
        liveData {
            request({
                val user = userService.queryUserDetailInfo().dataConvert()
                BalanceUtils.saveBalance(user.userBasic.beans)
                val codeTtl = user.inviteCodeTtl
                if (codeTtl > 0) {
                    //显示邀请码入口
                    codeShowStatus.value = true
                    inviteCodeTimer(codeTtl)
                } else {
                    codeShowStatus.value = false
                }
                if (user.nameDefault == BusiConstant.True) {
                    SPUtils.commitString(SPParamKey.DefaultNickname, BusiConstant.True)
                }
                if (user.headDefault == BusiConstant.True) {
                    SPUtils.commitString(SPParamKey.DefaultHeader, BusiConstant.True)
                }

                emit(ReactiveData(NetStateType.SUCCESS, user))
            }, error = { e ->
                logger("报错了：$e")
//                emit(ReactiveData(NetStateType.ERROR, error = e.coverError()))
                emit(e.convertError())
            }, needLoadState = it == QueryType.INIT)


        }

    }

    private var mCodeDisposable: Disposable? = null

    /**
     * 邀请码倒计时
     */
    private fun inviteCodeTimer(count: Long) {
        mCodeDisposable = Observable.timer(count, TimeUnit.SECONDS)
            .subscribe({
                codeShowStatus.postValue(false)
            }, {})
    }

    /**
     * 清除倒计时
     */
    fun clearCodeTimer() {
        mCodeDisposable?.dispose()
        codeShowStatus.postValue(false)
    }


    fun checkToAnchor() {
        viewModelScope.launch {
            request({
                userService.checkToAnchor()
                    .dataConvert(intArrayOf(ErrorCodes.NOT_INFO_COMPLETE, ErrorCodes.NOT_BIND_WECHAT, ErrorCodes.NOT_REAL_NAME))
                checkAuthorResult.value = true.convertRtData()
            }, error = {
                checkAuthorResult.value = it.convertError()
            })
        }


    }


    override fun onCleared() {
        super.onCleared()
        mCodeDisposable?.dispose()
    }

}