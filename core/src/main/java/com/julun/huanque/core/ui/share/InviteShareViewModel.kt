package com.julun.huanque.core.ui.share

import androidx.lifecycle.*
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.SharePosterInfo
import com.julun.huanque.common.bean.beans.ShareType
import com.julun.huanque.common.bean.forms.SharePosterQueryForm
import com.julun.huanque.common.bean.forms.ShareWay
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ShareTypeEnum
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.ShareService
import com.julun.huanque.common.suger.*
import com.julun.huanque.core.R
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/21 20:49
 *
 *@Description: VoiceSignViewModel
 *
 */
class InviteShareViewModel : BaseViewModel() {


    private val service: ShareService by lazy { Requests.create(ShareService::class.java) }

    val sharePosters: MutableLiveData<ReactiveData<SharePosterInfo>> by lazy { MutableLiveData<ReactiveData<SharePosterInfo>>() }

    val shares: MutableLiveData<ReactiveData<MutableList<ShareType>>> by lazy { MutableLiveData<ReactiveData<MutableList<ShareType>>>() }


    fun queryShareType() {
        shares.value = mutableListOf<ShareType>().apply {
            add(ShareType().apply {
                this.res = R.mipmap.icon_share_wx
                this.title = "微信好友"
                this.type = ShareTypeEnum.WeChat
            })
            add(ShareType().apply {
                this.res = R.mipmap.icon_share_pyq
                this.title = "朋友圈"
                this.type = ShareTypeEnum.FriendCircle
            })
            add(ShareType().apply {
                this.res = R.mipmap.icon_share_wb
                this.title = "微博"
                this.type = ShareTypeEnum.Sina
            })
            add(ShareType().apply {
                this.res = R.mipmap.icon_share_save_image
                this.title = "保存图片"
                this.type = ShareTypeEnum.SaveImage
            })
        }.convertRtData()
    }

    fun querySharePoster(applyModule: String) {
        viewModelScope.launch {
            request({
                val result = service.sharePoster(SharePosterQueryForm(applyModule)).dataConvert()
                if (result.inviteCode.isNotEmpty()) {
                    result.posterList.forEach {
                        it.inviteCode = result.inviteCode
                    }
                }

                sharePosters.value = result.convertRtData()
            }, error = { e ->
                logger("报错了：$e")
                sharePosters.value = e.convertError()
            })

        }
    }
}