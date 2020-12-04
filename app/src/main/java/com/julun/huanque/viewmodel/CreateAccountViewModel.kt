package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.forms.CreateAccountForm
import com.julun.huanque.common.bean.forms.NicknameForm
import com.julun.huanque.common.bean.forms.UserIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.coroutines.launch
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2020/12/3 9:29
 *@描述 创建分身ViewModel
 */
class CreateAccountViewModel : BaseViewModel() {
    //生日
    var birthdayDate: Date? = null

    //昵称是否有变化
    var nicknameChange = false

    //头像数据
    val headerPicData: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //昵称是否可用标识
    val nicknameEnable: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //隐藏上传头像的标记位
    val loadingDismissState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //创建账号成功标识
    val createAccountSuccess: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    private val userService: UserService by lazy {
        Requests.create(
            UserService::class.java
        )
    }

    /**
     * 校验昵称
     */
    fun checkNickName(chickName: String) {
        nicknameChange = false
        viewModelScope.launch {
            request({
                val result = userService.checkNickName(NicknameForm(chickName)).dataConvert()
                nicknameEnable.value = true
            })
        }
    }

    /**
     * 上传头像
     */
    fun uploadHead(path: String) {
        OssUpLoadManager.uploadFiles(
            arrayListOf(path),
            OssUpLoadManager.HEAD_POSITION
        ) { code, list ->
            loadingDismissState.value = true
            if (code == OssUpLoadManager.CODE_SUCCESS) {
                logger("头像上传oss成功：${list}")
                val headPic = list?.firstOrNull()
                if (headPic != null) {
//                    updateInformation(headPic, sex, bir, nickname, iCode)
                    headerPicData.value = headPic
                }

            } else {
//                uploadHeadState.value = null
            }
        }
    }

    /**
     * 创建账号
     */
    fun subCreate(form: CreateAccountForm) {
        viewModelScope.launch {
            request({
                userService.subCreate(form).dataConvert()
                ToastUtils.show("分身创建成功")
                createAccountSuccess.value = true
            }, {})
        }
    }
}