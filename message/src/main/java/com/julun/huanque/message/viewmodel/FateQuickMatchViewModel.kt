package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.AccostMsg
import com.julun.huanque.common.bean.beans.ChatBubble
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.forms.FriendIdForm
import com.julun.huanque.common.bean.forms.QuickAccostForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.MessageCustomBeanType
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.SessionUtils
import io.rong.imlib.model.Conversation
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/10/10 20:08
 *@描述 快速匹配网络请求使用的ViewModel
 */
class FateQuickMatchViewModel : BaseViewModel() {
    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //消息数据
    val msgData: MutableLiveData<AccostMsg> by lazy { MutableLiveData<AccostMsg>() }

    //显示alert标识
    val showAlertFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //派单ID  快捷回复之后，将对应的item设置为已回复
    val fateIdBean: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    //获取常用语接口调用的标识位
    private var randomWordsing = false

    /**
     * 随机获取一条常用语
     */
    fun getRandomWords(userId: Long, fateId: String) {
        if (randomWordsing) {
            return
        }
        randomWordsing = true
        viewModelScope.launch {
            request({
                val result = socialService.chatWordsRandom(FriendIdForm(userId)).dataConvert(intArrayOf(204))
                sendAccostMessage(result)
                fateIdBean.value = fateId
                msgData.value = result
            }, {
                if (it is ResponseError && it.busiCode == 204) {
                    showAlertFlag.value = true
                }
            }, {
                randomWordsing = false
            })
        }
    }

    /**
     * 发送消息
     */
    private fun sendAccostMessage(accostMsg: AccostMsg) {
        val targetUser = accostMsg.targetUserInfo
        //设置本人数据
        val user = RoomUserChatExtra().apply {
            headPic = SessionUtils.getHeaderPic()
            senderId = SessionUtils.getUserId()
            nickname = SessionUtils.getNickName()
            sex = SessionUtils.getSex()
            chatBubble = if (targetUser.intimateLevel >= 4) {
                //亲密度达到4级，有气泡权限
                SPUtils.getObject<ChatBubble>(SPParamKey.PRIVATE_CHAT_BUBBLE, ChatBubble::class.java)
            } else {
                null
            }
        }
        //设置本人数据
        RongCloudManager.resetUSerInfoPrivate(user)

        if (accostMsg.contentType == "Text") {
            //文本消息
            RongCloudManager.send(accostMsg.content, "${targetUser.userId}", targetUserObj = targetUser.apply { fee = accostMsg.consumeBeans })
        } else if (accostMsg.contentType == "Gift") {
            //送礼消息
            val msg = RongCloudManager.obtainCustomMessage(
                "${targetUser.userId}",
                targetUser,
                Conversation.ConversationType.PRIVATE,
                MessageCustomBeanType.Gift,
                accostMsg.gift
            )
            RongCloudManager.sendCustomMessage(msg)
        }

    }

}