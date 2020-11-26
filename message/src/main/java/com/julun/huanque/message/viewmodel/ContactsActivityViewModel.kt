package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.SocialListBean
import com.julun.huanque.common.bean.beans.UserDataTab
import com.julun.huanque.common.bean.forms.ContactsForm
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
class ContactsActivityViewModel : BaseViewModel() {
    private val service: SocialService by lazy { Requests.create(SocialService::class.java) }

    //顶部tab数据
    val tabListData: MutableLiveData<MutableList<UserDataTab>> by lazy { MutableLiveData<MutableList<UserDataTab>>() }
    //官方运营id
    val officialUserId: MutableLiveData<Long> by lazy { MutableLiveData<Long>() }

    val socialListData: MutableLiveData<SocialListBean> by lazy { MutableLiveData<SocialListBean>() }

    //关注列表 关注状态变化
//    val followChangeFlag: MutableLiveData<UserInfoChangeResult> by lazy { MutableLiveData<UserInfoChangeResult>() }

    //刷新关注列表
    val followRefreshFlag : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //关注列表需要刷新的标识位
    var followNeedRefresh = false

    /**
     * 获取联系人数据
     */
    fun getContacts() {
        viewModelScope.launch {
            request({
                val contactsData = service.socialList(ContactsForm()).dataConvert()
                tabListData.value = contactsData.userDataTabList
                socialListData.value = contactsData
                officialUserId.value = contactsData.officialUserId
            }, {
                if (it is ResponseError) {
                    ToastUtils.show(it.busiMessage)
                }
            })
        }
    }
}