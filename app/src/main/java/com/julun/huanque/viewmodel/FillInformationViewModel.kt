package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.events.ImformationCompleteBean
import com.julun.huanque.common.bean.forms.UpdateInformationForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.net.service.UserService
import kotlinx.coroutines.launch
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2020/7/3 19:29
 *@描述 完善数据页面
 */
class FillInformationViewModel : BaseViewModel() {

    companion object {
        //第一步（昵称，性别，生日）
        const val FIRST = "FIRST"

        //第二步(头像)
        const val SECOND = "SECOND"
    }

    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    val currentStatus: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    var completeBean = ImformationCompleteBean()

    /**
     * 上传昵称之类的 状态
     */
    val updateInformationState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    var birthdayData: Date? = null

    /**
     * @param sex 性别
     * @param bir 生日
     * @param nickname 昵称
     * @param code 邀请码
     */
    fun updateInformation(sex: String, bir: String, nickname: String, code: String) {
        viewModelScope.launch {
            request({
                updateInformationState.value = true
                userService.updateInformation(UpdateInformationForm(sexType = sex, birthday = bir, nickname = nickname, invitationCode = code))
                    .dataConvert()
                updateInformationState.value = false
                completeBean.sextype = sex
                completeBean.birthday = bir
                completeBean.nickname = nickname
                currentStatus.value = SECOND
            }, {
                updateInformationState.value = false
                if(it is ResponseError){
                    ToastUtils.show(it.busiMessage)
                }
            }, {})
        }
    }

}