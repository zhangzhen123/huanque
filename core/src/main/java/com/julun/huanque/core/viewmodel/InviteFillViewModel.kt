package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.forms.InviteCompleteForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2021/1/12 9:47
 *@描述 邀请好友弹窗使用
 */
class InviteFillViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //当前邀请类型
    var mType = ""
    var userId = 0L

    //邀请完善接口调用成功
    val inviteSuccess: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    /**
     * 邀请填写
     * @param dialog 是否在dialog中使用
     */
    fun inviteFill(dialog: Boolean = true) {
        viewModelScope.launch {
            request({
                userService.inviteComplete(InviteCompleteForm(userId, mType)).dataConvert()
                if (dialog) {
                    inviteSuccess.value = true
                }
                when (mType) {
                    InviteCompleteForm.Information -> {
                        ToastUtils.show("已邀请TA填写资料")
                    }
                    InviteCompleteForm.AuthTag -> {
                        //认证标签
                        ToastUtils.show("已邀请TA上传拥有的标签")
                    }
                    InviteCompleteForm.LikeTag -> {
                        //喜欢标签
                        ToastUtils.show("已邀请TA添加喜欢的标签")
                    }
                    else -> {
                    }
                }

            }, {})
        }
    }

}