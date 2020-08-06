package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.forms.SettingForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.launch

/**
 * @author WanZhiYuan
 * @date 2020/07/15
 */
class MessageSettingViewModel : BaseViewModel() {


    /**
     * 获取消息设置信息
     */
    val settingResult = queryState.switchMap {
        liveData {
                request({
                    val data = Requests.create(SocialService::class.java).settings().dataConvert()
                    emit(data)
                }, error = { e ->
                    if (e !is ResponseError) {
                        ToastUtils.show("网络异常，请重试~！")
                    }
                }, needLoadState = it==QueryType.INIT)
        }
    }

    /**
     * 更新消息设置相关
     * @param privateMsgFee 非好友私信费用
     * @param foldMsg 折叠非好友 密友消息
     * @param answer 接听语音
     * @param privateMsgRemind 私信提醒
     * @param followRemind 关注提醒
     */
    fun updateSetting(
        privateMsgFee: Int? = null, foldMsg: String? = null, answer: String? = null,
        privateMsgRemind: String? = null, followRemind: String? = null
    ) {
        viewModelScope.launch {
            request({
                Requests.create(SocialService::class.java).updateSettings(
                    SettingForm(
                        privateMsgFee, foldMsg
                        , answer, privateMsgRemind, followRemind
                    )
                ).dataConvert()
                queryState.value = QueryType.REFRESH
            }, error = { e ->
                if (e !is ResponseError) {
                    ToastUtils.show("网络异常，请重试~！")
                }
            }, needLoadState = true)
        }
    }
}