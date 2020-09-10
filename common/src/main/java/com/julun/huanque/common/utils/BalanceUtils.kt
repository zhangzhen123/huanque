package com.julun.huanque.common.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.database.HuanQueDatabase
import com.julun.huanque.common.database.table.Balance
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by dong on 2018/8/8.
 */
object BalanceUtils {
    val userService: UserService by lazy { Requests.create(UserService::class.java) }

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

    /**
     * 从服务端获取最新余额
     */
    fun queryLatestBalance() {
        GlobalScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    val result = userService.beans().dataConvert()
                    saveBalance(result.beans)
                }
            }
        }

    }
}