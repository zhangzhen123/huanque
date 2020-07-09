package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.forms.UpdateHeadForm
import com.julun.huanque.common.bean.forms.UpdateInformationForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.SessionUtils
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

    /**
     * 上传昵称之类的 状态
     */
    val updateInformationState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    var birthdayData: Date? = null

    /**
     * 上传头像回调  为Null代表失败
     */
    val uploadHeadState: MutableLiveData<String?> by lazy { MutableLiveData<String?>() }


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
                userService.updateInformation(
                    UpdateInformationForm(
                        sexType = sex,
                        birthday = bir,
                        nickname = nickname,
                        invitationCode = code
                    )
                )
                    .dataConvert()
                updateInformationState.value = false
                SessionUtils.setNickName(nickname)
//                completeBean.sextype = sex
//                completeBean.birthday = bir
//                completeBean.nickname = nickname
                currentStatus.value = SECOND
            }, {
                updateInformationState.value = false
                if (it is ResponseError) {
                    ToastUtils.show(it.busiMessage)
                }
            }, {})
        }
    }

    /**
     * 模拟图片上传成功
     */
    fun headerSuccess(headerPic : String) {
        SessionUtils.setHeaderPic(headerPic)
        SessionUtils.setRegComplete(true)
    }

    /**
     * 上传头像
     */
    fun uploadHead(path: String) {
        OssUpLoadManager.uploadFiles(arrayListOf(path), OssUpLoadManager.HEAD_POSITION) { code, list ->
            if (code == OssUpLoadManager.CODE_SUCCESS) {
                logger("头像上传oss成功：${list}")
                val headPic = list?.firstOrNull()
                if (headPic != null) {
                    viewModelScope.launch {
                        request({
                            userService.updateHeadPic(UpdateHeadForm(headPic))
                            logger("头像修改通知后台成功：${list}")
                            headerSuccess(headPic)
                            uploadHeadState.value = headPic
                        }, error = {
                            uploadHeadState.value = null
                        })
                    }
                }

            } else {
                uploadHeadState.value = null
            }
        }
    }

}