package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.AccostMsg
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.bean.forms.QuickAccostForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/10/10 20:08
 *@描述 快速匹配网络请求使用的ViewModel
 */
class FateQuickMatchViewModel : BaseViewModel() {
    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //消息数据
    val msgData: MutableLiveData<AccostMsg> by lazy { MutableLiveData<AccostMsg>() }

    //显示alert标识
    val showAlertFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    /**
     * 随机获取一条常用语
     */
    fun getRandomWords(userId: Long) {
        viewModelScope.launch {
            request({
                val result = socialService.chatWordsRandom(FriendIdForm(userId)).dataConvert(intArrayOf(204))
                msgData.value = result
            }, {
                if (it is ResponseError && it.busiCode == 204) {
                    showAlertFlag.value = true
                }
            })
        }
    }

}