package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.PostShareBean
import com.julun.huanque.common.bean.beans.SocialListBean
import com.julun.huanque.common.bean.beans.SocialUserInfo
import com.julun.huanque.common.bean.beans.StatusResult
import com.julun.huanque.common.bean.events.ShareSuccessEvent
import com.julun.huanque.common.bean.forms.ContactsForm
import com.julun.huanque.common.bean.forms.PostShareForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ShareTypeEnum
import com.julun.huanque.common.manager.SendMessageManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.handleResponse
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2020/11/26 9:50
 *@描述  获取密友数据的ViewModel
 */
class ShareFriendsViewModel : BaseViewModel() {
    private val service: SocialService by lazy { Requests.create(SocialService::class.java) }

    //联系人数据
    val socialListBean: MutableLiveData<SocialListBean> by lazy { MutableLiveData<SocialListBean>() }

    //动态分享对象
    var mPostShareBean: PostShareBean? = null

    private var offset = 0

    /**
     * 获取密友列表
     */
    fun queryContacets() {
        viewModelScope.launch {
            request({
                val homeListData = service.socialList(ContactsForm("Intimate", offset)).dataConvert()
                val list = homeListData.linkList
                offset += list.size
                socialListBean.value = homeListData
            }, {}, needLoadState = true)
        }
    }

    /**
     * 发送动态消息
     */
    fun sendPostMessage(user: SocialUserInfo) {
        ToastUtils.show("分享成功")
        SendMessageManager.sendPostMessage(mPostShareBean ?: return, user)
        Requests.create(SocialService::class.java)
            .saveShareLog(PostShareForm(ShareTypeEnum.Chat, mPostShareBean?.postId ?: 0))
            .handleResponse(makeSubscriber<StatusResult> {
                logger("分享保存记录成功")
                if (it.status == BusiConstant.True) {
                    EventBus.getDefault().post(ShareSuccessEvent(mPostShareBean?.postId ?: 0, null))
                }
            }.ifError {
                if (it is ResponseError) {
                    logger("分享保存记录失败 , ${it.busiMessage}")
                } else {
                    logger("分享保存记录失败 , ${it.message}")
                }
            })
    }
}