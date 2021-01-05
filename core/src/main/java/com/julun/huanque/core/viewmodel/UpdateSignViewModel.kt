package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.UserProcessBean
import com.julun.huanque.common.bean.forms.UpdateUserInfoForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2020/12/30 15:01
 *@描述 更新用户昵称
 */
class UpdateSignViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //原始昵称
    var mOriginalSign = ""

    //个签更新成功标识位
    val perfectionData: MutableLiveData<UserProcessBean> by lazy { MutableLiveData<UserProcessBean>() }

    //是否正在更新个性签名
    private var updateing = false

    /**
     * 更新昵称
     */
    fun updateSign(sign: String) {
        if (updateing || sign.isEmpty() || sign == mOriginalSign) {
            return
        }
        updateing = true
        viewModelScope.launch {
            request({
                val form = UpdateUserInfoForm(mySign = sign)
                perfectionData.value = userService.updateUserInfo(form).dataConvert()
                //通知外界，基础数据有变化
                EventBus.getDefault().post(form)
                mOriginalSign = sign
            }, {}, { updateing = false })
        }
    }

}