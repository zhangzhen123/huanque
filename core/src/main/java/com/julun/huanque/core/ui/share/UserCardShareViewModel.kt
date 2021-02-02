package com.julun.huanque.core.ui.share

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.ShareType
import com.julun.huanque.common.bean.beans.UserCardShareInfo
import com.julun.huanque.common.bean.forms.UserIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ShareTypeEnum
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.ShareService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.bitmap.BitmapUtil
import com.julun.huanque.core.R
import kotlinx.coroutines.launch

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2021/2/1 13:17
 *
 *@Description: UserCardShareViewModel
 *
 */

class UserCardShareViewModel : BaseViewModel() {


    private val service: ShareService by lazy { Requests.create(ShareService::class.java) }

    val shares: MutableLiveData<ReactiveData<MutableList<ShareType>>> by lazy { MutableLiveData<ReactiveData<MutableList<ShareType>>>() }

    val userCardInfo: MutableLiveData<ReactiveData<UserCardShareInfo>> by lazy { MutableLiveData<ReactiveData<UserCardShareInfo>>() }

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

    fun queryCardInfo(userId: Long) {
        viewModelScope.launch {
            request({
                val result = service.cardInfo(UserIdForm(userId)).dataConvert()

                repeat(2) {
                    val bitmap = BitmapUtil.base64ToBitmap(result.qrCodeBase64.replace("data:image/png;base64,", ""))
                    if (bitmap != null) {
                        result.bitmaps.add(bitmap)
                    }
                }
                userCardInfo.value = result.convertRtData()
            }, error = { e ->
                logger("报错了：$e")
                userCardInfo.value = e.convertError()
            })

        }
    }

}