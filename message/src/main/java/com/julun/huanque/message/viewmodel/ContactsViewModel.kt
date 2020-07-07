package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.SocialListBean
import com.julun.huanque.common.bean.beans.UserDataTab
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/7/7 9:56
 *@描述 联系人使用的ViewModel
 */
class ContactsViewModel : BaseViewModel() {
    private val service: SocialService by lazy { Requests.create(SocialService::class.java) }

    //顶部tab数据
    val tabListData: MutableLiveData<MutableList<UserDataTab>> by lazy { MutableLiveData<MutableList<UserDataTab>>() }

    val socialListData: MutableLiveData<SocialListBean> by lazy { MutableLiveData<SocialListBean>() }

    /**
     * 获取联系人数据
     */
    fun getContacts() {
        viewModelScope.launch {
            request({
                val contactsData = service.socialList().dataConvert()
                tabListData.value = contactsData.userDataTabList
                socialListData.value = contactsData
            }, {
                if (it is ResponseError) {
                    ToastUtils.show(it.busiMessage)
                }
            })
        }
    }

}