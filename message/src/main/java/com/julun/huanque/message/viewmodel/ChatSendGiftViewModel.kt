package com.julun.huanque.message.viewmodel

import androidx.lifecycle.*
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.ChatGift
import com.julun.huanque.common.bean.beans.ChatGiftInfo
import com.julun.huanque.common.bean.beans.ChatGroupGift
import com.julun.huanque.common.bean.beans.ChatSendResult
import com.julun.huanque.common.bean.forms.SendChatGiftForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.BalanceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/9 19:54
 *
 *@Description: 聊天赠送礼物
 *
 */
class ChatSendGiftViewModel : BaseViewModel() {
    private val service: SocialService by lazy { Requests.create(SocialService::class.java) }

    //送礼成功的对象
    val sendGiftBean: MutableLiveData<ChatGift> by lazy { MutableLiveData<ChatGift>() }

    //礼物分页数目
    private val pageLimit = 8
    val giftList: LiveData<ReactiveData<ChatGiftInfo>> = queryState.switchMap { type ->
        liveData {

            request({
                val homeListData = service.giftsInfo().dataConvert()
                //将数据分页处理
                homeListData.giftList.sliceBySubLengthNew(pageLimit).forEachIndexed { _, list ->
                    homeListData.viewPagerData.add(ChatGroupGift(list as MutableList<ChatGift>))
                }
                BalanceUtils.saveBalance(homeListData.beans)
                emit(ReactiveData(NetStateType.SUCCESS, homeListData))
            }, error = { e ->
                logger("报错了：$e")
//                emit(ReactiveData(NetStateType.ERROR, error = e.coverError()))
                emit(e.coverError())
            }, final = {
                logger("最终返回")
            }, needLoadState = type == QueryType.INIT)

        }

    }

    val sendResult: MutableLiveData<ReactiveData<ChatSendResult>> = MutableLiveData()
    fun sendGift(friendId: Long, chatGift: ChatGift, count: Int = 1) {
        viewModelScope.launch {
            request({
                val result = service.sendGift(SendChatGiftForm(friendId, chatGift.chatGiftId, count)).dataConvert()
                sendResult.value = ReactiveData(state = NetStateType.SUCCESS, data = result)
                sendGiftBean.value = chatGift.apply { giftCount = count }
                BalanceUtils.saveBalance(result.beans)
            }, error = {
                logger("赠送的结果 报错=${Thread.currentThread().name}")
//                sendResult.value = ReactiveData(state = NetStateType.ERROR, error = it.coverError())
                sendResult.value =it.coverError()
            })


        }

    }

}