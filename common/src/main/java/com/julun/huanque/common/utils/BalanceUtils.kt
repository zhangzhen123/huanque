package com.julun.huanque.common.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.database.HuanQueDatabase
import com.julun.huanque.common.database.table.Balance
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Created by dong on 2018/8/8.
 */
object BalanceUtils {

    /**
     * 获取余额
     */
    fun getBalance(): LiveData<Long> {
        val userId = SessionUtils.getUserId()
        return if (userId == 0L) {
            MutableLiveData<Long>()
        } else {
            HuanQueDatabase.getInstance().balanceDao().getBalance(userId).switchMap {
                val data = MutableLiveData<Long>()
                if (it?.userId == SessionUtils.getUserId()) {
                    data.value = it.balance
                    return@switchMap data
                } else {
                    return@switchMap data
                }
            }
        }
    }

    /**
     * 保存余额
     */
    fun saveBalance(bean: Long) {
        val userId = SessionUtils.getUserId()
        if (userId > 0) {
            Completable.complete()
                .subscribeOn(Schedulers.io())
                .subscribe({ HuanQueDatabase.getInstance().balanceDao().insert(Balance(userId, bean)) }, { it.printStackTrace() })
        }
    }
}