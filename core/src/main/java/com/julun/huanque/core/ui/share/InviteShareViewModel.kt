package com.julun.huanque.core.ui.share

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.PostShareForm
import com.julun.huanque.common.bean.forms.ShareLiveForm
import com.julun.huanque.common.bean.forms.SharePosterImageForm
import com.julun.huanque.common.bean.forms.SharePosterQueryForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ShareFromModule
import com.julun.huanque.common.constant.ShareTypeEnum
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.ShareService
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.bitmap.BitmapUtil
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.activity_invite_share.*
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

    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    val sharePosters: MutableLiveData<ReactiveData<SharePosterInfo>> by lazy { MutableLiveData<ReactiveData<SharePosterInfo>>() }

    val shares: MutableLiveData<ReactiveData<MutableList<ShareType>>> by lazy { MutableLiveData<ReactiveData<MutableList<ShareType>>>() }

    val liveShareInfo: MutableLiveData<ReactiveData<ShareObject>> by lazy { MutableLiveData<ReactiveData<ShareObject>>() }

    /**
     * 动态从服务端获取的分享数据
     */
    val postShareBeanData: MutableLiveData<PostShareBean> by lazy { MutableLiveData<PostShareBean>() }

    //分享类型(动态，评价，直播间 等等)
    var type: String = ""

    var programInfo: MicAnchor? = null
    fun queryShareType(applyModule: String) {
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
            if (ShareFromModule.Program == applyModule) {
                add(ShareType().apply {
                    this.res = R.mipmap.icon_share_image
                    this.title = "分享图片"
                    this.type = ShareTypeEnum.ShareImage
                })
            } else {
                add(ShareType().apply {
                    this.res = R.mipmap.icon_share_save_image
                    this.title = "保存图片"
                    this.type = ShareTypeEnum.SaveImage
                })
            }

        }.convertRtData()
    }

    /**
     * @param hasInner 是否是站内分享
     */
    fun getShareType(shareImg: Boolean, hasInner: Boolean = false): MutableList<ShareType> {
        return mutableListOf<ShareType>().apply {
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
            if (hasInner) {
                //站内分享
                add(ShareType().apply {
                    this.res = R.mipmap.icon_share_inner
                    this.title = "站内消息"
                    this.type = ShareTypeEnum.Chat
                })
            } else {
                if (shareImg) {
                    add(ShareType().apply {
                        this.res = R.mipmap.icon_share_image
                        this.title = "分享图片"
                        this.type = ShareTypeEnum.ShareImage
                    })
                } else {
                    add(ShareType().apply {
                        this.res = R.mipmap.icon_share_save_image
                        this.title = "保存图片"
                        this.type = ShareTypeEnum.SaveImage
                    })
                }
            }

        }
    }

    fun querySharePoster(applyModule: String) {
        viewModelScope.launch {
            request({
                val form = when (applyModule) {
                    ShareFromModule.Invite -> {
                        SharePosterQueryForm(applyModule)
                    }
                    else -> {
                        SharePosterQueryForm(applyModule)
                    }
                }

                val result = service.sharePoster(form).dataConvert()
                sharePosters.value = result.convertRtData()
                result.posterList.forEach { post ->
                    if (result.inviteCode.isNotEmpty()) {
                        post.inviteCode = result.inviteCode
                    }
                    post.qrBitmap = BitmapUtil.base64ToBitmap(post.qrCodeBase64.replace("data:image/png;base64,", ""))
                }
            }, error = { e ->
                logger("报错了：$e")
                sharePosters.value = e.convertError()
            })

        }
    }

    fun queryLiveQrCode() {
        viewModelScope.launch {
            request({
                val url = programInfo?.headPic ?: return@request
                val form = SharePosterImageForm(programId = programInfo?.programId ?: return@request)
                val result = service.programShare(form).dataConvert()
                val bitmap = BitmapUtil.base64ToBitmap(result.replace("data:image/png;base64,", ""))
                val info = SharePosterInfo(
                    posterList = mutableListOf<SharePoster>(
                        SharePoster(
                            applyModule = ShareFromModule.Program,
                            posterPic = programInfo?.prePic ?: ""
                        ).apply {
                            authorName = programInfo?.programName ?: ""
                            qrBitmap = bitmap
                        }
                    )
                )
                sharePosters.value = info.convertRtData()
            }, error = { e ->
                logger("报错了：$e")
                sharePosters.value = e.convertError()
            })

        }
    }

    fun queryLiveShareInfo() {
        viewModelScope.launch {
            request({
                val url = programInfo?.headPic ?: return@request
                val form = ShareLiveForm(programId = programInfo?.programId ?: return@request)
                val result = service.programShareInfo(form).dataConvert()

                liveShareInfo.value = result.convertRtData()
            }, error = { e ->
                logger("报错了：$e")
                liveShareInfo.value = e.convertError()
            })

        }
    }

    /**
     * 获取动态分享信息
     */
    fun queryPostShareInfo(shareType: String?, postId: Long, commentId: Long? = null) {
        viewModelScope.launch {
            request({
                val result = socialService.postShare(PostShareForm(shareType, postId, commentId)).dataConvert()
                if (commentId == null) {
                    result.shareType = shareType ?: ""
                }
                val bitmap = BitmapUtil.base64ToBitmap(result.qrCode.replace("data:image/png;base64,", ""))
                result.qrBitmap = bitmap
                postShareBeanData.value = result.apply {
                    if (commentId == null && pic.isNotEmpty() && !pic.contains("?")) {
                        //分享动态
                        pic = "${pic}?x-oss-process=image/resize,m_fixed,w_100,h_100"
                    }

                }
            }, {})
        }
    }

}