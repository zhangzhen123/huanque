package com.julun.huanque.common.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.database.HuanQueDatabase
import com.julun.huanque.common.database.table.LoginStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *@创建者   dong
 *@创建时间 2020/7/28 17:50
 *@描述
 */
object LoginStatusUtils {
    //获取登录状态
    fun getLoginStatus(): LiveData<Boolean> {
        val loginData = MutableLiveData<Boolean>()
        val status = HuanQueDatabase.getInstance().loginStatusDao().getLoginStatus(SessionUtils.getUserId())
        if (status == null) {
            loginData.value = null
        } else {
            loginData.value = status.value?.login ?: false
        }
        return loginData
    }

    /**
     * 保存登录状态
     */
    fun loginSuccess() {
        //登录成功
        GlobalScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    val status = LoginStatus(SessionUtils.getUserId(), true)
                    HuanQueDatabase.getInstance().loginStatusDao().insert(status)
                }
            }

        }

    }

    /**
     * 登录失效
     */
    fun logout() {
        GlobalScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    HuanQueDatabase.getInstance().loginStatusDao().deleteLoginStatus()
                }
            }
        }

    }


}