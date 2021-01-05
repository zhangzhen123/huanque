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
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2021/1/5 13:33
 *@描述 更新生日ViewModel
 */
class UpdateBirthdayViewModel : BaseViewModel() {
    private val userService : UserService by lazy { Requests.create(UserService::class.java) }
    //原始生日
    var originalDate: Date? = null

    //生日数据
    val birthdayData: MutableLiveData<Date> by lazy { MutableLiveData<Date>() }

    //资料完善度数据
    val processData: MutableLiveData<UserProcessBean> by lazy { MutableLiveData<UserProcessBean>() }

    /**
     * 更新生日
     */
    fun updateBirthday(birthday : String){
        viewModelScope.launch {
            request({
                val form = UpdateUserInfoForm(birthday = birthday)
                processData.value = userService.updateUserInfo(form).dataConvert()
                EventBus.getDefault().post(form)
            },{})
        }
    }

}