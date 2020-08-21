package com.julun.huanque.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.forms.NicknameForm
import com.julun.huanque.common.bean.forms.UpdateInformationForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.LoginStatusUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.support.LoginManager
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

    private val userService: UserService by lazy { Requests.create(
        UserService::class.java) }

    val currentStatus: MutableLiveData<String> by lazy { MutableLiveData<String>() }

//    /**
//     * 上传昵称之类的 状态
//     */
//    val updateInformationState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    var birthdayData: Date? = null

    /**
     * 上传头像回调  为Null代表失败
     */
    val uploadHeadState: MutableLiveData<String?> by lazy { MutableLiveData<String?>() }

    //昵称是否可用标识
    val nicknameEnable: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //登录状态
    val loginStatus: LiveData<Boolean> by lazy { LoginStatusUtils.getLoginStatus() }

    //昵称是否有变化
    var nicknameChange = false


    /**
     * @param sex 性别
     * @param bir 生日
     * @param nickname 昵称
     * @param code 邀请码
     */
    private fun updateInformation(headerPic: String, sex: String, bir: String, nickname: String, code: String) {
        viewModelScope.launch {
            request({
//                updateInformationState.value = true
                val result = userService.updateInformation(
                    UpdateInformationForm(
                        sexType = sex,
                        birthday = bir,
                        nickname = nickname,
                        invitationCode = code,
                        headPic = headerPic
                    )
                )
                    .dataConvert()
//                updateInformationState.value = false
                SessionUtils.setNickName(nickname)
                SessionUtils.setHeaderPic(headerPic)
                if (result.sex.isNotEmpty()) {
                    SessionUtils.setSex(result.sex)
                }
                if (result.imToken.isNotEmpty()) {
                    SessionUtils.setRongImToken(result.imToken)
                }
                LoginManager.loginSuccessComplete()
                uploadHeadState.value = headerPic
            }, {
//                updateInformationState.value = false
                uploadHeadState.value = null
                if (it is ResponseError) {
                    ToastUtils.show(it.busiMessage)
                }
            }, {})
        }
    }

    /**
     * 上传头像
     */
    fun uploadHead(path: String, sex: String, bir: String, nickname: String, iCode: String) {
        OssUpLoadManager.uploadFiles(arrayListOf(path), OssUpLoadManager.HEAD_POSITION) { code, list ->
            if (code == OssUpLoadManager.CODE_SUCCESS) {
                logger("头像上传oss成功：${list}")
                val headPic = list?.firstOrNull()
                if (headPic != null) {
                    updateInformation(headPic, sex, bir, nickname, iCode)
                }

            } else {
                uploadHeadState.value = null
            }
        }
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


}