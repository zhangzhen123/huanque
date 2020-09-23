package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.UpdateSexBean
import com.julun.huanque.common.bean.forms.SexForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.support.LoginManager
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/9/18 15:35
 *@描述 更新性别页面使用的ViewModel
 */
class SelectSexViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //用户数据
    val infoBean: MutableLiveData<UpdateSexBean> by lazy { MutableLiveData<UpdateSexBean>() }

    /**
     * 设置性别
     */
    fun updateSex(male: Boolean) {
        viewModelScope.launch {
            request({
                val maleStr = if (male) {
                    Sex.MALE
                } else {
                    Sex.FEMALE
                }
                val result = userService.initInfoBySex(SexForm(maleStr)).dataConvert()
                infoBean.value = result
                SessionUtils.setInviteCode(result.invitationCode)
                SessionUtils.setNickName(result.nickname)
                SessionUtils.setHeaderPic(result.headPic)
                if (result.sex.isNotEmpty()) {
                    SessionUtils.setSex(result.sex)
                }
                if (result.imToken.isNotEmpty()) {
                    SessionUtils.setRongImToken(result.imToken)
                }
                LoginManager.loginSuccessComplete()
            }, {})
        }
    }

}