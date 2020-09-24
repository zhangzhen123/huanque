package com.julun.huanque.common.ui.video

import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.forms.SaveTeachVideoForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/24 10:47
 *
 *@Description: VideoPageModel
 *
 */
class VideoPageModel : BaseViewModel() {


    private val mUserService: UserService by lazy { Requests.create(UserService::class.java) }


//    val saveResult: MutableLiveData<VoidResult> by lazy { MutableLiveData<VoidResult>() }

    fun saveTeachVideo(videoId: Long){
        viewModelScope.launch {
            request({
                val result = mUserService.saveTeachVideo(SaveTeachVideoForm(videoId)).dataConvert()
//                saveResult.value = result
            })
        }

    }
}