package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.LoginTagInfo
import com.julun.huanque.common.bean.beans.UpdateSexBean
import com.julun.huanque.common.bean.forms.SocialWishIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/12/23 19:33
 *@描述 选择社交意向
 */
class SelectSocialWishViewModel : BaseViewModel() {
    //性别接口返回的数据
    val loginTagData: MutableLiveData<LoginTagInfo> by lazy { MutableLiveData<LoginTagInfo>() }

    /**
     * 保存社交意愿数据
     */
    fun saveWish(wishIdStr: String) {
        viewModelScope.launch {
            request({
                Requests.create(UserService::class.java).socialWish(SocialWishIdForm(wishIdStr))
                    .dataConvert()
            }, {})
        }
    }
}